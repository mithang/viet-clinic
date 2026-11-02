package com.geeke.admin.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.geeke.admin.entity.Router;
import com.geeke.admin.entity.User;
import com.geeke.admin.service.RouterService;
import com.geeke.common.controller.CrudController;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;

/**
 * Quản lý tuyến đườngController
 * @author lys
 * @version 2021-11-18
 */
@RestController
@RequestMapping(value = "/admin/router")
public class RouterController extends CrudController<RouterService, Router> {

    @Autowired
    protected RouterService routerService;

    @Override
    protected RouterService getService() {
        return routerService;
    }

    @PostMapping(value = "tree")
    public ResponseEntity<JSONObject> tree(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        List<Router> result = routerService.tree(searchParams.getParams(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    // Y2b: chỉ chính mình mới được xem cảnh báo hết hạn của mình, mẫu
    // SysCollectController. Caller duy nhất trong client là header.vue, luôn
    // gửi currentUser.id — không có luồng admin xem hộ user khác.
    private boolean isSelf(String userId) {
        if (userId == null) {
            return false;
        }
        try {
            User sessionUser = SessionUtils.getUser();
            return sessionUser != null && userId.equals(sessionUser.getId());
        } catch (Exception e) {
            return false;
        }
    }

    //LấyNgười dùngCó hay khôngsở hữucóThời hạn hiệu lựctrướccảnh báoQuyền
    @GetMapping("getUserIndateWarning/{userId}")
    public ResponseEntity<JSONObject> getUserIndateWarning(@PathVariable("userId") String userId){
        if (!hasAnySessionPermission() || !isSelf(userId)) {
            return forbidden();
        }
        List<String> userIndateWarning = routerService.getUserIndateWarning(userId);
        return ResponseEntity.ok(ResultUtil.successJson(userIndateWarning));
    }
}