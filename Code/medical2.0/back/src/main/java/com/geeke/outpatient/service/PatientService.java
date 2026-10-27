package com.geeke.outpatient.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.medicareutils.config.MedicareConfigProperties;
import com.geeke.medicareutils.service.MdPsnDataService;
import com.geeke.medicareutils.util.MdRequestUtil;
import com.geeke.outpatient.dao.PatientDao;
import com.geeke.outpatient.dao.PatientMdDataMapper;
import com.geeke.outpatient.entity.Patient;
import com.geeke.outpatient.entity.PatientMdData;
import com.geeke.utils.IdGen;
import com.geeke.utils.SessionUtils;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Date;
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