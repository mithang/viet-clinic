package com.geeke.stock.service;

import com.geeke.common.persistence.DataEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Tồn khoTínhcông việccụ thểdịch vụ
 * cung cấpChung của Tồn khoTínhlogic, Xóa bỏDrugService và StuffServiceTrongtrùng lặpmã
 */
@Service
public class StockCalculationService {

    /**
     * TínhvàĐặtthực thể của Số lượng tồn kho
     * Tồn kho = Tổng lượng nhập kho - Đãsử dụngTồn kho - báohỏngTồn kho
     *
     * @param entity Thuốc hoặc Vật tưthực thể
     */
    public void calculateAndSetInventory(StockEntity entity) {
        if (entity == null || entity.getStock() == null || entity.getStock().getStorageStock() == null) {
            if (entity != null) {
                entity.setInventory(0);
            }
            return;
        }
        StockInfo stock = entity.getStock();
        BigDecimal usedStock = stock.getUsedStock() == null ? BigDecimal.ZERO : stock.getUsedStock();
        BigDecimal reimburseStock = stock.getReimburseStock() == null ? BigDecimal.ZERO : stock.getReimburseStock();
        BigDecimal inventory = stock.getStorageStock().subtract(usedStock.add(reimburseStock));
        entity.setInventory(inventory.setScale(0, RoundingMode.HALF_UP).intValue());
    }

    /**
     * Tồn khothực thểgiao diện
     * Drug và StuffđềuCầnTriển khainàygiao diện
     */
    public interface StockEntity {
        StockInfo getStock();
        void setInventory(int inventory);
    }

    /**
     * Tồn khoThông tingiao diện
     */
    public interface StockInfo {
        BigDecimal getStorageStock();
        BigDecimal getUsedStock();
        BigDecimal getReimburseStock();
    }
}
