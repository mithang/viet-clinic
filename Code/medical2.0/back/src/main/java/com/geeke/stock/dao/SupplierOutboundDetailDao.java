package com.geeke.stock.dao;

import com.geeke.common.data.PageRequest;
import com.geeke.common.persistence.CrudDao;
import com.geeke.stock.entity.InventoryVerificationDetail;
import com.geeke.stock.entity.MedicinalStorageControl;
import com.geeke.stock.entity.SupplierOutbound;
import com.geeke.stock.entity.SupplierOutboundDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Phiếu xuất khoDAOGiao diện
 * @author txl
 * @version 2022-06-02/
 */
@Mapper
public interface SupplierOutboundDetailDao extends CrudDao<SupplierOutboundDetail> {
    void save(@Param("supplierOutboundDetails") List<SupplierOutboundDetail> supplierOutboundDetails);

    List<SupplierOutboundDetail> getByOutboundId(String outboundId);

    void deleteByOutboundId(String outboundId);

    /**
     * DS66: atomically claim a line-quantity transition: set the line's
     * number to newNumber only when it still holds oldNumber. Returns 1
     * when this caller won the transition, 0 when another edit moved the
     * line first (the service then re-reads and decides no-op vs stale).
     */
    int claimNumber(@Param("id") String id, @Param("newNumber") java.math.BigDecimal newNumber,
                    @Param("oldNumber") java.math.BigDecimal oldNumber);

    /** DS66: locking re-read of one line (SELECT ... FOR UPDATE). */
    SupplierOutboundDetail getForUpdate(String id);
}