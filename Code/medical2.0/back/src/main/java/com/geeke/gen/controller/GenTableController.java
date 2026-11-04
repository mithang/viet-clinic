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
        // ITER67 item 1 (8c): gen_table la metadata codegen toan cuc, khong
        // co cot company nen khong guard tenant duoc — san theo thao tac,
        // theo mau DictTypeController: save phu ca create lan update.
        if (!hasSessionPermission("genTable:create") && !hasSessionPermission("genTable:update")) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        String id = genTableService.save(entity).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    @PostMapping(value = "delete")
    public ResponseEntity<JSONObject> delete(@RequestBody GenTable entity) {
        // ITER67 item 1 (8c): san theo thao tac, thay hasAnySessionPermission.
        if (!hasSessionPermission("genTable:delete")) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        int rows = genTableService.delete(entity);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

    @PostMapping(value = "bulkInsert")
    public ResponseEntity<JSONObject> bulkInsert(@RequestBody List<GenTable> entitys) {
        // ITER67 item 1 (8c): san theo thao tac, thay hasAnySessionPermission.
        if (!hasSessionPermission("genTable:create")) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        List<String> ids = genTableService.bulkInsert(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(ids));
    }

    @PostMapping(value = "bulkUpdate")
    public ResponseEntity<JSONObject> bulkUpdate(@RequestBody List<GenTable> entitys) {
        // ITER67 item 1 (8c): san theo thao tac, thay hasAnySessionPermission.
        if (!hasSessionPermission("genTable:update")) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        List<String> ids = genTableService.bulkUpdate(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(ids));
    }

    @PostMapping(value = "bulkDelete")
    public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<GenTable> entitys) {
        // ITER67 item 1 (8c): san theo thao tac, thay hasAnySessionPermission.
        if (!hasSessionPermission("genTable:delete")) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        int rows = genTableService.bulkDelete(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

    @PostMapping(value = "importJson")
    public ResponseEntity<JSONObject> importJson(@RequestBody GenTable entity) {
        // ITER67 item 1 (8c): san theo thao tac + phuong an B cho nhanh
        // create chet NPE (gop chung 1 commit voi san, theo bai hoc
        // LargeScreen: sua NPE rieng se mo duong tao metadata dang chet).
        // Can cu shape file export that: trang genTable export bang getById
        // -> JSON.stringify(data) -> genTable.json, luon co id cua dong da
        // ton tai; import-create (id trong) khong co caller that nao.
        // Vi vay import chi cap nhat: id trong -> 400 tuong minh (khong
        // NPE 500, khong tao dong); id co -> can genTable:update.
        if (entity == null || entity.getId() == null || entity.getId().trim().isEmpty()) {
            if (!hasSessionPermission("genTable:create")) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                        .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
            }
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_400,
                            "Import JSON yêu cầu id của bảng đã tồn tại (file export genTable.json luôn có id); không tạo bảng mới qua import"));
        }
        if (!hasSessionPermission("genTable:update")) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        String id = genTableService.importJson(entity).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }
}