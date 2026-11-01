package com.geeke.booking.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.booking.entity.BookingRequest;
import com.geeke.booking.service.BookingRequestService;
import com.geeke.common.controller.CrudController;
import com.geeke.common.data.Page;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.geeke.utils.constants.ErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Yêu cầu đặt lịch Controller (G2 research loop).
 *
 * POST /booking/requests/intake — intake công khai, KHÔNG cần tài khoản:
 * là endpoint anon duy nhất của module (xem ShiroConfiguration). Nó chỉ
 * ghi bảng booking_request và chỉ đọc lại chính bảng đó để chống trùng;
 * không đọc/ghi dữ liệu clinical, không có trường chẩn đoán.
 * Mọi endpoint còn lại đều fail-closed với tài khoản 0 quyền
 * (hasAnySessionPermission, theo sàn của CrudController).
 *
 * GET  /booking/requests/pending?offset=&limit= — hàng chờ pending
 *      của tenant đang đăng nhập.
 * GET  /booking/requests/prior?phone= — các yêu cầu trước đó của một SĐT
 *      (xử lý trùng SĐT), chỉ tra bảng booking_request.
 * POST /booking/requests/{id}/confirm — xác nhận 1 thao tác thành lịch
 *      hẹn thật (registration qua subscribe_date/subscribe_project);
 *      trả về registration_id. Ngày không có lịch trực bác sĩ thì bị
 *      chặn (400) và yêu cầu vẫn pending.
 * POST /booking/requests/{id}/reject {reason} — từ chối có lưu lý do;
 *      bản ghi rời hàng chờ nhưng giữ lại lý do.
 */
@RestController
@RequestMapping(value = "/booking/requests")
public class BookingRequestController extends CrudController<BookingRequestService, BookingRequest> {

    @Autowired
    protected BookingRequestService bookingRequestService;

    @Override
    protected BookingRequestService getService() {
        return bookingRequestService;
    }

    @PostMapping(value = "intake")
    public ResponseEntity<JSONObject> intake(@RequestBody BookingRequest entity,
                                             HttpServletRequest request) {
        try {
            String bodyCompanyId = null;
            if (entity.getCompany() != null) {
                bodyCompanyId = entity.getCompany().getId();
            }
            BookingRequest saved = bookingRequestService.submitIntake(
                    entity, bodyCompanyId, request.getRemoteAddr());
            return ResponseEntity.ok(ResultUtil.successJson(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ResultUtil.errorJson(ErrorEnum.E_50002, e.getMessage()));
        }
    }

    @GetMapping(value = "pending")
    public ResponseEntity<JSONObject> pending(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        Page<BookingRequest> result = bookingRequestService.pendingPage(
                SessionUtils.getLoginTenantId(), offset, limit);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @GetMapping(value = "prior")
    public ResponseEntity<JSONObject> prior(
            @RequestParam(value = "phone") String phone,
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "50") int limit) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        try {
            Page<BookingRequest> result = bookingRequestService.priorPage(
                    SessionUtils.getLoginTenantId(), phone, offset, limit);
            return ResponseEntity.ok(ResultUtil.successJson(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ResultUtil.errorJson(ErrorEnum.E_50002, e.getMessage()));
        }
    }

    @PostMapping(value = "{id}/confirm")
    public ResponseEntity<JSONObject> confirm(@PathVariable("id") String id) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        try {
            String registrationId = bookingRequestService.confirm(id, SessionUtils.getLoginTenantId());
            return ResponseEntity.ok(ResultUtil.successJson(registrationId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ResultUtil.errorJson(ErrorEnum.E_50002, e.getMessage()));
        }
    }

    @PostMapping(value = "{id}/reject")
    public ResponseEntity<JSONObject> reject(@PathVariable("id") String id,
                                             @RequestBody JSONObject body) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        String reason = body == null ? null : body.getString("reason");
        if (StringUtils.isBlank(reason)) {
            return ResponseEntity.badRequest().body(
                    ResultUtil.errorJson(ErrorEnum.E_50002, "Lý do từ chối không được để trống."));
        }
        try {
            int rows = bookingRequestService.reject(id, SessionUtils.getLoginTenantId(), reason);
            if (rows <= 0) {
                return ResponseEntity.badRequest().body(ResultUtil.errorJson(ErrorEnum.E_50002,
                        "Yêu cầu đã được xử lý trước đó hoặc không thuộc phòng khám này."));
            }
            return ResponseEntity.ok(ResultUtil.successJson(rows));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ResultUtil.errorJson(ErrorEnum.E_50002, e.getMessage()));
        }
    }
}
