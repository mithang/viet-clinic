package com.geeke.medicareutils.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.outpatient.entity.Registration;

public interface MdCommonService {
    /**
     * 冲đúngDữ liệugiao dịch
     * Description
     * khả dụnggiao dịchmã: [2101]Bác sĩQuyết toán, [2102]nhà thuốcQuyết toán, [2103]nhà thuốcQuyết toánhủy, [2207]Quyết toán ngoại trú, 
     * [2208]Hủy quyết toán ngoại trú, [2304]Quyết toán nhập viện, [2305]Quyết toán nhập việnhủy, [2401]nhập việnlàm thủ tục, 
     * [2304A]Quyết toán nhập viện; 
     */
    String reversalData_2601(String psnNo, String omsgid, String oinfno);


    /**
     * điện tửbệnh ánTải lên 4701
     * @return
     */
    String upMedicalRecord_4701(Registration registration);











}
