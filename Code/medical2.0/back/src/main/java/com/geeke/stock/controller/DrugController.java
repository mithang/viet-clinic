package com.geeke.stock.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.CrudController;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.common.service.ServiceException;
import com.geeke.stock.entity.Drug;
import com.geeke.stock.service.DrugService;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.geeke.utils.constants.ErrorEnum;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;




/**
 * Thông tin thuốcController
 * @author txl
 * @version 2022-06-07
 */
@RestController
@RequestMapping(value = "/stock/drug")
public class DrugController extends CrudController<DrugService, Drug> {

    @Autowired
    protected DrugService drugService;

    @Override
    protected DrugService getService() {
        return drugService;
    }

    @PostMapping(value = {"list", ""})
    public ResponseEntity<JSONObject> listPage(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        Page<Drug> result = drugService.listPages(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        if (result.getRows() != null) {
            //Đặt đơn vị theo vòng lặp, Frontend cần kiểm soát để thuốc được bán lẻ hiển thị đơn vị bán lẻ, Trường hợp không cho phép bán lẻ thì hiển thị đơn vị đóng gói
            result.getRows().forEach(x -> {
                if (x.getIsUnpackSell().equals("1")) {
                    x.setStockUnit(x.getPreparationUnit().getName());
                } else {
                    x.setStockUnit(x.getPack().getName());
                }
            });
        }
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }


    /**
     * chỉTruy vấntenant của Thuốc
     *
     * @param searchParams
     * @return
     */
    @PostMapping(value = {"listByInstitution"})
    public ResponseEntity<JSONObject> listByInstitution(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        Page<Drug> result = drugService.listByInstitution(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        if (result.getRows() != null) {
            //Đặt đơn vị theo vòng lặp, Frontend cần kiểm soát để thuốc được bán lẻ hiển thị đơn vị bán lẻ, Trường hợp không cho phép bán lẻ thì hiển thị đơn vị đóng gói
            result.getRows().forEach(x -> {
                if (x.getIsUnpackSell().equals("1")) {
                    x.setStockUnit(x.getPreparationUnit().getName());
                } else {
                    x.setStockUnit(x.getPack().getName());
                }
            });
        }
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * chỉTruy vấnnàycông ty của Thuốc
     *
     * @param searchParams
     * @return
     */
    @PostMapping(value = {"listByCompany"})
    public ResponseEntity<JSONObject> listByCompany(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        Page<Drug> result = drugService.listByCompany(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        if (result.getRows() != null) {
            //Đặt đơn vị theo vòng lặp, Frontend cần kiểm soát để thuốc được bán lẻ hiển thị đơn vị bán lẻ, Trường hợp không cho phép bán lẻ thì hiển thị đơn vị đóng gói
            result.getRows().forEach(x -> {
                if (x.getIsUnpackSell().equals("1")) {
                    x.setStockUnit(x.getPreparationUnit().getName());
                } else {
                    x.setStockUnit(x.getPack().getName());
                }
            });
        }
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }


    /**
     * chỉTruy vấnbản thânPhòng khámdưới của Thuốc
     * @param searchParams
     * @return
     */
    @PostMapping(value = "listAll")
    public ResponseEntity<JSONObject> listAll(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        List<Drug> result = drugService.listAllDrug(searchParams.getParams(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @PostMapping(value = "listAllStock")
    public ResponseEntity<JSONObject> listAllStock(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        List<Drug> result = drugService.listAllStock(searchParams.getParams(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @PostMapping(value = "listAllStock2")
    public ResponseEntity<JSONObject> listAllStock2(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        List<Drug> result = drugService.listAllStock2(searchParams.getParams(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@RequestBody Drug entity) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        String id = null;
        try {
            id = drugService.save(entity).getId();
        } catch (ServiceException e) {
            // Iteration 65 item 6: chi trung lap that giu tin hieu cu 200+50001
            // (DrugService.repeat nem ServiceException voi dung message nay);
            // loi nghiep vu khac — dac biet guard tenant — tra 400 dung message,
            // khong gan nhan trung lap. Khong doi hanh vi guard, chi doi tin hieu.
            if ("Thông tin thuốc trùng lặp".equals(e.getMessage())) {
                return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_50001, "Thông tin thuốc trùng lặp"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultUtil.errorJson(ErrorEnum.E_400, businessMessage(e)));
        } catch (Exception e) {
            // Trung lap that o tang DB (unique index) van 50001; moi loi khac
            // (NPE, vi pham NOT NULL, ...) tra 400 thay vi 200+50001 sai nhan.
            if (isDuplicateKey(e)) {
                return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_50001, "Thông tin thuốc trùng lặp"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultUtil.errorJson(ErrorEnum.E_400, businessMessage(e)));
        }
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    /**
     * Iteration 65 item 6: message tra ve cho loi 400 — lay message that cua
     * exception, fallback ve message mac dinh cua E_400 khi message rong
     * (vi du NPE khong co message).
     */
    private String businessMessage(Exception e) {
        String msg = e.getMessage();
        return StringUtils.isBlank(msg) ? ErrorEnum.E_400.getErrorMsg() : msg;
    }

    /**
     * Iteration 65 item 6: nhan dien trung lap that o tang DB qua chuoi cause
     * (DuplicateKeyException cua Spring hoac "Duplicate entry" cua MySQL).
     */
    private boolean isDuplicateKey(Throwable e) {
        while (e != null) {
            if (e instanceof DuplicateKeyException) { return true; }
            String msg = e.getMessage();
            if (msg != null && msg.contains("Duplicate entry")) { return true; }
            e = e.getCause();
        }
        return false;
    }

    /**
     * Iteration 39 P1: 4 endpoint updateAll ghi hang loat theo company_id cua
     * payload, truoc day chi co san hasAnySessionPermission nen token tenant
     * nay gui company tenant khac se ghi de toan bo thuoc/vat tu tenant do.
     * Chan truoc khi ghi: company payload thieu hoac khac session tenant
     * -> 400, khong ghi dong nao. Session rong/"null" (luong noi bo khong
     * session) giu hanh vi cu nhu verifyTenantOwnership. UI that chi gui
     * company: currentUser.company nen hanh vi own khong doi.
     */
    private boolean isForeignCompany(String payloadCompanyId) {
        if (StringUtils.isBlank(payloadCompanyId)) { return true; }
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) { return false; }
        return !tenantId.equals(payloadCompanyId);
    }

    @PostMapping(value = "updateAllIndate")
    public ResponseEntity<JSONObject> updateAllIndate(@RequestBody Drug drug) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        String companyId = drug.getCompany() == null ? null : drug.getCompany().getId();
        if (isForeignCompany(companyId)) { return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultUtil.errorJson(ErrorEnum.E_400)); }
        int i = drugService.updateAllIndate(drug.getIndate(), companyId);
        return ResponseEntity.ok(ResultUtil.successJson(i));
    }

    @PostMapping(value = "updateAllInventory")
    public ResponseEntity<JSONObject> updateAllInventory(@RequestBody Drug drug) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        String companyId = drug.getCompany() == null ? null : drug.getCompany().getId();
        if (isForeignCompany(companyId)) { return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultUtil.errorJson(ErrorEnum.E_400)); }
        int i = drugService.updateAllInventory(drug.getInventoryFloor(), companyId);
        return ResponseEntity.ok(ResultUtil.successJson(i));
    }

    @PostMapping(value = "inventory")
    public ResponseEntity<JSONObject> inventory(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        List<Drug> result = drugService.inventory(searchParams.getParams(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @PostMapping("/uploadExcel")
    @ApiOperation("excel")
    public ResponseEntity taskUploadExcel(@RequestParam(value = "file") MultipartFile file) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        try {
            List<String> error = drugService.excel(file);
            return ResponseEntity.ok(ResultUtil.successJson(error));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultUtil.warningJson(ErrorEnum.E_50001, e.getMessage()));
        }
    }

    /**
     * tenantThuốcđồng bộđếnPhòng khám
     *
     * @param entitys
     * @return
     */
    @PostMapping(value = "syncToClinic")
    public ResponseEntity<JSONObject> syncToClinic(@RequestBody List<Drug> entitys) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        List<String> ids = drugService.syncToClinic(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(ids));
    }


}
