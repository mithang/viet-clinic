package com.geeke.stock.dao;

import com.geeke.common.data.PageRequest;
import com.geeke.stock.entity.Drug;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.stock.entity.MedicinalStockControl;

import java.util.List;

/**
 * Thuốc/Kiểm soát tổng tồn kho vật tưDAOGiao diện
 * @author hl
 * @version 2022-09-26
 */
@Mapper
public interface MedicinalStockControlDao extends CrudDao<MedicinalStockControl> {
    List<MedicinalStockControl> getByDrugOrStuffId(@Param("drugOrStuffId")String drugOrStuffId,@Param("companyId")String companyId);

    List<MedicinalStockControl> inventory(@Param("companyID") String companyID, @Param("drugID") String drugID);

    // DS31: atomic create-or-resurrect of the (drug, company) aggregate row
    // on uk_msc_drug_company. Returns 1 when a new row was inserted, 2 when
    // an existing row was updated, 0 when the row already had del_flag='0'.
    int insertOrResurrect(MedicinalStockControl entity);

    // DS31: revive a soft-deleted aggregate row (del_flag -> '0') with the
    // stock totals supplied by the caller (recomputed from live detail
    // rows). The generic update deliberately never touches del_flag, so a
    // dedicated statement is required. No-op when the row is already live.
    int resurrect(MedicinalStockControl entity);

    // DS31: live detail totals for one (drug, company) pair, used to
    // recompute a resurrected aggregate row so it again equals the detail
    // sum. Reads medicinal_storage_control, not the aggregate table.
    MedicinalStockControl sumDetailStocks(@Param("drugStuffId") String drugStuffId, @Param("companyId") String companyId);

    // DS31: atomically add deltas to the aggregate storage/surplus totals.
    // A read-modify-write in Java loses concurrent imports (8 parallel
    // imports each read the same total and overwrote each other, leaving
    // 1.0 instead of 8.0). A single UPDATE ... SET col = col + delta
    // serializes on the row lock instead. Returns the rows updated (1).
    int addStorageStocks(@Param("id") String id, @Param("storageDelta") java.math.BigDecimal storageDelta,
                         @Param("surplusDelta") java.math.BigDecimal surplusDelta,
                         @Param("updateBy") String updateBy, @Param("updateDate") java.util.Date updateDate);

    // DS32: atomically add deltas to ALL six aggregate stock columns.
    // Same lost-update shape as addStorageStocks, but the void / occupy /
    // dispense / refund paths move occupy, used, reimburse and
    // return-todo-fee as well (probe-proven: 8 parallel voids left the
    // aggregate at 10035 instead of 10000, 8 parallel outbound examines
    // left aggregate used at 5 instead of 40). IFNULL guards the columns
    // because return_todo_fee_stock is NULL on live rows. Returns 1.
    int adjustStocks(@Param("id") String id,
                     @Param("storageDelta") java.math.BigDecimal storageDelta,
                     @Param("surplusDelta") java.math.BigDecimal surplusDelta,
                     @Param("occupyDelta") java.math.BigDecimal occupyDelta,
                     @Param("usedDelta") java.math.BigDecimal usedDelta,
                     @Param("reimburseDelta") java.math.BigDecimal reimburseDelta,
                     @Param("returnTodoFeeDelta") java.math.BigDecimal returnTodoFeeDelta,
                     @Param("updateBy") String updateBy, @Param("updateDate") java.util.Date updateDate);

    List<MedicinalStockControl> listAlls(PageRequest pageRequest);

    List<MedicinalStockControl> listPreAll(PageRequest pageRequest);



}