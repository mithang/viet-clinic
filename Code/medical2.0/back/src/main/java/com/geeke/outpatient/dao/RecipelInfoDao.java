package com.geeke.outpatient.dao;

import com.geeke.common.data.PageRequest;
import com.geeke.outpatient.entity.RecipelInfoDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.outpatient.entity.RecipelInfo;

import java.util.List;

/**
 * Thông tin đơn thuốcDAOGiao diện
 * @author txl
 * @version 2022-06-20
 */
@Mapper
public interface RecipelInfoDao extends CrudDao<RecipelInfo> {
    //Theoca bệnhidLấytất cả của Đơn thuốcThông tin
    List<RecipelInfo> getByMedicalId(String registrationId);

    int updateById(@Param("cureType") int cureType,@Param("infuseType") int infuseType,@Param("id") String  id);

    List<RecipelInfoDTO> getHistoryRecipel(PageRequest pageRequest);

    void updateSchedule(@Param("id")String id, @Param("cureType")int cureType);
    /** X3: company của đơn thuốc theo id, phục vụ kiểm tra sở hữu ở controller */
    String getRecipelInfoCompanyId(@Param("id") String id);

    /** DS52: no del_flag filter here on purpose -- the old code's get() has
     * none either, so a soft-deleted recipel with is_dispension='0' keeps
     * exactly its old behaviour. The claim only adds atomicity. */
    int claimDispensed(@Param("id") String id);

    /** DS53: atomic charge claim, same pattern as claimDispensed. Sets
     * is_pay='1' only when the recipel is still unpaid (is_pay '0' or
     * NULL); returns rows affected (0 = already paid/refunded or missing).
     * No del_flag filter, same reason as claimDispensed. */
    int claimPaid(@Param("id") String id);

    /** DS54: atomic refund claim, same pattern as claimPaid. Sets
     * is_pay='2' only when the recipel is still PAID (is_pay '1');
     * returns rows affected (0 = already refunded / never paid /
     * missing). Exactly one concurrent refund wins the claim, so a
     * parallel double refund cannot write a second refund TollInfo,
     * a second REFUND audit row, or run materialRefund twice.
     * No del_flag filter, same reason as claimDispensed. */
    int claimRefunded(@Param("id") String id);
}