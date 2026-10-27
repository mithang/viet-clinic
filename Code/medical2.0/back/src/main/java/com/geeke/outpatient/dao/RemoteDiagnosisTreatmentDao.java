package com.geeke.outpatient.dao;

import com.geeke.common.data.PageRequest;
import com.geeke.common.persistence.CrudDao;
import com.geeke.outpatient.entity.Patient;
import com.geeke.outpatient.entity.RecipelInfo;
import com.geeke.outpatient.entity.Registration;
import com.geeke.outpatient.entity.RemoteDiagnosisTreatment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * từ xaChẩn đoánQuản lýDAOGiao diện
 * @author zh
 * @version 2023-12-06
 */
@Mapper
public interface RemoteDiagnosisTreatmentDao extends CrudDao<Patient> {
    /** idTruy vấn */
    RemoteDiagnosisTreatment diagnosisById(@Param("id")String id);
    /** Đăng ký khámTruy vấn */
    RemoteDiagnosisTreatment getRegistrationId(@Param("registrationId")String registrationId);
    /** Số lượngTruy vấn */
    int diagnosisCount(PageRequest pageRequest);
    /** Truy vấn phân trang */
    List<RemoteDiagnosisTreatment> diagnosisPage(PageRequest pageRequest);
    /** Thêm mớiDữ liệu */
    int diagnosisInsert(RemoteDiagnosisTreatment entity);
    /** SửaDữ liệu */
    int diagnosisUpdate(RemoteDiagnosisTreatment entity);
    /** Sửa trạng thái */
    int modifiedState(RemoteDiagnosisTreatment entity);
    /** khám chữa bệnh từ xaThu phí */
    int chargeState(@Param("id")String id);
    /** Lấy thông tin đơn thuốc */
    List<RecipelInfo> getCase(@Param("id")String id);
    /** SửaTrạng thái thu phí */
    int chargeEdit(@Param("id")String id);
    /** XóaBản ghi */
    int diagnosisDelete(@Param("id")String id);

}