package com.geeke.collect.controller;

import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSONObject;
import com.geeke.admin.entity.User;
import com.geeke.collect.entity.SysCollect;
import com.geeke.collect.service.SysCollectService;
import com.geeke.common.controller.CrudController;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;

/**
 * Mục yêu thíchController
 * @author szy
 * @version 2021-07-28
 *
 * Ownership: favorites là dữ liệu của từng user. Trước đây mọi token hợp lệ
 * đều đọc/ghi được favorites của user khác (listAllByUserId/updateBatch nhận
 * userId trên path mà không đối chiếu session; save nhận userId trong
 * payload; delete chỉ nhận id). Vòng này chặn: chỉ chính mình mới được
 * đọc/ghi/xóa favorites của mình, ngược lại 403 fail-closed. Frontend
 * (home/components/bookmark.vue) luôn gửi userId của chính mình, và delete
 * chỉ gửi {id} nên delete phải load bản ghi để đối chiếu, không đối chiếu
 * payload.
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

    /**
     * Theo người dùngidTruy vấnyêu thíchDanh sách
     */
    @GetMapping(value = "listAllByUserId/{userId}")
    public ResponseEntity<JSONObject> listAllByUserId(@PathVariable("userId") String userId) {
        if (!isSelf(userId)) {
            return forbidden();
        }
        List<SysCollect> result = sysCollectService.listAllByUserId(userId);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * di độngyêu thíchkích hoạt
     */
    @PostMapping(value = "updateBatch/{userId}")
    public ResponseEntity<JSONObject> updateBatch(@RequestBody List<SysCollect> sysCollectList, @PathVariable String userId) {
        if (!isSelf(userId)) {
            return forbidden();
        }
        List<SysCollect> result = sysCollectService.updateBatch(sysCollectList, userId);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * Lưu yêu thích: payload phải mang userId của chính mình.
     */
    @Override
    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@Valid @RequestBody SysCollect entity) {
        if (entity == null || entity.getUserId() == null || !isSelf(entity.getUserId().getId())) {
            return forbidden();
        }
        return super.save(entity);
    }

    /**
     * Xóayêu thích(giữ lại @DeleteMapping Để tương thích với lời gọi từ frontend)
     * Payload frontend chỉ có {id} nên load bản ghi để đối chiếu chủ sở hữu.
     */
    @Override
    @DeleteMapping(value = "delete")
    public ResponseEntity<JSONObject> delete(@RequestBody SysCollect entity) {
        if (entity == null || entity.getId() == null) {
            return forbidden();
        }
        SysCollect existing = sysCollectService.get(entity.getId());
        if (existing == null || existing.getUserId() == null || !isSelf(existing.getUserId().getId())) {
            return forbidden();
        }
        return super.delete(entity);
    }
}
