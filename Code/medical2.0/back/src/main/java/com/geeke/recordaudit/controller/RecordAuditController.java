package com.geeke.recordaudit.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.recordaudit.entity.RecordAccessAudit;
import com.geeke.recordaudit.service.RecordAccessAuditService;
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

import java.util.List;

/**
 * Nhật ký truy cập hồ sơ Controller (G8 research loop).
 *
 * Chỉ 1 endpoint, fail-closed với tài khoản 0 quyền
 * (hasAnySessionPermission — tài khoản không có resource nào nhận 403).
 * Không kế thừa CrudController nên không có route CRUD chung nào, và
 * đặc biệt KHÔNG có route tạo/xóa/sửa audit: nhật ký là append-only,
 * chỉ backend tự ghi khi xem/sửa hồ sơ thật.
 * Không thêm dòng anon nào vào Shiro: endpoint rơi vào "/**" -> authc,
 * tức bắt buộc đăng nhập rồi mới tới kiểm tra quyền; không token trả
 * 20011 như mọi endpoint authc khác.
 *
 * GET /record-audit/list?patientId=&action=&limit= — các dòng audit của
 * tenant đang đăng nhập, mới nhất trước. patientId/action trống nghĩa
 * là không lọc; limit kẹp [1, 200] ở service. Hành động ngoài
 * VIEW/LIST/EDIT trả 400 (không lộ dữ liệu).
 */
@RestController
@RequestMapping(value = "/record-audit")
public class RecordAuditController extends BaseController {

    @Autowired
    private RecordAccessAuditService recordAccessAuditService;

    @GetMapping(value = "list")
    public ResponseEntity<JSONObject> list(
            @RequestParam(value = "patientId", required = false) String patientId,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "limit", required = false) Integer limit) {
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        if (action != null && !action.trim().isEmpty()
                && !RecordAccessAuditService.ACTION_VIEW.equals(action.trim())
                && !RecordAccessAuditService.ACTION_LIST.equals(action.trim())
                && !RecordAccessAuditService.ACTION_EDIT.equals(action.trim())) {
            return ResponseEntity.badRequest()
                    .body(ResultUtil.errorJson(ErrorEnum.E_50002, "Hành động lọc không hợp lệ (chỉ VIEW, LIST hoặc EDIT)."));
        }
        List<RecordAccessAudit> audits = recordAccessAuditService.listAudits(
                SessionUtils.getLoginTenantId(), patientId, action, limit);
        return ResponseEntity.ok(ResultUtil.successJson(audits));
    }
}
