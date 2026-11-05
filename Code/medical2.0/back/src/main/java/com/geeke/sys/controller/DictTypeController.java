package com.geeke.sys.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.CrudController;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.sys.entity.DictType;
import com.geeke.sys.service.DictTypeService;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.constants.ErrorEnum;

/**
 * Loại từ điểnController
 * @author lys
 * @version 2021-08-20
 *
 * Kiểm tra quyền theo cùng cơ chế fail-closed đã dùng cho patient/role
 * listPage (BaseController.hasSessionPermission, thiếu quyền -> 403 E_502).
 * Các endpoint dưới đây chỉ được trang quản trị từ điển và trang cấu hình
 * thuộc tính hệ thống gọi; dropdown nghiệp vụ ở các trang chính không dùng
 * dictType/list hay listAll mà dùng dictItem/listByCode (lookup bắt buộc,
 * không có resource riêng trong DB nên cố tình không chặn ở vòng này).
 */
@RestController
@RequestMapping(value = "/sys/dictType")
public class DictTypeController extends CrudController<DictTypeService, DictType> {

	@Autowired
	protected DictTypeService dictTypeService;

	@Override
	protected DictTypeService getService() {
		return dictTypeService;
	}

    @Override
    @PostMapping(value = {"list", ""})
    public ResponseEntity<JSONObject> listPage(@RequestBody SearchParams searchParams) {
        if (!hasSessionPermission("dictType:list")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        Page<DictType> result = getService().listPage(searchParams.getParams(), searchParams.getOffset(),
                searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @Override
    @PostMapping(value = "listAll")
    public ResponseEntity<JSONObject> listAll(@RequestBody SearchParams searchParams) {
        if (!hasSessionPermission("dictType:list")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        List<DictType> result = getService().listAll(searchParams.getParams(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @Override
    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@Valid @RequestBody DictType entity) {
        if (!hasSessionPermission("dictType:create") && !hasSessionPermission("dictType:update")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        String id = getService().save(entity).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    @Override
    @PostMapping(value = "delete")
    public ResponseEntity<JSONObject> delete(@RequestBody DictType entity) {
        if (entity == null) {
            return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_400));
        }
        if (!hasSessionPermission("dictType:delete")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        int rows = getService().delete(entity);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

    // DS3: san bulk — controller nay da san save/delete/list don
    // bang dictType:* nhung KHONG override bulk, nen 3 endpoint
    // generic (chi can bat ky resource nao) ghi thang vao bang
    // sys_dict_type TOAN CUC (khong co company_id): clinic
    // bulkInsert/bulkUpdate/bulkDelete deu 200 live. Map nhu
    // DictItemController: bulkInsert -> create, bulkUpdate ->
    // update, bulkDelete -> delete. DictTypeDao CO du 3 statement
    // bulk (khac cac duong bulk chet vi thieu mapper) nen san nay
    // la chan that, khong phai chan truoc mapper.
    @Override
    @PostMapping(value = "bulkInsert")
    public ResponseEntity<JSONObject> bulkInsert(@RequestBody List<DictType> entitys) {
        if (!hasSessionPermission("dictType:create")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        List<String> ids = getService().bulkInsert(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(ids));
    }

    @Override
    @PostMapping(value = "bulkUpdate")
    public ResponseEntity<JSONObject> bulkUpdate(@RequestBody List<DictType> entitys) {
        if (!hasSessionPermission("dictType:update")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        List<String> ids = getService().bulkUpdate(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(ids));
    }

    @Override
    @PostMapping(value = "bulkDelete")
    public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<DictType> entitys) {
        if (!hasSessionPermission("dictType:delete")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        int rows = getService().bulkDelete(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

    @PostMapping(value = "importJson")
    public ResponseEntity<JSONObject> importJson(@RequestBody DictType entity) {
        if (!hasSessionPermission("dictType:create") && !hasSessionPermission("dictType:update")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        String id = dictTypeService.importJson(entity).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }
}
