package com.geeke.medicareutils.service;

import com.alibaba.fastjson.JSONArray;
import com.geeke.medicareutils.domain.respo.MdFeeDetail;
import com.geeke.outpatient.entity.Registration;

import java.time.LocalDateTime;

/**
 * Đăng ký khám BHYT, Khám bệnh, Chẩn đoán, Quyết toán
 */
public interface MdRegistrationService {
    /**
     * ngoại trúĐăng ký khám BHYT
     * @return
     */
    String  getRegistrationInfo_2201(Registration registration);

    /**
     * ngoại trúĐăng ký khámlấyhủy
     * @param registration
     * @return
     */
    String revokeRegistrationInfo_2202(Registration registration);


    /**
     * Tải lên thông tin khám ngoại trú
     * @param registration
     * @return
     */
    String upRegistrationInfo_2203(Registration registration);


    /**
     * Tải lên thông tin khám ngoại trú 2203A
     *
     * @param registration
     * @return
     */
    String upRegistrationInfoList_2203A(Registration registration);


    /**
     * ngoại trúChi phíChi tiếtThông tinTải lên 2204
     *
     * @param
     * @return
     */
    String upRegistrationMoneyInfo_2204(JSONArray jsonArray);

    /**
     * ngoại trúChi phíChi tiếthủyChi tiết 2205
     *
     * @param
     * @return
     */
    String revokeRegistrationMoneyInfo_2205(String mdtrtId, String chrgBchno, String psnNo, String expContent);



    /**
     * ngoại trútrướcQuyết toán 2206
     *
     * @param registration Thông tin đăng ký khám
     * @param mdFeeDetail  Thu phíThông tin
     * @return
     */
    String processOutpatientPreSettlement_2206(Registration registration, MdFeeDetail mdFeeDetail, String chrgBchno, String acctUsedFlag);


    /**
     * Quyết toán ngoại trú 2207
     *
     * @param registration Thông tin đăng ký khám
     * @param mdFeeDetail  Chi phíChi tiếtXuất
     * @param chrgBchno    Thu phíSố lô
     * @param acctUsedFlag Có hay khôngtựphí
     * @return
     */
    String executeOutpatientPreSettlement_2207(Registration registration, MdFeeDetail mdFeeDetail, String chrgBchno, String acctUsedFlag);

    /**
     * Hủy quyết toán ngoại trú  2208
     *
     * @param setlId
     * @param psnNo
     * @param mdtrtId
     * @return
     */
    String revokeOutpatientSettlement_2208(String setlId, String  psnNo, String mdtrtId);


    /**
     * khám bệnhThông tinTruy vấn
     *
     * @param registration
     * @param begntime
     * @param endtime
     * @return
     */
    String getPsnVisitData_5201(Registration registration, LocalDateTime begntime, LocalDateTime endtime);

    /**
     * Thông tin chẩn đoánTruy vấn
     *
     * @param registration
     * @return
     */
    String getPsnDiseData_5202(Registration registration);

    /**
     * Quyết toánThông tinTruy vấn
     *
     * @return
     */
    String getSettlementInfo_5203(String psnNo, String setlId, String mdtrtId);

    /**
     * Chi phíChi tiếtTruy vấn
     *
     * @param psnNo
     * @param setlId
     * @param mdtrtId
     * @return
     */
    String getExpenseDetails_5204(String psnNo, String setlId, String mdtrtId);

    /**
     * Khám chữa bệnh ngoại trú và cấp cứuBản ghi
     *
     * @param registration Thông tin đăng ký khám
     * @return
     */
    String getEmergencyOutpatientRecords_4301(Registration registration);

    /**
     * Chi tiếtxét duyệtviệctrước phútdịch vụ phân tích
     *
     * @param registration
     * @return
     */
    String analyzeDetailReviewPreCheck_3301(Registration registration);

    /**
     * Chi tiếtxét duyệtviệc trong  phútdịch vụ phân tích
     *
     * @param registration 3102
     * @return
     */
    String analyzeDetailReviewDuringProcess_3102(Registration registration);


    /**
     * y dượcCơ sởChi phíQuyết toánđối vớitổngsổ sách
     *
     * @return
     */
    String matchPharmacyCostsWithGeneralLedger_3201();

    /**
     * Bảng chi tiết đối soát quyết toán chi phí của cơ sở y tế
     *
     * @return
     */
    String matchPharmacyCostsWithDetailAccounts_3202();
}
