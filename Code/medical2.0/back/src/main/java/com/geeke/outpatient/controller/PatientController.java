package com.geeke.outpatient.controller;

import java.util.List;

import com.geeke.outpatient.entity.Registration;
import com.geeke.outpatient.service.RegistrationService;
import com.geeke.recordaudit.service.RecordAccessAuditService;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.constants.ErrorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import com.alibaba.fastjson.JSONObject;
import com.geeke.outpatient.entity.Patient;
import com.geeke.outpatient.service.PatientService;
import com.geeke.common.controller.CrudController;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.constants.ErrorEnum;
import org.springframework.http.HttpStatus;

/**
 * Thông tin bệnh nhânController
 * @author txl
 * @version 2022-06-23
 */
@RestController
@RequestMapping(value = "/outpatient/patient")
public class PatientController extends CrudController<PatientService, Patient> {

	@Autowired
	protected PatientService patientService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private RecordAccessAuditService recordAccessAuditService;

    private static final Logger auditLogger = LoggerFactory.getLogger(PatientController.class);

    /**
     * G8 — Ghi audit truy cập hồ sơ. Ghi audit không bao giờ được làm hỏng
     * luồng khám chữa: mọi lỗi ghi đều được log lại và nuốt ở đây, response
     * gốc vẫn trả bình thường. Ghi thất bại thì nhật ký thiếu dòng đó
     * (lỗi sẽ hiện trong log backend để phát hiện), chứ không trả lỗi cho
     * người đang xem/sửa hồ sơ.
     */
    private void logAccessSafely(String action, String patientId, String patientName) {
        try {
            recordAccessAuditService.logAccess(SessionUtils.getLoginTenantId(),
                    SessionUtils.getUserJson(), patientId, patientName, action);
        } catch (Exception e) {
            auditLogger.warn("Ghi nhật ký truy cập hồ sơ thất bại ({} patient {}): {}",
                    action, patientId, e.getMessage());
        }
    }

    @Override
    protected PatientService getService() {
        return patientService;
    }

