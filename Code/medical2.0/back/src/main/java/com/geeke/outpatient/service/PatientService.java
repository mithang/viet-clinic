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
               if(patients.size()>0&&!Objects.equals(patients.get(0).getPhone(),patient.getPhone())){
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


        //Lấy thông tin có người dùng đó không theo số CMND/CCCD của người dùng
        List<Patient> patientByCard = this.dao.getPatientByCard(patient.getCard(), patient.getCompany().getId(),null);

        //NếuTồn tạiđóNgười dùng, nhưng có Không cóliên kếtchỉ địnhopenid của nóithìtiến hành dòngliên kếtchỉ địnhthao tác
        if(!ObjectUtils.isEmpty(patientByCard.get(0)) && patientByCard.get(0).getOpenId()==null || Objects.equals(patientByCard.get(0).getOpenId(),"") && !ObjectUtils.isEmpty(patientByCard.get(0))){
            patient.setId(patientByCard.get(0).getId());
            patient.setUpdateDate(new Date());
            this.dao.wxUpdate(patient);
            return patient;
        }else if(!ObjectUtils.isEmpty(patientByCard.get(0)) && !ObjectUtils.isEmpty(patientByCard.get(0).getOpenId())){
            if(Objects.equals(patient.getOpenId(),patientByCard.get(0).getOpenId())){
                throw new ServiceException("đóBệnh nhânsố CMND/CCCDĐãliên kếtchỉ định!");
            }else {
                throw new ServiceException("Số CMND/CCCD của bệnh nhân này đã liên kết với tài khoản WeChat khác, Không thểtrùng lặpliên kếtchỉ định!");
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

    public Patient getByCard(Patient patient){
        return this.dao.getPatientByCard(patient.getCard(), patient.getCompany().getId(),null).get(0);
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