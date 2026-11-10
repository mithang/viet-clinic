package com.geeke.medicareutils.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geeke.common.service.ServiceException;
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
        // DS181: luot kham MOI (chua co id) thi getPatientByregistrationId
        // tra null vi SELECT theo registration.id -- truoc day NPE 500 o
        // patientTemp.getCard()/getName() ben duoi. Fallback sang benh
        // nhan tren entity; van khong co thi 400 thay vi 500.
        Patient patientTemp = patientDao.getPatientByregistrationId(registration.getId()) ;
        if (patientTemp == null && registration.getPatientId() != null
                && registration.getPatientId().getId() != null) {
            patientTemp = patientDao.get(registration.getPatientId().getId());
        }
        if (patientTemp == null) {
            throw new ServiceException("Không tìm thấy bệnh nhân của lượt khám");
        }
        if (registration.getCardType() == null
                || registration.getCardType().getValue() == null) {
            throw new ServiceException("Chưa chọn loại giấy tờ khám BHYT");
        }
        //Loại giấy tờ khám bệnh chờchỉ định TODO đọcthẻLấy
        data.put("mdtrt_cert_type",registration.getCardType().getValue());
        //“01” thì điền chứng từ điện tử Token,  là “02” thì điền số CMND/CCCD,  là “03” thì điền số thẻ an sinh xã hội
        data.put("mdtrt_cert_no",registration.getMdtrtCertNo());
        //thẻnhận dạngloạimã 03 giờbắt buộc
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
        // DS181: middleware tra ve khong phai JSON object / thieu
        // baseinfo truoc day NPE/JSONException 500 o getBaseinfo().
        // YbWebApiUtil da guard rong/khong-phai-JSON, day la guard them
        // cho dang JSON khong dung (vd array) va thieu baseinfo.
        Output_1101 output1101;
        try {
            output1101 = JSONObject.parseObject(webApiUtil.getMedicareData("1101", jsonObject.toJSONString()), Output_1101.class);
        } catch (com.alibaba.fastjson.JSONException e) {
            throw new ServiceException("Cổng BHYT trung gian trả về dữ liệu 1101 không hợp lệ");
        }
        if (output1101 == null || output1101.getBaseinfo() == null) {
            throw new ServiceException("Cổng BHYT trung gian trả về dữ liệu 1101 không hợp lệ (thiếu baseinfo)");
        }
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
            Output_1101.InsuInfo insuInfo = output1101.getInsuinfo().get(0); // giả địnhđặtchỉlấyDòng mộtCáitham gia bảo hiểmThông tin
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
