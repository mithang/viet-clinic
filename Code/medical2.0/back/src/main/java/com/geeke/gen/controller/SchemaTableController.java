package com.geeke.gen.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.gen.entity.GenTable;
import com.geeke.gen.service.SchemaTableService;
import com.geeke.sys.controller.BaseController;
import com.geeke.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Điều khiển bảng hệ thốngController
 * @author lys
 * @version 2019-09-13
 */
@RestController
@RequestMapping(value = "/gen/schemaTable")
public class SchemaTableController extends BaseController {

	@Autowired
	private SchemaTableService schemaTableService;
	
	@GetMapping("/{id}")
    public ResponseEntity<JSONObject> getById(@PathVariable("id") String id) {
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
		GenTable entity = schemaTableService.get(id);
        return ResponseEntity.ok(ResultUtil.successJson(entity));
    }
	
	@PostMapping(value = {"list", ""})
    public ResponseEntity<JSONObject> listPage(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        Page<GenTable> result = schemaTableService.listPage(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }
    
    @PostMapping(value = "listAll")
    public ResponseEntity<JSONObject> listAll(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        List<GenTable> result = schemaTableService.listAll(searchParams.getParams(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }   

    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@RequestBody GenTable entity) {
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
    	String id = schemaTableService.save(entity).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }
  
    @PostMapping(value = "delete")
    public ResponseEntity<JSONObject> delete(@RequestBody GenTable entity) {
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        int rows = schemaTableService.delete(entity);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

	
}