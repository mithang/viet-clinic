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

    List<SupplierStorage> listByCode(@Param("companyId") String companyId, @Param("pinyinCode") String pinyinCode, @Param("status") String status, @Param("startTime") String startTime, @Param("endTime") String endTime);
}