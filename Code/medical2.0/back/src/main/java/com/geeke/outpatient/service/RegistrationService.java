package com.geeke.outpatient.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.medicareutils.service.MdRegistrationService;
import com.geeke.outpatient.dao.MedicalRecordDao;
import com.geeke.outpatient.dao.RecipelInfoDao;
import com.geeke.outpatient.dao.RegistrationDao;
import com.geeke.outpatient.entity.*;
import com.geeke.sys.entity.Action;
import com.geeke.utils.IdGen;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Thông tin đăng ký khámService
 * @author txl
 * @version 2022-06-15
 */
 
@Service("registrationService")
@Transactional(readOnly = true)
public class RegistrationService extends CrudService<RegistrationDao, Registration>{
    private final RegistrationDao registrationDao;

    private final MedicalRecordDao medicalRecordDao;

    private final PatientService patientService;

    private final MedicalRecordService medicalRecordService;

    //Đăng ký khám BHYT
    private final MdRegistrationService mdRegistrationService;

    public RegistrationService(RegistrationDao registrationDao, MedicalRecordDao medicalRecordDao,
            PatientService patientService, @Lazy MedicalRecordService medicalRecordService,
            MdRegistrationService mdRegistrationService) {
        this.registrationDao = registrationDao;
        this.medicalRecordDao = medicalRecordDao;
        this.patientService = patientService;
        this.medicalRecordService = medicalRecordService;
        this.mdRegistrationService = mdRegistrationService;
    }

    /**
     * H4: gán số thứ tự chờ khám trong ngày cho mỗi lượt đăng ký mới.
     * Số đếm theo (phòng khám, bác sĩ, ngày phòng khám Asia/Ho_Chi_Minh),
     * bắt đầu từ 1 mỗi ngày nhờ khóa chính của queue_counter theo queue_date;
     * cửa sổ ngày [00:00, 00:00 hôm sau) khớp quy ước GMT+7 của G6/H5/H1
     * (create_date lưu theo giờ tường GMT+7). Lượt đăng ký không có bác sĩ
     * hoặc không xác định được tenant thì không gán số (giữ NULL, không bịa).
     * Chỉ gán khi tạo mới (id rỗng) và client chưa gửi số: cập nhật sau đó
     * không đánh lại số, tránh xáo trộn thứ tự gọi khám.
     */
    @Override
    @Transactional(readOnly = false)
    public Registration save(Registration entity) {
        if (StringUtils.isBlank(entity.getId()) && entity.getQueueNumber() == null) {
            assignQueueNumber(entity);
        }
        return super.save(entity);
    }

