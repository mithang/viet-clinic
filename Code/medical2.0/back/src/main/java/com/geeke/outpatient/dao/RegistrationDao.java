package com.geeke.outpatient.dao;

import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.persistence.CrudDao;
import com.geeke.outpatient.entity.MedicalRecord;
import com.geeke.outpatient.entity.PageRegistration;
import com.geeke.outpatient.entity.Registration;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

/**
 * Thông tin đăng ký khámDAOGiao diện
 * @author txl
 * @version 2022-06-15
 */
@Mapper
public interface RegistrationDao extends CrudDao<Registration> {
    @Update("update registration set status = #{status},department_id=#{departmentId},doctor_id=#{doctorId} where id = #{id}")
    int updateStatus(@Param("id") String id,@Param("status") String status,@Param("departmentId")String departmentId,@Param("doctorId")String doctorId);

    /** N2: cấp số thứ tự cho lượt đã gán bác sĩ sau (chỉ khi queue_number còn NULL, do service kiểm tra trước). */
    @Update("update registration set queue_number = #{queueNumber} where id = #{id}")
    int updateQueueNumber(@Param("id") String id, @Param("queueNumber") Integer queueNumber);

    @Update("update registration set status = #{status},refund_registration_pay_type = #{refund_registration_pay_type}" +
            ",refund_registration_remarks=#{refund_registration_remarks},exit_number_date=#{exit_number_date} where id = #{id}")
    int refundRegistrationPay(@Param("id") String id, @Param("status") String status,
                              @Param("refund_registration_pay_type") String refundRegistrationPayType,
                              @Param("refund_registration_remarks") String refundRegistrationRemarks,
                              @Param("exit_number_date") Date exitNumberDate
                              );

    List<Registration> conditionList(@Param("value") String value,
                                     @Param("companyId")String companyId,
                                     @Param("limit")int limit,
                                     @Param("offset")int offset);

    int countConditionList(@Param("value") String value,@Param("companyId")String companyId);

    void updateRecipeStatus(String id);

    int updateStatusByCompanyId(@Param("companyId") String companyId,@Param("createDate") Date createDate,@Param("subscribeDate") Date subscribeDate);

    List<String> countId(PageRegistration pageRegistration);

    List<String> listPages(PageRegistration pageRegistration);

    List<Registration> findById(@Param("list") List<String> list,@Param("pageRegistration") PageRegistration pageRegistration);

    List<Registration> getByPatientId(String patientId);

    List<Registration> listByOpenId(@Param("openId") String openId,
                                    @Param("companyId") String companyId,
                                    @Param("offset") int offset,
                                    @Param("limit") int limit);

    int countByOpenId(@Param("openId") String openId,
                      @Param("companyId") String companyId);

    int wxReturnPay(Registration registrations);

    List<String> wxCount(PageRegistration pageRegistration);

    List<String> wxListPages(PageRegistration pageRegistration);

    List<String> wxDispensingCount(PageRegistration pageRegistration);

    List<String> wxDispensingListPages(PageRegistration pageRegistration);

    int registrationupdate(Registration registrations);

    /**
     * H4: tăng bộ đếm số thứ tự theo ngày của một bác sĩ trong một phòng khám.
     * Nguyên tử nhờ khóa chính (company_id, doctor_id, queue_date) + ON DUPLICATE
     * KEY UPDATE: 2 lượt đăng ký đồng thời không nhận cùng số vì upsert giữ
     * khóa dòng tới hết transaction. KHÔNG dùng LAST_INSERT_ID() để đọc số:
     * nhánh INSERT của upsert không có cột auto-increment nên không set
     * LAST_INSERT_ID, đọc nó sẽ ra giá trị cũ còn sót trên connection pool
     * (lỗi thật gặp ở lượt kiểm chứng đầu: số đầu tiên đọc ra 3314).
     * Đọc lại bằng SELECT last_number trong cùng transaction — dòng đang bị
     * khóa bởi chính upsert này nên không có transaction khác chen ngang.
     */
    @Insert("INSERT INTO queue_counter(company_id, doctor_id, queue_date, last_number) "
            + "VALUES(#{companyId}, #{doctorId}, #{queueDate}, 1) "
            + "ON DUPLICATE KEY UPDATE last_number = last_number + 1")
    int advanceQueueCounter(@Param("companyId") String companyId,
                            @Param("doctorId") String doctorId,
                            @Param("queueDate") String queueDate);

    @Select("SELECT last_number FROM queue_counter WHERE company_id = #{companyId} "
            + "AND doctor_id = #{doctorId} AND queue_date = #{queueDate}")
    Integer getQueueCounter(@Param("companyId") String companyId,
                            @Param("doctorId") String doctorId,
                            @Param("queueDate") String queueDate);
}