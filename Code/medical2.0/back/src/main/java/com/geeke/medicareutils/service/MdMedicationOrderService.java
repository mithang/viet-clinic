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

    // DS192: queryElectronicPrescriptionInfo_Ld7202() + impl stub
    // (return null) DA XOA — dead code, 0 caller (RecipelInfoReviewController
    // chi goi Ld7801/7802/7101/7804/7805/7104; khong frontend/tools/schedule/
    // reflection nao goi). INFO_LIST "Ld7202" trong MdRequestUtil KHONG
    // phai method nay (ma giao dich BHYT, dung live) nen giu.
    // DS192: feedbackPrescriptionReviewResult_Ld7102() + impl stub
    // (return null) DA XOA — cung bang chung 0 caller; INFO_LIST "Ld7102" giu.

    // DS191: queryPrescriptionDrugDirectory_Ld7806() DA XOA — dead code,
    // 0 caller (khong controller/schedule/reflection nao goi; chi co
    // trong interface + impl). INFO_LIST "Ld7806" trong MdRequestUtil
    // KHONG phai method nay (danh sach ma API BHYT, dung live) nen giu.

}
