package com.geeke.followup.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.CrudController;
import com.geeke.common.data.Page;
import com.geeke.followup.entity.FollowupReminder;
import com.geeke.followup.service.FollowupReminderService;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Nhắc lịch tái khám Controller (G1 research loop).
 *
 * CRUD chuẩn kế thừa từ CrudController (list/listAll/save/delete/...),
 * cộng 3 endpoint riêng. Mọi endpoint đều fail-closed với tài khoản
 * 0 quyền (hasAnySessionPermission, theo sàn của CrudController):
 * tài khoản không có resource nào nhận 403.
 *
 * GET  /followup/reminders/due?offset=&limit= — trang đến hạn/quá hạn
 *      của tenant đang đăng nhập, kèm daysOverdue từng dòng.
 * POST /followup/reminders/{id}/acknowledge — xác nhận một nhắc lịch
 *      (rời khỏi danh sách đến hạn). Trả số dòng cập nhật (0 = đã xử
 *      lý trước đó hoặc sai tenant).
 * POST /followup/reminders/runDailyJob — chạy bù job ngày cho tenant
 *      đang đăng nhập (khi backend lỡ nhịp 08:00). Trả số thông báo
 *      đã tạo, idempotent trong ngày.
 */
@RestController
@RequestMapping(value = "/followup/reminders")
public class FollowupReminderController extends CrudController<FollowupReminderService, FollowupReminder> {

    @Autowired
    protected FollowupReminderService followupReminderService;

    @Override
    protected FollowupReminderService getService() {
        return followupReminderService;
    }

    @GetMapping(value = "due")
    public ResponseEntity<JSONObject> due(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        Page<FollowupReminder> result = followupReminderService.duePage(
                SessionUtils.getLoginTenantId(), offset, limit);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @PostMapping(value = "{id}/acknowledge")
    public ResponseEntity<JSONObject> acknowledge(@PathVariable("id") String id) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        int rows = followupReminderService.acknowledge(id, SessionUtils.getLoginTenantId());
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

    @PostMapping(value = "runDailyJob")
    public ResponseEntity<JSONObject> runDailyJob() {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        int created = followupReminderService.createDueNotices(SessionUtils.getLoginTenantId());
        return ResponseEntity.ok(ResultUtil.successJson(created));
    }
}
