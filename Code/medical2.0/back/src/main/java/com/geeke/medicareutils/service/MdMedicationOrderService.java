package com.geeke.medicareutils.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.outpatient.entity.RecipelInfoReview;

/**
 * @Description Đơn thuốc điện tử
 * @Author Hzx
 * @Date 2024/10/30 11:45
 */
public interface MdMedicationOrderService {

    // [Ld7801]Tải lên đơn thuốc điện tửkiểm tra trước
    JSONObject validateElectronicPrescriptionUpload_Ld7801(RecipelInfoReview recipelInfoReview);

    // [Ld7802]điện tửĐơn thuốcBHYTđiện tửchữ ký
    JSONObject signElectronicPrescriptionWithInsurance_Ld7802(RecipelInfoReview recipelInfoReview, String ysId);

    // [Ld7101]Tải lên đơn thuốc điện tử
    JSONObject uploadElectronicPrescription_Ld7101(RecipelInfoReview recipelInfoReview,String ysId);


    JSONObject queryElectronicPrescriptionReviewResult_Ld7805(RecipelInfoReview recipelInfoReview);

    // [Ld7804]điện tửĐơn thuốclấy thuốcKết quảTruy vấn
    JSONObject queryPrescriptionDispensingResult_Ld7804(RecipelInfoReview recipelInfoReview);

    //Hủy đơn thuốc điện tử
    JSONObject cancelElectronicPrescription_Ld7104(RecipelInfoReview recipelInfoReview,String undoRea);

    // [Ld7202]điện tửĐơn thuốcThông tinTruy vấn
    JSONObject queryElectronicPrescriptionInfo_Ld7202();


    // [Ld7102]điện tửĐơn thuốcxét duyệtKết quảphản hồi
    JSONObject feedbackPrescriptionReviewResult_Ld7102();


    // [Ld7806]Thuốc trong đơn thuốc điện tửdanh mụcTruy vấn
    Boolean queryPrescriptionDrugDirectory_Ld7806();


}
