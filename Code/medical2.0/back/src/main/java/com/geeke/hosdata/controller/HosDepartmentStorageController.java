package com.geeke.hosdata.controller;


import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.geeke.common.constants.BizConstants;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Parameter;
import com.geeke.hosdata.constant.ApiUrl;
import com.geeke.hosdata.entity.HisDrugs;
import com.geeke.hosdata.entity.HisDrugsStorage;
import com.geeke.hosdata.util.HttpUtil;
import com.geeke.org.entity.Company;
import com.geeke.stock.entity.*;
import com.geeke.stock.service.DrugService;
import com.geeke.stock.service.SupplierStockService;
import com.geeke.sys.entity.DictItem;
import com.geeke.sys.utils.SessionUtils;
import com.geeke.utils.ResultUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * bản việnKhoaphát thuốc
 */
@RestController
@RequestMapping("/hosdata/HosDepartmentStorage")
@RequiredArgsConstructor
public class HosDepartmentStorageController {

    private final HttpUtil httpUtil;
    
    private final DrugService drugService;

    private  final HosCollectDataController hosCollectDataController;

    private final SupplierStockService supplierStockService ;


    /**
     * Truy vấnphát thuốcThông tin giờthìđồng bộThông tin thuốc
     * @param searchParams
     * @return
     */
    @PostMapping(value = "getHosDrugsStorage")
    public ResponseEntity<JSONObject> getHosMaterials(@RequestBody SearchParams searchParams) {
        List<Parameter> params = searchParams.getNewParams();
        List<Parameter> newParams = new ArrayList<>();
        params.forEach(item ->{
            // giới hạnchỉ địnhnàyKhoa
            if(item.getColumnName().equals("kscode") &&item.getValue().equals("")){
                item.setValue(SessionUtils.getUserJson().getString("companyId"));
            }
            newParams.add(new Parameter(item.getColumnName(),item.getQueryType(),item.getValue()));
        });
        JSONObject hosData = httpUtil.getHosData(searchParams, ApiUrl.HOS_GET_DRUGS_STORAGE_PAGE);
        JSONArray jsonArray = hosData.getJSONObject("BusData").getJSONObject("data").getJSONArray("rows");
        if(!jsonArray.isEmpty()){
            List<HisDrugsStorage> hisDrugsStorages = new ArrayList<>();
            List<HisDrugs> hisDrugs = new ArrayList<>();
            jsonArray.forEach(item->{
                HisDrugsStorage hisDrugsStorage = BeanUtil.copyProperties(item, HisDrugsStorage.class);
                hisDrugsStorages.add(hisDrugsStorage);
            });
            for (HisDrugsStorage hisDrugsStorage : hisDrugsStorages) {
                String ypdm = hisDrugsStorage.getYpdm();
                Drug drug = drugService.getByCode(ypdm,SessionUtils.getUserJson().getString("companyId"));
                if (drug == null) {
                    newParams.forEach(item-> {
                        if("ypdm".equals(item.getColumnName())){
                            item.setValue(ypdm);
                        }
                    });
                    searchParams.setParams(newParams);
                    JSONArray rows = hosCollectDataController.getJsonObject(searchParams).getJSONArray("rows");
                    rows.forEach(item->{
                        HisDrugs hisDrug = BeanUtil.copyProperties(item, HisDrugs.class);
                        hisDrugs.add(hisDrug);
                    });
                }
            }
            if(!hisDrugs.isEmpty()){
                //chènChưađồng bộ của Thông tin thuốc
                hosCollectDataController.getStrings(hisDrugs.stream()
                        .collect(Collectors.collectingAndThen(
                                Collectors.groupingBy(HisDrugs::getCode), // Theocode phútnhóm
                                map -> map.values().stream()            // lấyramỗiCáicodetương ứng của Giá trị(tức làĐối tượngDanh sách)
                                        .map(list -> list.get(0))           // Lấy phần tử đầu tiên từ mỗi danh sách làm đại diện
                                        .collect(Collectors.toList())      // 收集ĐếnmộtCáimới của Danh sách
                        )));

            }
           hosData.getJSONObject("BusData").getJSONObject("data").put("rows", hisDrugsStorages);
        }
        return ResponseEntity.ok(ResultUtil.successJson(hosData.getJSONObject("BusData").getJSONObject("data")));
    }


