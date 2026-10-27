package com.geeke.medicareutils.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geeke.medicareutils.domain.respo.Output_1101;
import com.geeke.medicareutils.service.MdPsnDataService;
import com.geeke.medicareutils.util.YbWebApiUtil;
import com.geeke.outpatient.dao.PatientDao;
import com.geeke.outpatient.entity.Patient;
import com.geeke.outpatient.entity.PatientMdData;
import com.geeke.outpatient.entity.Registration;
import com.geeke.outpatient.service.PatientMdDataService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MdPsnDataServiceImpl  implements MdPsnDataService {

    //private final MdRequestUtil mdRequestUtil;

    private final YbWebApiUtil webApiUtil;

    private final PatientMdDataService patientMdDataService;

    private final PatientDao patientDao;


    @Override
    public Boolean getAndSetPsnData(Registration registration) {
        //xây dựngVui lòngyêu cầuTham số
        JSONObject data = new JSONObject();
        //LấyThông tin bệnh nhân
        Patient patientTemp = patientDao.getPatientByregistrationId(registration.getId()) ;
        //Loại giấy tờ khám bệnh chờchỉ định TODO đọcthẻLấy
        data.put("mdtrt_cert_type",registration.getCardType().getValue());
        //“01” thì điền chứng từ điện tử Token,  là “02” thì điền số CMND/CCCD,  là “03” thì điền số thẻ an sinh xã hội
        data.put("mdtrt_cert_no",registration.getMdtrtCertNo());
        //thẻ识loạimã 03 giờbắt buộc
        data.put("card_sn","");
        if("03".equals(registration.getCardType().getValue())){
            data.put("card_sn",patientTemp.getCard());
        }
        //Thời gian bắt đầu, Lấylịch sửtham gia bảo hiểmThông tin giờtruyền vào
        data.put("begntime","");
        //Loại giấy tờ nhân viên Mặc địnhCMND/CCCD TODO chờđối vớitiếptruyềnGiá trị
        data.put("psn_cert_type","01");
        //nhân viêngiấy tờsố
        data.put("certno",patientTemp.getCard());
        //Tên nhân viên
        data.put("psn_name",patientTemp.getName());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data",data);
        Output_1101 output1101 = JSONObject.parseObject(webApiUtil.getMedicareData("1101", jsonObject.toJSONString()), Output_1101.class);
        //xây dựngBHYTBệnh nhâncơ bảnThông tin
        PatientMdData patientMdData = new PatientMdData();
        //cơ bảnThông tin
        patientMdData.setPatientId(Long.valueOf(patientTemp.getId()));
        //patientMdData.setOrganizeId(Long.valueOf(SessionUtils.getLoginTenantId()));

        // Lấy BaseInfo LớpTrongThông tin
        Output_1101.BaseInfo baseInfo = output1101.getBaseinfo();
        patientMdData.setPsnNo(baseInfo.getPsn_no());
        patientMdData.setPsnCertType(baseInfo.getPsn_cert_type());
        patientMdData.setPsnName(baseInfo.getPsn_name());
        patientMdData.setCertno(baseInfo.getCertno());
        patientMdData.setGend(baseInfo.getGend());
        patientMdData.setNaty(baseInfo.getNaty());
        patientMdData.setBrdy(baseInfo.getBrdy());
        patientMdData.setAge(baseInfo.getAge());
        patientMdData.setExpcontent("");
        // Lấy InsuInfo LớpTrongThông tin
        if (output1101.getInsuinfo() != null && !output1101.getInsuinfo().isEmpty()) {
            Output_1101.InsuInfo insuInfo = output1101.getInsuinfo().get(0); // 假đặtchỉlấyDòng mộtCáitham gia bảo hiểmThông tin
            patientMdData.setBalc(insuInfo.getBalc());
            patientMdData.setInsutype(insuInfo.getInsutype());
            patientMdData.setPsnType(insuInfo.getPsn_type());
            patientMdData.setPsnInsuStas(insuInfo.getPsn_insu_stas());
            patientMdData.setPsnInsuDate(insuInfo.getPsn_insu_date());
            patientMdData.setPausInsuDate(insuInfo.getPaus_insu_date());
            patientMdData.setCvlservFlag(insuInfo.getCvlserv_flag());
            patientMdData.setInsuplcAdmdvs(insuInfo.getInsuplc_admdvs());
            patientMdData.setEmpName(insuInfo.getEmp_name());
        }


        //TheoBệnh nhânidChèn hoặc cập nhật
        return   patientMdDataService.saveOrUpdate(patientMdData, new LambdaQueryWrapper<PatientMdData>()
                .eq(PatientMdData::getPatientId, patientMdData.getPatientId()));
    }




}
