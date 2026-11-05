package com.geeke.outpatient.controller;

import java.util.List;

import com.geeke.outpatient.entity.Registration;
import com.geeke.outpatient.service.RegistrationService;
import com.geeke.recordaudit.service.RecordAccessAuditService;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.geeke.utils.constants.ErrorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import com.alibaba.fastjson.JSONObject;
import com.geeke.org.entity.Company;
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
        // E1 (iteration 52): ep tenant theo session nhu add (D1) truoc khi
        // save. Truoc day company lay thang tu payload nen token tenant A
        // gui company tenant B tao that benh nhan o B (nhanh insert cua
        // CrudService.save khong verify tenant). Nay: session co tenant thi
        // bo qua company payload; session rong giu fallback body cu.
        // Nhanh update van an toan: CrudService.save van verify stored-row
        // nen sua dong la van bi chan du payload company da bi ep ve OWN.
        // UI that gui company = currentUser.company.id (own tenant) nen
        // khong doi hanh vi hop le (registrationForm/schedulingForm).
        String tenant = sessionTenantOr(entity.getCompany() == null ? null : entity.getCompany().getId());
        if (tenant != null) {
            entity.setCompany(new Company(tenant));
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
     * bulkDelete có override riêng ngay dưới (S2), cũng ghi DELETE theo
     * từng hồ sơ còn hiệu lực.
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

    /**
     * S2 — Xóa hàng loạt: sao chép đúng CrudController.bulkDelete (quyền
     * sàn hasAnySessionPermission; gốc không có beforeDelete/afterDelete
     * nên không có hook nào để sao chép), cộng 1 dòng audit DELETE cho
     * mỗi hồ sơ CÒN HIỆU LỰC trước khi xóa. Ghi theo tập đọc trước,
     * không ghi theo rows trả về: UPDATE bulk không lọc del_flag nên
     * rows đếm cả id đã xóa từ trước (như xóa đơn R1 trả rows=1 khi
     * xóa lại), và 1 id lặp trong body cũng chỉ tính 1 lần (map
     * LinkedHashMap khử trùng). Tên lấy từ hồ sơ đọc trước qua service
     * trực tiếp (không qua getById để không ghi thêm VIEW), fallback
     * tên trong body; lỗi đọc trước được nuốt để bulkDelete gốc tự
     * quyết quyền sở hữu tenant từng entity. Chỉ là lỗ hổng tầng API:
     * frontend hiện không có Vue nào gọi bulkDeletePatient (chỉ export
     * ở api/outpatient/patient.js), không bịa thành nút UI.
     */
    @Override
    @PostMapping(value = "bulkDelete")
    public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<Patient> entitys) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        java.util.Map<String, String> activeNames = new java.util.LinkedHashMap<>();
        if (entitys != null) {
            for (Patient entity : entitys) {
                if (entity == null || entity.getId() == null || entity.getId().trim().isEmpty()) {
                    continue;
                }
                String id = entity.getId();
                if (activeNames.containsKey(id)) {
                    continue;
                }
                try {
                    Patient existing = getService().get(id);
                    if (existing != null
                            && com.geeke.common.persistence.BaseEntity.DEL_FLAG_NORMAL
                                    .equals(existing.getDelFlag())) {
                        String name = existing.getName();
                        if (name == null || name.trim().isEmpty()) {
                            name = entity.getName();
                        }
                        activeNames.put(id, name);
                    }
                } catch (Exception ignored) {
                    // Như R1: lỗi đọc trước không đổi hành vi xóa.
                }
            }
        }
        int rows = getService().bulkDelete(entitys);
        for (java.util.Map.Entry<String, String> entry : activeNames.entrySet()) {
            logAccessSafely(RecordAccessAuditService.ACTION_DELETE, entry.getKey(), entry.getValue());
        }
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

    /**
     * T3 — Quyền sàn parity với save: wxSave ghi (chèn mới hoặc cập
     * nhật openId cho hồ sơ đã có) nên tài khoản 0 quyền nhận 403
     * trước khi service chạy. Không nới lỏng cho luồng mini-program
     * chưa có bằng chứng: client/src không có caller nào cho đường
     * này, /auth/wxToken chỉ là login thường và Shiro không anon
     * /outpatient/**, nên không có bằng chứng luồng hợp lệ nào cần
     * 0 quyền mà vẫn gọi wxSave.
     */
    //WeChatLưu
    @PostMapping(value = "wxSave")
    public ResponseEntity<JSONObject> wxSave(@RequestBody Patient entity) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
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

    /**
     * R2 — Tra cứu 1 hồ sơ theo CCCD: trả đủ hồ sơ như getById nên ghi
     * đúng 1 dòng VIEW với patientId đó (chỉ khi tìm thấy; service trả null
     * khi không khớp nên không có dòng audit cho kết quả rỗng).
     * S3 — Quyền sàn parity với getById: tài khoản 0 quyền nhận 403
     * trước khi service chạy, nên lượt bị chặn cũng không ghi VIEW.
     * Không lên patient:list: frontend không có caller nào cho endpoint
     * này, và mức hasAnySessionPermission đã đúng với getById cùng
     * mức tiết lộ (khác lookupDuplicates/listPage vốn là đường duyệt).
     */
    @PostMapping(value = "getByCard")
    public ResponseEntity<JSONObject> getByCard(@RequestBody Patient patient) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        // DS7 (round 7): ep tenant theo session nhu getByOpenId (C3) — truoc
        // day company lay thang tu payload nen token tenant A gui company
        // tenant B + CCCD cua benh nhan B la doc duoc toan bo ho so B, va
        // payload thieu company thi NPE 500 o service. Session co tenant thi
        // bo qua company payload; session rong giu fallback body cu; tenant
        // rong -> tra null fail-closed (khong query, khong audit).
        String tenant = sessionTenantOr(patient.getCompany() == null ? null : patient.getCompany().getId());
        if (tenant == null || tenant.trim().isEmpty()) {
            return ResponseEntity.ok(ResultUtil.successJson(null));
        }
        Patient byCard = patientService.getByCard(patient.getCard(), tenant);
        if (byCard != null) {
            logAccessSafely(RecordAccessAuditService.ACTION_VIEW, byCard.getId(), byCard.getName());
        }
        return ResponseEntity.ok(ResultUtil.successJson(byCard));
    }

    /**
     * R2 — Tra cứu theo openId: trả danh sách hồ sơ đầy đủ, không phải
     * trang danh sách duyệt (listPage ghi 1 LIST với patientId null).
     * Ghi 1 dòng VIEW cho mỗi hồ sơ trả về để giữ patientId truy vết
     * được hồ sơ nào đã tiết lộ; kết quả rỗng không ghi dòng nào.
     * Tái hiện R2: openId thực tế trả đúng 1 dòng, nên 1 lần gọi thường
     * = 1 dòng VIEW như getById; nếu dữ liệu lệch có nhiều hồ sơ cùng
     * openId thì mỗi hồ sơ vẫn có dòng riêng (đúng mục đích nhật ký).
     * S3 — Quyền sàn như getByCard ở trên (parity getById, không
     * patient:list): 0 quyền → 403 trước khi service/audit chạy.
     */
    // C3 (iteration 50): ep tenant theo session, theo mau V1 cua
    // RegistrationController.sessionTenantOr. Truoc day company lay thang tu
    // payload nen token tenant A gui company tenant B + openId cua benh nhan B
    // la doc duoc toan bo ho so B; payload thieu company thi
    // getCompany().getId() NPE 500. Nay: session co tenant thi bo qua company
    // payload (chi tim trong tenant session, tra rong neu openId thuoc tenant
    // khac); session rong giu fallback body cu; company null khong con NPE
    // (dao WHERE company_id = session tenant, khong khop thi tra rong).
    private String sessionTenantOr(String fallback) {
        String tenant = SessionUtils.getLoginTenantId();
        return (StringUtils.isNotBlank(tenant) && !"null".equals(tenant)) ? tenant : fallback;
    }

    @PostMapping(value = "getByOpenId")
    public ResponseEntity<JSONObject> getByOpenId(@RequestBody Patient patient){
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        List<Patient> patients = patientService.getByOpenId(patient.getOpenId(),
                sessionTenantOr(patient.getCompany() == null ? null : patient.getCompany().getId()));
        if (patients != null) {
            for (Patient p : patients) {
                if (p != null) {
                    logAccessSafely(RecordAccessAuditService.ACTION_VIEW, p.getId(), p.getName());
                }
            }
        }
        return ResponseEntity.ok(ResultUtil.successJson(patients));
    }

    /* Tra cứu thông tin bệnh nhân theo họ tên và CMND/CCCD rồi đăng ký khám */
    // T3 — Quyền sàn parity với save (xem wxSave): inquire tạo lượt
    // khám thật nên tài khoản 0 quyền nhận 403 trước khi service chạy.
    @PostMapping(value = "inquire")
    public ResponseEntity<JSONObject> inquire(@RequestBody Patient patient) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        // C4 (iteration 50): ep tenant tra cuu theo session, theo mau C3/V1.
        // Truoc day company lay thang tu payload nen token tenant A gui company
        // tenant B + ten/CCCD benh nhan B la tao duoc luot kham that trong
        // tenant B (insert cua CrudService.save khong verify tenant). Nay:
        // session co tenant thi chi tra cuu trong tenant session; benh nhan
        // tenant khac -> result null -> warning, khong tao luot. Session rong
        // giu fallback body cu (sessionTenantOr tra payload/null nhu C3).
        String tenant = sessionTenantOr(patient.getCompany() == null ? null : patient.getCompany().getId());
        if (tenant != null) {
            patient.setCompany(new Company(tenant));
        }
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
    // T3 — Quyền sàn parity với save (xem wxSave): add tạo bệnh nhân
    // rồi tạo lượt khám nên tài khoản 0 quyền nhận 403 trước service.
    @PostMapping(value = "add")
    public ResponseEntity<JSONObject> add(@RequestBody Patient patient) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        // D1 (iteration 51): ep tenant theo session nhu inquire (C4) truoc
        // khi save. Truoc day company lay thang tu payload nen token tenant
        // A gui company tenant B tao that ca benh nhan lan luot kham o B
        // (nhanh insert cua CrudService.save khong verify tenant). Nay:
        // session co tenant thi bo qua company payload; session rong giu
        // fallback body cu; company null van giu hanh vi cu o service.
        String tenant = sessionTenantOr(patient.getCompany() == null ? null : patient.getCompany().getId());
        if (tenant != null) {
            patient.setCompany(new Company(tenant));
        }
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
