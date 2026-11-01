package com.geeke.daydashboard.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.daydashboard.service.DayDashboardService;
import com.geeke.sys.controller.BaseController;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.constants.ErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tổng quan ngày cho chủ phòng khám Controller (H5 research loop).
 *
 * Chỉ 1 endpoint, fail-closed với tài khoản 0 quyền
 * (hasAnySessionPermission — tài khoản không có resource nào nhận 403),
 * theo đúng mẫu G6 (ChargeSummaryController). Không kế thừa
 * CrudController nên không có route CRUD chung nào. Không thêm dòng
 * anon nào vào Shiro: endpoint rơi vào "/**" -> authc, tức bắt buộc
 * đăng nhập rồi mới tới kiểm tra quyền.
 *
 * GET /day-dashboard/day?date=YYYY-MM-DD — tổng quan ngày phòng khám
 * (Asia/Ho_Chi_Minh) của tenant đang đăng nhập: {date, timezone,
 * windowStart, windowEnd, visits, charges, stock, definitions}.
 * Thiếu/sai định dạng date trả 400. Chỉ đọc, không ghi DB, không
 * migration mới, không router/menu mới.
 */
@RestController
@RequestMapping(value = "/day-dashboard")
public class DayDashboardController extends BaseController {

    @Autowired
    private DayDashboardService dayDashboardService;

    @GetMapping(value = "day")
    public ResponseEntity<JSONObject> dayDashboard(
            @RequestParam(value = "date", required = false) String date) {
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        try {
            JSONObject data = dayDashboardService.dayDashboard(
                    SessionUtils.getLoginTenantId(), date);
            return ResponseEntity.ok(ResultUtil.successJson(data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ResultUtil.errorJson(ErrorEnum.E_50002, e.getMessage()));
        }
    }
}
