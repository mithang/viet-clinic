package com.geeke.member.service;

import java.util.*;

import com.geeke.member.entity.MemberItem;
import com.geeke.member.entity.MemberManagementDetail;
import com.geeke.member.entity.MemberSet;
import com.geeke.outpatient.entity.RecipelDetail;
import com.geeke.outpatient.entity.RecipelInfoEvt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import com.geeke.common.service.ServiceException;
import com.geeke.member.dao.MemberManagementDao;
import com.geeke.member.entity.MemberManagement;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Maps;
import org.springframework.util.CollectionUtils;

/**
 * Quản lý thẻ hội viênService
 * @author rys
 * @version 2022-10-26
 */
 
@Service("memberManagementService")
@Transactional(readOnly = true)
public class MemberManagementService extends CrudService<MemberManagementDao, MemberManagement>{
        @Autowired
        private MemberManagementDetailService memberManagementDetailService;

        @Autowired
        private MemberSetService memberSetService;

        @Autowired
        private MemberItemService memberItemService;

        @Transactional(readOnly = false)
        public MemberManagement save(MemberManagement memberManagement){
            // DS149 C: chan do dai truoc moi ghi (mau DS148 A), ca hai
            // nhanh insert/update ben duoi (deu goi thang dao, khong qua
            // super.save). Truong KHONG co @Length: phone -> varchar(255)
            // (probe C7); gia tri DictItem gender/type .value ->
            // varchar(100) (probe C8, duong save don bind .value);
            // truong co @Length van can guard cho duong bulk moi them:
            // name 255 (DataEntity @Length 100 chan truoc o save don),
            // age/card/memberName 100, status 1, identityCard 255.
            // Gioi han theo cot DB; null-safe. Luu y: bulkInsert cua
            // mapper truoc day bind NGUYEN object #{entity.gender}/
            // #{entity.type} (khong .value) -- quirk rieng da sua o
            // DS150 A (mapper -> .value); guard .value van chan truoc
            // duong bulk neu value qua dai.
            checkMemberManagementLengths(memberManagement);
            // Sửa (id đã có) trước đây rơi vào cùng nhánh INSERT với Tạo nên
            // luôn lỗi trùng khóa chính — form Sửa qua UI không bao giờ lưu
            // được dù dialog Sửa đã có sẵn. Nhánh update dùng mapper update
            // (giữ nguyên card, không sinh lại, không trừ số lượng thẻ lại).
            if (StringUtils.isNotBlank(memberManagement.getId())) {
                // ITER36 N1: this override bypassed the generic save guards
                // (M1/M2) by calling dao.update directly, so a foreign card id
                // + own-tenant payload company overwrote + re-homed the card
                // (reproduced live). Same two checks as generic save.
                verifyTenantOwnership(memberManagement);
                verifyTenantOwnershipAgainstStoredRow(memberManagement);
                memberManagement.preUpdate();
                this.dao.update(memberManagement);
                return memberManagement;
            }
            // ITER64 item 1: this insert branch bypassed super.save, so the
            // generic insert guard does not cover it. Verify BEFORE any
            // write (dao.insert + detail saves + set stock decrement); a
            // foreign payload company used to plant a card row into a
            // foreign tenant (probe-proven with an OWN set, so the trailing
            // memberSetService.get(setId) check cannot mask it). The update
            // branch above already has both checks (ITER36 N1).
            verifyTenantOwnership(memberManagement);
            memberManagement.preInsert();
            String id = memberManagement.getId();
            String card = new Date().getTime() + "";
            card = card.substring(card.length()-8);
            memberManagement.setCard(card);
            //Khi liên kết thẻ, kiểm tra người dùng đã làm thẻ chưa
            List<MemberManagement> memberManagements = this.dao.getByPatientAndMember(memberManagement.getPatient().getId(),memberManagement.getMemberSet().getId());
            if(!CollectionUtils.isEmpty(memberManagements)){
                throw new ServiceException("đó Người dùng Đã liên kết chỉ định rồi đó Hội viên thẻ, Không thể trùng lặp liên kết chỉ định");
            }
            int insert = this.dao.insert(memberManagement);
            //LưutốtsauSẽ Dữ liệuLưuĐếnChi tiếtbảng trong 

            // DS42: payload chi co memberSet.id (khong kem danh sach hang
            // muc) truoc day getMemberItem() null -> NPE 500 o vong for
            // ben duoi. Khong co hang muc thi khong sinh dong chi tiet.
            List<MemberItem> memberItem = memberManagement.getMemberSet().getMemberItem();
            if (memberItem != null) {
            for (MemberItem item : memberItem) {
                MemberManagementDetail memberManagementDetail = new MemberManagementDetail();
                memberManagementDetail.setMemberManagement(memberManagement);
                memberManagementDetail.setMemberItem(item);
                memberManagementDetail.setNumber(item.getNumber());
                memberManagementDetail.setUseNumber(0);
                memberManagementDetail.setCompany(memberManagement.getCompany());
                memberManagementDetailService.save(memberManagementDetail);
            }
            }
            //liên kếtthẻThành côngCầnđikhấu trừHội viênthẻSố lượng
            // ITER36 N1: the set id comes from the payload, so creating a
            // card against a foreign set used to decrement the FOREIGN
            // set's stock (reproduced live: 50 -> 49). get() verifies the
            // stored set's tenant and throws for a foreign set, rolling
            // this whole save back; an own set passes unchanged.
            if(insert>0){
                memberSetService.get(memberManagement.getMemberSet().getId());
                int i = memberSetService.updateNumber(memberManagement.getMemberSet().getId());
            }
            return memberManagement;
        }