    /**
     * DS119 A: mapper <update id="delete"> co nhanh <otherwise> xoa mem
     * theo patient_id khi id rong -- delete khong id + patientId se xoa
     * sach moi luot dang ky cua benh nhan. Chan o service: chi xoa khi
     * co id; khong caller noi bo nao dung duong khong id (kiem tra
     * 2026-11-07).
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(Registration entity) {
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được lượt đăng ký cần xóa.");
        }
        // DS134 C: ghost / da xoa mem (del_flag '1') -> 400 fail-closed
        // (truoc day: ghost -> 200 data 0 im lang, xoa lan 2 -> 200 rows 1
        // vi mapper soft-delete khong loc del_flag; CrudService chi
        // check tenant tren stored row, stored null thi bo qua -- lop
        // DS123). checkExists (DS103) KHONG tai dung: cho qua blank id,
        // khong check delFlag. bulkDelete ghost CO TINH GIU 200 rows 0
        // (DS99/DS106/DS126 B idempotency).
        Registration stored = dao.get(entity.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Lượt đăng ký không tồn tại: " + entity.getId());
        }
        verifyTenantOwnershipAgainstStoredRow(entity);
        return super.delete(entity);
    }

    private void assignQueueNumber(Registration entity) {
        String companyId = entity.getCompany() != null ? entity.getCompany().getId() : null;
        if (StringUtils.isBlank(companyId)) {
            companyId = SessionUtils.getLoginTenantId();
        }
        String doctorId = entity.getDoctor() != null ? entity.getDoctor().getId() : null;
        if (StringUtils.isBlank(companyId) || StringUtils.isBlank(doctorId)) {
            return;
        }
        String queueDate = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).toString();
        registrationDao.advanceQueueCounter(companyId, doctorId, queueDate);
        Integer number = registrationDao.getQueueCounter(companyId, doctorId, queueDate);
        if (number != null) {
            entity.setQueueNumber(number);
        }
    }

    /**
     * N2: gán bác sĩ sau (đường điểm danh /status) cũng cấp số thứ tự.
     * Booking đã xác nhận được tạo không có bác sĩ nên confirm không gán
     * số (đúng H4); khi nhân viên gán bác sĩ qua đường này mà lượt vẫn
     * chưa có số thì cấp số theo cùng counter (phòng khám, bác sĩ, ngày
     * phòng khám hiện tại). Ngày hiện tại là đúng vì điểm danh ở frontend
     * đã chặn lượt có subscribeDate khác hôm nay ("Chưa đến giờ hẹn").
     * Idempotent: lượt đã có số thì giữ nguyên, không đánh lại; doctorId
     * rỗng thì giữ nguyên hành vi cũ (không gán, không bịa số).
     * O1: blank departmentId/doctorId được chuẩn hóa thành NULL trước khi
     * gọi DAO, vì @Update của DAO luôn set cả 2 cột số và MySQL báo 500
     * "Incorrect integer value: ''" khi nhận chuỗi rỗng. NULL mang ngữ
     * nghĩa "gỡ khoa/bác sĩ", không im lặng giữ giá trị cũ như <if> động.
     */
    @Transactional(readOnly = false)
    public int updateStatus(String id,String status,String departmentId,String doctorId)
    {
        // ITER37 O1: kiểm tra dòng DB đã lưu trước khi ghi, theo mẫu M1.
        // Trước đây updateStatus gọi thẳng dao.updateStatus (WHERE id,
        // không lọc company) nên token tenant 309 đổi được lượt khám của
        // tenant 297 và còn cấp số từ counter của tenant lạ (tái hiện
        // tools/iter37-o1-repro.py). Probe chỉ mang id: helper tự đọc
        // dòng đã lưu qua dao.get, dòng lạ (không phải tenant đăng nhập
        // và không phải institution-shared) → ServiceException 400,
        // transaction rollback nên không ghi gì, không tăng counter.
        // Id rỗng / dòng không tồn tại / company rỗng vẫn qua như
        // ngữ nghĩa cũ của helper (ghi 0 dòng).
        Registration probe = new Registration();
        probe.setId(id);
        verifyTenantOwnershipAgainstStoredRow(probe);
        if (StringUtils.isBlank(departmentId)) {
            departmentId = null;
        }
        if (StringUtils.isBlank(doctorId)) {
            doctorId = null;
        }
        int res = dao.updateStatus(id,status,departmentId,doctorId);
        if (res > 0 && StringUtils.isNotBlank(doctorId)) {
            Registration existing = dao.get(id);
            if (existing != null && existing.getQueueNumber() == null) {
                String companyId = existing.getCompany() != null ? existing.getCompany().getId() : null;
                if (StringUtils.isBlank(companyId)) {
                    companyId = SessionUtils.getLoginTenantId();
                }
                if (StringUtils.isNotBlank(companyId)) {
                    String queueDate = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).toString();
                    registrationDao.advanceQueueCounter(companyId, doctorId, queueDate);
                    Integer number = registrationDao.getQueueCounter(companyId, doctorId, queueDate);
                    if (number != null) {
                        registrationDao.updateQueueNumber(id, number);
                    }
                }
            }
        }
        return res;
    }
    @Transactional(readOnly = false)
    public int updateStatusByCompanyId(String companyId){
        //Tínhhết hạnThời gian(24nhỏ giờtrước)
        long time = new Date().getTime();
        Date date = new Date(time - 24 * 60 * 60 * 1000);
        return dao.updateStatusByCompanyId(companyId, date, date);
    }

    /**
     * Từ  phúttrangKết quả của Dòng mộtmụcBản ghi trong đề xuấtlấycompanyId, vàCập nhậthết hạnTrạng thái
     * Xóa bỏ Controller  trong trùng lặp của  companyId đề xuấtlấylogic
     */
    @Transactional(readOnly = false)
    public <T> void updateExpiredStatusFromPage(Page<T> result, java.util.function.Function<T, String> companyIdExtractor) {
        if (result != null && result.getTotal() > 0 && result.getRows() != null && !result.getRows().isEmpty()) {
            String companyId = companyIdExtractor.apply(result.getRows().get(0));
            if (companyId != null) {
                updateStatusByCompanyId(companyId);
            }
        }
    }
    /**
     * hủy số khám
     * @author lin.zeng
     **/
    @Transactional(readOnly = false)
    public int refundRegistrationPay(String id,String status,
                              String refundRegistrationPayType, String refundRegistrationRemarks,Date exitNumberDate
    )
    {
        // O4-37: kiem tra tenant dong DB da luu truoc khi dao ghi (nhu
        // updateStatus O1-37) — truoc day controller chi get() o nhanh
        // remarks = ma da hoan phi, nhanh else goi thang dao (WHERE id,
        // khong loc company) nen token tenant 309 huy duoc luot kham
        // tenant 297. Dong la -> ServiceException -> 400, khong ghi gi;
        // id rong/khong ton tai/company rong van qua nhu cu (ghi 0 dong).
        Registration probe = new Registration();
        probe.setId(id);
        verifyTenantOwnershipAgainstStoredRow(probe);
        // DS64: hoan phi bang atomic claim thay cho dao
        // refundRegistrationPay vo dieu kien (xem wxReturnPay ben duoi):
        // guard status o controller chi chay khi remarks == _2, client
        // that gui remarks="" nen guard khong bao gio chay — tuan tu thu
        // 2 van duoc nhan 200 data 1 va client tao them phieu hoan phi.
        // Claim 0 dong -> 400 nhu truong hop tuan tu tuong duong (wx).
        Registration claim = new Registration();
        claim.setId(id);
        com.geeke.sys.entity.DictItem statusItem = new com.geeke.sys.entity.DictItem();
        statusItem.setValue(status);
        claim.setStatus(statusItem);
        com.geeke.sys.entity.DictItem payTypeItem = new com.geeke.sys.entity.DictItem();
        payTypeItem.setValue(refundRegistrationPayType);
        claim.setRefundRegistrationPayType(payTypeItem);
        claim.setRefundRegistrationRemarks(refundRegistrationRemarks);
        claim.setExitNumberDate(exitNumberDate);
        int i = dao.claimRefund(claim);
        if (i != 1) {
            // Dong khong ton tai / id rong: giu hanh vi cu (tra 0, khong
            // nem) nhu dao cu ghi 0 dong; dong da ton tai nhung het
            // trang thai duoc hoan -> 400 nhu truong hop tuan tu (wx).
            Registration stored = this.get(id);
            if (stored == null) {
                return 0;
            }
            throw new com.geeke.common.service.ServiceException("bạnĐãlấyhủyđặt lịch, khôngcó thểlại lầnlấyhủy!");
        }
        return i;
    }

    public Page<Registration> listConditionPage(String param, int offset, int limit) {
        return paginate(
            () -> this.dao.countConditionList(param, SessionUtils.getLoginTenantId()),
            () -> this.dao.conditionList(param, SessionUtils.getLoginTenantId(), limit, offset)
        );
    }
    @Transactional(readOnly = false)
    public void updateRecipeStatus(String id) {
        registrationDao.updateRecipeStatus(id);
    }

    @Transactional(readOnly = true)
    public Page<Registration> listPages(PageRegistration pageRegistration) {
        // Z1b: companyId tu body khong duoc phep lai tenant khac — SQL
        // countId/listPages loc a.company_id=#{companyId} truc tiep, nen
        // body tenant la truoc day tra ve dang ky cua tenant do (duong
        // patidwsflist/dispensing//v2/list dung chung method nay).
        // Session rong/\"null\" giu hanh vi cu nhu verifyTenantOwnership.
        forceSessionTenant(pageRegistration);
        if (!pageRegistration.isValidColumnName()) {
            throw new IllegalArgumentException("Invalid column name: " + pageRegistration.getColumnName());
        }
        List<String> strings = this.dao.countId(pageRegistration);
        int total=strings.size();
        List<Registration> list = null;
        if (total > 0) {

            List<String> strings1 = this.dao.listPages(pageRegistration);
            if(strings1.size()>0){
               list = this.dao.findById(strings1,pageRegistration);
            }
        }

        return new Page<>((long)total, list);
    }

    // Z1b: ghi de companyId body bang session tenant (dung chung cho
    // listPages; xem ghi chu o listPages).
    private void forceSessionTenant(PageRegistration pageRegistration) {
        String tenant = SessionUtils.getLoginTenantId();
        if (tenant != null && !tenant.isEmpty() && !"null".equals(tenant)) {
            pageRegistration.setCompanyId(tenant);
        }
    }


    public List<String> whetherTheDispensing(PageRegistration pageRegistration) {
        List<String> strings = this.dao.countId(pageRegistration);
        return strings;
    }

    public List<String> whetherChargeOrNot(PageRegistration pageRegistration) {
        List<String> strings = this.dao.countId(pageRegistration);
        return strings;
    }

    @Transactional(readOnly = false)
    public Registration wxSave(Registration entity) {
        // C1 (iteration 50): wxSave truoc day goi doInsert/doUpdate truc tiep,
        // ne ca verifyTenantOwnership lan stored-row check cua CrudService.save
        // (mapper update chi WHERE id) — token tenant A tao/ghi de luot tenant B.
        // Ap dung dung ngu nghia CrudService.save: payload company phai khop
        // session tenant (company rong/session rong giu hanh vi cu); update
        // them stored-row check de id dong la + company own khong qua duoc.
        verifyTenantOwnership(entity);
        if (StringUtils.isNotBlank(entity.getId())) {
            verifyTenantOwnershipAgainstStoredRow(entity);
        }
        if (StringUtils.isBlank(entity.getId())) {
            if (entity.getQueueNumber() == null) {
                assignQueueNumber(entity);
            }
            entity.preInsert();
            this.doInsert(entity);
            Action created = this.createAction("created", entity);
            created.setCreateBy("WeChat");
            created.setUpdateBy("WeChat");
            this.saveAction(created);
        } else {
            entity.preUpdate();
            this.doUpdate(entity);
            this.saveAction(this.createAction("updated", entity));
        }
        return entity;
    }

    @Transactional(readOnly = true)
    public Page<Registration> getRegistrationByOpenId(List<Parameter> parameters, int offset, int limit, String orderby) {
        PageRequest pageRequest = new PageRequest(offset, limit, parameters, orderby);
        List<Parameter> params = pageRequest.getParams();
        String openId = (String) params.get(1).getValue();
        String companyId = (String) params.get(0).getValue();

        // sử dụngDữ liệukho phúttrang, Tránhbộ nhớ phúttrang và N+1Truy vấn
        return paginate(
            () -> this.dao.countByOpenId(openId, companyId),
            () -> this.dao.listByOpenId(openId, companyId, offset, limit)
        );
    }

    @Transactional(readOnly = false)
    public int wxReturnPay(Registration registrations) {
        // DS64: hoan phi bang atomic claim (mau DS52-DS63) thay cho dao
        // wxReturnPay vo dieu kien. Controller da kiem tra status truoc
        // (read-then-act) nen tuan tu thu 2 bi 400, nhung 8 song song co
        // 5 duoc nhan (probe): cac luong deu doc qua guard truoc khi
        // luong thang commit. Claim chi lat khi dong van o trang thai
        // duoc hoan (NULL/_3/_5, xem RegistrationDao.xml claimRefund);
        // 0 dong -> ServiceException 400 voi dung thong diep tu choi
        // tuan tu, khong ghi gi, thu lai tren luot kham moi thi duoc.
        int i = dao.claimRefund(registrations);
        if (i != 1) {
            throw new com.geeke.common.service.ServiceException("bạnĐãlấyhủyđặt lịch, khôngcó thểlại lầnlấyhủy!");
        }

        return i;
    }


    public Page<ReceptionEvt> wxListPages(PageRegistration pageRegistration) {
        if (!pageRegistration.isValidColumnName()) {
            throw new IllegalArgumentException("Invalid column name: " + pageRegistration.getColumnName());
        }
        List<String> strings = this.dao.wxCount(pageRegistration);
        int total=strings.size();
        List<Registration> list = null;
        List<ReceptionEvt> lists = new ArrayList<>();
        if (total > 0) {

            List<String> strings1 = this.dao.wxListPages(pageRegistration);

            if(strings1.size()>0){
               // list = this.dao.findById(strings1,pageRegistration);
                for (String s : strings1) {
                    ReceptionEvt receptionEvt = medicalRecordService.allNewQuery(s, null);
                    lists.add(receptionEvt);
                }
            }
        }

        return new Page<>((long)total, lists);
    }

    public Page<ReceptionEvt> wxDispensingListPages(PageRegistration pageRegistration) {
        if (!pageRegistration.isValidColumnName()) {
            throw new IllegalArgumentException("Invalid column name: " + pageRegistration.getColumnName());
        }
        List<String> strings = this.dao.wxDispensingCount(pageRegistration);
        int total=strings.size();
        List<Registration> list = null;
        List<ReceptionEvt> lists = new ArrayList<>();
        if (total > 0) {

            List<String> strings1 = this.dao.wxDispensingListPages(pageRegistration);

            if(strings1.size()>0){
                // list = this.dao.findById(strings1,pageRegistration);
                for (String s : strings1) {
                    ReceptionEvt receptionEvt = medicalRecordService.allNewQuery(s, null);
                    lists.add(receptionEvt);
                }
            }
        }

        return new Page<>((long)total, lists);
    }

    @Transactional(readOnly = false)
    public int registrationupdate(Registration registrations) {
        // ITER62 P2: verify stored-row company before writing, never trust
        // payload company.id. The mapper WHERE is company_id=#{company.id}
        // from the payload, so a caller could previously write a foreign
        // visit by carrying its company id (reproduced live:
        // POST registationupdate with a foreign id + foreign company
        // changed that visit's pulse NULL -> 99). The helper reads the
        // stored row via dao.get (which selects a.company_id), so a
        // foreign stored row -> ServiceException 400 before any write,
        // while blank id / missing row / blank stored company keep the
        // legacy 0-row behaviour, exactly like updateStatus (ITER37 O1).
        // An own stored row with a mismatched payload company still
        // writes 0 rows via the mapper WHERE, unchanged from before.
        if (registrations != null) {
            verifyTenantOwnershipAgainstStoredRow(registrations);
        }
        int i = dao.registrationupdate(registrations);

        return i;
    }

    @Transactional(readOnly = false)
    public int medicalRecordInserts(MedicalRecord medicalRecord) {
        // DS82: direct-DAO insert bypasses the DS81 MedicalRecordService.save
        // guard (no @Valid runs on this path — the controller parses the
        // entity from JSON and calls straight here), so a null patientTell /
        // doctor id / registration id 500d on the NOT NULL columns, and a
        // >128-char patientTell 500d under STRICT_TRANS_TABLES. Fail closed
        // 400 here, in the same words as the DS81 guard. Null-only for
        // patientTell: live rows carry '' (inserted by this same path), so
        // blank is NOT rejected; length is capped at the DB 128.
        if (medicalRecord.getPatientTell() == null) {
            throw new com.geeke.common.service.ServiceException("Lý do khám chính không được để trống");
        }
        if (medicalRecord.getPatientTell().length() > 128) {
            throw new com.geeke.common.service.ServiceException("Lý do khám chính độ dài phải nằm trong khoảng 1 đến 128");
        }
        if (medicalRecord.getDoctor() == null || medicalRecord.getDoctor().getId() == null) {
            throw new com.geeke.common.service.ServiceException("Bác sĩ không được để trống");
        }
        if (medicalRecord.getRegistration() == null || medicalRecord.getRegistration().getId() == null) {
            throw new com.geeke.common.service.ServiceException("Lượt đăng ký khám không được để trống");
        }
        medicalRecord.setDiagnose("");
        medicalRecord.setId(IdGen.uuid());
        medicalRecord.setCompany(SessionUtils.getUser().getCompany());
        medicalRecord.setCreateBy(SessionUtils.getUser().getUpdateBy());
        medicalRecord.setCreateDate(SessionUtils.getUser().getUpdateDate());
        medicalRecord.setUpdateBy(SessionUtils.getUser().getUpdateBy());
        medicalRecord.setUpdateDate(SessionUtils.getUser().getUpdateDate());
        medicalRecord.setDelFlag("0");
        int result = medicalRecordDao.insert(medicalRecord);
        return result;
    }

}