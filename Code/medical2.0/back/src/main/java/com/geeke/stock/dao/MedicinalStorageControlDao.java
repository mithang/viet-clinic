package com.geeke.stock.dao;

import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.stock.entity.MedicinalStorageControl;
import com.geeke.stock.entity.SupplierStock;

/**
 * Thuốc/Kiểm soát chi tiết nhập kho vật tưDAOGiao diện
 * @author hl
 * @version 2022-09-26
 */
@Mapper
public interface MedicinalStorageControlDao extends CrudDao<MedicinalStorageControl> {
    List<MedicinalStorageControl> getSurplusStockByDrugStuffId(@Param("drugStuffId")String drugStuffId);

    List<MedicinalStorageControl> getSurplusStockByDrugStuffIdTo(@Param("drugStuffId") String drugStuffId,
                                                                 @Param("companyId") String companyId);

    List<MedicinalStorageControl> getAll(@Param("companyId") String compayId,@Param("type") String type,@Param("variety") String variety);

    BigDecimal getByDrugOrStuffId(String drugOrStuffId,String companyId);
    BigDecimal getByDrugOrStuffIds(@Param("list") List<String> drugOrStuffIds);

    // DS32: atomically add deltas to ALL six detail stock columns. Same
    // lost-update shape as the aggregate bump (probe-proven: 8 parallel
    // outbound examines left the shared detail row at used 5 instead of
    // 40). IFNULL guards the columns because return_todo_fee_stock is
    // NULL on live rows. Returns the rows updated (1).
    int adjustStocks(@Param("id") String id,
                     @Param("storageDelta") java.math.BigDecimal storageDelta,
                     @Param("surplusDelta") java.math.BigDecimal surplusDelta,
                     @Param("occupyDelta") java.math.BigDecimal occupyDelta,
                     @Param("usedDelta") java.math.BigDecimal usedDelta,
                     @Param("reimburseDelta") java.math.BigDecimal reimburseDelta,
                     @Param("returnTodoFeeDelta") java.math.BigDecimal returnTodoFeeDelta,
                     @Param("updateBy") String updateBy, @Param("updateDate") java.util.Date updateDate);

    // DS32: atomically occupy a detail row, but only when its surplus still
    // covers the amount (preOccupyStock check-then-act). Without the guard
    // two concurrent prescriptions can both read surplus 10000 and both
    // occupy 6000, leaving surplus -2000. Returns 1 when applied, 0 when
    // the row no longer has enough surplus (caller throws stock-not-enough
    // and the transaction rolls back). surplusDelta is negative.
    int occupyStocks(@Param("id") String id,
                     @Param("occupyDelta") java.math.BigDecimal occupyDelta,
                     @Param("surplusDelta") java.math.BigDecimal surplusDelta,
                     @Param("requiredSurplus") java.math.BigDecimal requiredSurplus,
                     @Param("updateBy") String updateBy, @Param("updateDate") java.util.Date updateDate);

    // DS33: atomically bump ONLY the occupy column of a detail row, and
    // only when the still-unfrozen surplus covers the amount: the guard
    // is surplus minus occupy >= delta. An outbound freeze does NOT move
    // surplus (that happens at examine, DS32 invariant), so a plain
    // surplus >= delta guard would still let two parallel creates of 6000
    // against surplus 10000 both pass and freeze 12000 total
    // (probe-proven). The guard is conservative where the shared occupy
    // column also holds prescription occupies (those already reduced
    // surplus, so surplus minus occupy understates what is free); it
    // never over-freezes, only over-refuses in that mixed case. A
    // non-positive delta always passes the guard (callers use the plain
    // atomic bump for decreases). Returns 1 when applied, 0 when refused.
    int freezeStocks(@Param("id") String id,
                     @Param("occupyDelta") java.math.BigDecimal occupyDelta,
                     @Param("updateBy") String updateBy, @Param("updateDate") java.util.Date updateDate);

    // DS34: conditionally zero a detail row for voiding an import slip,
    // only when it still holds exactly the storage/surplus the caller read.
    // invalidStorageStock used to set both columns to 0 with absolute
    // save(): parallel cancels of the same slip all read storage==surplus
    // and all zeroed the row, while EACH decremented the aggregate by the
    // read values (probe-proven: aggregate 9965 vs detail 10000 for one
    // slip of 5). Returns 1 when zeroed, 0 when another thread already
    // voided the row (or its stock moved since the read); the caller then
    // re-reads and either skips the line or throws fail-closed.
    int zeroVoidStocks(@Param("id") String id,
                       @Param("storageStock") java.math.BigDecimal storageStock,
                       @Param("surplusStock") java.math.BigDecimal surplusStock,
                       @Param("updateBy") String updateBy, @Param("updateDate") java.util.Date updateDate);

}