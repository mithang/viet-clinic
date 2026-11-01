package com.geeke.vietqr.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.sys.controller.BaseController;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.constants.ErrorEnum;
import com.geeke.vietqr.entity.VietqrConfig;
import com.geeke.vietqr.service.VietqrConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cấu hình VietQR Controller (G4 research loop).
 *
 * Chỉ 2 endpoint, đều fail-closed với tài khoản 0 quyền
 * (hasAnySessionPermission — tài khoản không có resource nào nhận 403).
 * Không kế thừa CrudController nên không có route CRUD chung nào:
 * cấu hình của tenant này không bao giờ liệt kê cho tenant khác.
 * Không thêm dòng anon nào vào Shiro: cả 2 endpoint đều rơi vào
 * "/**" -> authc, tức bắt buộc đăng nhập rồi mới tới kiểm tra quyền.
 *
 * GET  /vietqr/config — {configured, config} của tenant đang đăng nhập;
 *      chưa cấu hình thì configured = false, config = null.
 * POST /vietqr/config — lưu (upsert) cấu hình của tenant đang đăng nhập.
 *      Dữ liệu sai trả 400. Đây chỉ là thông tin nhận tiền hiển thị trên
 *      mã QR, không phải credential, và không kích hoạt xác nhận thanh
 *      toán tự động nào.
 */
@RestController
@RequestMapping(value = "/vietqr/config")
public class VietqrConfigController extends BaseController {

    @Autowired
    private VietqrConfigService vietqrConfigService;

    @GetMapping
    public ResponseEntity<JSONObject> getConfig() {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        VietqrConfig config = vietqrConfigService.getActive(SessionUtils.getLoginTenantId());
        JSONObject data = new JSONObject();
        data.put("configured", config != null);
        data.put("config", config);
        return ResponseEntity.ok(ResultUtil.successJson(data));
    }

    @PostMapping
    public ResponseEntity<JSONObject> saveConfig(@RequestBody VietqrConfig input) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        try {
            VietqrConfig saved = vietqrConfigService.saveConfig(
                    SessionUtils.getLoginTenantId(), input);
            return ResponseEntity.ok(ResultUtil.successJson(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ResultUtil.errorJson(ErrorEnum.E_50002, e.getMessage()));
        }
    }

    private ResponseEntity<JSONObject> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ResultUtil.errorJson(ErrorEnum.E_502));
    }
}
