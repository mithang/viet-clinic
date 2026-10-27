package com.geeke.medicareutils.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geeke.admin.entity.User;
import com.geeke.medicareutils.domain.respo.MdFeeDetail;
import com.geeke.medicareutils.service.MdRegistrationService;
import com.geeke.medicareutils.util.YbWebApiUtil;
import com.geeke.org.entity.ClinicOffice;
import com.geeke.outpatient.entity.MedicalRecord;
import com.geeke.outpatient.entity.PatientMdData;
import com.geeke.outpatient.entity.Registration;
import com.geeke.outpatient.service.MedicalRecordService;
import com.geeke.outpatient.service.PatientMdDataService;
import com.geeke.toll.service.TollInfoService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class MdRegistrationServiceImpl implements MdRegistrationService {

//    private final MdRequestUtil mdRequestUtil;

    private final YbWebApiUtil mdRequestUtil;

    private final PatientMdDataService patientMdDataService;

    private final TollInfoService tollInfoService;

    private final MedicalRecordService medicalRecordService;

    public MdRegistrationServiceImpl(YbWebApiUtil mdRequestUtil,
                                     PatientMdDataService patientMdDataService,
                                     @Lazy TollInfoService tollInfoService,
                                     @Lazy MedicalRecordService medicalRecordService) {
        this.mdRequestUtil = mdRequestUtil;
        this.patientMdDataService = patientMdDataService;
        this.tollInfoService = tollInfoService;
        this.medicalRecordService = medicalRecordService;
    }



    /**
     * ngoại trúĐăng ký khám  2201
     * @param registration
     * @return
     */
    @Override
    public String getRegistrationInfo_2201(Registration registration) {
        Map<String,Object> data = new HashMap<>();
        //LấyThông tin BHYT của bệnh nhân
        PatientMdData patientMdData = patientMdDataService.getOne( new LambdaQueryWrapper<PatientMdData>().eq(PatientMdData::getPatientId,registration.getPatientId().getId()));
        //Đăng ký khámBác sĩThông tin
        User doctor = registration.getDoctor();
        //Đăng ký khámThông tin khoa
        ClinicOffice clinicOffice = registration.getClinicOffice();
        //Vui lòngyêu cầuThông tinxây dựng
        data.put("psn_no",patientMdData.getPsnNo());
        //
        data.put("insutype",patientMdData.getInsutype());
        data.put("begntime",registration.getCreateDate());
        //Loại giấy tờ khám bệnh
        data.put("mdtrt_cert_type",registration.getCardType());
        // khám bệnhchứng từmã số
        data.put("mdtrt_cert_no",registration.getMdtrtCertNo());
        //Số thứ tự duy nhất của khám ngoại trú
        data.put("ipt_otp_no",registration.getId());
        //Bác sĩMã
        data.put("atddr_no",doctor.getUserExt().getPracPsnCode());
        //Bác sĩTên
        data.put("dr_name",doctor.getName());
        data.put("dept_code",clinicOffice.getCode());
        data.put("dept_name",clinicOffice.getName());
        data.put("caty",clinicOffice.getCategory());
        data.put("expContent","");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", JSON.toJSON(data));
        return mdRequestUtil.getMedicareData("2201",jsonObject.toJSONString());
    }

    @Override
    public String revokeRegistrationInfo_2202(Registration registration) {
        //LấyThông tin BHYT của bệnh nhân
        PatientMdData patientMdData = patientMdDataService.getOne( new LambdaQueryWrapper<PatientMdData>().eq(PatientMdData::getPatientId,registration.getPatientId().getId()));
        Map<String,Object> data = new HashMap<>();
        data.put("psn_no",patientMdData.getPsnNo());
        data.put("mdtrt_id",registration.getMdtrtId());
        data.put("ipt_otp_no",registration.getId());
        data.put("expContent","");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", JSON.toJSON(data));
        return mdRequestUtil.getMedicareData("2202",jsonObject.toJSONString());
    }

    /**
     * Tải lên thông tin khám ngoại trú 2203
     * @param registration
     * @return
     */
    @Override
    public String upRegistrationInfo_2203(Registration registration) {
        //đơn dòngkhám bệnhThông tin
        JSONObject data = new JSONObject();
        //Chẩn đoánnhiều dòngDữ liệu
        JSONArray jsonArray = new JSONArray();
        //Thông tin BHYT của bệnh nhân
        PatientMdData patientMdData = patientMdDataService.getOne(new LambdaQueryWrapper<PatientMdData>().eq(PatientMdData::getPatientId, registration.getPatientId().getId()));
        //Thông tin khoa của bệnh nhân
        ClinicOffice clinicOffice = registration.getClinicOffice();
        //Thông tin bác sĩ của bệnh nhân
        User doctor = registration.getDoctor();
        //Thông tin bệnh án của nhân viên
        List<MedicalRecord> record = medicalRecordService.getByOrder(registration.getId());
        MedicalRecord medicalRecord = record.get(0);
        //khám bệnhTham số
        data.put("mdtrt_id", registration.getMdtrtId());
        data.put("psn_no", patientMdData.getPsnNo());
        data.put("med_type", registration.getMedType().getValue());
        data.put("begntime", registration.getCreateDate());
        //Mô tả chính về tình trạng bệnh TODO
        data.put("main_cond_dscr", "");
        //Mã loại bệnh    TODO
        data.put("dise_code", "");
        //Tên loại bệnh TODO
        data.put("dise_name", "");
        data.put("birctrl_type", "");
        data.put("bitctrl_matn_date", "");
        data.put("matn_type", "");
        data.put("geso_val", "");
        data.put("expContent", "");

        //Chẩn đoánTham số
        JSONObject object = new JSONObject();
        //Loại chẩn đoán 1Chẩn đoán tây y 2 trong ychínhbệnhChẩn đoán 3 trong ychínhchứngChẩn đoán 4Phẫu thuậtthao tác 5.Loại bệnh thanh toán theo loại bệnh TODO chỉcho phépmộtCáiChẩn đoán
        if (Objects.nonNull(medicalRecord.getWesternDiagnose())) {
            //Tâyy
            object.put("diag_type", "1");
            object.put("diag_name", medicalRecord.getWesternDiagnose());

        }
        if (Objects.nonNull(medicalRecord.getChinaDiagnose())) {
            //Đông y
            object.put("diag_type", "2");
            object.put("diag_name", medicalRecord.getChinaDiagnose());
        }
        object.put("diag_srt_no", "1");
        object.put("diag_dept", clinicOffice.getName());
        object.put("dise_dor_no", doctor.getUserExt().getPracPsnCode());
        object.put("dise_dor_name", doctor.getUserExt().getName());
        object.put("diag_time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(medicalRecord.getCreateDate()));
        //TODO Mặc địnhHiệu lực 1, 0không hợp lệ
        object.put("vali_flag", "1");
        jsonArray.add(object);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mdtrtinfo", data);
        jsonObject.put("diseinfo", jsonArray);
        return mdRequestUtil.getMedicareData("2203", jsonObject.toJSONString());
    }

    /**
     * @param registration
     * @return ngoại trúThông tinTải lênA 2203A
     */
    @Override
    public String upRegistrationInfoList_2203A(Registration registration) {
        // tạongoàitầng JSONObject
        JSONObject jsonObject = new JSONObject();

        //Thông tin BHYT của bệnh nhân
        PatientMdData patientMdData = patientMdDataService.getOne(new LambdaQueryWrapper<PatientMdData>().eq(PatientMdData::getPatientId, registration.getPatientId().getId()));
        //Thông tin khoa của bệnh nhân
        ClinicOffice clinicOffice = registration.getClinicOffice();
        //Thông tin bác sĩ của bệnh nhân
        User doctor = registration.getDoctor();
        //Thông tin bệnh án của nhân viên
        List<MedicalRecord> record = medicalRecordService.getByOrder(registration.getId());
        MedicalRecord medicalRecord = record.get(0);

        // Tạo mdtrtinfo Đối tượng
        JSONObject mdtrtinfo = new JSONObject();
        mdtrtinfo.put("mdtrt_id", registration.getMdtrtId());
        mdtrtinfo.put("psn_no", patientMdData.getPsnNo());
        mdtrtinfo.put("med_type", registration.getMedType());
        mdtrtinfo.put("begntime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        mdtrtinfo.put("main_cond_dscr", "");

        mdtrtinfo.put("dise_codg", "");
        mdtrtinfo.put("dise_name", "");
        mdtrtinfo.put("birctrl_type", "");
        mdtrtinfo.put("birctrl_matn_date", "");
        mdtrtinfo.put("matn_type", "");
        mdtrtinfo.put("geso_val", 0);
        mdtrtinfo.put("expContent", "");

        // Sẽ  mdtrtinfo ThêmĐếnchínhĐối tượng
        jsonObject.put("mdtrtinfo", mdtrtinfo);

        // Tạo diseinfo Mảng
        JSONArray diseinfoArray = new JSONArray();
        JSONObject diseinfo = new JSONObject();
        if (Objects.nonNull(medicalRecord.getWesternDiagnose())) {
            //Tâyy
            diseinfo.put("diag_type", "1");
            diseinfo.put("diag_name", medicalRecord.getWesternDiagnose());

        }
        if (Objects.nonNull(medicalRecord.getChinaDiagnose())) {
            //Đông y
            diseinfo.put("diag_type", "2");
            diseinfo.put("diag_name", medicalRecord.getChinaDiagnose());
        }
        diseinfo.put("diag_srt_no", "1");
        diseinfo.put("diag_code", medicalRecord.getDiagnosisCode()); //Mã chẩn đoán
        diseinfo.put("diag_name", medicalRecord.getDiagnose());
        diseinfo.put("diag_dept", clinicOffice.getName());
        diseinfo.put("dise_dor_no", doctor.getUserExt().getPracPsnCode());
        diseinfo.put("dise_dor_name", doctor.getName());
        diseinfo.put("diag_time", medicalRecord.getCreateDate());
        diseinfo.put("vali_flag", "1");

        // Sẽ  diseinfo Đối tượngThêmĐếnmảng
        diseinfoArray.add(diseinfo);

        // Sẽ  diseinfoArray ThêmĐếnchínhĐối tượng
        jsonObject.put("diseinfo", diseinfoArray);
        return mdRequestUtil.getMedicareData("2203A", jsonObject.toJSONString());
    }

    /**
     * ngoại trúChi phíChi tiếtThông tinTải lên  2204
     *
     * @param
     * @return
     */
    @Override
    public String upRegistrationMoneyInfo_2204(JSONArray jsonArray) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("feedetail",jsonArray);
        return mdRequestUtil.getMedicareData("2204", jsonObject.toJSONString());
    }

    /**
     * ngoại trúChi tiếtTải lênhủy 2205
     *
     * @param mdtrtId
     * @param chrgBchno
     * @param psnNo
     * @param expContent
     * @return
     */
    @Override
    public String revokeRegistrationMoneyInfo_2205(String mdtrtId, String chrgBchno, String psnNo, String expContent) {
        JSONObject data = new JSONObject();
        data.put("mdtrt_id", mdtrtId);
        data.put("chrg_bchno", chrgBchno);//000thìlấyhủyTất cả
        data.put("psn_no", psnNo);
        data.put("expContent", expContent);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", data);
        return mdRequestUtil.getMedicareData("2205", jsonObject.toJSONString());
    }

    /**
     * ngoại trútrướcQuyết toán 2206
     *
     * @param registration Thông tin đăng ký khám
     * @param mdFeeDetail  Thu phíThông tin
     * @return
     */
    @Override
    public String processOutpatientPreSettlement_2206(Registration registration, MdFeeDetail mdFeeDetail, String chrgBchno, String acctUsedFlag) {
        JSONObject data = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        PatientMdData patientMdData = patientMdDataService.getOne(new LambdaQueryWrapper<PatientMdData>().eq(PatientMdData::getPatientId, registration.getPatientId().getId()));
        data.put("psn_no", patientMdData.getPsnNo());
        //Loại giấy tờ khám bệnh TODO
        data.put("mdtrt_cert_type","");
        //“01” thì điền chứng từ điện tử Token,  là “02” thì điền số CMND/CCCD,  là “03” thì điền số thẻ an sinh xã hội
        data.put("mdtrt_cert_no","");
        //Loại hình y tế
        data.put("med_type", registration.getMedType().getValue());
        data.put("medfee_sumamt", mdFeeDetail.getDetItemFeeSumamt());
        data.put("psn_setlway","01");//01 theohạng mụcQuyết toán  20 theochỉ địnhsố tiềnQuyết toán
        data.put("mdtrt_id",registration.getMdtrtId());
        data.put("chrg_bchno",chrgBchno);
        //Cá nhânTài khoảnsử dụngcờ 0 không 1 có 
        data.put("acc_used_flag",acctUsedFlag);
        //Loại hình bảo hiểm
        data.put("insutype",patientMdData.getInsutype());
        data.put("expContent","");
        jsonObject.put("data",data);
        return mdRequestUtil.getMedicareData("2206",jsonObject.toJSONString());
    }

    /**
     * Quyết toán ngoại trú
     *
     * @param registration
     * @param mdFeeDetail
     * @param chrgBchno
     * @param acctUsedFlag
     */
    @Override
    public String executeOutpatientPreSettlement_2207(Registration registration, MdFeeDetail mdFeeDetail, String chrgBchno, String acctUsedFlag) {
        JSONObject data = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        PatientMdData patientMdData = patientMdDataService.getOne(new LambdaQueryWrapper<PatientMdData>().eq(PatientMdData::getPatientId, registration.getPatientId().getId()));
        data.put("psn_no", patientMdData.getPsnNo());
        //Loại giấy tờ khám bệnh TODO
        data.put("mdtrt_cert_type","");
        //“01” thì điền chứng từ điện tử Token,  là “02” thì điền số CMND/CCCD,  là “03” thì điền số thẻ an sinh xã hội
        data.put("mdtrt_cert_no","");
        //Loại hình y tế
        data.put("med_type", registration.getMedType().getValue());

        data.put("medfee_sumamt", mdFeeDetail.getDetItemFeeSumamt());
        data.put("psn_setlway","01");
        data.put("mdtrt_id",registration.getMdtrtId());
        data.put("chrg_bchno",chrgBchno);
        //Cá nhânTài khoảnsử dụngcờ 0 không 1 có 
        data.put("acc_used_flag",acctUsedFlag);
        //Loại hình bảo hiểm
        data.put("insutype",patientMdData.getInsutype());
        //TODO Số hóa đơn
        data.put("invono","");
        data.put("fulamt_ownpay_amt", mdFeeDetail.getFulamtOwnpayAmt());
        data.put("overlmt_selfpay", mdFeeDetail.getOverlmtAmt());
        data.put("preselfpay_amt", mdFeeDetail.getPreselfpayAmt());
        data.put("inscp_scp_amt", mdFeeDetail.getInscpScpAmt());
        data.put("expContent","");
        jsonObject.put("data",data);
        return mdRequestUtil.getMedicareData("2207",jsonObject.toJSONString());
    }

    /**
     * Hủy quyết toán ngoại trú
     *
     * @return
     */
    @Override
    public String revokeOutpatientSettlement_2208(String setlId, String psnNo, String mdtrtId ) {
             JSONObject data = new JSONObject();
             JSONObject jsonObject = new JSONObject();
             data.put("setl_id",setlId);
             data.put("psn_no",psnNo);
             data.put("mdtrt_id",mdtrtId);
             jsonObject.put("data",data);
        return mdRequestUtil.getMedicareData("2208",jsonObject.toJSONString());
    }


    /**
     * nhân viênkhám bệnhThông tin
     *
     * @param registration
     * @param begntime
     * @param endtime
     * @return
     */
    @Override
    public String getPsnVisitData_5201(Registration registration, LocalDateTime begntime, LocalDateTime endtime) {
        JSONObject data = new JSONObject();
        PatientMdData patientMdData = patientMdDataService.getOne(new LambdaQueryWrapper<PatientMdData>().eq(PatientMdData::getPatientId, registration.getPatientId().getId()));
        data.put("psn_no",patientMdData.getPsnNo());
        data.put("begntime",begntime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        data.put("endtime",endtime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        data.put("med_type",registration.getMedType().getValue());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", JSON.toJSON(data));
        return   mdRequestUtil.getMedicareData("5201",jsonObject.toJSONString()) ;
    }

    @Override
    public String getPsnDiseData_5202(Registration registration) {
        JSONObject data = new JSONObject();
        PatientMdData patientMdData = patientMdDataService.getOne(new LambdaQueryWrapper<PatientMdData>().eq(PatientMdData::getPatientId, registration.getPatientId().getId()));
        data.put("psn_no",patientMdData.getPsnNo());
        data.put("mdtrt_id",registration.getMdtrtId());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", JSON.toJSON(data));
        return  mdRequestUtil.getMedicareData("5202",jsonObject.toJSONString());
    }

    /**
     * Quyết toánThông tinTruy vấn
     *
     * @param psnNo
     * @param setlId
     * @param mdtrtId
     * @return
     */
    @Override
    public String getSettlementInfo_5203(String psnNo, String setlId, String mdtrtId) {
        JSONObject data = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        data.put("setl_id",setlId);
        data.put("psn_no",psnNo);
        data.put("mdtrt_id",mdtrtId);
        jsonObject.put("data",data);
        return mdRequestUtil.getMedicareData("5203",jsonObject.toJSONString());

    }

    /**
     * Chi phíChi tiếtTruy vấn
     *
     * @param psnNo
     * @param setlId
     * @param mdtrtId
     * @return
     */
    @Override
    public String getExpenseDetails_5204(String psnNo, String setlId, String mdtrtId) {
        JSONObject data = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        data.put("setl_id",setlId);
        data.put("psn_no",psnNo);
        data.put("mdtrt_id",mdtrtId);
        jsonObject.put("data",data);
        return mdRequestUtil.getMedicareData("5204",jsonObject.toJSONString());
    }

    /**
     * Khám chữa bệnh ngoại trú và cấp cứuBản ghi
     *
     * @param registration Thông tin đăng ký khám
     * @return
     */
    @Override
    public String getEmergencyOutpatientRecords_4301(Registration registration) {
        JSONObject rgstinfo = new JSONObject();
        JSONObject caseinfo = new JSONObject();
        JSONObject diseinfo = new JSONObject();
        JSONObject rxinfo = new JSONObject();
        //Đăng ký khám
        rgstinfo.put("mdtrt_sn", "");               // Số thứ tự khám bệnh
        rgstinfo.put("mdtrt_id", "");               // Khám bệnh ID
        rgstinfo.put("psn_no", "");                 // Mã nhân viên
        rgstinfo.put("rgst_type_code", "");         // Đăng ký khámLớpmã loại
        rgstinfo.put("rgst_way_code", "");          // Đăng ký khámcáchmã
        rgstinfo.put("rgst_serv_fee", "");          // Phí đăng ký khám/yviệcdịch vụphí
        rgstinfo.put("ordr_way_code", "");          // đặt lịchđườngmã
        rgstinfo.put("retnr_flag", "");             // hủy số khámcờ
        rgstinfo.put("fstdiag_flag", "");           // Khám lần đầucờ
        rgstinfo.put("mdtrt_flag", "");             // khám bệnhcờ
        rgstinfo.put("rgst_retnr_time", "");        // Đăng ký khám/hủy số khámThời gian
        rgstinfo.put("medfee_paymtd_code", "");    // y tếChi phíphương thức thanh toánmã
        rgstinfo.put("vali_flag", "");             // Cờ hiệu lực
        //bệnh ánThông tin
        caseinfo.put("mdtrt_date", "");             // Ngày khám bệnh
        caseinfo.put("chfcomp", "");                 // Lý do khám chính
        caseinfo.put("attk_date_time", "");         // Ngày phát bệnhThời gian
        caseinfo.put("mdtrt_rea", "");               // khám bệnhgốcdo
        caseinfo.put("illhis", "");                  // bệnh史
        caseinfo.put("algs", "");                    // Tiền sử dị ứng
        caseinfo.put("aise_code", "");              // qua敏nguồnmã
        caseinfo.put("phex", "");                    // trathể
        caseinfo.put("disa_info_code", "");         // 残疾tình hìnhmã
        caseinfo.put("symp_name", "");              // triệu chứngTên
        caseinfo.put("symp_code", "");              // triệu chứngmã
        caseinfo.put("dspo_opnn", "");               // nơiđặtý kiến
        caseinfo.put("dept_code", "");               // Mã khoa
        caseinfo.put("dept_name", "");               // Tên khoa
        caseinfo.put("vali_flag", "");               // Cờ hiệu lực
        //Chẩn đoán
        diseinfo.put("tcm_diag_flag", "");        //  trong yChẩn đoáncờ
        diseinfo.put("maindiag_flag", "");        // Cờ chẩn đoán chính
        diseinfo.put("diag_code", "");            // Mã chẩn đoán
        diseinfo.put("diag_name", "");            // Tên chẩn đoán
        diseinfo.put("tcm_dise_code", "");        // Mã tên bệnh Đông y
        diseinfo.put("tcm_dise_name", "");        // Tên bệnh Đông yTên
        diseinfo.put("tcmsymp_code", "");         // Mã chứng hậu Đông y
        diseinfo.put("tcmsymp", "");               // Chứng hậu Đông y
        diseinfo.put("vali_flag", "");           // Cờ hiệu lực
        //Đơn thuốc
        rxinfo.put("rxno", "");                          // Số đơn thuốc
        rxinfo.put("rx_prsc_time","");          // Đơn thuốcThời gian kê đơn
        rxinfo.put("rx_type_code", "");                  // Đơn thuốcLớpmã loại
        rxinfo.put("rx_item_type_code", "");             // Đơn thuốchạng mục phútLớpmã
        rxinfo.put("rx_item_type_name", "");             // Tên phân loại hạng mục đơn thuốc
        rxinfo.put("rx_detl_id", "");                    // Đơn thuốcChi tiếtmã
        rxinfo.put("rx_detl_name", "");                  // Đơn thuốcChi tiếtTên
        rxinfo.put("tcmdrug_type_name", "");             // Tên loại thuốc Đông y
        rxinfo.put("tcmdrug_type_code", "");             //  trong thuốcLớpmã loại
        rxinfo.put("tcmherb_foote", "");                 // Chú thích cuối trang về thảo dược
        rxinfo.put("medn_type_code", "");                // thuốcLoạimã
        rxinfo.put("medn_type_name", "");                // thuốcLoại
        rxinfo.put("drug_dosform", "");                  // Dạng bào chế của thuốcmã
        rxinfo.put("drug_dosform_name", "");             // Dạng bào chế của thuốcTên
        rxinfo.put("drug_spec", "");                      // Quy cách thuốc
        rxinfo.put("drug_used_frqu", "");                // Sử dụng thuốc-tần suấttỷ lệ
        rxinfo.put("drug_used_idose", 0.0);              // Sử dụng thuốc-tổngliềulượng
        rxinfo.put("drug_used_sdose", 0);                // Sử dụng thuốc- lầnliềulượng
        rxinfo.put("drug_used_dosunt", "");              // Sử dụng thuốc-Đơn vị liều lượng
        rxinfo.put("drug_used_way_code", "");            // Sử dụng thuốc-đườngmã
        rxinfo.put("drug_medc_way", "");                 // Sử dụng thuốc-đường
        rxinfo.put("skintst_dicm", "");                  // Test da判loại
        rxinfo.put("medc_begntime", "");         // dùngthuốcThời gian bắt đầu
        rxinfo.put("medc_endtime", "");          // dùngthuốcdừngdừngThời gian
        rxinfo.put("medc_days", 0);                       // Số ngày dùng thuốc
        rxinfo.put("main_medc_flag", "");                // Cờ thuốc dùng chính
        rxinfo.put("urgt_flag", "");                      // thêm急cờ
        rxinfo.put("unif_purc_drug", "");                // thống nhấtMua hàngThuốc
        rxinfo.put("drug_purc_code", "");                // ThuốcMua hàngmã
        rxinfo.put("drug_mgt_plaf_code", "");            // ThuốcQuản lý平máymã
        rxinfo.put("bas_medn_flag", "");                 // Cờ thuốc cơ bản
        rxinfo.put("vali_flag", "");                     // Cờ hiệu lực
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("rgstinfo", rgstinfo);
        jsonObject.put("caseinfo", caseinfo);
        jsonObject.put("diseinfo", diseinfo);
        jsonObject.put("rxinfo", rxinfo);

        return mdRequestUtil.getMedicareData("4301", jsonObject.toJSONString());
    }

    /**
     * Chi tiếtxét duyệtviệctrước phútdịch vụ phân tích
     *
     * @param registration
     * @return
     */
    @Override
    public String analyzeDetailReviewPreCheck_3301(Registration registration) {
        //Thông tin BHYT của bệnh nhân
        PatientMdData patientMdData = patientMdDataService.getOne(new LambdaQueryWrapper<PatientMdData>().eq(PatientMdData::getPatientId, registration.getPatientId().getId()));
        //Thông tin khoa của bệnh nhân
        ClinicOffice clinicOffice = registration.getClinicOffice();
        //Thông tin bác sĩ của bệnh nhân
        User doctor = registration.getDoctor();
        //Thông tin bệnh án của nhân viên
        List<MedicalRecord> record = medicalRecordService.getByOrder(registration.getId());
        MedicalRecord medicalRecord = record.get(0);

        // Tạo data JSON Đối tượng
        JSONObject data = new JSONObject();

        JSONArray patientDtos = new JSONArray();
        JSONObject patientDto = new JSONObject();

        patientDto.put("patn_id", "");
        patientDto.put("patn_name", "");
        patientDto.put("gend", "");
        patientDto.put("brdy", "");
        patientDto.put("poolarea", "");
        patientDto.put("curr_mdtrt_id", "");

        JSONArray encounterDtos = new JSONArray();
        JSONObject encounterDto = new JSONObject();

        encounterDto.put("mdtrt_id", "");
        encounterDto.put("medins_id", "");
        encounterDto.put("medins_name", "");
        encounterDto.put("medins_admdvs", "");
        encounterDto.put("medins_type", "");
        encounterDto.put("medins_lv", "");
        encounterDto.put("wardarea_codg", "");
        encounterDto.put("wardno", "");
        encounterDto.put("bedno", "");
        encounterDto.put("adm_date", "");
        encounterDto.put("dscg_date", "");
        encounterDto.put("dscg_main_dise_codg", "");
        encounterDto.put("dscg_main_dise_name", "");

        JSONArray diagnoseDtos = new JSONArray();
        JSONObject diagnoseDto = new JSONObject();

        diagnoseDto.put("dise_id", "");
        diagnoseDto.put("inout_dise_type", "");
        diagnoseDto.put("maindise_flag", "");
        diagnoseDto.put("dias_srt_no", "");
        diagnoseDto.put("dise_codg", "");
        diagnoseDto.put("dise_name", "");
        diagnoseDto.put("dise_date", "");
        diagnoseDtos.add(diagnoseDto);
        encounterDto.put("fsi_diagnose_dtos", diagnoseDtos);

        encounterDto.put("dr_codg", "");
        encounterDto.put("adm_dept_codg", "");
        encounterDto.put("adm_dept_name", "");
        encounterDto.put("dscg_dept_codg", "");
        encounterDto.put("dscg_dept_name", "");
        encounterDto.put("med_mdtrt_type", "");
        encounterDto.put("med_type", "");

        JSONArray orderDtos = new JSONArray();
        JSONObject orderDto = new JSONObject();

        orderDto.put("rx_id", "");
        orderDto.put("rxno", "");
        orderDto.put("grpno", "");
        orderDto.put("long_drord_flag", "");
        orderDto.put("hilist_type", "");
        orderDto.put("chrg_type", "");
        orderDto.put("drord_bhvr", "");
        orderDto.put("hilist_code", "");
        orderDto.put("hilist_name", "");
        orderDto.put("hilist_dosform", "");
        orderDto.put("hilist_lv", "");
        orderDto.put("hilist_pric", "");
        orderDto.put("lv1_hosp_item_pric", "");
        orderDto.put("lv2_hosp_item_pric", "");
        orderDto.put("lv3_hosp_item_pric", "");
        orderDto.put("hilist_memo", "");
        orderDto.put("hosplist_code", "");
        orderDto.put("hosplist_name", "");
        orderDto.put("hosplist_dosform", "");
        orderDto.put("cnt", "");
        orderDto.put("pric", "");
        orderDto.put("sumamt", "");
        orderDto.put("ownpay_amt", "");
        orderDto.put("selfpay_amt", "");
        orderDto.put("spec", "");
        orderDto.put("spec_unt", "");
        orderDto.put("drord_begn_date", "");
        orderDto.put("drord_stop_date", "");
        orderDto.put("drord_dept_codg", "");
        orderDto.put("drord_dept_name", "");
        orderDto.put("drord_dr_codg", "");
        orderDto.put("drord_dr_name", "");
        orderDto.put("drord_dr_profttl", "");
        orderDto.put("curr_drord_flag", "");
        orderDtos.add(orderDto);

        encounterDto.put("fsi_order_dtos", orderDtos);
        encounterDto.put("matn_stas", "");
        encounterDto.put("medfee_sumamt", "");
        encounterDto.put("ownpay_amt", "");
        encounterDto.put("selfpay_amt", "");
        encounterDto.put("acct_payamt", "");
        encounterDto.put("ma_amt", "");
        encounterDto.put("hifp_payamt", "");
        encounterDto.put("setl_totlnum", "");
        encounterDto.put("insutype", "");
        encounterDto.put("reim_flag", "");
        encounterDto.put("out_setl_flag", "");
        encounterDto.put("fsi_operation_dtos", "");

        encounterDtos.add(encounterDto);
        patientDto.put("fsi_encounter_dtos", encounterDtos);

        patientDtos.add(patientDto);
        data.put("patient_dtos", patientDtos);
        data.put("rule_ids", null);
        data.put("trig_scen", "");
        data.put("syscode", null);
        JSONObject  jsonObject = new JSONObject();
        jsonObject.put("data", data);
        return mdRequestUtil.getMedicareData("3301", jsonObject.toJSONString());
    }

    /**
     * Chi tiếtxét duyệtviệc trong  phútdịch vụ phân tích
     *
     * @param registration 3102
     * @return
     */
    @Override
    public String analyzeDetailReviewDuringProcess_3102(Registration registration) {
        JSONObject jsonObject = new JSONObject();
        JSONObject patientDto = new JSONObject();
        //Thông tin BHYT của bệnh nhân
        PatientMdData patientMdData = patientMdDataService.getOne(new LambdaQueryWrapper<PatientMdData>().eq(PatientMdData::getPatientId, registration.getPatientId().getId()));
        //Thông tin khoa của bệnh nhân
        ClinicOffice clinicOffice = registration.getClinicOffice();
        //Thông tin bác sĩ của bệnh nhân
        User doctor = registration.getDoctor();
        //Thông tin bệnh án của nhân viên
        List<MedicalRecord> record = medicalRecordService.getByOrder(registration.getId());
        MedicalRecord medicalRecord = record.get(0);

        patientDto.put("patn_id", "");
        patientDto.put("patn_name", "");
        patientDto.put("gend", "");
        patientDto.put("brdy", "");
        patientDto.put("poolarea", "");
        patientDto.put("curr_mdtrt_id", "");

        // tạokhám bệnhThông tin JSON Đối tượng
        JSONObject encounterDto = new JSONObject();
        encounterDto.put("mdtrt_id", "");
        encounterDto.put("medins_id", "");
        encounterDto.put("medins_name", "");
        encounterDto.put("medins_admdvs", "");
        encounterDto.put("medins_type", "");
        encounterDto.put("medins_lv", "");
        encounterDto.put("wardarea_codg", "");
        encounterDto.put("wardno", "");
        encounterDto.put("bedno", "");
        encounterDto.put("adm_date", "");
        encounterDto.put("dscg_date", "");
        encounterDto.put("dscg_main_dise_codg", "");
        encounterDto.put("dscg_main_dise_name", "");

        // tạoThông tin chẩn đoán JSON Mảng
        JSONArray diagnoseDtos = new JSONArray();
        JSONObject diagnoseDto = new JSONObject();
        diagnoseDto.put("dise_id", "");
        diagnoseDto.put("inout_dise_type", "");
        diagnoseDto.put("maindise_flag", "");
        diagnoseDto.put("dias_srt_no", "");
        diagnoseDto.put("dise_codg", "");
        diagnoseDto.put("dise_name", "");
        diagnoseDto.put("dise_date", "");
        diagnoseDtos.add(diagnoseDto);
        encounterDto.put("fsi_diagnose_dtos", diagnoseDtos);

        encounterDto.put("dr_codg", "");
        encounterDto.put("adm_dept_codg", "");
        encounterDto.put("adm_dept_name", "");
        encounterDto.put("dscg_dept_codg", "");
        encounterDto.put("dscg_dept_name", "");
        encounterDto.put("med_mdtrt_type", "");
        encounterDto.put("med_type", "");

        // tạoY lệnhThông tin JSON Mảng
        JSONArray orderDtos = new JSONArray();
        JSONObject orderDto = new JSONObject();
        orderDto.put("rx_id", "");
        orderDto.put("rxno", "");
        orderDto.put("grpno", "");
        orderDto.put("long_drord_flag", "");
        orderDto.put("hilist_type", "");
        orderDto.put("chrg_type", "");
        orderDto.put("drord_bhvr", "");
        orderDto.put("hilist_code", "");
        orderDto.put("hilist_name", "");
        orderDto.put("hilist_dosform", "");
        orderDto.put("hilist_lv", "");
        orderDto.put("hilist_pric", "");
        orderDto.put("lv1_hosp_item_pric", "");
        orderDto.put("lv2_hosp_item_pric", "");
        orderDto.put("lv3_hosp_item_pric", "");
        orderDto.put("hilist_memo", "");
        orderDto.put("hosplist_code", "");
        orderDto.put("hosplist_name", "");
        orderDto.put("hosplist_dosform", "");
        orderDto.put("cnt", "");
        orderDto.put("pric", "");
        orderDto.put("sumamt", "");
        orderDto.put("ownpay_amt", "");
        orderDto.put("selfpay_amt", "");
        orderDto.put("spec", "");
        orderDto.put("spec_unt", "");
        orderDto.put("drord_begn_date", "");
        orderDto.put("drord_stop_date", "");
        orderDto.put("drord_dept_codg", "");
        orderDto.put("drord_dept_name", "");
        orderDto.put("drord_dr_codg", "");
        orderDto.put("drord_dr_name", "");
        orderDto.put("drord_dr_profttl", "");
        orderDto.put("curr_drord_flag", "");
        orderDtos.add(orderDto);
        encounterDto.put("fsi_order_dtos", orderDtos);

        encounterDto.put("matn_stas", "");
        encounterDto.put("medfee_sumamt", "");
        encounterDto.put("ownpay_amt", "");
        encounterDto.put("selfpay_amt", "");
        encounterDto.put("acct_payamt", "");
        encounterDto.put("ma_amt", "");
        encounterDto.put("hifp_payamt", "");
        encounterDto.put("setl_totlnum", "");
        encounterDto.put("insutype", "");
        encounterDto.put("reim_flag", "");
        encounterDto.put("out_setl_flag", "");
        encounterDto.put("fsi_operation_dtos", "");

        // Sẽ khám bệnhThông tinThêmĐếnThông tin bệnh nhân trong 
        JSONArray encounterDtos = new JSONArray();
        encounterDtos.add(encounterDto);
        patientDto.put("fsi_encounter_dtos", encounterDtos);
        patientDto.put("fsi_his_data_dto", "");

        // Sẽ Thông tin bệnh nhânThêmĐến data Trong đối tượng
        JSONObject data = new JSONObject();
        JSONArray patientDtos = new JSONArray();
        patientDtos.add(patientDto);
        data.put("patient_dtos", patientDtos);
        data.put("rule_ids", "");
        data.put("trig_scen", "");
        data.put("syscode", "");
        jsonObject.put("data", data);
         return mdRequestUtil.getMedicareData("3102", jsonObject.toJSONString());
    }

    /**
     * y dượcCơ sởChi phíQuyết toánđối vớitổngsổ sách
     *
     * @return
     */
    @Override
    public String matchPharmacyCostsWithGeneralLedger_3201() {
        return null;
    }


    /**
     * Bảng chi tiết đối soát quyết toán chi phí của cơ sở y tế
     *
     * @return
     */
    @Override
    public String matchPharmacyCostsWithDetailAccounts_3202() {
        return null;
    }
}
