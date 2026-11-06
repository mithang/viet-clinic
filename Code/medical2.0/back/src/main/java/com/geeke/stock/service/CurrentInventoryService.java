package com.geeke.stock.service;

import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.common.data.Parameter;

import java.util.ArrayList;
import com.geeke.stock.entity.*;
import com.geeke.utils.excel.ExcelExportBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service("currentInventoryService")
@Transactional(readOnly = true)
public class CurrentInventoryService {
    @Autowired
    private DrugService drugService;

    @Autowired
    private SupplierStockService supplierStockService;

    @Autowired
    private StuffService stuffService;

    @Autowired
    private MedicinalStockControlService medicinalStockControlService;

    @Autowired
    private MedicinalStorageControlService medicinalStorageControlService;

    // DS97: các thống kê giá trị tồn kho trước đây chia/trừ trực tiếp trên dữ
    // liệu nullable (drug.price/preparation NULL, stuff.priceOutSell NULL,
    // packNumber 0, MSC dangling -> preparation ''/packNumber null) nên MỘT
    // dòng xấu làm cả endpoint 500 (NPE/NFE/ArithmeticException /0). Dòng xấu
    // đóng góp 0 vào tổng (giữ nguyên tổng của các dòng tốt), không 500.
    private static BigDecimal safeDecimal(Object value) {
        if (value == null) {
            return null;
        }
        try {
            String s = String.valueOf(value).trim();
            if (s.isEmpty() || "null".equalsIgnoreCase(s)) {
                return null;
            }
            return new BigDecimal(s);
        } catch (Exception e) {
            return null;
        }
    }

