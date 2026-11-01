package com.geeke.chargesummary.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.chargesummary.service.ChargeSummaryService;
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
 * Tổng kết thu phí cuối ngày Controller (G6 research loop).
 *
 * Chỉ 1 endpoint, fail-closed với tài khoản 0 quyền
 * (hasAnySessionPermission — tài khoản không có resource nào nhận 403).
 * Không kế thừa CrudController nên không có route CRUD chung nào.
 * Không thêm dòng anon nào vào Shiro: endpoint rơi vào "/**" -> authc,
 * tức bắt buộc đăng nhập rồi mới tới kiểm tra quyền.
 *
 * GET /charge-summary/day?date=YYYY-MM-DD — tổng kết ngày phòng khám
 * (Asia/Ho_Chi_Minh) của tenant đang đăng nhập: {date, timezone,
 * windowStart, windowEnd, rows, totals, refunds}. Thiếu/sai định dạng
 * date trả 400. Chỉ đọc toll_info + toll_audit_log, không ghi DB.
 */
@RestController
@RequestMapping(value = "/charge-summary")
public class ChargeSummaryController extends BaseController {

    @Autowired
    private ChargeSummaryService chargeSummaryService;

    @GetMapping(value = "day")
    public ResponseEntity<JSONObject> daySummary(
            @RequestParam(value = "date", required = false) String date) {
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        try {
            JSONObject data = chargeSummaryService.daySummary(
                    SessionUtils.getLoginTenantId(), date);
            return ResponseEntity.ok(ResultUtil.successJson(data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ResultUtil.errorJson(ErrorEnum.E_50002, e.getMessage()));
        }
    }
}
