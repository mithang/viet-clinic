package com.geeke.outpatient.dao;

import com.geeke.common.persistence.CrudDao;
import com.geeke.outpatient.entity.Patient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Thông tin bệnh nhânDAOGiao diện
 * @author txl
 * @version 2022-06-23
 */
@Mapper
public interface PatientDao extends CrudDao<Patient> {

    Patient getPatientByPhone(@Param("phoneNumber") String phoneNumber,@Param("companyId") String companyId);

    List<Patient> countPatientByPhone(@Param("phoneNumber") String phoneNumber,@Param("companyId") String companyId,@Param("id") String id);

    List<Patient> getPatientByCard(@Param("card") String card,@Param("companyId") String companyId,@Param("id") String id);

    Patient getPatientByregistrationId(String registrationId);
    List<Patient> getByOpenId(@Param("openId") String openId,@Param("companyId") String companyId);

    void wxUpdate(Patient patient);

    void wxInsert(Patient patient);

    /* Tra cứu thông tin bệnh nhân theo họ tên và CMND/CCCD của bệnh nhân */
    Patient inquire(Patient patient);

}