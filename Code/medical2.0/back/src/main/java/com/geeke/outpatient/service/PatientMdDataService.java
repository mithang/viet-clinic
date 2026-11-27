package com.geeke.outpatient.service;

import com.geeke.outpatient.entity.PatientMdData;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 47826
* @description Đối với bảng[patient_md_data(Bảng thông tin BHYT của bệnh nhân)] của Dữ liệukhothao tácService
* @createDate 2024-09-10 14:55:58
*/
public interface PatientMdDataService extends IService<PatientMdData> {

    /**
     * DS757: lấy duy nhất một dòng thông tin BHYT của bệnh nhân.
     * Bảng patient_md_data không có unique index trên patient_id nên
     * getOne() ném TooManyResultsException (500) khi tồn tại dòng trùng
     * (họ DS755/DS756). Trả null khi chưa có dòng nào (caller giữ
     * thông báo 400 như cũ), fail-closed 400 khi trùng lặp.
     */
    PatientMdData getUniqueByPatientId(String patientId);

}
