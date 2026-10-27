package com.geeke.outpatient.dao;

import com.geeke.outpatient.entity.PatientMdData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.mapstruct.Mapper;

/**
* @author 47826
* @description Đối với bảng[patient_md_data(Bảng thông tin BHYT của bệnh nhân)] của Dữ liệukhothao tácMapper
* @createDate 2024-09-09 16:14:57
* @Entity com.geeke.outpatient.entity.PatientMdData
*/
@Mapper
public interface PatientMdDataMapper extends BaseMapper<PatientMdData> {

}




