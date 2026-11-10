package com.geeke.outpatient.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.geeke.common.persistence.BaseEntity;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.medicareutils.config.MedicareConfigProperties;
import com.geeke.medicareutils.service.MdPsnDataService;
import com.geeke.medicareutils.util.MdRequestUtil;
import com.geeke.outpatient.dao.PatientDao;
import com.geeke.outpatient.dao.PatientMdDataMapper;
import com.geeke.outpatient.entity.Patient;
import com.geeke.outpatient.entity.PatientDuplicate;
import com.geeke.outpatient.entity.PatientMdData;
import com.geeke.utils.IdGen;
import com.geeke.utils.SessionUtils;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Thông tin bệnh nhânService
 * @author txl
 * @version 2022-06-23
 */
 
@Service("patientService")
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PatientService extends CrudService<PatientDao, Patient>{
    private final PatientDao patientDao;


    @Override
    @Transactional(readOnly = false)
    public Patient save(Patient patient) {
        // DS127 C: save ghost id fail-closed 400 (DS94/DS120 class).
        // Truoc day id la roi thang vao CrudService.save -> doUpdate 0
        // dong -> 200 silent no-op (probe C1). Chi chap nhan update khi
        // dong da ton tai (dao.get chi LEFT JOIN nen khong tra null oan
        // nhu get() cua Review); insert (id rong) giu nguyen. Dat truoc
        // cac guard trung SDT/CCCD vi chung deref getCompany().getId().
        // Caller noi bo (MedicalRecordService saveAllRecordInfo /
        // reception, TollInfoService processRetailCharge) chi gui id
        // rong (benh nhan moi) hoac id dong da ton tai; nhanh ghost o
        // processRetailCharge truoc day von chi la silent no-op vi
        // CrudService.save khong upsert, nay 400 ro rang thay vi lang.
        if (patient == null) {
            throw new ServiceException("Thông tin bệnh nhân không được để trống");
        }
        // DS148 B: chan do dai truoc moi xu ly (name 46..100 qua @Valid
        // roi 500 o DB do @Length lech cot; cardType/openId khong @Length;
        // gia tri DictItem gender/nexus viet thang vao varchar(64)).
        checkPatientLengths(patient);
        if (patient.getId() != null && !patient.getId().trim().isEmpty()
                && this.dao.get(patient.getId()) == null) {
            throw new ServiceException("Bệnh nhân không tồn tại: " + patient.getId());
        }
        Map<String, String> colMaps = Maps.newHashMap();
        // Thông tin liên hệ của bệnh nhân đã tồn tại
        colMaps.clear();
        colMaps.put("phone", "phone");
        
//        if(exists(dao, patient, colMaps)) {
//            throw new ServiceException("Thông tin liên hệ của bệnh nhân đã tồn tại");
//        }
       if(patient.getId()==null){
           //Kiểm traSố điện thoạisốCó tồn tại không
           if(!Objects.equals(patient.getPhone(),"") && patient.getPhone()!=null){
               if(patientDao.countPatientByPhone(patient.getPhone(),patient.getCompany().getId(),null).size()>0){
                   throw new ServiceException("Thông tin liên hệ của bệnh nhân đã tồn tại");

               }
           }

           //Kiểm traCMND/CCCDCó tồn tại không
           if(!Objects.equals(patient.getCard(),"") && patient.getCard()!=null){
               List<Patient> patientByCard = patientDao.getPatientByCard(patient.getCard(), patient.getCompany().getId(),null);
               if(!CollectionUtils.isEmpty(patientByCard)){
                       throw new ServiceException("Bệnh nhânsố CMND/CCCDĐã tồn tại");
               }
           }
       }

       if(patient.getId()!=null){
           //Kiểm traSố điện thoạisốCó tồn tại không
           if(!Objects.equals(patient.getPhone(),"") && patient.getPhone()!=null){
               List<Patient> patients = patientDao.countPatientByPhone(patient.getPhone(), patient.getCompany().getId(),patient.getId());
               // P1: dong tra ve (neu co) theo dinh nghia da co cung SDT va
               // da loai ban than bang id != null, nen chi can co dong la
               // trung; dieu kien cu so phone cua dong voi chinh phone da
               // loc nen luon bang va khong bao gio nem.
               if(patients.size()>0){
                   throw new ServiceException("Thông tin liên hệ của bệnh nhân đã tồn tại");

               }
           }

           //Kiểm traCMND/CCCDCó tồn tại không
           if(!Objects.equals(patient.getCard(),"") && patient.getCard()!=null){
               List<Patient> patientByCard = patientDao.getPatientByCard(patient.getCard(), patient.getCompany().getId(),patient.getId());
               if(!CollectionUtils.isEmpty(patientByCard)){
                   if(Objects.equals(patientByCard.get(0).getCard(),patient.getCard())) {
                       throw new ServiceException("Bệnh nhânsố CMND/CCCDĐã tồn tại");
                   }
               }
           }
       }


        return super.save(patient);
    }

    @Transactional(readOnly = false)
    public Patient wxSave(Patient patient) {

        // C2 (iteration 50): wxSave truoc day lay company truc tiep tu payload
        // roi dao.wxUpdate (WHERE id tran, khong loc company) / wxInsert ghi
        // thang company payload — token tenant A ghi de / tao benh nhan tenant B.
        // Payload thieu company thi getCompany().getId() NPE 500. Chan truoc
        // moi ghi, theo dung ngu nghia C1: company null/rong -> 400 (khong
        // the xac dinh tenant, khong de NPE); company lech session tenant ->
        // 400 qua verifyTenantOwnership; company rong/session rong giu hanh
        // vi cu o verifyTenantOwnership nhung khong toi duoc day khi thieu
        // company vi guard null o tren da chan.
        if (patient.getCompany() == null || patient.getCompany().getId() == null
                || patient.getCompany().getId().trim().isEmpty()) {
            throw new ServiceException("Thiếu thông tin phòng khám (company), không xác định được tenant");
        }
        // DS176 A: wxSave ghi thang qua dao.wxInsert/wxUpdate, vong qua
        // checkPatientLengths cua save() — chuoi qua dai se 500 o DB.
        checkPatientLengths(patient);
        verifyTenantOwnership(patient);
        //Lấy thông tin có người dùng đó không theo số CMND/CCCD của người dùng
        List<Patient> patientByCard = this.dao.getPatientByCard(patient.getCard(), patient.getCompany().getId(),null);

        //NếuTồn tạiđóNgười dùng, nhưng có Không cóliên kếtchỉ địnhopenid của nóithìtiến hành dòngliên kếtchỉ địnhthao tác
        //C2 — list rỗng (CCCD chưa tồn tại) không được deref get(0): guard
        //rỗng ở cả hai nhánh để rơi về nhánh insert bên dưới, giữ nguyên
        //điều kiện &&/|| cũ cho list không rỗng, không đổi getByCard.
        if(!CollectionUtils.isEmpty(patientByCard) && (!ObjectUtils.isEmpty(patientByCard.get(0)) && patientByCard.get(0).getOpenId()==null || Objects.equals(patientByCard.get(0).getOpenId(),"") && !ObjectUtils.isEmpty(patientByCard.get(0)))){
            patient.setId(patientByCard.get(0).getId());
            // C2: stored-row check kieu C1/Q4 — id lay tu dong lookup phai
            // thuoc session tenant, khong chi tin company payload (lookup
            // da loc theo payload company, day la lop phong thu thu hai).
            verifyTenantOwnershipAgainstStoredRow(patient);
            patient.setUpdateDate(new Date());
            this.dao.wxUpdate(patient);
            return patient;
        }else if(!CollectionUtils.isEmpty(patientByCard) && !ObjectUtils.isEmpty(patientByCard.get(0)) && !ObjectUtils.isEmpty(patientByCard.get(0).getOpenId())){
            if(Objects.equals(patient.getOpenId(),patientByCard.get(0).getOpenId())){
                throw new ServiceException("đó Bệnh nhân số CMND/CCCD Đã liên kết chỉ định!");
            }else {
                throw new ServiceException("Số CMND/CCCD của bệnh nhân này đã liên kết với tài khoản WeChat khác, Không thể trùng lặp liên kết chỉ định!");
            }
        }else {
            patient.setId(IdGen.uuid());
            patient.setCreateBy("WeChat");
            patient.setUpdateBy("WeChat");
            patient.setCreateDate(new Date());
            patient.setUpdateDate(new Date());
            this.dao.wxInsert(patient);
            return patient;
        }

//        Patient patientByPhone = this.dao.getPatientByPhone(patient.getPhone(), patient.getCompany().getId());
//        if(!ObjectUtils.isEmpty(patientByPhone) && patientByPhone.getOpenId()==null){
//
//        }


    }

    @Transactional(readOnly = false)
    public Patient saveSuper(Patient patient) {
        return super.save(patient);
    }

    /**
     * DS127 C: bulkInsert/bulkUpdate fail-closed 400 (DS94/DS120 class).
     * Duong bulk bypass @Valid nen cac truong @NotNull cua Patient
     * (DS79: name/gender/age/month/phone/card) phai kiem tra tay o day:
     * bulkInsert thieu name truoc day 500 NOT NULL o mapper (probe C5),
     * [null] NPE 500 o preInsert (C4). bulkUpdate ghost id 200 silent,
     * mixed ghi mot phan (C6/C7). Tien kiem TOAN BO list truoc khi ghi
     * dong nao. null/[] giu 400 nhu hien co (C2/C3/C8/C9). Delete:
     * DS128 C doi single delete ghost/rong/da xoa mem sang 400 (xem
     * delete() duoi); bulkDelete ghost giu 200 idempotent (DS126 B).
     * Khong them guard trung SDT/CCCD vao bulk — ngoai pham vi probe,
     * duong bulk hien khong co guard do.
     */
    private void validateRequiredForBulk(Patient patient) {
        if (patient == null) {
            throw new ServiceException("Thông tin bệnh nhân không được để trống");
        }
        if (patient.getName() == null || patient.getName().trim().isEmpty()) {
            throw new ServiceException("Tên bệnh nhân không được để trống");
        }
        if (patient.getGender() == null || patient.getGender().getValue() == null
                || patient.getGender().getValue().trim().isEmpty()) {
            throw new ServiceException("Giới tính không được để trống");
        }
        if (patient.getAge() == null || patient.getAge().trim().isEmpty()) {
            throw new ServiceException("Tuổi không được để trống");
        }
        if (patient.getMonth() == null || patient.getMonth().trim().isEmpty()) {
            throw new ServiceException("Tháng không được để trống");
        }
        if (patient.getPhone() == null || patient.getPhone().trim().isEmpty()) {
            throw new ServiceException("Số điện thoại liên hệ không được để trống");
        }
        if (patient.getCard() == null || patient.getCard().trim().isEmpty()) {
            throw new ServiceException("Số CMND/CCCD không được để trống");
        }
    }

    /**
     * DS148 B: chan do dai qua dai qua bulk/save. bulkInsert/bulkUpdate
     * khong qua @Valid nen cac truong co @Length van 500 o mapper khi
     * qua bulk; name @Length(max=100) LECH voi DB varchar(45) nen single
     * save name 46..100 qua @Valid roi 500 o DB (probe B1). cardType la
     * String thuong varchar(10) khong co @Length (probe B2); openId
     * varchar(255) khong co @Length (probe B3); gia tri DictItem
     * gender/withPatientNexus viet thang vao varchar(64) trong khi
     * DictItem @Length(128) khong guard cot dich (probe B4). Gioi han
     * lay theo cot DB, mau checkCostItemLengths (DS146 A / DS147 B);
     * KHONG sua annotation @Length(max=100) cua name -- service check
     * la diem chan thuc thi cho ca save() + bulk*. Null-safe.
     */
    private static void checkPatientLengths(Patient patient) {
        if (patient == null) {
            return;
        }
        checkLength(patient.getName(), 45, "Tên bệnh nhân độ dài phải nằm trong khoảng 1 đến 45");
        checkLength(patient.getAge(), 45, "Tuổi Độ dài phải nằm trong khoảng 1 đến 45");
        checkLength(patient.getMonth(), 45, "Tháng Độ dài phải nằm trong khoảng 1 đến 45");
        checkLength(patient.getPhone(), 45, "liên hệ cách Độ dài phải nằm trong khoảng 1 đến 45");
        checkLength(patient.getCard(), 45, "số CMND/CCCD Độ dài phải nằm trong khoảng 1 đến 45");
        checkLength(patient.getCardType(), 10, "Loại giấy tờ Độ dài phải nằm trong khoảng 0 đến 10");
        checkLength(patient.getHealthCardNo(), 45, "sức khỏe thẻ số Độ dài phải nằm trong khoảng 0 đến 45");
        checkLength(patient.getProvince(), 45, "Độ dài tỉnh phải nằm trong khoảng 0 đến 45");
        checkLength(patient.getCity(), 45, "Độ dài thành phố phải nằm trong khoảng 0 đến 45");
        checkLength(patient.getArea(), 45, "Độ dài quận phải nằm trong khoảng 0 đến 45");
        checkLength(patient.getAddress(), 45, "Độ dài địa chỉ chi tiết phải nằm trong khoảng 0 đến 45");
        checkLength(patient.getGuardianName(), 100, "người giám hộ Độ dài phải nằm trong khoảng 0 đến 100");
        checkLength(patient.getGuardianPhone(), 100, "người giám hộ Độ dài số điện thoại phải nằm trong khoảng 0 đến 100");
        checkLength(patient.getOpenId(), 255, "Độ dài định danh WeChat phải nằm trong khoảng 0 đến 255");
        checkLength(patient.getRemarks(), 255, "Ghi chú Độ dài phải nằm trong khoảng 0 đến 255");
        checkDictLength(patient.getGender(), 64, "Giới tính Độ dài phải nằm trong khoảng 0 đến 64");
        checkDictLength(patient.getWithPatientNexus(), 64, "Quan hệ với bệnh nhân Độ dài phải nằm trong khoảng 0 đến 64");
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

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<Patient> entitys) {
        if (CollectionUtils.isEmpty(entitys)) {
            throw new ServiceException("Danh sách bệnh nhân thêm mới không được để trống");
        }
        for (Patient entity : entitys) {
            validateRequiredForBulk(entity);
            checkPatientLengths(entity);
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<Patient> entitys) {
        if (CollectionUtils.isEmpty(entitys)) {
            throw new ServiceException("Danh sách bệnh nhân cập nhật không được để trống");
        }
        for (Patient entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Thông tin bệnh nhân không được để trống");
            }
            if (entity.getId() == null || entity.getId().trim().isEmpty()) {
                throw new ServiceException("Cập nhật hàng loạt yêu cầu id của bệnh nhân đã tồn tại");
            }
            if (this.dao.get(entity.getId()) == null) {
                throw new ServiceException("Bệnh nhân không tồn tại: " + entity.getId());
            }
            checkPatientLengths(entity);
        }
        return super.bulkUpdate(entitys);
    }

    /**
     * DS128 C: single delete fail-closed 400 -- id rong / id ma / dong
     * da xoa mem (mapper UPDATE SET del_flag='1' WHERE id, KHONG loc
     * del_flag nen xoa lan 2 van rows 1 va ghi them 1 audit DELETED
     * trung -- mau DS123). Deep search: DS123/DS124/DS125 da doi single
     * delete ghost sang 400 o ~25 service anh em; DS127 giu 200 o day
     * theo "parity DS123" la nguoc -- chinh DS123 la vong pha parity
     * 200 do. Chi bulkDelete ghost moi giu 200 idempotent
     * (DS99/DS106/DS126 B), khong doi o day. Kiem tra ton tai bang
     * dao.get (DS127 C: get chi LEFT JOIN va KHONG loc del_flag nen
     * tra ca dong da xoa mem; delFlag phan biet); mau DrugService.
     * Khong caller noi bo nao goi patientService.delete.
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(Patient patient) {
        if (patient == null || patient.getId() == null || patient.getId().trim().isEmpty()) {
            throw new ServiceException("Không xác định được bệnh nhân cần xóa.");
        }
        Patient stored = this.dao.get(patient.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Bệnh nhân không tồn tại: " + patient.getId());
        }
        return super.delete(patient);
    }

    public Patient getPatientByPhone(String phoneNumber){
        return this.dao.getPatientByPhone(phoneNumber, SessionUtils.getLoginTenantId());
    }

    /**
     * G3 — Tra trùng bệnh nhân cho form đăng ký, KHÔNG tự động merge.
     *
     * Thứ tự khớp: SĐT trước, rồi CMND/CCCD; các khớp SĐT đứng trước các
     * khớp chỉ theo CMND/CCCD, một bệnh nhân khớp cả hai chỉ xuất hiện một
     * lần với matchType "both". SĐT chung của gia đình trả về nhiều kết quả
     * để lễ tân chọn; bệnh nhân không có SĐT vẫn khớp được theo CMND/CCCD.
     *
     * Chuẩn hóa: chỉ giữ chữ số (bỏ khoảng trắng, gạch, chấm, ngoặc, dấu
     * +); SĐT dạng +84/84 đầu số được quy về 0 (cùng quy tắc G2). CMND 9
     * số và CCCD 12 số là hai chuỗi khác nhau nên không bao giờ khớp chéo
     * nhau. So khớp ở Java trên danh sách bệnh nhân còn hiệu lực của
     * tenant đang đăng nhập (quy mô một phòng khám, tối đa 500 dòng) thay
     * vì REPLACE lồng nhau trong SQL — vừa đúng với mọi định dạng SĐT đã
     * lưu, vừa không cần đổi schema. Chỉ đọc, không ghi, không merge.
     *
     * Validator CMND/CCCD hiện tại không nằm ở đây và không bị động tới.
     */
    private static final ZoneId DB_ZONE = ZoneId.of("GMT+7");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int DUPLICATE_LIMIT = 20;

    public static String normalizePhone(String raw) {
        if (raw == null) {
            return null;
        }
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.startsWith("84") && digits.length() >= 11) {
            digits = "0" + digits.substring(2);
        }
        return digits.isEmpty() ? null : digits;
    }

    public static String normalizeCard(String raw) {
        if (raw == null) {
            return null;
        }
        String digits = raw.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? null : digits;
    }

    public List<PatientDuplicate> findDuplicates(String phone, String card) {
        String normPhone = normalizePhone(phone);
        String normCard = normalizeCard(card);
        String rawCard = card == null ? null : card.trim();
        if (normPhone == null && normCard == null && (rawCard == null || rawCard.isEmpty())) {
            return new ArrayList<PatientDuplicate>();
        }
        // delFlag truyền tường minh — bài học G1 (xem PatientDao).
        List<Patient> actives = this.dao.listActiveByCompany(
                SessionUtils.getLoginTenantId(), BaseEntity.DEL_FLAG_NORMAL, 500);
        LinkedHashMap<String, PatientDuplicate> phoneMatches = new LinkedHashMap<String, PatientDuplicate>();
        LinkedHashMap<String, PatientDuplicate> cardMatches = new LinkedHashMap<String, PatientDuplicate>();
        for (Patient p : actives) {
            if (p == null || p.getId() == null) {
                continue;
            }
            boolean phoneHit = normPhone != null && normPhone.equals(normalizePhone(p.getPhone()));
            boolean cardHit = false;
            if (normCard != null && normCard.equals(normalizeCard(p.getCard()))) {
                cardHit = true;
            } else if (rawCard != null && !rawCard.isEmpty()
                    && p.getCard() != null && rawCard.equalsIgnoreCase(p.getCard().trim())) {
                cardHit = true;
            }
            if (!phoneHit && !cardHit) {
                continue;
            }
            PatientDuplicate dto = toDuplicate(p);
            if (phoneHit) {
                dto.setMatchType(cardHit ? "both" : "phone");
                phoneMatches.put(p.getId(), dto);
            } else {
                dto.setMatchType("card");
                cardMatches.put(p.getId(), dto);
            }
        }
        List<PatientDuplicate> result = new ArrayList<PatientDuplicate>();
        result.addAll(phoneMatches.values());
        result.addAll(cardMatches.values());
        if (result.size() > DUPLICATE_LIMIT) {
            return result.subList(0, DUPLICATE_LIMIT);
        }
        return result;
    }

    private PatientDuplicate toDuplicate(Patient p) {
        PatientDuplicate dto = new PatientDuplicate();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setPhone(p.getPhone());
        dto.setCard(p.getCard());
        if (p.getGender() != null) {
            dto.setGenderValue(p.getGender().getValue());
            dto.setGenderName(p.getGender().getName());
        }
        dto.setAge(p.getAge());
        dto.setMonth(p.getMonth());
        if (p.getBirthday() != null) {
            // Cột DATE: ngày đọc về phải hiểu theo GMT+7 (= vùng phiên
            // JDBC sau H1, bài học G2: vùng hiểu ngày phải bằng vùng phiên).
            dto.setBirthday(p.getBirthday().toInstant().atZone(DB_ZONE).toLocalDate().format(DATE_FMT));
        }
        dto.setHealthCardNo(p.getHealthCardNo());
        dto.setProvince(p.getProvince());
        dto.setCity(p.getCity());
        dto.setArea(p.getArea());
        dto.setAddress(p.getAddress());
        dto.setGuardianName(p.getGuardianName());
        dto.setGuardianPhone(p.getGuardianPhone());
        return dto;
    }

    // DS7 (round 7): getByCard cu deref .get(0) khong guard (list rong ->
    // IndexOutOfBounds 500) va getCompany().getId() NPE khi thieu company.
    // Nay tra null khi khong khop / thieu company (controller da xu ly
    // null: khong ghi audit, tra successJson(null)); tenant do controller
    // ep theo session (parity C3 getByOpenId), service chi tra dong dau.
    public Patient getByCard(String card, String companyId){
        if (companyId == null || companyId.trim().isEmpty()) {
            return null;
        }
        List<Patient> list = this.dao.getPatientByCard(card, companyId, null);
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    public Patient getPatientByregistrationId(String registrationId) {
        Patient patient=this.dao.getPatientByregistrationId(registrationId);
        return patient;
    }

    public List<Patient> getByOpenId(String openId,String companyId) {
        List<Patient> patients = this.dao.getByOpenId(openId,companyId);
        return patients;
    }

    /* Tra cứu thông tin bệnh nhân theo họ tên và CMND/CCCD của bệnh nhân */
    public Patient inquire(Patient patient) {
        Patient patients = this.dao.inquire(patient);
        return patients;
    }

}