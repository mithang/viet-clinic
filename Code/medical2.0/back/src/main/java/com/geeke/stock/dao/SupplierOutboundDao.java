package com.geeke.stock.dao;

import com.geeke.common.persistence.CrudDao;
import com.geeke.stock.entity.SupplierOutbound;
import com.geeke.stock.entity.SupplierStorage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Phiếu xuất khoDAOGiao diện
 * @author txl
 * @version 2022-06-02/
 */
@Mapper
public interface SupplierOutboundDao extends CrudDao<SupplierOutbound> {
    String getCode(String companyId);
    void examinee(SupplierOutbound entity);

    // DS34: conditional cancel claim: set status to cancelled (3) only when
    // the slip is neither already cancelled (3) nor already examined (2).
    // cancel() used to check the status in Java and then update it
    // unconditionally, so parallel cancels of the same slip all passed the
    // check and each released the freeze (probe-proven: 5/8 cancels
    // accepted, detail occupy -20 for one slip of 5). Returns 1 when the
    // claim won, 0 when another thread already cancelled/examined the slip.
    int cancel(SupplierOutbound entity);

    // DS34: conditional examine claim, for the same reason: examine() applied
    // the stock moves for every line FIRST and only set status=2 at the end,
    // so 8 parallel examines of one slip of 5 all applied the confirm move
    // (detail used 40, surplus 9960, occupy -35, probe-proven). Claimed up
    // front, before any stock move; the transaction rolls the claim back if
    // a later move throws. Returns 1 when the claim won, 0 otherwise.
    int claimExamine(@Param("id") String id, @Param("status") int status);
}