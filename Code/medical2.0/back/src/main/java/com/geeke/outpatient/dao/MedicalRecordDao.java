package com.geeke.outpatient.dao;

import com.geeke.common.data.PageRequest;
import com.geeke.common.persistence.CrudDao;
import com.geeke.outpatient.entity.MedicalRecord;
import com.geeke.outpatient.entity.Recordpatvo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Điền bệnh ánDAOGiao diện
 * @author txl
 * @version 2022-06-13
 */
@Mapper
public interface MedicalRecordDao extends CrudDao<MedicalRecord> {

    List<MedicalRecord> listMedicalRecordByChargeStatus(@Param("chargeStatus")String chargeStatus,
                                                        @Param("companyId")String companyId);

    MedicalRecord getByRegistrationId(@Param("registrationId") String registrationId);

    List<Recordpatvo> recordpatlist(@Param("companyId")String companyId,@Param("id")String id );

    /**
     * Tiền sử dị ứng mới nhất đã lưu của một bệnh nhân trong cùng phòng khám.
     * Bệnh án không có cột patient_id nên nối qua registration (registration
     * là cầu nối bệnh nhân — bệnh án). Chỉ đọc, không tạo dữ liệu mới.
     */
    String getLatestAllergyHistory(@Param("companyId") String companyId, @Param("patientId") String patientId);

    List<MedicalRecord> getByOrder(@Param("registration")String registration);
}