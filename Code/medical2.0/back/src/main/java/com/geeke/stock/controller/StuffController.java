package com.geeke.stock.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.CrudController;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.common.service.ServiceException;
import com.geeke.stock.entity.MedicalProject;
import com.geeke.stock.entity.Stuff;
import com.geeke.stock.service.StuffService;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.geeke.utils.constants.ErrorEnum;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Thông tin vật tưController
 * @author txl
 * @version 2022-06-22
 */
@RestController
@RequestMapping(value = "/stock/stuff")
public class StuffController extends CrudController<StuffService, Stuff> {

	@Autowired
	protected StuffService stuffService;

	@Override
	protected StuffService getService() {
		return stuffService;
	}

    @PostMapping(value = {"list", ""})
    public ResponseEntity<JSONObject> listPage(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        Page<Stuff> result = stuffService.listPages(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @PostMapping(value = {"listForMedical", ""})
    public ResponseEntity<JSONObject> listForMedical(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        List<MedicalProject> result = stuffService.listPageForMedical(searchParams.getParams(),searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @PostMapping(value = "listAll")
    public ResponseEntity<JSONObject> listAll(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        List<Stuff> result = stuffService.listAlls(searchParams.getParams(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    // DS79: them @Valid de cac @NotNull/@Length tren Stuff (status,
    // pinyinCode, name, packNumber, minUnit, packUnit) fail-closed 400
    // voi message tieng Viet thay vi loi DB lo ra kem SQL.
    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@Valid @RequestBody Stuff entity) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        String id = null;
        try {
            id = stuffService.save(entity).getId();
        } catch (ServiceException e) {
            // Iteration 65 item 6: Stuff khong co nhanh trung lap bang
            // ServiceException (trung lap nem NegativeArraySizeException, xem
            // duoi) nen moi ServiceException o day deu la loi nghiep vu that
            // — dac biet guard tenant — tra 400 dung message, khong gan nhan
            // trung lap. Khong doi hanh vi guard, chi doi tin hieu.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultUtil.errorJson(ErrorEnum.E_400, businessMessage(e)));
        } catch (NegativeArraySizeException e) {
            // Trung lap that (StuffService.repeat) giu tin hieu cu 200+50001;
            // NegativeArraySizeException khac message thi tra 400.
            if ("Thông tin vật tư trùng lặp".equals(e.getMessage())) {
                return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_50001, "Thông tin vật tư trùng lặp"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultUtil.errorJson(ErrorEnum.E_400, businessMessage(e)));
        } catch (Exception e) {
            // Trung lap that o tang DB (unique index) van 50001; moi loi khac
            // (vi pham NOT NULL, ...) tra 400 thay vi 200+50001 sai nhan.
            if (isDuplicateKey(e)) {
                return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_50001, "Thông tin vật tư trùng lặp"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultUtil.errorJson(ErrorEnum.E_400, businessMessage(e)));
        }
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    /**
     * Iteration 65 item 6: message tra ve cho loi 400 — lay message that cua
     * exception, fallback ve message mac dinh cua E_400 khi message rong.
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
     * Iteration 39 P1: nhu DrugController — 2 endpoint updateAll ghi hang
     * loat theo company_id cua payload; chan company thieu hoac khac session
     * tenant bang 400 truoc khi ghi, session rong giu hanh vi cu.
     */
    private boolean isForeignCompany(String payloadCompanyId) {
        if (StringUtils.isBlank(payloadCompanyId)) { return true; }
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) { return false; }
        return !tenantId.equals(payloadCompanyId);
    }

    @PostMapping(value = "updateAllIndate")
    public ResponseEntity<JSONObject> updateAllIndate(@RequestBody Stuff stuff){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        String companyId = stuff.getCompany() == null ? null : stuff.getCompany().getId();
        if (isForeignCompany(companyId)) { return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultUtil.errorJson(ErrorEnum.E_400)); }
        int i =stuffService.updateAllIndate(stuff.getIndate(),companyId);
        return ResponseEntity.ok(ResultUtil.successJson(i));
    }

    @PostMapping(value = "updateAllInventory")
    public ResponseEntity<JSONObject> updateAllInventory(@RequestBody Stuff stuff){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        String companyId = stuff.getCompany() == null ? null : stuff.getCompany().getId();
        if (isForeignCompany(companyId)) { return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultUtil.errorJson(ErrorEnum.E_400)); }
        int i =stuffService.updateAllInventory(stuff.getInventoryFloor(),companyId);
        return ResponseEntity.ok(ResultUtil.successJson(i));
    }

    @PostMapping(value = "inventory")
    public ResponseEntity<JSONObject> inventory(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        List<Stuff> result = stuffService.inventory(searchParams.getParams(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @PostMapping("/uploadExcel")
    @ApiOperation("excel")
    public ResponseEntity taskUploadExcel(@RequestParam(value = "file") MultipartFile file) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        List<?> excel = null;
        try {
            excel = stuffService.excel(file);
        } catch (Exception e) {
            return ResponseEntity.ok(ResultUtil.warningJson(ErrorEnum.E_50001, e.getMessage()));
        }
        return ResponseEntity.ok(ResultUtil.successJson(excel));
    }

    /**
     * chỉTruy vấntenant của Vật tư
     *
     * @param searchParams
     * @return
     */
    @PostMapping(value = {"listByInstitution"})
    public ResponseEntity<JSONObject> listByInstitution(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        Page<Stuff> result = stuffService.listByInstitution(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * chỉTruy vấnnàycông ty của Vật tư
     *
     * @param searchParams
     * @return
     */
    @PostMapping(value = {"listByCompany"})
    public ResponseEntity<JSONObject> listByCompany(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        Page<Stuff> result = stuffService.listByCompany(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * tenantVật tưđồng bộđếnPhòng khám
     *
     * @param entitys
     * @return
     */
    @PostMapping(value = "syncToClinic")
    public ResponseEntity<JSONObject> syncToClinic(@RequestBody List<Stuff> entitys) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        List<String> ids = stuffService.syncToClinic(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(ids));
    }
}
