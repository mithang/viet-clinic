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

    // DS115 B: khong co endpoint save/delete -- day la trinh duyet
    // READ-ONLY tren information_schema.tables (bang he thong chi doc
    // cua MySQL): moi INSERT/UPDATE/DELETE deu loi, va DS94 chi map
    // loi SQL do thanh 400 nen endpoint chet van tra 400 nhu loi
    // dau vao. Khong co menu/router/frontend nao goi ghi o day;
    // duong ghi dung cho bang code-gen la GenTableService (/gen/genTable,
    // DS99 fail-closed). getById nhan TEN BANG lam id (identity that
    // cua schema browser) vi information_schema.tables khong co cot id.
}