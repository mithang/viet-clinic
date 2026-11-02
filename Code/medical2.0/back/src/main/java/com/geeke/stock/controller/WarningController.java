package com.geeke.stock.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.stock.entity.Drug;
import com.geeke.stock.entity.Stuff;
import com.geeke.stock.entity.SupplierStock;
import com.geeke.stock.service.WarningService;
import com.geeke.sys.controller.BaseController;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.constants.ErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/stock/warning")
public class WarningController extends BaseController {
    @Autowired
    private WarningService warningService;

    @PostMapping("getDrugIndateWarning")
    public ResponseEntity<JSONObject> getDrugIndateWarning(@RequestBody SearchParams searchParams){
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        Page<SupplierStock> indateWarning = warningService.getDrugIndateWarning(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(indateWarning));
    }

    @PostMapping("getDrugInventoryWarning")
    public ResponseEntity<JSONObject> getDrugInventoryWarning(@RequestBody SearchParams searchParams){
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        Page<Drug> drugInventoryWarning = warningService.getDrugInventoryWarning(searchParams);
        return ResponseEntity.ok(ResultUtil.successJson(drugInventoryWarning));
    }

    @PostMapping("getStuffIndateWarning")
    public ResponseEntity<JSONObject> getStuffIndateWarning(@RequestBody SearchParams searchParams){
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        Page<SupplierStock> indateWarning = warningService.getStuffIndateWarning(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(indateWarning));
    }

    @PostMapping("getStuffInventoryWarning")
    public ResponseEntity<JSONObject> getStuffInventoryWarning(@RequestBody SearchParams searchParams){
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        Page<Stuff> stuffInventoryWarning = warningService.getStuffInventoryWarning(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(stuffInventoryWarning));
    }

    @PostMapping("exportTable")
    public void exportTable(@RequestBody SearchParams searchParams, HttpServletResponse response, HttpServletRequest request) throws IOException {
        if (!hasAnySessionPermission()) {
            // Ham void khong tra ResponseEntity duoc: set status 403 roi return truoc khi service ghi file.
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        warningService.exportTable(searchParams,response);
    }
}
