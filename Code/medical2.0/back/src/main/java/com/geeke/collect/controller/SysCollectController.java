package com.geeke.collect.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSONObject;
import com.geeke.collect.entity.SysCollect;
import com.geeke.collect.service.SysCollectService;
import com.geeke.common.controller.CrudController;
import com.geeke.utils.ResultUtil;

/**
 * Mục yêu thíchController
 * @author szy
 * @version 2021-07-28
 */
@RestController
@RequestMapping(value = "/collect/sysCollect")
public class SysCollectController extends CrudController<SysCollectService, SysCollect> {

    @Autowired
    protected SysCollectService sysCollectService;

    @Override
    protected SysCollectService getService() {
        return sysCollectService;
    }

    /**
     * Theo người dùngidTruy vấnyêu thíchDanh sách
     */
    @GetMapping(value = "listAllByUserId/{userId}")
    public ResponseEntity<JSONObject> listAllByUserId(@PathVariable("userId") String userId) {
        List<SysCollect> result = sysCollectService.listAllByUserId(userId);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * di độngyêu thíchkích hoạt
     */
    @PostMapping(value = "updateBatch/{userId}")
    public ResponseEntity<JSONObject> updateBatch(@RequestBody List<SysCollect> sysCollectList, @PathVariable String userId) {
        List<SysCollect> result = sysCollectService.updateBatch(sysCollectList, userId);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * Xóayêu thích(giữ lại @DeleteMapping Để tương thích với lời gọi từ frontend)
     */
    @Override
    @DeleteMapping(value = "delete")
    public ResponseEntity<JSONObject> delete(@RequestBody SysCollect entity) {
        return super.delete(entity);
    }
}