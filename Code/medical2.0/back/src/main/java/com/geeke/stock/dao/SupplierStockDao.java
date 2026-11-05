package com.geeke.stock.dao;

import com.geeke.common.data.PageRequest;
import com.geeke.common.persistence.CrudDao;
import com.geeke.stock.entity.SupplierStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tồn kho nhà cung cấpDAOGiao diện
 * @author txl
 * @version 2022-06-09
 */
@Mapper
public interface SupplierStockDao extends CrudDao<SupplierStock> {
    List<SupplierStock> getByStorageId(@Param("storageId")String id);

    /** DS27: số dòng nhập kho HIS đã có cho (company, crkmxId). */
    int countByHisCrkmxId(@Param("companyId") String companyId, @Param("hisCrkmxId") String hisCrkmxId);

    BigDecimal getByDrugId(String drugOrStuffId);

    void updateCancel(@Param("list") List<SupplierStock> supplierStockList);

    BigDecimal getByStuffId(String stuffId);

    List<SupplierStock> getByStuffIdDetail(String drugStuffId);

    int countWarning(PageRequest pageRequest);

    List<SupplierStock> getDrugIndateWarning(PageRequest pageRequest);

    int countStuffWarning(PageRequest pageRequest);

    List<SupplierStock> getStuffIndateWarning(PageRequest pageRequest);

}