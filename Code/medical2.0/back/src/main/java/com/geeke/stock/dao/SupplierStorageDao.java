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

    List<SupplierStorage> listByCode(@Param("companyId") String companyId, @Param("pinyinCode") String pinyinCode, @Param("status") String status, @Param("startTime") String startTime, @Param("endTime") String endTime);
}