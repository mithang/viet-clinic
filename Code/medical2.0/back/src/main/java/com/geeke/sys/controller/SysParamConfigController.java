package com.geeke.sys.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.CrudController;
import com.geeke.sys.entity.SysParamConfig;
import com.geeke.sys.service.SysParamConfigService;
import com.geeke.utils.ResultUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * Hệ thốngcấu hìnhController
 * @author lc
 */
@RestController
@RequestMapping(value = "/sys/paramConfig")
public class SysParamConfigController extends CrudController<SysParamConfigService, SysParamConfig> {

    @Resource
    protected SysParamConfigService sysParamConfigService;

    @Override
    protected SysParamConfigService getService() {
        return sysParamConfigService;
    }

    @PostMapping(value = "save/list")
    public ResponseEntity<JSONObject> saveList(@RequestBody List<SysParamConfig> entitys) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC Y2f: tai khoan 0-resource bi 403.
        if (CollectionUtil.isNotEmpty(entitys)) {
            // N3e: tien kiem TOAN BO list truoc khi luu dong nao — fail-closed
            // ca list (mau N1): neu chi dua vao service.save tung dong thi
            // list hon hop [own, foreign] se ghi xong dong own roi moi nem
            // 400 o dong la; tien kiem giu ca list nguyen ven.
            entitys.forEach(sysParamConfigService::checkParamWriteTenant);
            entitys.forEach(sysParamConfigService::save);
        }
        return ResponseEntity.ok(ResultUtil.successJson("thao tácThành công"));
    }

    @DeleteMapping(value = "delete")
    public ResponseEntity<JSONObject> delete(@RequestBody SysParamConfig entity) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC Y2f: tai khoan 0-resource bi 403.
        int rows = sysParamConfigService.delete(entity);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

}