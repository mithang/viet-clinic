package com.geeke.stock.dao;

import com.geeke.common.persistence.CrudDao;
import com.geeke.stock.entity.SupplierStorage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Phiếu nhập khoDAOGiao diện
 * @author txl
 * @version 2022-06-02
 */
@Mapper
public interface SupplierStorageDao extends CrudDao<SupplierStorage> {
    String getCode(String companyId);

    // DS34: atomically claim a slip for voiding: set examine to VOIDED only
    // when it is not already VOIDED (a NULL examine counts as not voided).
    // cancel() used to load the slip, set VOIDED and save it, with no
    // status guard at all: 8 parallel cancels of one slip of 5 all ran
    // invalidStorageStock and each decremented the aggregate by 5, leaving
    // it at 9965 against detail SUM 10000 (probe-proven). The claim is a
    // single conditional UPDATE, so exactly one caller wins; the losers get
    // 0 rows and throw. Returns 1 when claimed, 0 when already voided.
    int voidExamine(@Param("id") String id, @Param("voided") String voided,
                    @Param("examineDate") java.util.Date examineDate,
                    @Param("updateBy") String updateBy, @Param("updateDate") java.util.Date updateDate);

    // DS55: atomically claim a slip for approval: set examine to the passed
    // value only when it is still PENDING. auditStorage() used to load the
    // slip, check examine == PENDING and then add stock -- a SELECT-then-act
    // with no claim, so 8 parallel audits of one slip of 5 all read PENDING
    // and each added 5 to the aggregate (probe-proven: aggregate 10025,
    // 5 op=1 records). The claim is a single conditional UPDATE, so exactly
    // one caller wins; the losers get 0 rows and skip the slip (idempotent
    // approve -- the sequential 2nd audit already returned 200 with no stock
    // change, so skipping preserves the existing contract). A NULL examine
    // never matches PENDING, so a slip with no examine state is skipped
    // instead of NPEing at getExamine().getValue(). Returns 1 when claimed,
    // 0 when already approved/voided/nonexistent.
    int claimAuditExamine(@Param("id") String id, @Param("passed") String passed,
                          @Param("pending") String pending,
                          @Param("examineDate") java.util.Date examineDate,
                          @Param("updateBy") String updateBy, @Param("updateDate") java.util.Date updateDate);

    List<SupplierStorage> listByCode(@Param("companyId") String companyId, @Param("pinyinCode") String pinyinCode, @Param("status") String status, @Param("startTime") String startTime, @Param("endTime") String endTime);
}