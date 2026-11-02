package com.geeke.gen.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.gen.entity.GenTable;
import com.geeke.gen.service.GenTableService;
import com.geeke.sys.controller.BaseController;
import com.geeke.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Quản lý bảng nghiệp vụController
 * @author lys
 * @version 2021-08-26
 */
@RestController
@RequestMapping(value = "/gen/genTable")
public class GenTableController extends BaseController {

	@Autowired
	private GenTableService genTableService;
	
    @GetMapping("/{id}")
    public ResponseEntity<JSONObject> getById(@PathVariable("id") String id) {
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        GenTable entity = genTableService.get(id);
        return ResponseEntity.ok(ResultUtil.successJson(entity));
    }
    
    @PostMapping(value = {"list", ""})
    public ResponseEntity<JSONObject> listPage(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        Page<GenTable> result = genTableService.listPage(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }
    
    @PostMapping(value = "listAll")
    public ResponseEntity<JSONObject> listAll(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        List<GenTable> result = genTableService.listAll(searchParams.getParams(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@RequestBody GenTable entity) {
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        String id = genTableService.save(entity).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }
  
    @PostMapping(value = "delete")
    public ResponseEntity<JSONObject> delete(@RequestBody GenTable entity) {
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        int rows = genTableService.delete(entity);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

    @PostMapping(value = "bulkInsert")
    public ResponseEntity<JSONObject> bulkInsert(@RequestBody List<GenTable> entitys) {
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        List<String> ids = genTableService.bulkInsert(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(ids));
    }
    
    @PostMapping(value = "bulkUpdate")
    public ResponseEntity<JSONObject> bulkUpdate(@RequestBody List<GenTable> entitys) {
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        List<String> ids = genTableService.bulkUpdate(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(ids));
    }
    
    @PostMapping(value = "bulkDelete")
    public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<GenTable> entitys) {
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        int rows = genTableService.bulkDelete(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

    @PostMapping(value = "importJson")
    public ResponseEntity<JSONObject> importJson(@RequestBody GenTable entity) {
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        String id = genTableService.importJson(entity).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }
}