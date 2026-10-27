package com.geeke.medicareutils.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.geeke.admin.entity.User;
import com.geeke.medicareutils.domain.reqpo.RequestData;
import com.geeke.medicareutils.domain.respo.MdCompanyData;
import com.geeke.medicareutils.domain.respo.MdStaffData;
import com.geeke.medicareutils.service.MdCompanyService;
import com.geeke.medicareutils.util.MdRequestUtil;
import com.geeke.medicareutils.util.YbWebApiUtil;
import com.geeke.org.entity.ClinicOffice;
import com.geeke.utils.SessionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * KhoaTriển khaiLớp
 */
@Service
@RequiredArgsConstructor
public class MdCompanyServiceImpl implements MdCompanyService {

//    private final MdRequestUtil mdRequestUtil;

    private final YbWebApiUtil mdRequestUtil;


    @Override
    public String getOrgCompanyInfo_1201(String infoNo, String name, String fixmedinsCode) {
        JSONObject data = new JSONObject();
        //y tế được chỉ địnhCơ sở
        data.put("fixmdins_type", "1");
        data.put("fixmdins_name", name);
        data.put("fixmedins_code", fixmedinsCode);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", data);
        return mdRequestUtil.getMedicareData("1201", jsonObject.toJSONString());
    }

    @Override
    public String upCompanyData_3401_3402(ClinicOffice clinicOffice, String infoNo) {
        JSONObject data = new JSONObject();
        data.put("hosp_dept_codg", clinicOffice.getCode());
        data.put("hosp_dept_name", clinicOffice.getName());

        data.put("caty", clinicOffice.getCategory());
        data.put("begntime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(clinicOffice.getCreateDate()));
        data.put("endtime", "");
        data.put("itro", clinicOffice.getItro());
        data.put("dept_resper_name", clinicOffice.getDeptResperName());
        data.put("dept_resper_tel", clinicOffice.getDeptResperTel());
        data.put("dept_estbdat", clinicOffice.getDeptEstbdat());
        data.put("dept_med_serv_scp", "");
        data.put("aprv_bed_cnt", clinicOffice.getAprvBedCnt());
        data.put("hi_crtf_bed_cnt", "");
        data.put("poolarea_no", clinicOffice.getPoolareaNo());
        data.put("tecn_psncnt", clinicOffice.getTecnPsncnt());
        data.put("phar_psncnt", clinicOffice.getPharPsncnt());
        data.put("dr_psncnt", clinicOffice.getDrPsncnt());
        data.put("nurs_psncnt", clinicOffice.getNursPsncnt());
        data.put("memo", "");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", data);
        return mdRequestUtil.getMedicareData(infoNo, jsonObject.toJSONString());
    }

    @Override
    public String upListCompanyData_3402A(List<ClinicOffice> clinicOffice, String infoNo) {
        JSONObject jsonObject = new JSONObject();

        return mdRequestUtil.getMedicareData(infoNo, jsonObject.toJSONString());
    }


    @Override
    public String delCompanyData_3403(ClinicOffice clinicOffice) {
        JSONObject data = new JSONObject();
        data.put("hosp_dept_codg", clinicOffice.getCode());
        data.put("hosp_dept_name", clinicOffice.getName());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", data);
        return mdRequestUtil.getMedicareData("3403", jsonObject.toJSONString());
    }

    @Override
    public String getCompanyInfo_5101() {
        RequestData requestData = new RequestData();
        //Lấy mã và tên cơ sở y tế được chỉ định
        String FixmedinsCode = SessionUtils.getUserJson().getJSONObject("company").getString("fixmedinsCode");
        String FixmedinsName = SessionUtils.getUserJson().getJSONObject("company").getString("name");
        requestData.setInfno("5101");
        //Bên gửiid
        String MsgId = FixmedinsCode + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "1234";
        requestData.setMsgid(MsgId);
        //Vùng BHYT khám bệnh
        requestData.setMdtrtarea_admvs("value4");
        //Vùng BHYT nơi tham gia bảo hiểm
        requestData.setInsuplc_admdvs("value3");
        //Mã hệ thống bên nhận
        requestData.setRecer_sys_code("value5");
        //Mã thiết bị
        requestData.setDev_no("value6");
        //Thông tin an toàn thiết bị
        requestData.setDev_safe_info("value7");
        //Thông tin chữ ký số của mã an toàn kết nối BHYT
        requestData.setCainfo("value8");
        //Loại chữ ký  Khuyến nghịSM2, SM3
        requestData.setSigntype("value9");
        //Số phiên bản giao diện
        requestData.setInfver("value9");
        //1-Người thực hiện; 2-Thiết bị đầu cuối tự phục vụ; 3-Thiết bị đầu cuối di động
        requestData.setOpter_type("value10");
        //Người thực hiện được truyền vào/Mã thiết bị đầu cuối
        requestData.setOpter("value11");
        //Tên người thực hiện
        requestData.setOpter_name("value12");
        //Thời gian giao dịch giao diện
        requestData.setInf_time("value13");
        //Đặt mã và tên cơ sở y tế được chỉ định
        requestData.setFixmedins_code(FixmedinsCode);
        requestData.setFixmedins_name(FixmedinsName);
        //Giao diện check-in
        //Lấy số thứ tự check-in từ bộ nhớ đệm cục bộ(BHYTChưaBật giờ là trống)
        requestData.setSign_no("");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", JSON.toJSON(requestData));
        return mdRequestUtil.getMedicareData("5101", jsonObject.toJSONString());
    }

    @Override
    public String getStaffInfo_5102(User user) {
        JSONObject data = new JSONObject();
        //nhân viên hành nghề phútLớp
        data.put("prac_psn_type", user.getUserExt());
        //Loại giấy tờ
        data.put("psn_cert_type", user.getUserExt());
        //Số giấy tờ
        data.put("certno", user.getUserExt());
        //nhân viênTên
        data.put("prac_psn_name", user.getUserExt());
        //Mã nhân viên hành nghề
        data.put("prac_psn_code", user.getUserExt());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", data);
        return mdRequestUtil.getMedicareData("5102", jsonObject.toJSONString());
    }


}
