package com.geeke.stock.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.CrudController;
import com.geeke.common.service.ServiceException;
import com.geeke.medicareutils.config.MedicareConfigProperties;
import com.geeke.medicareutils.service.MdInventoryService;
import com.geeke.org.entity.Company;
import com.geeke.stock.entity.DispensingEvt;
import com.geeke.stock.entity.Drug;
import com.geeke.stock.entity.StorageEvt;
import com.geeke.stock.entity.SupplierStock;
import com.geeke.stock.service.DrugService;
import com.geeke.stock.service.SupplierStockService;
import com.geeke.stock.service.SupplierStorageService;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Tồn kho nhà cung cấpController
 * @author txl
 * @version 2022-06-09
 */
@RestController
@RequestMapping(value = "/stock/supplierStock")
public class SupplierStockController extends CrudController<SupplierStockService, SupplierStock> {

    @Autowired
    protected SupplierStockService supplierStockService;

    @Autowired
    private SupplierStorageService supplierStorageService;

    @Lazy
    @Autowired
    private DrugService drugService;

    @Autowired
    private MedicareConfigProperties medicareConfigProperties;

    @Lazy
    @Autowired
    private MdInventoryService mdInventoryService;

    @Override
    protected SupplierStockService getService() {
        return supplierStockService;
    }

