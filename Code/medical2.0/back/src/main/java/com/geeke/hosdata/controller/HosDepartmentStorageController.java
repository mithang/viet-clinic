package com.geeke.hosdata.controller;


import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.geeke.common.constants.BizConstants;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Parameter;
import com.geeke.common.service.ServiceException;
import com.geeke.hosdata.constant.ApiUrl;
import com.geeke.hosdata.entity.HisDrugs;
import com.geeke.hosdata.entity.HisDrugsStorage;
import com.geeke.hosdata.util.HttpUtil;
import com.geeke.sys.controller.BaseController;
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
public class HosDepartmentStorageController extends BaseController {

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
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
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
                                        .collect(Collectors.toList())      // thutậpĐếnmộtCáimới của Danh sách
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
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        StorageEvt storageEvt = new StorageEvt();
        SupplierStorage supplierStorage = new SupplierStorage();
        List<SupplierStock> supplierStockList = new ArrayList<>();
        List<String> alreadyImportedIds = new ArrayList<>();
        String companyId = SessionUtils.getUserJson().getString("companyId");
        int totalNumber = 0; // Khởi tạotổng số là 0
        for (int i = 0; i < hisDrugsStorages.size(); i++){
            // DS25: moi dong HIS duoc kiem chung TRUOC moi ghi — truoc day
            // dong khong dung duoc lam ca lo 500 ma khong noi ro dong nao
            // (saves/savesTo chi chay sau vong lap nen nem o day la
            // fail-closed, khong ghi mot phan): ma thuoc chua dong bo vao
            // kho cua phong kham (getByCode loc code + company session tra
            // null) -> drug.getPreparation() NPE; ckzhyz null/khong phai so
            // -> NPE/NumberFormatException; he so > 1 ma preparation null
            // hoac khong co '.' -> NPE/StringIndexOutOfBounds; sl/pfj null
            // -> NPE khi unbox. Ma chua dong bo la truong hop BINH THUONG
            // (luong getHosDrugsStorage cung dong bo thuoc chua co), nen
            // tra 400 ke ten ma de nhan vien dong bo truoc roi duyet lai.
            // Duong hop le khong doi mot byte: cac dong duoi day giu nguyen.
            HisDrugsStorage hisItem = hisDrugsStorages.get(i);
            // DS27: chan ghi trung TRUOC moi kiem chung khac — dong HIS da
            // nhap kho truoc do cho cung company (cung crkmxId) thi bo qua:
            // khong kiem chung lai thuoc (thuoc co the da bi xoa khoi danh
            // muc sau lan nhap truoc, nhung dong da nhap thi khong can
            // kiem chung nua) va khong ghi them dong nao. Dong khong co
            // crkmxId thi khong dedup duoc, nhap nhu truoc.
            if (hisItem.getCrkmxId() != null && !hisItem.getCrkmxId().isEmpty()
                    && supplierStockService.countImportedHisDetail(companyId, hisItem.getCrkmxId()) > 0) {
                alreadyImportedIds.add(hisItem.getCrkmxId());
                continue;
            }
            Drug drug = drugService.getByCode(hisItem.getYpdm(), companyId);
            if (drug == null) {
                throw new ServiceException("Thuốc mã " + hisItem.getYpdm() + " chưa có trong kho của phòng khám, hãy đồng bộ thuốc trước khi duyệt nhập kho");
            }
            int factor;
            try {
                factor = Integer.parseInt(hisItem.getCkzhyz());
            } catch (NumberFormatException | NullPointerException e) {
                throw new ServiceException("Hệ số chuyển đổi của thuốc mã " + hisItem.getYpdm() + " không hợp lệ: " + hisItem.getCkzhyz());
            }
            if (hisItem.getSl() == null || hisItem.getPfj() == null) {
                throw new ServiceException("Số lượng hoặc đơn giá của thuốc mã " + hisItem.getYpdm() + " bị trống");
            }
            if (factor > 1 && (drug.getPreparation() == null || !drug.getPreparation().contains("."))) {
                throw new ServiceException("Quy cách của thuốc mã " + hisItem.getYpdm() + " không có dạng số thập phân nên không đổi được đơn vị chuẩn");
            }
            SupplierStock supplierStock = new SupplierStock();
            //sinhsảnLô
            supplierStock.setBatchNo(hisDrugsStorages.get(i).getPc());
            // DS27: luu id duy nhat cua dong HIS de lan dong bo sau nhan ra dong da nhap
            supplierStock.setHisCrkmxId(hisItem.getCrkmxId());
            //Thông tin thuốc
            supplierStock.setDrug(drug);
            //Vật tưMặc định là null
            supplierStock.setStuff(new Stuff());
            //Kiểm tra số lượng theo bản viện là đơn vị nhỏ nhất hay đơn vị chuẩn
            if(factor > 1){
                //tiêu chuẩnĐơn vị
                supplierStock.setNumber(hisDrugsStorages.get(i).getSl() * Integer.parseInt( (drug.getPreparation().substring(0,drug.getPreparation().indexOf(".")))));
                supplierStock.setLeastBid(hisDrugsStorages.get(i).getPfj().divide(new BigDecimal(factor),2,BigDecimal.ROUND_HALF_UP));
            }else {
                supplierStock.setNumber(hisDrugsStorages.get(i).getSl());
                supplierStock.setLeastBid(hisDrugsStorages.get(i).getPfj());
            }
            //Phòng khámid
            Company company = new Company();
            company.setId(companyId);
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
        // DS27: chi ghi phieu khi con dong moi — neu tat ca cac dong deu
        // da nhap truoc do thi khong tao phieu rong (savesTo se insert mot
        // supplier_storage tong 0 ma khong co dong hang nao).
        if (!supplierStockList.isEmpty()) {
            //Bắt đầuThêmTồn khoThông tin
            supplierStorage.setNumber(totalNumber); // Sẽ cộng dồnthêm của Kết quảgánGiá trịchosupplierStorage
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
        }
        // DS26: callback HIS chi la thong bao SAU KHI nhap kho da commit
        // (truoc day mot callback nem lam ca request 500 DU DA GHI XONG).
        // Nay moi item duoc callback rieng trong try/catch: loi callback
        // chi ghi WARN + danh dau, khong doi trang thai nhap kho thanh
        // that bai; tra 100 kem co hisCallbackFailed de UI canh bao thay
        // vi bao thanh cong tron ven. Tra ve false (HIS tra != 200) cung
        // tinh la that bai callback, khong phai chi khi nem exception.
        // DS27: ghi trung khi dong bo lai da duoc chan o tren (bo qua dong
        // co crkmxId da nhap); callback van chay cho ca dong da nhap de
        // HIS co co hoi danh dau da duyet.
        List<String> callbackFailed = new ArrayList<>();
        hisDrugsStorages.forEach(item -> {
            try {
                Boolean ok = httpUtil.UpdateHosData(ApiUrl.HOS_UPDATE_DRUGS_STORAGE_STATUS, item.getCrkmxId());
                if (!Boolean.TRUE.equals(ok)) {
                    callbackFailed.add(item.getCrkmxId());
                    logger.warn("HIS callback trả về không phải 200 cho crkmxId {}", item.getCrkmxId());
                }
            } catch (Exception e) {
                callbackFailed.add(item.getCrkmxId());
                logger.warn("HIS callback thất bại cho crkmxId {} (nhập kho vẫn giữ nguyên): {}", item.getCrkmxId(), e.getMessage());
            }
        });
        JSONObject data = new JSONObject();
        data.put("hisCallbackFailed", !callbackFailed.isEmpty());
        if (!callbackFailed.isEmpty()) {
            data.put("hisCallbackFailedIds", callbackFailed);
        }
        // DS27: cac dong HIS da nhap truoc do, da bo qua de tranh ghi
        // trung. Callback van chay cho ca cac dong nay (vong lap tren):
        // neu HIS van chua danh dau da duyet thi day la co hoi bao lai.
        data.put("hisAlreadyImported", !alreadyImportedIds.isEmpty());
        if (!alreadyImportedIds.isEmpty()) {
            data.put("hisAlreadyImportedIds", alreadyImportedIds);
        }
        return ResponseEntity.ok(ResultUtil.successJson(data));
    }



}