    /**
     * G8 — Xem chi tiết 1 bệnh nhân: ghi đúng 1 dòng VIEW (chỉ khi tìm
     * thấy bệnh nhân). Quyền giữ nguyên như CrudController gốc
     * (hasAnySessionPermission) để không làm hẹp luồng xem hồ sơ sẵn có.
     */
    @Override
    @GetMapping("/{id}")
    public ResponseEntity<JSONObject> getById(@PathVariable("id") String id) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        Patient entity = getService().get(id);
        if (entity != null) {
            logAccessSafely(RecordAccessAuditService.ACTION_VIEW, entity.getId(), entity.getName());
        }
        return ResponseEntity.ok(ResultUtil.successJson(entity));
    }

    /**
     * Danh sách bệnh nhân có phân trang: chỉ tài khoản có quyền tài nguyên
     * "patient:list" mới được gọi. Trước đây mọi token hợp lệ đều đọc được
     * danh sách bệnh nhân, vì filter chỉ kiểm tra xác thực. Lọc theo tenant
     * vẫn do service đảm nhiệm (ensureCompanyFilter: chỉ trả bệnh nhân thuộc
     * phòng khám đang đăng nhập).
     *
     * G8 — Mở danh sách ghi ĐÚNG 1 dòng LIST (patientId null), không ghi
     * theo từng bệnh nhân trong trang, để nhật ký không phình theo số
     * dòng danh sách.
     */
    @Override
    @PostMapping(value = {"list", ""})
    public ResponseEntity<JSONObject> listPage(@RequestBody SearchParams searchParams) {
        if (!hasSessionPermission("patient:list")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        Page<Patient> result = getService().listPage(searchParams.getParams(), searchParams.getOffset(),
                searchParams.getLimit(), searchParams.getOrderby());
        logAccessSafely(RecordAccessAuditService.ACTION_LIST, null, null);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * G8 — Danh sách toàn bộ (listAll) cũng ghi đúng 1 dòng LIST như
     * listPage, vì đây cũng là một lượt xem danh sách, không phải xem
     * từng hồ sơ.
     */
    @Override
    @PostMapping(value = "listAll")
    public ResponseEntity<JSONObject> listAll(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        List<Patient> result = getService().listAll(searchParams.getParams(), searchParams.getOrderby());
        logAccessSafely(RecordAccessAuditService.ACTION_LIST, null, null);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * G8 — Lưu hồ sơ: chỉ ghi EDIT khi sửa hồ sơ ĐÃ tồn tại (id khác
     * trống và get(id) tìm thấy). Tạo mới bệnh nhân không ghi EDIT vì
     * chưa có hồ sơ trước đó để truy cập. Phần còn lại sao chép đúng
     * CrudController.save (quyền sàn, beforeSave/afterSave) để không
     * đổi hành vi lưu sẵn có.
     */
    @Override
    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@Valid @RequestBody Patient entity) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        if (!beforeSave(entity)) {
            return ResponseEntity.ok(ResultUtil.warningJson(ErrorEnum.E_50002, "LưutrướcKiểm traChưaQua"));
        }
        boolean wasExisting = entity.getId() != null && !entity.getId().trim().isEmpty()
                && getService().get(entity.getId()) != null;
        String id = getService().save(entity).getId();
        afterSave(entity);
        if (wasExisting) {
            logAccessSafely(RecordAccessAuditService.ACTION_EDIT, id, entity.getName());
        }
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    /**
     * R1 — Xóa hồ sơ: sao chép đúng CrudController.delete (kiểm tra
     * entity null, quyền sàn hasAnySessionPermission, beforeDelete /
     * afterDelete) để không hẹp/nới quyền xóa sẵn có, cộng thêm 1 dòng
     * audit DELETE — chỉ khi xóa thật sự có hiệu lực (rows > 0, id
     * khác trống và hồ sơ còn hiệu lực trước khi xóa). Tên bệnh nhân lấy từ hồ sơ đọc trước khi xóa (đường
     * service trực tiếp, không qua getById nên không ghi thêm VIEW);
     * nếu body xóa đã kèm tên thì dùng tên đó. Ghi qua logAccessSafely
     * nên lỗi ghi không hỏng luồng xóa, như các hook G8 khác.
     * bulkDelete vẫn kế thừa CrudController (không ghi audit theo từng
     * hồ sơ vì service chỉ trả tổng rows) — ghi nhận làm item riêng,
     * không mở rộng lén trong R1.
     */
    @Override
    @PostMapping(value = "delete")
    public ResponseEntity<JSONObject> delete(@RequestBody Patient entity) {
        if (entity == null) {
            return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_400));
        }
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        if (!beforeDelete(entity)) {
            return ResponseEntity.ok(ResultUtil.warningJson(ErrorEnum.E_50002, "XóatrướcKiểm traChưaQua"));
        }
        String id = entity.getId();
        String name = entity.getName();
        boolean wasActive = false;
        if (id != null && !id.trim().isEmpty()) {
            try {
                Patient existing = getService().get(id);
                if (existing != null) {
                    // UPDATE xóa mềm không lọc del_flag nên xóa lại hồ sơ
                    // đã xóa vẫn trả rows=1: chỉ ghi DELETE khi hồ sơ còn
                    // hiệu lực trước khi xóa, tránh ghi đúp cho lần xóa lại.
                    wasActive = com.geeke.common.persistence.BaseEntity.DEL_FLAG_NORMAL
                            .equals(existing.getDelFlag());
                    if (name == null || name.trim().isEmpty()) {
                        name = existing.getName();
                    }
                }
            } catch (Exception ignored) {
                // Lỗi đọc trước không được đổi hành vi xóa: delete gốc
                // tự kiểm tra quyền sở hữu tenant, cứ để nó quyết định.
            }
        }
        int rows = getService().delete(entity);
        afterDelete(entity);
        if (rows > 0 && wasActive && id != null && !id.trim().isEmpty()) {
            logAccessSafely(RecordAccessAuditService.ACTION_DELETE, id, name);
        }
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

    //WeChatLưu
    @PostMapping(value = "wxSave")
    public ResponseEntity<JSONObject> wxSave(@RequestBody Patient entity) {
        String id = patientService.wxSave(entity).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    /**
     * G3 — Tra trùng bệnh nhân cho form đăng ký: SĐT trước, rồi CMND/CCCD,
     * chuẩn hóa ở service. Chỉ đọc, không tạo, không merge — lễ tân chọn
     * rõ ràng ở frontend hoặc tạo mới. Fail-closed như listPage: tài khoản
     * không có quyền "patient:list" nhận 403. SĐT/card trống cả hai trả về
     * danh sách rỗng, không prompt.
     */
    @PostMapping(value = "lookupDuplicates")
    public ResponseEntity<JSONObject> lookupDuplicates(@RequestBody JSONObject body) {
        if (!hasSessionPermission("patient:list")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        String phone = body == null ? null : body.getString("phone");
        String card = body == null ? null : body.getString("card");
        return ResponseEntity.ok(ResultUtil.successJson(patientService.findDuplicates(phone, card)));
    }

    @PostMapping(value = "getByCard")
    public ResponseEntity<JSONObject> getByCard(@RequestBody Patient patient) {
        Patient byCard = patientService.getByCard(patient);
        return ResponseEntity.ok(ResultUtil.successJson(byCard));
    }

    @PostMapping(value = "getByOpenId")
    public ResponseEntity<JSONObject> getByOpenId(@RequestBody Patient patient){
        List<Patient> patients = patientService.getByOpenId(patient.getOpenId(),patient.getCompany().getId());
        return ResponseEntity.ok(ResultUtil.successJson(patients));
    }

    /* Tra cứu thông tin bệnh nhân theo họ tên và CMND/CCCD rồi đăng ký khám */
    @PostMapping(value = "inquire")
    public ResponseEntity<JSONObject> inquire(@RequestBody Patient patient) {
        // Truy vấnThông tin bệnh nhân
        Patient result = patientService.inquire(patient);
        if (result==null){
            return ResponseEntity.ok(ResultUtil.warningJson(ErrorEnum.E_50001, "Truy vấnThất bại, khôngđóThông tin bệnh nhân"));
        }
        // Đăng ký khám
        Registration registration = new Registration();
        registration.setPatientId(result);
        registration.setCompany(result.getCompany());
        String id = registrationService.save(registration).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    /* ThêmBệnh nhânhồ sơánThông tinvàĐăng ký khám */
    @PostMapping(value = "add")
    public ResponseEntity<JSONObject> add(@RequestBody Patient patient) {
        // ThêmThông tin bệnh nhân
        Patient result = patientService.save(patient);
        // Đăng ký khám
        Registration registration = new Registration();
        registration.setPatientId(result);
        registration.setCompany(result.getCompany());
        String id = registrationService.save(registration).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

}