    @GetMapping("/stock/{sid}")
    public ResponseEntity<JSONObject> getByStorageId(@PathVariable("sid") String sid) {
        if (!hasAnySessionPermission()) { return forbidden(); }
        List<SupplierStock> byStorageId = supplierStockService.getByStorageId(sid);
        return ResponseEntity.ok(ResultUtil.successJson(byStorageId));
    }

    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@RequestBody SupplierStock entity) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        logger.info("dayin:", entity);
        // L5 (iteration 59): ep tenant theo session cho NHANH INSERT
        // (id trong), mau K1/K2, truoc moi ghi. Truoc day payload id
        // trong + company tenant la tao that dong ton kho o tenant la
        // (nhanh insert cua CrudService.save khong verify tenant; tai
        // hien tools/iter59-l5.py case A). Huong ep (khong chan 400)
        // vi: form that supplierStockForm.vue lay company_List tu
        // listCompanyAll — probe live cho testfull chi tra [co so 297,
        // OWN 309], company la 4464 khong co trong dropdown — va cac
        // dropdown cung form (supplier/drug/stuff/phieu nhap/factory)
        // deu loc cung company_id = currentUser.company.id, nen tenant
        // hop le duy nhat cua dong moi la session tenant; ep khong doi
        // hanh vi form hop le nao. Nhanh UPDATE KHONG ep: guard
        // stored-row cua CrudService.save van chan payload company la
        // (case C van 400 + dong van OWN). Session rong/"null" giu
        // fallback body cu qua sessionTenantOr (luong noi bo khong
        // session). Chi duong save don nay doi; inStorage /
        // inStorageByCompany / updateStock / auditStorage goi service
        // truc tiep, ngoai pham vi L5, khong anh huong.
        if (StringUtils.isBlank(entity.getId())) {
            String tenant = sessionTenantOr(entity.getCompany() == null ? null : entity.getCompany().getId());
            if (tenant != null) {
                entity.setCompany(new Company(tenant));
            }
        }
        String id = supplierStockService.save(entity).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    // L5 (iteration 59): mau K2. Body khong duoc dieu khien tenant nua:
    // session rong/"null" moi giu gia tri body cu.
    private String sessionTenantOr(String fallback) {
        String tenant = SessionUtils.getLoginTenantId();
        return (StringUtils.isNotBlank(tenant) && !"null".equals(tenant)) ? tenant : fallback;
    }

    @PostMapping(value = "inStorage")
    public ResponseEntity<JSONObject> inStorage(@RequestBody StorageEvt storageEvt) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        // DS166 C: vong tra cuu thuoc ben duoi deref item.getDrug()
        // .getGoodsName() khong guard -- list null / item null / drug null
        // -> NPE 500 truoc moi ghi. Fail-closed 400 truoc moi buoc, ke ca
        // khi storageEvt / supplierStorage null (savesTo() NPE o service,
        // nhung chan som o controller cho ro rang).
        if (storageEvt == null || storageEvt.getSupplierStorage() == null
                || storageEvt.getSupplierStockList() == null) {
            throw new ServiceException("Dữ liệu nhập kho không được để trống");
        }
        for (SupplierStock item : storageEvt.getSupplierStockList()) {
            if (item == null || item.getDrug() == null) {
                throw new ServiceException("Dòng nhập kho thiếu thông tin thuốc");
            }
        }
        // M4 (iteration 60): ep tenant theo session cho phieu + tung dong
        // nhap kho, mau L5 (save don). Ly do: savesTo() luu phieu/dong
        // qua CrudService.save nhanh INSERT (khong verify tenant) nen
        // payload company la ghi thang dong vao tenant la — probe
        // iter60-m4 V4/V6: dong drug-null + company OTHER -> 200, phieu
        // + dong hien o OTHER. Truong hop dong co drug that hien bi
        // chan 400 nho co che ngau nhien (drugTemp bi doi company sang
        // OTHER roi initStockTo get lai trong cung transaction, cache
        // MyBatis tra ve dung object da doi nen verify nem 400 truoc
        // khi ghi) — ep o controller lam cho chan nay xac dinh, khong
        // dua vao cache. Form that (supplierStorage + auditWarehousing
        // addstorage) gui company_id = currentUser.company.id nen ep
        // khong doi hanh vi UI hop le nao. Session rong/"null" giu
        // payload cu (luong noi bo khong session). Chi duong inStorage
        // nay doi; inStorageByCompany KHONG ep vi form that cua no
        // (distributionDrugs addstorage) gui otherInfo.companyId do
        // nguoi dung chon — nhap kho cho phong kham duoc chi dinh la
        // dung cross-company theo thiet ke, khong phai lo hong.
        String tenant = SessionUtils.getLoginTenantId();
        if (StringUtils.isNotBlank(tenant) && !"null".equals(tenant)) {
            if (storageEvt.getSupplierStorage() != null) {
                storageEvt.getSupplierStorage().setCompany(new Company(tenant));
            }
            if (storageEvt.getSupplierStockList() != null) {
                storageEvt.getSupplierStockList().forEach(item -> {
                    if (item != null) {
                        item.setCompany(new Company(tenant));
                    }
                });
            }
        }
        //Khi nhập kho hàng loạt mà không có thông tin thuốc thì thêm thông tin thuốc
        storageEvt.getSupplierStockList().forEach(item -> {
            Drug drug = drugService.getByNameAndPrice(item.getDrug().getGoodsName(),item.getRetailPrice());
            if(drug != null){
                item.setDrug(drug);
            }
          }
         );
        supplierStockService.checkStockDateOrder(storageEvt); // DS261: han su dung truoc ngay san xuat -> 400 truoc moi ghi
        supplierStockService.checkProduceDateNotFuture(storageEvt); // DS265: ngay san xuat o tuong lai -> 400 truoc moi ghi
        supplierStockService.saves(storageEvt);
        supplierStockService.savesTo(storageEvt);
        if("true".equals(medicareConfigProperties.getCheck())){
            //mởBHYTgọi Thuốc tạmvàMặc định điềuđiều chuyểnnhập kho
            if(storageEvt.getSupplierStorage().getBreed().equals(1)){
                mdInventoryService.updateInventoryList_3502A(storageEvt,"101");
            }
        }
        return ResponseEntity.ok(ResultUtil.successJson());
    }

    @PostMapping(value = "cancel")
    public ResponseEntity<JSONObject> cancel(@RequestBody String id) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        supplierStorageService.cancel(id);
        return ResponseEntity.ok(ResultUtil.successJson());
    }

    @PostMapping(value = "updateStock")
    public ResponseEntity<JSONObject> updateStock(@RequestBody DispensingEvt dispensingEvt) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        //Phát thuốc, Trả thuốc
        if (null != dispensingEvt.getDispensingDetailEvtList() && !dispensingEvt.getDispensingDetailEvtList().isEmpty()) {
            supplierStockService.updateStock(dispensingEvt);
        }
        if("true".equals(medicareConfigProperties.getCheck())){
            //mởBHYTgọi phát thuốcTrả thuốcSửaTồn kho
            mdInventoryService.updateInventoryList_3502A(dispensingEvt);

        }
        return ResponseEntity.ok(ResultUtil.successJson());
    }


    /**
     * chochỉchỉ định của Phòng khámtiến hành dòngphát thuốc
     *
     * @param storageEvt
     * @return
     */
    @PostMapping(value = "inStorageByCompany")
    public ResponseEntity<JSONObject> inStorageByCompany(@RequestBody StorageEvt storageEvt) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        supplierStockService.checkStockDateOrder(storageEvt); // DS261: han su dung truoc ngay san xuat -> 400 truoc moi ghi
        supplierStockService.checkProduceDateNotFuture(storageEvt); // DS265: ngay san xuat o tuong lai -> 400 truoc moi ghi
        supplierStockService.saves(storageEvt);
        supplierStockService.savesToAudit(storageEvt);
        return ResponseEntity.ok(ResultUtil.successJson());
    }


    /**
     * Phòng khámxét duyệtnhập kho
     *
     * @return
     */
    @PostMapping(value = "auditStorage")
    public ResponseEntity<JSONObject> auditStorage(@RequestBody StorageEvt storageEvt) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        supplierStockService.auditStorage(storageEvt);
        return ResponseEntity.ok(ResultUtil.successJson());
    }

}
