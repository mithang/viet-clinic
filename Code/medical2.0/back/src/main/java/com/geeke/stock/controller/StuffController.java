package com.geeke.stock.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.CrudController;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.stock.entity.MedicalProject;
import com.geeke.stock.entity.Stuff;
import com.geeke.stock.service.StuffService;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.geeke.utils.constants.ErrorEnum;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@RequestBody Stuff entity) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        String id = null;
        try {
            id = stuffService.save(entity).getId();
        } catch (Exception e) {
            return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_50001, "Thông tin vật tư trùng lặp"));
        }
        return ResponseEntity.ok(ResultUtil.successJson(id));
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
