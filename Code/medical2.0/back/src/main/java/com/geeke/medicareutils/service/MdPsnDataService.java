package com.geeke.medicareutils.service;


import com.alibaba.fastjson.JSONObject;
import com.geeke.outpatient.entity.Patient;
import com.geeke.outpatient.entity.Registration;

import java.time.LocalDateTime;

/**
 * Thông tin BHYT của bệnh nhân
 */
public interface MdPsnDataService {
    /**
     * Lấynhân viêncơ bảnThông tin
     * @param
     * @return
     */
    Boolean  getAndSetPsnData( Registration registration);



}