    // DS149 C: bulk* moi them de guard duong bulkInsert/bulkUpdate
    // (truoc day khong co override, CrudService thang khong guard do
    // dai).
    // DS150 A: bulkInsert/bulkUpdate truoc day van 500 sau khi sua
    // mapper .value: cot card NOT NULL nhung chi save() don sinh card
    // (8 ky tu cuoi timestamp) truoc insert; duong bulk ghi thang
    // #{entity.card} = null. Sinh card thieu o bulkInsert (cung cach
    // nhu save()); bulkUpdate giu card da luu khi payload khong kem
    // card (nhu nhanh update cua save()), chi sinh moi khi ca dong da
    // luu cung khong co card. Khong sao chep cac tac dung phu khac cua
    // save() (kiem tra trung the / tru so luong the) -- bulk* theo
    // thiet ke khong qua save().
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<MemberManagement> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách thẻ hội viên không được để trống");
        }
        for (MemberManagement entity : entitys) {
            checkMemberManagementLengths(entity);
            if (StringUtils.isBlank(entity.getCard())) {
                String card = new Date().getTime() + "";
                entity.setCard(card.substring(card.length() - 8));
            }
        }
        return super.bulkInsert(entitys);
    }

    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<MemberManagement> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách thẻ hội viên không được để trống");
        }
        for (MemberManagement entity : entitys) {
            checkMemberManagementLengths(entity);
            if (StringUtils.isBlank(entity.getCard())) {
                MemberManagement stored = this.dao.get(entity.getId());
                if (stored != null && StringUtils.isNotBlank(stored.getCard())) {
                    entity.setCard(stored.getCard());
                } else {
                    String card = new Date().getTime() + "";
                    entity.setCard(card.substring(card.length() - 8));
                }
            }
        }
        return super.bulkUpdate(entitys);
    }

    // DS149 C: xem ghi chu o save().
    private static void checkMemberManagementLengths(MemberManagement entity) {
        if (entity == null) {
            return;
        }
        checkLength(entity.getName(), 255, "Tên hội viên Độ dài phải nằm trong khoảng 0 đến 255");
        checkLength(entity.getPhone(), 255, "Số điện thoại Độ dài phải nằm trong khoảng 0 đến 255");
        checkLength(entity.getAge(), 100, "Tuổi Độ dài phải nằm trong khoảng 0 đến 100");
        checkLength(entity.getCard(), 100, "Thẻ Độ dài phải nằm trong khoảng 0 đến 100");
        checkLength(entity.getMemberName(), 100, "Tên hội viên Độ dài phải nằm trong khoảng 0 đến 100");
        checkLength(entity.getStatus(), 1, "Trạng thái Độ dài phải nằm trong khoảng 1 đến 1");
        checkLength(entity.getIdentityCard(), 255, "Chứng minh nhân dân Độ dài phải nằm trong khoảng 0 đến 255");
        checkDictLength(entity.getGender(), 100, "Giới tính Độ dài phải nằm trong khoảng 0 đến 100");
        checkDictLength(entity.getType(), 100, "Loại thẻ hội viên Độ dài phải nằm trong khoảng 0 đến 100");
    }

    private static void checkDictLength(com.geeke.sys.entity.DictItem item, int max, String message) {
        if (item != null) {
            checkLength(item.getValue(), max, message);
        }
    }

    private static void checkLength(String value, int max, String message) {
        if (value != null && value.length() > max) {
            throw new ServiceException(message);
        }
    }
    /**
     * DS132 C: single delete fail-closed 400. Truoc day khong co
     * delete() override; mapper <delete> khong dung del_flag ma set
     * status='2' (da vo hieu hoa) KHONG loc status, bang cung khong
     * co cot del_flag (status 0=chua dung, 1=da dung, 2=da vo hieu
     * hoa, 4=het han) -- cung lop DS123/DS129/DS130/DS131: ghost/
     * blank -> 200 data 0 im lang, xoa lap (status da '2') -> 200
     * rows 1. Chan truoc bang dao.get (khong loc status nen tra ca
     * dong da vo hieu hoa, status phan biet) theo mau CodeRule/
     * DS129 A, chi status '2' tinh la da xoa; the live o status 0/1/4
     * van vo hieu hoa duoc nhu cu (UI goi day la "Vo hieu hoa").
     * checkExists (DS103) KHONG tai dung (cho qua blank, khong check
     * trang thai, dung chung voi save/bulkUpdate). bulkDelete ghost
     * CO TINH GIU 200 rows 0 (DS99/DS106/DS126 B idempotency) --
     * khong doi. Luu y: mapper bulkDelete la HARD DELETE trong khi
     * single delete chi set status='2' -- lech nay ghi nhan lam ung
     * vien DS133, khong doi o day.
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(MemberManagement memberManagement) {
        if (memberManagement == null || StringUtils.isBlank(memberManagement.getId())) {
            throw new ServiceException("Không xác định được thẻ hội viên cần xóa.");
        }
        MemberManagement stored = dao.get(memberManagement.getId());
        if (stored == null || "2".equals(stored.getStatus())) {
            throw new ServiceException("Thẻ hội viên không tồn tại: " + memberManagement.getId());
        }
        return super.delete(memberManagement);
    }

    /**
     * DS133 B: bulkDelete ap dung dung ngu nghia cua single delete
     * (DS132 C). Truoc day khong co override: CrudService.bulkDelete
     * chay thang dao.bulkDelete, ma mapper bulkDelete la HARD DELETE
     * trong khi single delete chi set status='2' (vo hieu hoa) --
     * bulk xoa vinh vien dong the, mat lich su, VA KHONG hoan tra
     * so luong set (addNumber) nhu single delete lam o controller,
     * nen xoa hang loat ro ri ton set (tai hien live: bulk [valid]
     * -> 200 rows 1, dong BIEN MAT, set giu 10). Khong co caller
     * noi bo va khong co .vue nao goi bulkDeleteMemberManagement
     * (chi api file export), nen khong doi hanh vi luong hien co.
     * Hai luot nhu vong lap cua base + bai hoc ITER37: luot 1
     * kiem tra TAT CA phan tu (malformed -> 400 theo guard DS126 B
     * o base; kiem tra tenant; giai quyet + kiem tra tenant cua
     * set da luu TRUOC moi ghi) de mot phan tu xau khong de lai
     * the da vo hieu hoa; luot 2 ap dung tung the live qua
     * super.delete() + addNumber(storedSetId). Ghost / da vo hieu
     * hoa (status '2') duoc BO QUA, 0 rows (idempotency bulk DS99
     * G11 / DS106 / DS126 B giu nguyen). The tro toi set khong ton
     * tai / number null -> 400 o luot 1, truoc khi ghi gi (cung
     * dieu kien addNumber nem ra o luot 2).
     */
    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<MemberManagement> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách xóa không được để trống");
        }
        List<MemberManagement> live = new ArrayList<>();
        List<String> liveSetIds = new ArrayList<>();
        for (MemberManagement entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Dòng xóa không được để trống");
            }
            if (StringUtils.isBlank(entity.getId())) {
                throw new ServiceException("Dòng xóa không xác định được id cần xóa");
            }
            verifyTenantOwnership(entity);
            verifyTenantOwnershipAgainstStoredRow(entity);
            MemberManagement stored = dao.get(entity.getId());
            if (stored == null || "2".equals(stored.getStatus())) {
                continue;
            }
            String storedSetId = stored.getMemberSet() != null ? stored.getMemberSet().getId() : null;
            if (StringUtils.isNotBlank(storedSetId)) {
                MemberSet storedSet = memberSetService.get(storedSetId);
                if (storedSet == null || storedSet.getNumber() == null) {
                    throw new ServiceException("Thẻ hội viên chưa có số lượng còn lại, không thể hoàn trả!");
                }
            }
            live.add(entity);
            liveSetIds.add(storedSetId);
        }
        int rows = 0;
        for (int i = 0; i < live.size(); i++) {
            int r = super.delete(live.get(i));
            if (r > 0) {
                rows += r;
                String setId = liveSetIds.get(i);
                if (StringUtils.isNotBlank(setId)) {
                    memberSetService.addNumber(setId);
                }
            }
        }
        return rows;
    }

    @Transactional(readOnly = false)
    public List<MemberManagement> getByMemberSetId(String memberSetId) {
        List<MemberManagement> memberManagements = this.dao.getByMemberSetId(memberSetId);
        return memberManagements;
    }
    
    @Transactional(readOnly = false)
    public List<MemberManagement> getMember(RecipelInfoEvt recipelInfoEvt) {
            //Lấyhạng mục khám chữa bệnhid
        List<RecipelDetail> recipelDetailEvtList = recipelInfoEvt.getRecipelDetailEvtList();

        //TheoBệnh nhânidLấycủa nóCó hay khôngliên kếtchỉ địnhrồitrải nghiệmthẻ
        List<MemberManagement> memberManagements = filterOwnTenant(this.dao.getByPatientId(recipelInfoEvt.getRecipelInfo().getRegistration().getPatientId().getId()));
        List<MemberManagement> memberManagementList = new ArrayList<>();

        //Theo người dùngliên kếtchỉ địnhthẻ, điLấycủa nóliên kếtchỉ định của hạng mục
        if(!CollectionUtils.isEmpty(memberManagements)){
            for (MemberManagement memberManagement : memberManagements) {
                List<MemberManagementDetail> memberManagementDetails = memberManagementDetailService.getByManagementId(memberManagement.getId());

                int count=0;
                for (MemberManagementDetail memberManagementDetail : memberManagementDetails) {
                    if(memberManagementDetail.getNumber()==memberManagementDetail.getUseNumber()){
                        count++;
                    }
                }
                //NếuđóHội viênthẻsử dụng lầnsốđềusử dụngxong, Kết thúcnày lầnvòng lặp
                if(count==memberManagementDetails.size()){
                    continue;
                }

                //Tồn tạiChưasử dụngxong của Hội viênthẻ giờ, tiến hành dòngKiểm tra
                boolean flage = false;
                MemberSet memberSet = memberSetService.get(memberManagement.getMemberSet().getId());
                //Lấytrải nghiệmthẻChi tiết
                List<MemberItem> memberItems = memberItemService.getByMemberSetId(memberSet.getId());
                for (MemberItem memberItem : memberItems) {
                    for (RecipelDetail recipelDetail : recipelDetailEvtList) {
                            if(Objects.equals(recipelDetail.getDrugStuffId().getCostItem().getId(),memberItem.getCostItem().getId())){
                                //Nếu bằng nhau thì lấy tiếp người dùng đó đã dùng hết chưa
                                MemberManagementDetail memberManagementDetail = memberManagementDetailService.getByManagementIdAndMemberItemId(memberManagement.getId(),memberItem.getId());
                                if(memberManagementDetail.getNumber()>memberManagementDetail.getUseNumber()) {
                                    flage = true;
                                    break;
                                }
                            }
                    }
                    if(flage){
                        break;
                    }
                }
                if(flage){
                    memberManagement.setMemberSet(memberSet);
                    memberManagement.setMemberItems(memberItems);
                    memberManagement.setMemberManagementDetails(memberManagementDetails);
                    memberManagementList.add(memberManagement);
                }
            }

            return memberManagementList;
        }else {
            return new ArrayList<MemberManagement>();
        }

    }

    @Transactional(readOnly = false)
    public void updaStatus(MemberManagement memberManagement) {
            this.dao.updateStatus(memberManagement);
    }

    // Z1a: the khach hang theo patient_id tran — chi giu the thuoc session
    // tenant truoc khi nap chi tiet. Truoc day the tenant la chi bi chan
    // gian tiep (400 tu CrudService.get khi nap MemberItem la), khong chac
    // chan: the khong co chi tiet hoac MemberItem dung chung se lot qua.
    // Session rong/"null" (luong noi bo khong session) giu hanh vi cu,
    // hang company null giu nhu verifyTenantOwnership.
    private List<MemberManagement> filterOwnTenant(List<MemberManagement> rows) {
        String tenant = com.geeke.utils.SessionUtils.getLoginTenantId();
        if (tenant == null || tenant.isEmpty() || "null".equals(tenant) || rows == null) {
            return rows;
        }
        List<MemberManagement> kept = new ArrayList<>();
        for (MemberManagement row : rows) {
            if (row == null || row.getCompany() == null || row.getCompany().getId() == null
                    || row.getCompany().getId().isEmpty() || tenant.equals(row.getCompany().getId())) {
                kept.add(row);
            }
        }
        return kept;
    }

    public List<MemberManagement> getByPatientId(String patientId) {
        List<MemberManagement> memberManagements = filterOwnTenant(this.dao.getByPatientId(patientId));
        List<MemberManagement> memberManagementList = new ArrayList<>();
        for (MemberManagement memberManagement : memberManagements) {
            List<MemberManagementDetail> memberManagementDetails = memberManagementDetailService.getByManagementId(memberManagement.getId());
            int count=0;
            for (MemberManagementDetail memberManagementDetail : memberManagementDetails) {
                if(memberManagementDetail.getNumber()==memberManagementDetail.getUseNumber()){
                    count++;
                }
            }
            if(count<memberManagementDetails.size()){
                memberManagementList.add(memberManagement);
            }
        }
        return memberManagementList;
    }
}