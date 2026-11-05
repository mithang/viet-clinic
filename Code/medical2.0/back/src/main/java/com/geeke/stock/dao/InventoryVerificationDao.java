package com.geeke.stock.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.stock.entity.InventoryVerification;

import java.math.BigDecimal;
import java.util.List;

/**
 * Kiểm kê tồn khoDAOGiao diện
 * @author rys
 * @version 2022-11-02
 */
@Mapper
public interface InventoryVerificationDao extends CrudDao<InventoryVerification> {
    /** DS56: claim nguyen tu chot kiem ke -- chi phieu dang status='0' moi
     * chuyen sang '1'; dung 1 caller thang, cac caller thua claim thi no-op
     * (mau DS55 claimAuditExamine). Thay the lenh UPDATE vo dieu kien cu. */
    int claimAccomplish(@Param("id") String id);

    void updateAllPrice(@Param("id") String id,@Param("allPrice") BigDecimal allPrice);

    /** DS65: khoa dong phieu cha (SELECT ... FOR UPDATE, khong JOIN de
     * chi khoa dung dong phieu) truoc bulkUpdate trong saveAll: tuan tu
     * hoa cac saveAll cung phieu, tranh deadlock giua bulkUpdate khoa
     * cac dong detail roi roi va SUM ... FOR UPDATE khoa toan bo detail
     * cua phieu. */
    InventoryVerification lockById(@Param("id") String id);

    List<InventoryVerification> getByCompanyId(@Param("companyId") String companyId,@Param("status") String status);
}