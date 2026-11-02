package com.geeke.outpatient.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.common.data.Parameter;
import com.geeke.outpatient.dao.RemoteDiagnosisTreatmentDao;
import com.geeke.outpatient.entity.*;
import com.geeke.outpatient.service.PatientService;
import com.geeke.outpatient.service.RecipelDetailService;
import com.geeke.outpatient.service.RecipelInfoService;
import com.geeke.outpatient.service.RemoteDiagnosisTreatmentService;
import com.geeke.sys.controller.BaseController;
import com.geeke.toll.entity.TollEvt;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.geeke.utils.constants.ErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * từ xaChẩn đoánQuản lýController
 * @author zh
 * @version 2023-12-06
 */
@RestController
@RequestMapping(value = "/outpatient/remoteDiagnosisTreatment")
public class RemoteDiagnosisTreatmentController extends BaseController {

	@Autowired
	private PatientService patientService;
	@Autowired
	private RecipelInfoService recipelInfoService;
	@Autowired
	private RemoteDiagnosisTreatmentService remoteDiagnosisTreatmentService;

    /**
     * X2: controller này extends BaseController nên không thừa hưởng guard
     * nào của CrudController — tự cấp forbidden() như CrudController.
     */
    private ResponseEntity<JSONObject> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ResultUtil.errorJson(ErrorEnum.E_502));
    }

    /**
     * X2: tenant của phiên đăng nhập; rỗng hoặc "null" (luồng nội bộ
     * không session) thì giữ hành vi cũ, như verifyTenantOwnership.
     */
    private String sessionTenantId() {
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return null;
        }
        return tenantId;
    }

    /**
     * X2: kiểm tra sở hữu cho các đường theo id trần. id không tồn tại
     * (company null) thì cho qua như cũ — service trả null/0, không tiết
     * lộ gì thêm.
     */
    private boolean isForeign(String ownerCompanyId) {
        String tenantId = sessionTenantId();
        return tenantId != null && ownerCompanyId != null && !tenantId.equals(ownerCompanyId);
    }

    /**
     * X2: listPage không qua CrudService.ensureCompanyFilter (diagnosisPage
     * đẩy thẳng params xuống DAO, Common.whereParams) nên body điều khiển
     * tenant như W2 — ép company theo session tại đây: xóa company_id của
     * body rồi thêm tenant phiên. Trang thật vốn chỉ gửi companyId của
     * chính mình nên không đổi luồng chính.
     */
    private void forceSessionCompany(SearchParams searchParams) {
        String tenantId = sessionTenantId();
        if (tenantId == null) {
            return;
        }
        List<Parameter> params = searchParams.getParams();
        if (params == null) {
            params = new ArrayList<>();
            searchParams.setParams(params);
        }
        Parameter.extractAndRemoveCompanyId(params);
        params.add(new Parameter("company_id", "=", tenantId));
    }

    /** TheoidTruy vấn */
    @GetMapping("/{id}")
    public ResponseEntity<JSONObject> getById(@PathVariable("id") String id) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC X2, parity W1.
        if (isForeign(remoteDiagnosisTreatmentService.getRemoteDiagnosisCompanyId(id))) { return forbidden(); } // X2: id tran, chan tenant khac.
        RemoteDiagnosisTreatment entity = remoteDiagnosisTreatmentService.diagnosisById(id);
        return ResponseEntity.ok(ResultUtil.successJson(entity));
    }

    /** TheoĐăng ký khámTruy vấn */
    @GetMapping("registrationId/{registrationId}")
    public ResponseEntity<JSONObject> getRegistrationId(@PathVariable("registrationId") String registrationId) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC X2, parity W1.
        if (isForeign(remoteDiagnosisTreatmentService.getRemoteDiagnosisCompanyIdByRegistrationId(registrationId))) { return forbidden(); } // X2: id tran, chan tenant khac.
        RemoteDiagnosisTreatment entity = remoteDiagnosisTreatmentService.getRegistrationId(registrationId);
        return ResponseEntity.ok(ResultUtil.successJson(entity));
    }

    /** Truy vấn phân trang */
    @PostMapping(value = {"list", ""})
    public ResponseEntity<JSONObject> listPage(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC X2, parity W1.
        forceSessionCompany(searchParams); // X2: body khong dieu khien tenant, nhu W2.
        Page<RemoteDiagnosisTreatment> result = remoteDiagnosisTreatmentService.diagnosisPage(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /** Lưuđăng kýVui lòng */
    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@RequestBody RemoteDiagnosisTreatment entity) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC X2, parity W1.
        if (StringUtils.isNotBlank(entity.getId())
                && isForeign(remoteDiagnosisTreatmentService.getRemoteDiagnosisCompanyId(entity.getId()))) { return forbidden(); } // X2: ghi cheo theo id tran.
        if (StringUtils.isBlank(entity.getId()) && entity.getCompany() != null
                && isForeign(entity.getCompany().getId())) { return forbidden(); } // X2: tao moi duoi company tenant khac.
        String id = remoteDiagnosisTreatmentService.diagnosisSave(entity).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    /** Đơn thuốcThông tinLưu */
    @PostMapping(value = "addRecipel")
    public ResponseEntity<JSONObject> addRecipel(@RequestBody RecipelInfo recipelInfo) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC X2, parity W1.
        RemoteDiagnosisTreatment entity = remoteDiagnosisTreatmentService.diagnosisById(recipelInfo.getDiagnosisId());
        if (entity != null && entity.getCompany() != null
                && isForeign(entity.getCompany().getId())) { return forbidden(); } // X2: don thuoc mang company theo entity la, nhu ke hoach.
        // entity null: giữ nguyên đường 500 cũ (NPE ở getCompany) như tiền lệ T1 — không đổi ngữ nghĩa ngoài sàn.
        recipelInfo.getCompany().setId(entity.getCompany().getId());
        String id = recipelInfoService.save(recipelInfo).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    /** Sửa trạng thái */
    @PostMapping(value = "modifiedState")
    public ResponseEntity<JSONObject> modifiedState(@RequestBody RemoteDiagnosisTreatment entity) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC X2, parity W1.
        if (StringUtils.isNotBlank(entity.getId())
                && isForeign(remoteDiagnosisTreatmentService.getRemoteDiagnosisCompanyId(entity.getId()))) { return forbidden(); } // X2: ghi cheo theo id tran.
        final int i = remoteDiagnosisTreatmentService.modifiedState(entity);
        return ResponseEntity.ok(ResultUtil.successJson(i));
    }

    /** Sửatừ xaThu phíThông tin */
    @PostMapping(value = "chargeState")
    public ResponseEntity<JSONObject> chargeState(@RequestBody @NotNull TollEvt tollEvt) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC X2, parity W1.
        // X2: thu phí chéo — chặn khi lượt khám hoặc bất kỳ đơn nào trong body thuộc tenant khác.
        if (tollEvt.getTollInfo() != null && tollEvt.getTollInfo().getMedical() != null
                && tollEvt.getTollInfo().getMedical().getRegistration() != null
                && isForeign(remoteDiagnosisTreatmentService.getRegistrationCompanyId(
                        tollEvt.getTollInfo().getMedical().getRegistration().getId()))) { return forbidden(); }
        if (tollEvt.getRecipelInfos() != null) {
            for (RecipelInfoEvt e : tollEvt.getRecipelInfos()) {
                if (e != null && e.getRecipelInfo() != null
                        && isForeign(remoteDiagnosisTreatmentService.getRecipelInfoCompanyId(e.getRecipelInfo().getId()))) { return forbidden(); }
            }
        }
        final int i = remoteDiagnosisTreatmentService.chargeState(tollEvt);
        return ResponseEntity.ok(ResultUtil.successJson(i));
    }

    /** Xóađăng kýVui lòng */
    @PostMapping(value = "delete")
    public ResponseEntity<JSONObject> delete(@RequestBody RemoteDiagnosisTreatment entity) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC X2, parity W1.
        if (StringUtils.isNotBlank(entity.getId())
                && isForeign(remoteDiagnosisTreatmentService.getRemoteDiagnosisCompanyId(entity.getId()))) { return forbidden(); } // X2: xoa cheo theo id tran.
        String id = entity.getId();
        int rows = remoteDiagnosisTreatmentService.diagnosisDelete(id);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

    @PostMapping(value = "listAll")
    public ResponseEntity<JSONObject> listAll(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC X2, parity W1.
        // listAll đã ép tenant qua CrudService.listAll -> ensureCompanyFilter (đã kiểm chứng: param lạ vẫn chỉ trả bệnh nhân tenant phiên).
        List<Patient> result = patientService.listAll(searchParams.getParams(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }


}