    // Số lượng còn lại của một dòng MSC (storage - used - reimburse); các cột
    // NOT NULL nhưng vẫn guard null để không NPE khi mapper/join thiếu.
    private static BigDecimal remainingStock(MedicinalStorageControl m) {
        if (m == null || m.getStorageStock() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal used = m.getUsedStock() == null ? BigDecimal.ZERO : m.getUsedStock();
        BigDecimal reimburse = m.getReimburseStock() == null ? BigDecimal.ZERO : m.getReimburseStock();
        return m.getStorageStock().subtract(used.add(reimburse));
    }

    // Số lượng còn lại của một thuốc/vật tư trong listAlls (stock join có thể
    // null khi thuốc/vật tư chưa có dòng tồn kho tổng) -> null để caller bỏ qua.
    private static BigDecimal remainingOfStock(MedicinalStockControl stock) {
        if (stock == null || stock.getStorageStock() == null) {
            return null;
        }
        BigDecimal used = stock.getUsedStock() == null ? BigDecimal.ZERO : stock.getUsedStock();
        BigDecimal reimburse = stock.getReimburseStock() == null ? BigDecimal.ZERO : stock.getReimburseStock();
        return stock.getStorageStock().subtract(used.add(reimburse));
    }

    // Đơn giá lẻ = giá bán / hệ số đóng gói; giá null, hệ số null/không số/
    // <= 0 -> null (dòng xấu đóng góp 0 vào tổng thay vì 500).
    private static BigDecimal unitPrice(BigDecimal price, Object packFactor) {
        if (price == null) {
            return null;
        }
        BigDecimal factor = safeDecimal(packFactor);
        if (factor == null || factor.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return price.divide(factor, 4, BigDecimal.ROUND_HALF_UP);
    }

    @Transactional(readOnly = true)
    public HashMap<String, BigDecimal> getAllDrugs(List<Parameter> params, String orderby) {
        List<Drug> drugs = drugService.listAllDrug(params, orderby);
        BigDecimal totalPrice = new BigDecimal("0");
        BigDecimal totalSellingPrice = new BigDecimal("0");
        for (Drug drug : drugs) {
            //LấymỗiCáiThuốc của giá nhậpTổng cộng
            //tiến hành dònggiá bánTính
            BigDecimal remaining = remainingOfStock(drug.getStock());
            BigDecimal unit = unitPrice(drug.getPrice(), drug.getPreparation());
            if (remaining != null && unit != null) {
                totalSellingPrice = totalSellingPrice.add(unit.multiply(remaining));
            }
        }

        //LấymỗiCáiThuốc của giá nhậpTổng cộng
        List<String> drugIds = new ArrayList<>();
        for (Drug d : drugs) { drugIds.add(d.getId()); }
        BigDecimal byDrugOrStuffIds = medicinalStorageControlService.getByDrugOrStuffIds(drugIds);
        if (byDrugOrStuffIds != null) {
            totalPrice = totalPrice.add(byDrugOrStuffIds);
        }

        HashMap<String, BigDecimal> totalMap = new HashMap<>();
        totalMap.put("totalPrice",totalPrice);
        totalMap.put("totalSellingPrice",totalSellingPrice);
        return totalMap;
    }

    @Transactional(readOnly = true)
    public Page<MedicinalStorageControl> getBatchNumberDrug(SearchParams searchParams) {
        Page<MedicinalStorageControl> medicinalStorageControlPage = medicinalStorageControlService.listPage(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());

        return medicinalStorageControlPage;
    }
    @Transactional(readOnly = true)
    public HashMap<String, BigDecimal> getgetStuffSalesStat(List<Parameter> params, String orderby) {
        List<Stuff> stuffs = stuffService.listAlls(params, orderby);
        BigDecimal totalPrice = new BigDecimal("0");
        BigDecimal totalSellingPrice = new BigDecimal("0");
        for (Stuff stuff : stuffs) {
            //LấyVật tư của tổnggiá nhập
            //LấyVật tư của tổnggiá bán
            BigDecimal remaining = remainingOfStock(stuff.getStock());
            BigDecimal unit = unitPrice(stuff.getPriceOutSell(), stuff.getPackNumber());
            if (remaining != null && unit != null) {
                totalSellingPrice = totalSellingPrice.add(unit.multiply(remaining));
            }
        }

        //LấyVật tư của tổnggiá nhập
        List<String> stuffIds = new ArrayList<>();
        for (Stuff s : stuffs) { stuffIds.add(s.getId()); }
        BigDecimal byDrugOrStuffIds = medicinalStorageControlService.getByDrugOrStuffIds(stuffIds);
        if (byDrugOrStuffIds != null) {
            totalPrice = totalPrice.add(byDrugOrStuffIds);
        }

        HashMap<String, BigDecimal> totalMap = new HashMap<>();
        totalMap.put("totalPrice",totalPrice);
        totalMap.put("totalSellingPrice",totalSellingPrice);
        return totalMap;
    }
    @Transactional(readOnly = true)
    public Page<MedicinalStorageControl> getBatchNumberStuff(SearchParams searchParams) {
        Page<MedicinalStorageControl> medicinalStorageControlPage = medicinalStorageControlService.listPage(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return medicinalStorageControlPage;
    }
    @Transactional(readOnly = true)
    public HashMap<String, BigDecimal> getDrugSalesStatByNumber(SearchParams searchParams) {
        List<MedicinalStorageControl> medicinalStorageControls = medicinalStorageControlService.listAll(searchParams.getParams(), searchParams.getOrder());
        BigDecimal totalPrice = new BigDecimal("0");
        BigDecimal totalSellingPrice = new BigDecimal("0");
        if(!CollectionUtils.isEmpty(medicinalStorageControls)){
            for (MedicinalStorageControl medicinalStorageControl : medicinalStorageControls) {
                BigDecimal remaining = remainingStock(medicinalStorageControl);

                //Lấytất cả của giá nhậpTổng cộng
                BigDecimal leastBid = medicinalStorageControl.getSupplierStock() == null
                        ? null : safeDecimal(medicinalStorageControl.getSupplierStock().getLeastBid());
                if (leastBid != null) {
                    totalPrice = totalPrice.add(leastBid.multiply(remaining));
                }

                //Lấytất cả của giá bán
                BigDecimal unit = medicinalStorageControl.getDrug() == null
                        ? null : unitPrice(medicinalStorageControl.getDrug().getPrice(),
                                medicinalStorageControl.getDrug().getPreparation());
                if (unit != null) {
                    totalSellingPrice = totalSellingPrice.add(remaining.multiply(unit));
                }
            }
        }
        HashMap<String, BigDecimal> totalMap = new HashMap<>();
        totalMap.put("totalPrice",totalPrice);
        totalMap.put("totalSellingPrice",totalSellingPrice);
        return totalMap;
    }

    @Transactional(readOnly = true)
    public HashMap<String, BigDecimal> getStuffSalesStatNumber(SearchParams searchParams) {
        List<MedicinalStorageControl> medicinalStorageControls = medicinalStorageControlService.listAll(searchParams.getParams(), searchParams.getOrder());
        BigDecimal totalPrice = new BigDecimal("0");
        BigDecimal totalSellingPrice = new BigDecimal("0");
        if(!CollectionUtils.isEmpty(medicinalStorageControls)){
            for (MedicinalStorageControl medicinalStorageControl : medicinalStorageControls) {
                BigDecimal remaining = remainingStock(medicinalStorageControl);
                //Lấygiá nhậpTổng cộng
                BigDecimal leastBid = medicinalStorageControl.getSupplierStock() == null
                        ? null : safeDecimal(medicinalStorageControl.getSupplierStock().getLeastBid());
                if (leastBid != null) {
                    totalPrice = totalPrice.add(leastBid.multiply(remaining));
                }

                //Tổng giá bán
                BigDecimal unit = medicinalStorageControl.getStuff() == null
                        ? null : unitPrice(medicinalStorageControl.getStuff().getPriceOutSell(),
                                medicinalStorageControl.getStuff().getPackNumber());
                if (unit != null) {
                    totalSellingPrice = totalSellingPrice.add(remaining.multiply(unit));
                }
            }
        }
        HashMap<String, BigDecimal> totalMap = new HashMap<>();
        totalMap.put("totalPrice",totalPrice);
        totalMap.put("totalSellingPrice",totalSellingPrice);
        return totalMap;
    }

    @Transactional
    public void exportTable(SearchParams searchParams, HttpServletResponse response) throws IOException {
        if(Objects.equals("1",searchParams.getColumnName())){
            exportDrug(searchParams,response);
        }else if(Objects.equals("2",searchParams.getColumnName())){
            exportDrugByBatchNo(searchParams,response);
        }else if(Objects.equals("3",searchParams.getColumnName())){
            exportStuff(searchParams,response);
        }else {
            exportStuffByBatchNo(searchParams,response);
        }
    }

    //Theo số lôXuấtVật tư
    private void exportStuffByBatchNo(SearchParams searchParams, HttpServletResponse response) throws IOException {
        List<MedicinalStorageControl> medicinalStorageControls = medicinalStorageControlService.listAll(searchParams.getParams(), searchParams.getOrder());

        // Trường phái sinh tính trước
        if (!CollectionUtils.isEmpty(medicinalStorageControls)) {
            for (MedicinalStorageControl msc : medicinalStorageControls) {
                // Số lượng còn lại(Giá trị số gốc)
                String inventory = msc.getStorageStock().subtract(msc.getUsedStock().add(msc.getReimburseStock())).stripTrailingZeros().toPlainString();
                msc.getStuff().setInventory(parseExportCount(inventory));

                // Tổng giá bán: giá/đơn vị nhỏ = giá bán / số lượng đóng gói.
                // Dòng MSC loại thuốc join stuff rỗng cho pack_number = 0 (mapper
                // IFNULL(stuff.pack_number,'')) nên packNum <= 0 thì giữ nguyên
                // giá bán, không chia cho 0 (mẫu TollInfo: mẫu số <= 0 giữ tổng).
                int packNum = msc.getStuff().getPackNumber() == null ? 0 : msc.getStuff().getPackNumber();
                BigDecimal selling = packNum > 0
                        ? new BigDecimal(msc.getStuff().getPriceOutSell() + "").divide(new BigDecimal(packNum), 4, BigDecimal.ROUND_HALF_UP)
                        : new BigDecimal(msc.getStuff().getPriceOutSell() + "");
                BigDecimal sellingTotal = new BigDecimal(msc.getStuff().getInventory()).multiply(selling);
                msc.getStuff().setRetailPrice(sellingTotal);

                // Tổng chi phí
                BigDecimal allBid = msc.getSupplierStock().getLeastBid().multiply(new BigDecimal(msc.getStuff().getInventory()));
                msc.getStuff().setBid(allBid);

                // Số lượng còn lại(Chuỗi đã định dạng)
                int inv = msc.getStuff().getInventory();
                String number;
                if (packNum > 0) {
                    number = Math.floor(inv / packNum) >= 0
                            ? (int) Math.floor(inv / packNum) + msc.getStuff().getPackUnit().getName()
                            + ((inv % packNum > 0) ? (inv % packNum) + msc.getStuff().getMinUnit().getName() : "")
                            : inv + msc.getStuff().getMinUnit().getName();
                } else {
                    // mẫu TollInfo: mẫu số <= 0 thì giữ nguyên tổng, đơn vị đóng gói
                    number = inv + msc.getStuff().getPackUnit().getName();
                }
                msc.getStuff().setStockNumber(number);
            }
        }

        new ExcelExportBuilder("Tồn kho vật tư hiện tại-Theo số lô")
                .addColumns(
                        new ExcelExportBuilder.Column("Tên vật tư", "stuff.name"),
                        new ExcelExportBuilder.Column("Phân loại vật tư", "stuff.type.name"),
                        new ExcelExportBuilder.Column("Quy cách", "supplierStock.norms"),
                        new ExcelExportBuilder.Column("Nhà sản xuất", "stuff.factory.name"),
                        new ExcelExportBuilder.Column("Nhà cung cấp", "supplierStock.supplierId.name"),
                        new ExcelExportBuilder.Column("Số lô", "supplierStock.batchNo"),
                        new ExcelExportBuilder.Column("Giá bán( đồng)", "stuff.priceOutSell"),
                        new ExcelExportBuilder.Column("Tổng giá bán( đồng)", "stuff.retailPrice"),
                        new ExcelExportBuilder.Column("Giá vốn( đồng)", "supplierStock.bid"),
                        new ExcelExportBuilder.Column("Tổng chi phí( đồng)", "stuff.bid"),
                        new ExcelExportBuilder.Column("Số lượng còn lại", "stuff.stockNumber")
                )
                .data(medicinalStorageControls)
                .write(response, "Tồn kho vật tư hiện tại-Theo số lô.xlsx");
    }

    //Theo hàng hóaXuấtVật tư
    private void exportStuff(SearchParams searchParams, HttpServletResponse response) throws IOException {
        List<Stuff> stuffs = stuffService.listAlls(searchParams.getParams(), searchParams.getOrder());

        // Trường phái sinh tính trước
        if (!CollectionUtils.isEmpty(stuffs)) {
            for (Stuff stuff : stuffs) {
                // Số lượng còn lại(Giá trị số gốc)
                if (!Objects.isNull(stuff.getStock()) && !Objects.isNull(stuff.getStock().getStorageStock())) {
                    String inventory = stuff.getStock().getStorageStock().subtract(stuff.getStock().getUsedStock().add(stuff.getStock().getReimburseStock())).stripTrailingZeros().toPlainString();
                    // DS96 F7: tồn kho thập phân "10.5" làm Integer.parseInt ném NFE -> cả file 500; dùng parseExportCount như exportDrug.
                    stuff.setInventory(parseExportCount(inventory));
                } else {
                    stuff.setInventory(0);
                }

                // Quy cách
                String specification = stuff.getPackNumber() + stuff.getMinUnit().getName() + "/" + stuff.getPackUnit().getName();
                stuff.setSpecifications(specification);

                // Tổng giá bán
                // DS92: priceOutSell NULL (cột nullable, save API chấp nhận)
                // làm new BigDecimal("null") ném NumberFormatException, và
                // packNumber 0 làm divide ném ArithmeticException, cả hai
                // đều vỡ toàn bộ file xuất -> giá null tính là 0,
                // packNumber <= 0 thì giữ nguyên giá, không chia cho 0
                // (cùng mẫu TollInfo/parseExportCount như exportDrug).
                BigDecimal priceOut = stuff.getPriceOutSell() == null ? BigDecimal.ZERO : stuff.getPriceOutSell();
                int packNumForSelling = stuff.getPackNumber() == null ? 0 : stuff.getPackNumber();
                BigDecimal selling = packNumForSelling > 0
                        ? priceOut.divide(new BigDecimal(packNumForSelling), 4, BigDecimal.ROUND_HALF_UP)
                        : priceOut;
                BigDecimal sellingTotal = new BigDecimal(stuff.getInventory()).multiply(selling);
                stuff.setRetailPrice(sellingTotal);

                // Tổng chi phí
                BigDecimal totalCost = medicinalStorageControlService.getByDrugOrStuffId(stuff.getId());
                stuff.setBid(totalCost);

                // Số lượng còn lại(Chuỗi đã định dạng)
                int inv = stuff.getInventory();
                int packNum = packNumForSelling;
                String number;
                if (packNum > 0) {
                    number = Math.floor(inv / packNum) >= 0
                            ? (int) Math.floor(inv / packNum) + stuff.getPackUnit().getName()
                            + ((inv % packNum > 0) ? (inv % packNum) + stuff.getMinUnit().getName() : "")
                            : inv + stuff.getMinUnit().getName();
                } else {
                    // DS92: packNumber 0 thì inv / packNum ném
                    // ArithmeticException -> giữ nguyên tổng, đơn vị đóng gói.
                    number = inv + stuff.getPackUnit().getName();
                }
                stuff.setStockNumber(number);
            }
        }

        new ExcelExportBuilder("Tồn kho vật tư hiện tại-Theo hàng hóa")
                .addColumns(
                        new ExcelExportBuilder.Column("Tên vật tư", "name"),
                        new ExcelExportBuilder.Column("Phân loại vật tư", "type.name"),
                        new ExcelExportBuilder.Column("Quy cách", "specifications"),
                        new ExcelExportBuilder.Column("Nhà sản xuất", "factory.name"),
                        new ExcelExportBuilder.Column("Giá bán( đồng)", "priceOutSell"),
                        new ExcelExportBuilder.Column("Tổng giá bán( đồng)", "retailPrice"),
                        new ExcelExportBuilder.Column("Tổng chi phí( đồng)", "bid"),
                        new ExcelExportBuilder.Column("Số lượng còn lại", "stockNumber")
                )
                .data(stuffs)
                .write(response, "Tồn kho vật tư hiện tại-Theo hàng hóa.xlsx");
    }

    //Theo số lôXuấtThuốc
    private void exportDrugByBatchNo(SearchParams searchParams, HttpServletResponse response) throws IOException {
        List<MedicinalStorageControl> medicinalStorageControls = medicinalStorageControlService.listAll(searchParams.getParams(), searchParams.getOrder());

        // Trường phái sinh tính trước
        if (!CollectionUtils.isEmpty(medicinalStorageControls)) {
            for (MedicinalStorageControl msc : medicinalStorageControls) {
                // Số lượng còn lại(Giá trị số gốc)
                String inventory = msc.getStorageStock().subtract(msc.getUsedStock().add(msc.getReimburseStock())).stripTrailingZeros().toPlainString();
                msc.getDrug().setInventory(parseExportCount(inventory));

                // Tổng giá bán: preparation là String dạng thập phân ("100.0")
                // nên parse bằng BigDecimal theo mẫu parseExportCount; prep <= 0
                // thì giữ nguyên giá bán, không chia cho 0.
                int prep = parseExportCount(msc.getDrug().getPreparation());
                BigDecimal selling = prep > 0
                        ? new BigDecimal(msc.getDrug().getPrice() + "").divide(new BigDecimal(msc.getDrug().getPreparation()), 4, BigDecimal.ROUND_HALF_UP)
                        : new BigDecimal(msc.getDrug().getPrice() + "");
                BigDecimal sellingTotal = msc.getStorageStock().subtract(msc.getUsedStock().add(msc.getReimburseStock())).multiply(selling);
                msc.getDrug().setRetailPrice(sellingTotal);

                // Tổng chi phí
                BigDecimal allBid = msc.getSupplierStock().getLeastBid().multiply(msc.getStorageStock().subtract(msc.getUsedStock().add(msc.getReimburseStock())));
                msc.getDrug().setBid(allBid);

                // Số lượng còn lại(Chuỗi đã định dạng)
                int inv = msc.getDrug().getInventory();
                String number;
                if (prep > 0) {
                    number = Math.floor(inv / prep) >= 0
                            ? (int) Math.floor(inv / prep) + msc.getDrug().getPack().getName()
                            + ((inv % prep > 0) ? (inv % prep) + msc.getDrug().getPreparationUnit().getName() : "")
                            : inv + msc.getDrug().getPreparationUnit().getName();
                } else {
                    // mẫu TollInfo: mẫu số <= 0 thì giữ nguyên tổng, đơn vị đóng gói
                    number = inv + msc.getDrug().getPack().getName();
                }
                msc.getDrug().setStockNumber(number);
            }
        }

        new ExcelExportBuilder("Tồn kho thuốc hiện tại-Theo số lô")
                .addColumns(
                        new ExcelExportBuilder.Column("Tên thuốc", "drug.goodsName"),
                        new ExcelExportBuilder.Column("Phân loại thuốc", "drug.type.name"),
                        new ExcelExportBuilder.Column("Quy cách", "supplierStock.norms"),
                        new ExcelExportBuilder.Column("Nhà sản xuất", "drug.factory.name"),
                        new ExcelExportBuilder.Column("Nhà cung cấp", "supplierStock.supplierId.name"),
                        new ExcelExportBuilder.Column("Số lô", "supplierStock.batchNo"),
                        new ExcelExportBuilder.Column("Giá bán( đồng)", "drug.price"),
                        new ExcelExportBuilder.Column("Tổng giá bán( đồng)", "drug.retailPrice"),
                        new ExcelExportBuilder.Column("Giá vốn( đồng)", "supplierStock.bid"),
                        new ExcelExportBuilder.Column("Tổng chi phí( đồng)", "drug.bid"),
                        new ExcelExportBuilder.Column("Số lượng còn lại", "drug.stockNumber")
                )
                .data(medicinalStorageControls)
                .write(response, "Tồn kho thuốc hiện tại-Theo số lô.xlsx");
    }

    //Theo hàng hóaXuấtThuốc
    @Transactional
    public void exportDrug(SearchParams searchParams, HttpServletResponse response) throws IOException {
        List<Drug> drugs = drugService.listAlls(searchParams.getParams(), searchParams.getOrder());

        // Trường phái sinh tính trước
        if (!CollectionUtils.isEmpty(drugs)) {
            for (Drug drug : drugs) {
                // Quy cách
                String specification = drug.getDosis() + drug.getDosisUnit().getName() + "*"
                        + drug.getPreparation() + drug.getPreparationUnit().getName() + "/" + drug.getPack().getName();
                drug.setYpgg(specification);

                // Số lượng còn lại(Giá trị số gốc)
                if (!Objects.isNull(drug.getStock()) && !Objects.isNull(drug.getStock().getStorageStock())) {
                    String inventory = drug.getStock().getStorageStock().subtract(drug.getStock().getUsedStock().add(drug.getStock().getReimburseStock())).stripTrailingZeros().toPlainString();
                    drug.setInventory(parseExportCount(inventory));
                } else {
                    drug.setInventory(0);
                }

                // Tổng giá bán: preparation là String dạng thập phân ("100.0")
                // nên parse bằng BigDecimal theo mẫu parseExportCount; prep <= 0
                // thì giữ nguyên giá bán, không chia cho 0.
                // DS92: drug.price NULL (cột nullable, save API chấp nhận)
                // làm new BigDecimal("null") ném NumberFormatException,
                // vỡ toàn bộ file xuất -> giá null tính là 0.
                int prep = parseExportCount(drug.getPreparation());
                BigDecimal price = drug.getPrice() == null ? BigDecimal.ZERO : drug.getPrice();
                BigDecimal selling = prep > 0
                        ? price.divide(new BigDecimal(drug.getPreparation()), 4, BigDecimal.ROUND_HALF_UP)
                        : price;
                BigDecimal sellingTotal = drug.getStock().getStorageStock().subtract(drug.getStock().getUsedStock().add(drug.getStock().getReimburseStock())).multiply(selling);
                drug.setRetailPrice(sellingTotal);

                // Tổng chi phí
                BigDecimal totalCost = medicinalStorageControlService.getByDrugOrStuffId(drug.getId());
                drug.setBid(totalCost);

                // Số lượng còn lại(Chuỗi đã định dạng)
                int inv = drug.getInventory();
                String number;
                if (prep > 0) {
                    number = Math.floor(inv / prep) >= 0
                            ? (int) Math.floor(inv / prep) + drug.getPack().getName()
                            + ((inv % prep > 0) ? (inv % prep) + drug.getPreparationUnit().getName() : "")
                            : inv + drug.getPreparationUnit().getName();
                } else {
                    // mẫu TollInfo: mẫu số <= 0 thì giữ nguyên tổng, đơn vị đóng gói
                    number = inv + drug.getPack().getName();
                }
                drug.setStockNumber(number);
            }
        }

        new ExcelExportBuilder("Tồn kho thuốc hiện tại-Theo hàng hóa")
                .addColumns(
                        new ExcelExportBuilder.Column("Tên thuốc", "goodsName"),
                        new ExcelExportBuilder.Column("Phân loại thuốc", "type.name"),
                        new ExcelExportBuilder.Column("Quy cách", "ypgg"),
                        new ExcelExportBuilder.Column("Nhà sản xuất", "factory.name"),
                        new ExcelExportBuilder.Column("Giá bán( đồng)", "price"),
                        new ExcelExportBuilder.Column("Tổng giá bán( đồng)", "retailPrice"),
                        new ExcelExportBuilder.Column("Tổng chi phí( đồng)", "bid"),
                        new ExcelExportBuilder.Column("Số lượng còn lại", "stockNumber")
                )
                .data(drugs)
                .write(response, "Tồn kho thuốc hiện tại-Theo hàng hóa.xlsx");
    }

    // Mẫu parseExportCount của TollInfoService (iter65/66): chuỗi số thập phân
    // dạng "100.0"/"6.0" về int; null/rỗng/không số -> 0, không ném 500.
    private int parseExportCount(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return new BigDecimal(value.trim()).intValue();
        } catch (Exception e) {
            return 0;
        }
    }

}