    /**
     * xét duyệtnhập khonàyPhòng khámbản việnphát thuốcDanh sách
     * @param hisDrugsStorages
     * @return
     */
    @PostMapping(value = "auditHosDrugsStorage")
    public ResponseEntity<JSONObject> auditHosMaterials(@RequestBody List<HisDrugsStorage> hisDrugsStorages) {
        StorageEvt storageEvt = new StorageEvt();
        SupplierStorage supplierStorage = new SupplierStorage();
        List<SupplierStock> supplierStockList = new ArrayList<>();
        int totalNumber = 0; // Khởi tạotổng số là 0
        for (int i = 0; i < hisDrugsStorages.size(); i++){
            Drug drug = drugService.getByCode(hisDrugsStorages.get(i).getYpdm(),SessionUtils.getUserJson().getString("companyId"));
            SupplierStock supplierStock = new SupplierStock();
            //sinhsảnLô
            supplierStock.setBatchNo(hisDrugsStorages.get(i).getPc());
            //Thông tin thuốc
            supplierStock.setDrug(drug);
            //Vật tưMặc định là null
            supplierStock.setStuff(new Stuff());
            //Kiểm tra số lượng theo bản viện là đơn vị nhỏ nhất hay đơn vị chuẩn
            if(Integer.parseInt(hisDrugsStorages.get(i).getCkzhyz()) > 1){
                //tiêu chuẩnĐơn vị
                supplierStock.setNumber(hisDrugsStorages.get(i).getSl() * Integer.parseInt( (drug.getPreparation().substring(0,drug.getPreparation().indexOf(".")))));
                supplierStock.setLeastBid(hisDrugsStorages.get(i).getPfj().divide(new BigDecimal(Integer.parseInt(hisDrugsStorages.get(i).getCkzhyz())),2,BigDecimal.ROUND_HALF_UP));
            }else {
                supplierStock.setNumber(hisDrugsStorages.get(i).getSl());
                supplierStock.setLeastBid(hisDrugsStorages.get(i).getPfj());
            }
            //Phòng khámid
            Company company = new Company();
            company.setId(SessionUtils.getUserJson().getString("companyId"));
            supplierStock.setCompany(company);
            supplierStorage.setCompany(company);
            //Ngày sản xuất
            if(hisDrugsStorages.get(i).getScrq() == null){
                //Nếu bản viện trống thì đặt thành thời gian hiện tại để tránh lỗi
                supplierStock.setProduceDate(new Date());
            }else{
                supplierStock.setProduceDate(hisDrugsStorages.get(i).getScrq());
            }

            //Thời hạn hiệu lực
            supplierStock.setEndDate(hisDrugsStorages.get(i).getYxq());
            Supplier supplier = new Supplier();
            //TODO Mặc địnhĐặttạmvàviếtchết là bản việnNhà cung cấp, sautiếp tụcTheocầnyêu cầuTối ưu hóa
            supplier.setId(BizConstants.DEFAULT_SUPPLIER_ID);
            supplierStock.setSupplierId(supplier);
            supplierStorage.setSupplier(supplier);
            //Giá nhập
            supplierStock.setBid(hisDrugsStorages.get(i).getPfj());
            supplierStockList.add(supplierStock);
            //Số hóa đơn
            supplierStorage.setInvoiceNumber(hisDrugsStorages.get(i).getFPh());
            totalNumber += supplierStock.getNumber();
        }
        //Bắt đầuThêmTồn khoThông tin
        supplierStorage.setNumber(totalNumber); // Sẽ 累thêm của Kết quảgánGiá trịchosupplierStorage
        //Thuốc 2
        supplierStorage.setBreed(2);
        supplierStorage.setExamineDate(new Date());
        DictItem dictItem = new DictItem();
        dictItem.setId(BizConstants.DICT_ITEM_STORAGE_EXAMINE_PASS);
        dictItem.setValue(BizConstants.SUPPLIER_STORAGE_EXAMINE_PASS);
        supplierStorage.setExamine(dictItem);
        supplierStorage.setType("1");
        supplierStorage.setCode("");


        storageEvt.setSupplierStockList(supplierStockList);
        storageEvt.setSupplierStorage(supplierStorage);

        supplierStockService.saves(storageEvt);
        supplierStockService.savesTo(storageEvt);
        // callbackbản việngiao diệnSửaĐãnhập khoTrạng thái
        hisDrugsStorages.forEach(item-> httpUtil.UpdateHosData(ApiUrl.HOS_UPDATE_DRUGS_STORAGE_STATUS, item.getCrkmxId()));
        return ResponseEntity.ok(ResultUtil.successJson());
    }



}
