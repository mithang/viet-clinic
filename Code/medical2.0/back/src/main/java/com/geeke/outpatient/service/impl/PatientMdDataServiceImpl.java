package com.geeke.outpatient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.geeke.common.service.ServiceException;
import com.geeke.outpatient.dao.PatientMdDataMapper;
import com.geeke.outpatient.entity.PatientMdData;
import com.geeke.outpatient.service.PatientMdDataService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientMdDataServiceImpl extends ServiceImpl<PatientMdDataMapper, PatientMdData> implements PatientMdDataService {

    /**
     * DS757: list() + fail-closed 400 khi trùng lặp, null khi thiếu.
     * Dùng chung cho mọi điểm gọi cũ getOne(eq patientId) ở
     * TollInfoService, MdRegistrationServiceImpl, MdCommonServiceImpl.
     */
    @Override
    public PatientMdData getUniqueByPatientId(String patientId) {
        List<PatientMdData> list = this.list(new LambdaQueryWrapper<PatientMdData>().eq(PatientMdData::getPatientId, patientId));
        if (list.size() > 1) {
            throw new ServiceException("Thông tin BHYT của bệnh nhân bị trùng lặp, liên hệ quản trị viên");
        }
        return list.isEmpty() ? null : list.get(0);
    }

}
