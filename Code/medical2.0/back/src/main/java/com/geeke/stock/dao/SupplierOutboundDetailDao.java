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

    /**
     * DS176 B: raw line count for one slip (plain table count, no
     * INNER JOIN resolve). getByOutboundId INNER JOINs
     * medicinal_storage_control, so a line whose medicinal_storage_id
     * is NULL/ghost vanishes from the resolved list; examine compares
     * this count against the resolved size to catch phantom approvals.
     */
    int countByOutboundId(String outboundId);

    /**
     * DS176 B: raw line ids for one slip (no INNER JOIN resolve).
     * save() uses it to tell a request line that exists but does not
     * resolve (corrupt medicinal_storage_id) apart from a ghost id.
     */
    List<String> listIdsByOutboundId(String outboundId);

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

    /**
     * DS67: atomically claim a line for deletion: delete the row only
     * when it still holds the given number. Returns 1 when this caller
     * won the deletion (and may release the freeze), 0 when another
     * edit/delete moved or removed the line first (the service then
     * re-reads and decides no-op vs stale refusal).
     */
    int deleteIfNumber(@Param("id") String id, @Param("number") java.math.BigDecimal number);
}