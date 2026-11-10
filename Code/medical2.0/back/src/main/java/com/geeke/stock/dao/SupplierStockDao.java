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

    /**
     * DS176 B2: đếm dòng nhập kho còn hiệu lực của một phiếu có initial
     * NULL hoặc company_id NULL (đếm thô trên bảng, không qua
     * listAll tenant-scoped). initial là int nguyên thủy ở entity nên
     * NULL ở DB đọc thành 0, còn dòng company NULL thì listAll lọc mất —
     * cả hai đều khiến addStorageStock duyệt phiếu "thành công" với số
     * tồn kho sai/không đổi nếu không chặn ở đây.
     */
    int countCorruptLines(@Param("storageId") String storageId);

    BigDecimal getByDrugId(String drugOrStuffId);

    void updateCancel(@Param("list") List<SupplierStock> supplierStockList);

    BigDecimal getByStuffId(String stuffId);

    List<SupplierStock> getByStuffIdDetail(String drugStuffId);

    int countWarning(PageRequest pageRequest);

    List<SupplierStock> getDrugIndateWarning(PageRequest pageRequest);

    int countStuffWarning(PageRequest pageRequest);

    List<SupplierStock> getStuffIndateWarning(PageRequest pageRequest);

}