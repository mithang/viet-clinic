package com.geeke.diagnosis.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.admin.entity.User;
import com.geeke.diagnosis.entity.DiagnosisCatalog;
import com.geeke.diagnosis.service.DiagnosisService;
import com.geeke.sys.controller.BaseController;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.constants.ErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Chẩn đoán ICD-10 Controller (H3 research loop).
 *
 * 4 endpoint, đều fail-closed với tài khoản 0 quyền
 * (hasAnySessionPermission — tài khoản không có resource nào nhận 403),
 * theo đúng mẫu G5/H5. Không kế thừa CrudController nên không có route
 * CRUD chung nào, và không có endpoint ghi catalog. Không thêm dòng
 * anon nào vào Shiro: các endpoint rơi vào "/**" -> authc, tức bắt
 * buộc đăng nhập rồi mới tới kiểm tra quyền; không token trả 20011
 * như các endpoint H5/G6.
 *
 * GET    /diagnosis/search?q=&page=&pageSize= — tìm mã hoặc tên tiếng
 *        Việt phía server, phân trang (mặc định 20, tối đa 50).
 * GET    /diagnosis/pinned — ghim của chính bác sĩ đang đăng nhập.
 * POST   /diagnosis/pin {diagnosisId} — ghim (idempotent); chẩn đoán
 *        không tồn tại trả 400.
 * DELETE /diagnosis/pin/{diagnosisId} — bỏ ghim (idempotent).
 *
 * Catalog dùng chung mọi tenant (chuẩn chung); ghim gắn theo doctor_id
 * của phiên đăng nhập, không nhận doctor_id từ client — client không
 * thể liệt kê hay sửa ghim của bác sĩ khác bằng cách đổi tham số.
 * Việc lưu mã vào hồ sơ KHÔNG qua controller này: mã đi theo trường
 * diagnosisCode sẵn có của MedicalRecord qua đường lưu allSave/v2/
 * allSave/saveWithFile (mapper đã nối cột diagnosis_code, migration
 * v11), nên chẩn đoán gõ tự do khi catalog thiếu vẫn lưu được và
 * không bao giờ bị endpoint này chặn.
 */
@RestController
@RequestMapping(value = "/diagnosis")
public class DiagnosisController extends BaseController {

    @Autowired
    private DiagnosisService diagnosisService;

    @GetMapping(value = "search")
    public ResponseEntity<JSONObject> search(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") int pageSize) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        JSONObject data = diagnosisService.search(q, page, pageSize);
        return ResponseEntity.ok(ResultUtil.successJson(data));
    }

    @GetMapping(value = "pinned")
    public ResponseEntity<JSONObject> pinned() {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        String doctorId = currentDoctorId();
        if (doctorId == null) {
            return forbidden();
        }
        List<DiagnosisCatalog> pinned = diagnosisService.listPinned(doctorId);
        return ResponseEntity.ok(ResultUtil.successJson(pinned));
    }

    @PostMapping(value = "pin")
    public ResponseEntity<JSONObject> pin(@RequestBody JSONObject body) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        String doctorId = currentDoctorId();
        if (doctorId == null) {
            return forbidden();
        }
        try {
            Long diagnosisId = body == null ? null : body.getLong("diagnosisId");
            DiagnosisCatalog pinned = diagnosisService.pin(doctorId, diagnosisId);
            return ResponseEntity.ok(ResultUtil.successJson(pinned));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ResultUtil.errorJson(ErrorEnum.E_50002, e.getMessage()));
        }
    }

    @DeleteMapping(value = "pin/{diagnosisId}")
    public ResponseEntity<JSONObject> unpin(@PathVariable("diagnosisId") Long diagnosisId) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        String doctorId = currentDoctorId();
        if (doctorId == null) {
            return forbidden();
        }
        diagnosisService.unpin(doctorId, diagnosisId);
        return ResponseEntity.ok(ResultUtil.successJson(true));
    }

    /**
     * Id user của phiên đăng nhập (mẫu MedicalRecordService dùng
     * SessionUtils.getUser()). Null nghĩa là phiên không mang thông
     * tin user — fail-closed 403, không đoán doctor_id từ client.
     */
    private String currentDoctorId() {
        try {
            User user = SessionUtils.getUser();
            if (user == null || user.getId() == null) {
                return null;
            }
            return String.valueOf(user.getId());
        } catch (Exception e) {
            return null;
        }
    }

    private ResponseEntity<JSONObject> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ResultUtil.errorJson(ErrorEnum.E_502));
    }
}
