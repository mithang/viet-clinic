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
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
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
 *
 * DS18 (deep-search round 18): the ownership guards above covered only
 * the CUSTOM endpoints, leaving the inherited generic CrudController
 * endpoints unguarded. sys_collect has NO company_id column, so
 * CrudService.verifyTenantOwnership early-returns and the generic
 * getById (floor = hasAnySessionPermission only) returned ANY user's
 * favorite row by id (probe-proven, tools/ds18-syscollect.py pre 15/15:
 * clinic GET foreign -> 200). Generic listPage/listAll/bulk* were NOT
 * leaks — SysCollectDao.xml has no listPage/listAll/bulk statements, so
 * they 500 and write nothing — but they are closed here anyway so a
 * future mapper addition cannot reopen them: lists return only the
 * session user's rows via the already-guarded listAllByUserId service
 * method (the same method the bookmark UI uses), and bulk* fail-closed
 * on any foreign item before delegating (delegated own calls keep the
 * existing no-mapper 500, unchanged). No service change, no new
 * resources, no role changes; the frontend bookmark API is untouched.
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

    private String sessionUserId() {
        try {
            User sessionUser = SessionUtils.getUser();
            return (sessionUser != null) ? sessionUser.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isSelfRow(SysCollect row) {
        return row != null && row.getUserId() != null && isSelf(row.getUserId().getId());
    }

    /**
     * DS18: generic getById was the one unguarded read — stored row of
     * another user (or with no user) -> 403. Nonexistent keeps the
     * generic 200+null contract, matching the DS17 read pattern.
     */
    @Override
    @GetMapping(value = "/{id}")
    public ResponseEntity<JSONObject> getById(@PathVariable("id") String id) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        SysCollect entity = getService().get(id);
        if (entity != null && !isSelfRow(entity)) {
            return forbidden();
        }
        return ResponseEntity.ok(ResultUtil.successJson(entity));
    }

    /**
     * DS18: generic lists had no mapper (500, no leak) — implement them
     * from the guarded service method instead, so they return only the
     * session user's favorites and can never leak a future mapper's rows.
     */
    @Override
    @PostMapping(value = {"list", ""})
    public ResponseEntity<JSONObject> listPage(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        String uid = sessionUserId();
        if (uid == null) {
            return forbidden();
        }
        List<SysCollect> own = sysCollectService.listAllByUserId(uid);
        if (own == null) {
            own = new java.util.ArrayList<>();
        }
        int offset = Math.max(0, searchParams.getOffset());
        int limit = searchParams.getLimit();
        int from = Math.min(offset, own.size());
        int to = limit > 0 ? Math.min(own.size(), from + limit) : own.size();
        if (to < from) {
            to = from;
        }
        Page<SysCollect> result = new Page<>(own.size(), own.subList(from, to));
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @Override
    @PostMapping(value = "listAll")
    public ResponseEntity<JSONObject> listAll(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        String uid = sessionUserId();
        if (uid == null) {
            return forbidden();
        }
        List<SysCollect> result = sysCollectService.listAllByUserId(uid);
        return ResponseEntity.ok(ResultUtil.successJson(result != null ? result : new java.util.ArrayList<>()));
    }

    /**
     * DS18: bulk* had no mapper (500, no write) — fail-closed on any
     * foreign item before delegating, so a future mapper cannot turn
     * them into cross-user writes. Own calls delegate unchanged.
     */
    @Override
    @PostMapping(value = "bulkInsert")
    public ResponseEntity<JSONObject> bulkInsert(@RequestBody List<SysCollect> entitys) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        if (entitys != null) {
            for (SysCollect entity : entitys) {
                if (!isSelfRow(entity)) {
                    return forbidden();
                }
            }
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @PostMapping(value = "bulkUpdate")
    public ResponseEntity<JSONObject> bulkUpdate(@RequestBody List<SysCollect> entitys) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        if (entitys != null) {
            for (SysCollect entity : entitys) {
                if (entity == null) {
                    return forbidden();
                }
                if (entity.getUserId() != null && !isSelf(entity.getUserId().getId())) {
                    return forbidden();
                }
                if (entity.getId() != null) {
                    SysCollect stored = sysCollectService.get(entity.getId());
                    if (stored != null && !isSelfRow(stored)) {
                        return forbidden();
                    }
                }
            }
        }
        return super.bulkUpdate(entitys);
    }

    @Override
    @PostMapping(value = "bulkDelete")
    public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<SysCollect> entitys) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        if (entitys != null) {
            for (SysCollect entity : entitys) {
                if (entity == null || entity.getId() == null) {
                    return forbidden();
                }
                SysCollect stored = sysCollectService.get(entity.getId());
                if (stored != null && !isSelfRow(stored)) {
                    return forbidden();
                }
            }
        }
        return super.bulkDelete(entitys);
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
