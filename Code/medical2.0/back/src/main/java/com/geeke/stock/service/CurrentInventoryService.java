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

    @Transactional(readOnly = true)
    public HashMap<String, BigDecimal> getAllDrugs(List<Parameter> params, String orderby) {
        List<Drug> drugs = drugService.listAllDrug(params, orderby);
        BigDecimal totalPrice = new BigDecimal("0");
        BigDecimal totalSellingPrice = new BigDecimal("0");
        for (Drug drug : drugs) {
            //LấymỗiCáiThuốc của giá nhậpTổng cộng
            //tiến hành dònggiá bánTính
            BigDecimal leastBid = drug.getPrice().divide(new BigDecimal(drug.getPreparation()),4,BigDecimal.ROUND_HALF_UP);
            BigDecimal multiply = leastBid.multiply(drug.getStock().getStorageStock().subtract((drug.getStock().getUsedStock().add(drug.getStock().getReimburseStock()))));
            totalSellingPrice = totalSellingPrice.add(multiply);
        }

        //LấymỗiCáiThuốc của giá nhậpTổng cộng
        List<String> drugIds = new ArrayList<>();
        for (Drug d : drugs) { drugIds.add(d.getId()); }
        BigDecimal byDrugOrStuffIds = medicinalStorageControlService.getByDrugOrStuffIds(drugIds);
        totalPrice = totalPrice.add(byDrugOrStuffIds);

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
            BigDecimal divide = stuff.getPriceOutSell().divide(new BigDecimal(stuff.getPackNumber() + ""), 4, BigDecimal.ROUND_HALF_UP);
            BigDecimal multiply = divide.multiply(stuff.getStock().getStorageStock().subtract((stuff.getStock().getUsedStock().add(stuff.getStock().getReimburseStock()))));
            totalSellingPrice = totalSellingPrice.add(multiply);
        }

        //LấyVật tư của tổnggiá nhập
        List<String> stuffIds = new ArrayList<>();
        for (Stuff s : stuffs) { stuffIds.add(s.getId()); }
        BigDecimal byDrugOrStuffIds = medicinalStorageControlService.getByDrugOrStuffIds(stuffIds);
        totalPrice = totalPrice.add(byDrugOrStuffIds);

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

                //Lấytất cả của giá nhậpTổng cộng
                BigDecimal purchasing = new BigDecimal(medicinalStorageControl.getSupplierStock().getLeastBid() + "").multiply(medicinalStorageControl.getStorageStock().subtract(medicinalStorageControl.getUsedStock().add(medicinalStorageControl.getReimburseStock())));
                totalPrice = totalPrice.add(purchasing);

                //Lấytất cả của giá bán
                BigDecimal divide = new BigDecimal((medicinalStorageControl.getDrug().getPrice()==null?0:medicinalStorageControl.getDrug().getPrice()) + "").divide(new BigDecimal((medicinalStorageControl.getDrug().getPreparation()==null?"0":medicinalStorageControl.getDrug().getPreparation())), 4, BigDecimal.ROUND_HALF_UP);
                BigDecimal selling = (medicinalStorageControl.getStorageStock().subtract(medicinalStorageControl.getUsedStock().add(medicinalStorageControl.getReimburseStock()))).multiply(divide);
                totalSellingPrice = totalSellingPrice.add(selling);
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
                //Lấygiá nhậpTổng cộng
                BigDecimal purchasing = new BigDecimal(medicinalStorageControl.getSupplierStock().getLeastBid() + "").multiply(medicinalStorageControl.getStorageStock().subtract(medicinalStorageControl.getUsedStock().add(medicinalStorageControl.getReimburseStock())));
                totalPrice = totalPrice.add(purchasing);

                //Tổng giá bán
                BigDecimal divide = new BigDecimal(medicinalStorageControl.getStuff().getPriceOutSell() + "").divide(new BigDecimal(medicinalStorageControl.getStuff().getPackNumber()),4, BigDecimal.ROUND_HALF_UP);
                BigDecimal selling = (medicinalStorageControl.getStorageStock().subtract(medicinalStorageControl.getUsedStock().add(medicinalStorageControl.getReimburseStock()))).multiply(divide);
                totalSellingPrice = totalSellingPrice.add(selling);
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
                msc.getStuff().setInventory(Integer.parseInt(inventory));

                // Tổng giá bán
                BigDecimal selling = new BigDecimal(msc.getStuff().getPriceOutSell() + "").divide(new BigDecimal(msc.getStuff().getPackNumber()), 4, BigDecimal.ROUND_HALF_UP);
                BigDecimal sellingTotal = new BigDecimal(msc.getStuff().getInventory()).multiply(selling);
                msc.getStuff().setRetailPrice(sellingTotal);

                // Tổng chi phí
                BigDecimal allBid = msc.getSupplierStock().getLeastBid().multiply(new BigDecimal(msc.getStuff().getInventory()));
                msc.getStuff().setBid(allBid);

                // Số lượng còn lại(Chuỗi đã định dạng)
                int inv = msc.getStuff().getInventory();
                int packNum = msc.getStuff().getPackNumber();
                String number = Math.floor(inv / packNum) >= 0
                        ? (int) Math.floor(inv / packNum) + msc.getStuff().getPackUnit().getName()
                        + ((inv % packNum > 0) ? (inv % packNum) + msc.getStuff().getMinUnit().getName() : "")
                        : inv + msc.getStuff().getMinUnit().getName();
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
                    stuff.setInventory(Integer.parseInt(inventory));
                } else {
                    stuff.setInventory(0);
                }

                // Quy cách
                String specification = stuff.getPackNumber() + stuff.getMinUnit().getName() + "/" + stuff.getPackUnit().getName();
                stuff.setSpecifications(specification);

                // Tổng giá bán
                BigDecimal selling = new BigDecimal(stuff.getPriceOutSell() + "").divide(new BigDecimal(stuff.getPackNumber()), 4, BigDecimal.ROUND_HALF_UP);
                BigDecimal sellingTotal = new BigDecimal(stuff.getInventory()).multiply(selling);
                stuff.setRetailPrice(sellingTotal);

                // Tổng chi phí
                BigDecimal totalCost = medicinalStorageControlService.getByDrugOrStuffId(stuff.getId());
                stuff.setBid(totalCost);

                // Số lượng còn lại(Chuỗi đã định dạng)
                int inv = stuff.getInventory();
                int packNum = stuff.getPackNumber();
                String number = Math.floor(inv / packNum) >= 0
                        ? (int) Math.floor(inv / packNum) + stuff.getPackUnit().getName()
                        + ((inv % packNum > 0) ? (inv % packNum) + stuff.getMinUnit().getName() : "")
                        : inv + stuff.getMinUnit().getName();
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
                msc.getDrug().setInventory(Integer.parseInt(inventory));

                // Tổng giá bán
                BigDecimal selling = new BigDecimal(msc.getDrug().getPrice() + "").divide(new BigDecimal(msc.getDrug().getPreparation()), 4, BigDecimal.ROUND_HALF_UP);
                BigDecimal sellingTotal = msc.getStorageStock().subtract(msc.getUsedStock().add(msc.getReimburseStock())).multiply(selling);
                msc.getDrug().setRetailPrice(sellingTotal);

                // Tổng chi phí
                BigDecimal allBid = msc.getSupplierStock().getLeastBid().multiply(msc.getStorageStock().subtract(msc.getUsedStock().add(msc.getReimburseStock())));
                msc.getDrug().setBid(allBid);

                // Số lượng còn lại(Chuỗi đã định dạng)
                int inv = msc.getDrug().getInventory();
                int prep = Integer.parseInt(msc.getDrug().getPreparation());
                String number = Math.floor(inv / prep) >= 0
                        ? (int) Math.floor(inv / prep) + msc.getDrug().getPack().getName()
                        + ((inv % prep > 0) ? (inv % prep) + msc.getDrug().getPreparationUnit().getName() : "")
                        : inv + msc.getDrug().getPreparationUnit().getName();
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
                    drug.setInventory(Integer.parseInt(inventory));
                } else {
                    drug.setInventory(0);
                }

                // Tổng giá bán
                BigDecimal selling = new BigDecimal(drug.getPrice() + "").divide(new BigDecimal(drug.getPreparation()), 4, BigDecimal.ROUND_HALF_UP);
                BigDecimal sellingTotal = drug.getStock().getStorageStock().subtract(drug.getStock().getUsedStock().add(drug.getStock().getReimburseStock())).multiply(selling);
                drug.setRetailPrice(sellingTotal);

                // Tổng chi phí
                BigDecimal totalCost = medicinalStorageControlService.getByDrugOrStuffId(drug.getId());
                drug.setBid(totalCost);

                // Số lượng còn lại(Chuỗi đã định dạng)
                int inv = drug.getInventory();
                int prep = Integer.parseInt(drug.getPreparation());
                String number = Math.floor(inv / prep) >= 0
                        ? (int) Math.floor(inv / prep) + drug.getPack().getName()
                        + ((inv % prep > 0) ? (inv % prep) + drug.getPreparationUnit().getName() : "")
                        : inv + drug.getPreparationUnit().getName();
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


}
