package com.geeke.sys.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.CrudController;
import com.geeke.sys.entity.Action;
import com.geeke.sys.service.RecycleService;
import com.geeke.utils.ResultUtil;

/**
 * Nhật ký thao tácController
 * @author lys   taycông việcbiênviết
 * @version 2019-12-12
 */
@RestController
@RequestMapping(value = "/sys/recycle")
public class RecycleController extends CrudController<RecycleService, Action> {

	@Autowired
	protected RecycleService recycleService;

	@Override
	protected RecycleService getService() {
		return recycleService;
	}

    @PostMapping(value = "restor")
    public ResponseEntity<JSONObject> restor(@RequestBody Action entity) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC Y2f: tai khoan 0-resource bi 403.
        // DS2: restor la thao tac admin (chi trang audit admin goi,
        // gate bang permission.restore; action:restore chi
        // 2000/2001/4000 giu). Truoc day chi can bat ky resource nao
        // cung restore duoc dong cua tenant minh neu DELETED action
        // mang company (DS1 chi chan dong la va dong NULL) — san nay
        // dong cua lai, khong phu thuoc vao company cua dong audit.
        if (!hasSessionPermission("action:restore")) { return forbidden(); }
        int rows = recycleService.restore(entity);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

    // DS2: khoa TOAN BO write generic tren /sys/recycle (mau iter67).
    // Controller nay thua ke CrudController nen /sys/recycle/save,
    // delete, bulk* la duong vong quanh khoa write cua /sys/action:
    // khong co frontend nao goi chung (api/sys/recycle.js chi export
    // restor), khong co luong hop le nao ghi dong Action bang tay
    // (ghi noi bo di qua DbActionSaver/service, khong qua controller).
    // Khoa ca voi admin (nhu iter67) — san 403 phu ca duong bulk
    // (ActionDao thieu statement nen hien 500, nhung san phai dung
    // truoc mapper).
    @Override
    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@Valid @RequestBody Action entity) {
        return forbidden();
    }

    @Override
    @PostMapping(value = "delete")
    public ResponseEntity<JSONObject> delete(@RequestBody Action entity) {
        return forbidden();
    }

    @Override
    @PostMapping(value = "bulkInsert")
    public ResponseEntity<JSONObject> bulkInsert(@RequestBody List<Action> entitys) {
        return forbidden();
    }

    @Override
    @PostMapping(value = "bulkUpdate")
    public ResponseEntity<JSONObject> bulkUpdate(@RequestBody List<Action> entitys) {
        return forbidden();
    }

    @Override
    @PostMapping(value = "bulkDelete")
    public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<Action> entitys) {
        return forbidden();
    }
}