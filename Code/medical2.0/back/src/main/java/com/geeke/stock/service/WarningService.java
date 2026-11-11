package com.geeke.stock.service;

import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.persistence.DataEntity;
import com.geeke.stock.entity.Drug;
import com.geeke.stock.entity.MedicinalStockControl;
import com.geeke.stock.entity.MedicinalStorageControl;
import com.geeke.stock.entity.Stuff;
import com.geeke.stock.entity.SupplierStock;
import com.geeke.utils.excel.ExcelExportBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service("warningService")
@Transactional(readOnly = true)
public class WarningService {

    @Autowired
    private SupplierStockService supplierStockService;

    @Autowired
    private DrugService drugService;

    @Autowired
    private StuffService stuffService;

    // DS96: các export cảnh báo trước đây dùng Integer.parseInt trực
    // tiếp trên preparation/packNumber/số tồn kho dạng chuỗi. Dữ liệu
    // thật có preparation "1.0" (NFE), packNumber 0 (/0), tồn kho
    // thập phân "10.5" (NFE) -> cả file 500. Mapper IFNULL bọc tên
    // type/factory/unit thành "" nên các deref tên thường an toàn,
    // nhưng stock (storage/used/reimburse) và endDate vẫn có thể
    // null. Các helper dưới đây giữ nguyên cách hiển thị cũ, chỉ làm
    // cho phép tính null-safe và thập phân-safe (theo mẫu
    // parseExportCount của CurrentInventoryService, DS92).
    private static BigDecimal parseDecimal(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static BigDecimal remainingStock(BigDecimal storage, BigDecimal used, BigDecimal reimburse) {
        BigDecimal s = storage == null ? BigDecimal.ZERO : storage;
        BigDecimal u = used == null ? BigDecimal.ZERO : used;
        BigDecimal r = reimburse == null ? BigDecimal.ZERO : reimburse;
        return s.subtract(u.add(r));
    }

    private static BigDecimal remainingStock(MedicinalStockControl stock) {
        if (stock == null) {
            return BigDecimal.ZERO;
        }
        return remainingStock(stock.getStorageStock(), stock.getUsedStock(), stock.getReimburseStock());
    }

    private static BigDecimal remainingStock(MedicinalStorageControl stock) {
        if (stock == null) {
            return BigDecimal.ZERO;
        }
        return remainingStock(stock.getStorageStock(), stock.getUsedStock(), stock.getReimburseStock());
    }

    private static String nameOrEmpty(DataEntity<?> entity) {
        return entity == null || entity.getName() == null ? "" : entity.getName();
    }

    private static String safeString(String value) {
        return value == null ? "" : value;
    }


    @Transactional(readOnly = true)
    public Page<SupplierStock> getDrugIndateWarning(List<Parameter> parameters, int offset, int limit, String orderby) {
        PageRequest pageRequest = new PageRequest(offset, limit, parameters, orderby);
        Page<SupplierStock> indateWarning = supplierStockService.getDrugIndateWarning(parameters, offset, limit, orderby);
        return indateWarning;


    }

    @Transactional(readOnly = true)
    public Page<Drug> getDrugInventoryWarning(SearchParams searchParams) {
        Page<Drug> drugInventoryWarning = drugService.getDrugInventoryWarning(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());

        return drugInventoryWarning;
    }

    @Transactional(readOnly = true)
    public Page<SupplierStock> getStuffIndateWarning(List<Parameter> parameters, int offset, int limit, String orderby) {
        Page<SupplierStock> stuffIndateWarning = supplierStockService.getStuffIndateWarning(parameters, offset, limit, orderby);
        return stuffIndateWarning;
    }


    public Page<Stuff> getStuffInventoryWarning(List<Parameter> params, int offset, int limit, String orderby) {
        Page<Stuff> stuffInventoryWarning = stuffService.getStuffInventoryWarning(params, offset, limit, orderby);
        return stuffInventoryWarning;
    }

    //trướccảnh báoXuất
    @Transactional
    public void exportTable(SearchParams searchParams, HttpServletResponse response) throws IOException {
        if(Objects.equals(searchParams.getColumnName(),"0")){
            //ThuốcThời hạn hiệu lựcXuất
            exportDrugIndate(searchParams,response);
        }else if(Objects.equals("1",searchParams.getColumnName())){
            exportDrugInventory(searchParams,response);
        }else if(Objects.equals("2",searchParams.getColumnName())){
            exportStuffIndate(searchParams,response);
        }else {
            exportStuffInventory(searchParams,response);
        }
    }

    //Xuất cảnh báo tồn kho vật tư
    private void exportStuffInventory(SearchParams searchParams, HttpServletResponse response) throws IOException {
        // DS203: getStuffInventoryWarning tran limit 10000
        // (PageRequest.MAX_PAGE_SIZE) nen khong the lay het 1000000 dong
        // mot lan nhu truoc — lay theo trang 10000 dong roi gop lai.
        List<Stuff> warningRows = new ArrayList<>();
        int exportOffset = 0;
        while (true) {
            Page<Stuff> stuffInventoryWarning = stuffService.getStuffInventoryWarning(searchParams.getParams(), exportOffset, PageRequest.MAX_PAGE_SIZE, searchParams.getOrder());
            if (CollectionUtils.isEmpty(stuffInventoryWarning.getRows())) {
                break;
            }
            warningRows.addAll(stuffInventoryWarning.getRows());
            if (stuffInventoryWarning.getRows().size() < PageRequest.MAX_PAGE_SIZE) {
                break;
            }
            exportOffset += PageRequest.MAX_PAGE_SIZE;
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        if (!CollectionUtils.isEmpty(warningRows)) {
            for (Stuff stuff : warningRows) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("name", stuff.getName());
                row.put("typeName", nameOrEmpty(stuff.getType()));
                // Quy cách
                String specification = (stuff.getPackNumber() == null ? "" : stuff.getPackNumber()) + nameOrEmpty(stuff.getMinUnit()) + "/" + nameOrEmpty(stuff.getPackUnit());
                row.put("specification", specification);
                row.put("factoryName", nameOrEmpty(stuff.getFactory()));
                // Số lượng còn lại (DS96: null-safe + thập phân-safe, intValue cắt phần lẻ như parseExportCount DS92)
                stuff.setInventory(remainingStock(stuff.getStock()).intValue());
                String number = formatStuffNumber(stuff);
                row.put("number", number);
                rows.add(row);
            }
        }
        new ExcelExportBuilder("Xuất cảnh báo tồn kho vật tư")
                .addColumns(
                        new ExcelExportBuilder.Column("Tên vật tư", "name"),
                        new ExcelExportBuilder.Column("Phân loại vật tư", "typeName"),
                        new ExcelExportBuilder.Column("Quy cách", "specification"),
                        new ExcelExportBuilder.Column("Nhà sản xuất", "factoryName"),
                        new ExcelExportBuilder.Column("Số lượng còn lại", "number")
                )
                .data(rows)
                .write(response, "Xuất cảnh báo tồn kho vật tư.xlsx");
    }

    //Xuất cảnh báo hết hạn vật tư
    private void exportStuffIndate(SearchParams searchParams, HttpServletResponse response) throws IOException {
        // DS203: nhu exportStuffInventory — lay theo trang 10000 dong
        // (PageRequest.MAX_PAGE_SIZE) roi gop lai, thay cho 1000000 dong.
        List<SupplierStock> warningRows = new ArrayList<>();
        int exportOffset = 0;
        while (true) {
            Page<SupplierStock> stuffIndateWarning = supplierStockService.getStuffIndateWarning(searchParams.getParams(), exportOffset, PageRequest.MAX_PAGE_SIZE, searchParams.getOrder());
            if (CollectionUtils.isEmpty(stuffIndateWarning.getRows())) {
                break;
            }
            warningRows.addAll(stuffIndateWarning.getRows());
            if (stuffIndateWarning.getRows().size() < PageRequest.MAX_PAGE_SIZE) {
                break;
            }
            exportOffset += PageRequest.MAX_PAGE_SIZE;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<Map<String, Object>> rows = new ArrayList<>();
        if (!CollectionUtils.isEmpty(warningRows)) {
            for (SupplierStock supplierStock : warningRows) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("stuffName", supplierStock.getStuff().getName());
                row.put("typeName", nameOrEmpty(supplierStock.getStuff().getType()));
                row.put("norms", supplierStock.getNorms());
                row.put("factoryName", nameOrEmpty(supplierStock.getFactory()));
                row.put("supplierName", nameOrEmpty(supplierStock.getSupplierId()));
                row.put("batchNo", supplierStock.getBatchNo());
                // Số lượng còn lại (DS96: null-safe + thập phân-safe)
                supplierStock.getStuff().setInventory(remainingStock(supplierStock.getMedicinalStorageControl()).intValue());
                row.put("number", formatStuffNumber(supplierStock.getStuff()));
                // Thời hạn hiệu lực
                row.put("endDate", supplierStock.getEndDate() == null ? "" : sdf.format(supplierStock.getEndDate()));
                // Trạng thái
                row.put("status", Objects.equals("0", supplierStock.getCancellation()) ? "tức làSẽ hết hạn" : "Đãhết hạn");
                rows.add(row);
            }
        }
        new ExcelExportBuilder("Xuất cảnh báo hết hạn vật tư")
                .addColumns(
                        new ExcelExportBuilder.Column("Tên vật tư", "stuffName"),
                        new ExcelExportBuilder.Column("Phân loại vật tư", "typeName"),
                        new ExcelExportBuilder.Column("Quy cách", "norms"),
                        new ExcelExportBuilder.Column("Nhà sản xuất", "factoryName"),
                        new ExcelExportBuilder.Column("Nhà cung cấp", "supplierName"),
                        new ExcelExportBuilder.Column("Số lô", "batchNo"),
                        new ExcelExportBuilder.Column("Số lượng còn lại", "number"),
                        new ExcelExportBuilder.Column("Thời hạn hiệu lực", "endDate"),
                        new ExcelExportBuilder.Column("Trạng thái", "status")
                )
                .data(rows)
                .write(response, "Xuất cảnh báo hết hạn vật tư.xlsx");
    }

    //Xuất cảnh báo tồn kho thuốc
    private void exportDrugInventory(SearchParams searchParams, HttpServletResponse response) throws IOException {
        // DS203: nhu exportStuffInventory — lay theo trang 10000 dong
        // (PageRequest.MAX_PAGE_SIZE) roi gop lai, thay cho 1000000 dong.
        List<Drug> warningRows = new ArrayList<>();
        int exportOffset = 0;
        while (true) {
            Page<Drug> drugInventoryWarning = drugService.getDrugInventoryWarning(searchParams.getParams(), exportOffset, PageRequest.MAX_PAGE_SIZE, searchParams.getOrder());
            if (CollectionUtils.isEmpty(drugInventoryWarning.getRows())) {
                break;
            }
            warningRows.addAll(drugInventoryWarning.getRows());
            if (drugInventoryWarning.getRows().size() < PageRequest.MAX_PAGE_SIZE) {
                break;
            }
            exportOffset += PageRequest.MAX_PAGE_SIZE;
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        if (!CollectionUtils.isEmpty(warningRows)) {
            for (Drug drug : warningRows) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("goodsName", drug.getGoodsName());
                row.put("typeName", nameOrEmpty(drug.getType()));
                // Quy cách
                String specification = safeString(drug.getDosis()) + nameOrEmpty(drug.getDosisUnit()) + "*" + safeString(drug.getPreparation()) + nameOrEmpty(drug.getPreparationUnit()) + "/" + nameOrEmpty(drug.getPack());
                row.put("specification", specification);
                row.put("factoryName", nameOrEmpty(drug.getFactory()));
                // Số lượng còn lại (DS96: null-safe + thập phân-safe, intValue cắt phần lẻ như parseExportCount DS92)
                drug.setInventory(remainingStock(drug.getStock()).intValue());
                row.put("number", formatDrugNumber(drug));
                rows.add(row);
            }
        }
        new ExcelExportBuilder("Xuất cảnh báo tồn kho thuốc")
                .addColumns(
                        new ExcelExportBuilder.Column("Tên thuốc", "goodsName"),
                        new ExcelExportBuilder.Column("Phân loại thuốc", "typeName"),
                        new ExcelExportBuilder.Column("Quy cách", "specification"),
                        new ExcelExportBuilder.Column("Nhà sản xuất", "factoryName"),
                        new ExcelExportBuilder.Column("Số lượng còn lại", "number")
                )
                .data(rows)
                .write(response, "Xuất cảnh báo tồn kho thuốc.xlsx");
    }

    //Xuất cảnh báo hết hạn thuốc
    private void exportDrugIndate(SearchParams searchParams, HttpServletResponse response) throws IOException {
        // DS203: nhu exportStuffInventory — lay theo trang 10000 dong
        // (PageRequest.MAX_PAGE_SIZE) roi gop lai, thay cho 1000000 dong.
        List<SupplierStock> warningRows = new ArrayList<>();
        int exportOffset = 0;
        while (true) {
            Page<SupplierStock> drugIndateWarning = supplierStockService.getDrugIndateWarning(searchParams.getParams(), exportOffset, PageRequest.MAX_PAGE_SIZE, searchParams.getOrder());
            if (CollectionUtils.isEmpty(drugIndateWarning.getRows())) {
                break;
            }
            warningRows.addAll(drugIndateWarning.getRows());
            if (drugIndateWarning.getRows().size() < PageRequest.MAX_PAGE_SIZE) {
                break;
            }
            exportOffset += PageRequest.MAX_PAGE_SIZE;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<Map<String, Object>> rows = new ArrayList<>();
        if (!CollectionUtils.isEmpty(warningRows)) {
            for (SupplierStock supplierStock : warningRows) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("drugName", supplierStock.getDrug().getGoodsName());
                row.put("typeName", nameOrEmpty(supplierStock.getDrug().getType()));
                row.put("norms", supplierStock.getNorms());
                row.put("factoryName", nameOrEmpty(supplierStock.getFactory()));
                row.put("supplierName", nameOrEmpty(supplierStock.getSupplierId()));
                row.put("batchNo", supplierStock.getBatchNo());
                // Số lượng còn lại (DS96: null-safe + thập phân-safe)
                supplierStock.getDrug().setInventory(remainingStock(supplierStock.getMedicinalStorageControl()).intValue());
                row.put("number", formatDrugNumber(supplierStock.getDrug()));
                // Thời hạn hiệu lực
                row.put("endDate", supplierStock.getEndDate() == null ? "" : sdf.format(supplierStock.getEndDate()));
                // Trạng thái
                row.put("status", Objects.equals("0", supplierStock.getCancellation()) ? "tức làSẽ hết hạn" : "Đãhết hạn");
                rows.add(row);
            }
        }
        new ExcelExportBuilder("Xuất cảnh báo hết hạn thuốc")
                .addColumns(
                        new ExcelExportBuilder.Column("Tên thuốc", "drugName"),
                        new ExcelExportBuilder.Column("Phân loại thuốc", "typeName"),
                        new ExcelExportBuilder.Column("Quy cách", "norms"),
                        new ExcelExportBuilder.Column("Nhà sản xuất", "factoryName"),
                        new ExcelExportBuilder.Column("Nhà cung cấp", "supplierName"),
                        new ExcelExportBuilder.Column("Số lô", "batchNo"),
                        new ExcelExportBuilder.Column("Số lượng còn lại", "number"),
                        new ExcelExportBuilder.Column("Thời hạn hiệu lực", "endDate"),
                        new ExcelExportBuilder.Column("Trạng thái", "status")
                )
                .data(rows)
                .write(response, "Xuất cảnh báo hết hạn thuốc.xlsx");
    }

    /**
     * định dạnghóaThuốcSố lượng còn lại, theoĐơn vị đóng góiquy đổihiển thị(như "2 hộp 3 viên")
     * DS96: preparation "1.0" (NFE), "0"/"0.5" (/0), rỗng/không phải số (NFE) đều
     * làm cả file 500. Quy đổi qua BigDecimal; không quy đổi được thì giữ nguyên
     * tổng + đơn vị bào chế (mẫu TollInfo).
     */
    private String formatDrugNumber(Drug drug) {
        int inventory = drug.getInventory();
        BigDecimal preparation = parseDecimal(drug.getPreparation());
        if (preparation == null || preparation.compareTo(BigDecimal.ZERO) <= 0) {
            return inventory + nameOrEmpty(drug.getPreparationUnit());
        }
        int prep = preparation.intValue();
        if (prep <= 0) {
            return inventory + nameOrEmpty(drug.getPreparationUnit());
        }
        int wholePacks = inventory / prep;
        int remainder = inventory % prep;
        if (wholePacks >= 0) {
            return wholePacks + nameOrEmpty(drug.getPack())
                    + (remainder > 0 ? remainder + nameOrEmpty(drug.getPreparationUnit()) : "");
        } else {
            return inventory + nameOrEmpty(drug.getPreparationUnit());
        }
    }

    /**
     * định dạnghóaVật tưSố lượng còn lại, theoĐơn vị đóng góiquy đổihiển thị(Như "2gói3Cái")
     * DS96: packNumber null/0 (/0 hoặc NPE khi unbox) làm cả file 500.
     * Không quy đổi được thì giữ nguyên tổng + đơn vị nhỏ nhất (mẫu TollInfo).
     */
    private String formatStuffNumber(Stuff stuff) {
        int inventory = stuff.getInventory();
        Integer packNumber = stuff.getPackNumber();
        if (packNumber == null || packNumber <= 0) {
            return inventory + nameOrEmpty(stuff.getMinUnit());
        }
        int wholePacks = inventory / packNumber;
        int remainder = inventory % packNumber;
        if (wholePacks >= 0) {
            return wholePacks + nameOrEmpty(stuff.getPackUnit())
                    + (remainder > 0 ? remainder + nameOrEmpty(stuff.getMinUnit()) : "");
        } else {
            return inventory + nameOrEmpty(stuff.getMinUnit());
        }
    }
}
