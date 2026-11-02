package com.geeke.toll.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.data.Page;
import com.geeke.common.service.ServiceException;
import com.geeke.outpatient.entity.Patient;
import com.geeke.sys.controller.BaseController;
import com.geeke.toll.entity.*;
import com.geeke.toll.service.OutpatientLogService;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.geeke.utils.constants.ErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * Nhật ký khám ngoại trúController
 *
 * @author zh
 * @version 2022-08-18
 */
@RestController
@RequestMapping(value = "/toll/outpatientLog")
public class OutpatientLogController extends BaseController {

    @Autowired
    private OutpatientLogService outpatientLogService;

    /**
     * X1: controller này extends BaseController nên không thừa hưởng guard
     * nào của CrudController — tự cấp forbidden() như CrudController.
     */
    private ResponseEntity<JSONObject> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ResultUtil.errorJson(ErrorEnum.E_502));
    }

    /**
     * X1: tenant của phiên đăng nhập; rỗng hoặc "null" (luồng nội bộ
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
     * X1: kiểm tra sở hữu cho các đường theo id trần (getLogs/update/
     * getPoverty). id không tồn tại (company null) thì cho qua như cũ —
     * service trả null/0, không tiết lộ gì thêm.
     */
    private boolean isForeign(String ownerCompanyId) {
        String tenantId = sessionTenantId();
        return tenantId != null && ownerCompanyId != null && !tenantId.equals(ownerCompanyId);
    }

    /**
     * X1: list/export ép company theo session (như W2): ghi đè companyId
     * bằng tenant phiên và xóa jgid/jgzt của body, để SQL luôn rơi vào
     * nhánh a.company_id = companyId chính xác, không rơi vào nhánh
     * institution (cùng parent) hay company lạ trong body. Trang thật
     * vốn chỉ gửi companyId của chính mình nên không đổi luồng chính.
     */
    private void forceSessionCompany(OutpatientLogRc rc) {
        String tenantId = sessionTenantId();
        if (tenantId != null) {
            rc.setCompanyId(tenantId);
            rc.setJgid(null);
            rc.setJgzt(null);
        }
    }

    /**
     * idTruy vấnNgàynhật ký
     */
    @GetMapping("/{id}")
    public ResponseEntity<JSONObject> getLogs(@PathVariable("id") String id) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC X1, parity W1.
        if (isForeign(outpatientLogService.getRegistrationCompanyId(id))) { return forbidden(); } // X1: id tran, chan tenant khac.
        OutpatientLog entity = outpatientLogService.getLogs(id);
        return ResponseEntity.ok(ResultUtil.successJson(entity));
    }

    /**
     * Truy vấntất cảNgàynhật ký
     */
    @PostMapping(value = "list")
    public ResponseEntity<JSONObject> listLog(@RequestBody OutpatientLogRc outpatientLogRc) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC X1, parity W1.
        forceSessionCompany(outpatientLogRc); // X1: body khong dieu khien tenant, nhu W2.
        Page<OutpatientLog> result = outpatientLogService.listLog(outpatientLogRc);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * SửaNgàynhật kýThông tin
     * Giữ nguyên chữ ký int (frontend chỉ dùng .then): trường hợp bị chặn
     * ném ServiceException để global handler trả 400, fail-closed.
     */
    @PutMapping(value = "update")
    public int updateLog(@RequestBody OutpatientLog outpatientLogRc) {
        if (!hasAnySessionPermission()) { throw new ServiceException("Không có quyền truy cập"); } // San RBAC X1.
        if (outpatientLogRc.getId() != null
                && isForeign(outpatientLogService.getRegistrationCompanyId(outpatientLogRc.getId()))) {
            throw new ServiceException("Không có quyền truy cập tenant khác"); // X1: ghi cheo theo id tran.
        }
        return outpatientLogService.updateLog(outpatientLogRc);

    }

    /**
     * Truy vấnhộ nghèo
     */
    @PostMapping(value = "getPoverty/{patientId}")
    public ResponseEntity<JSONObject> getPoverty(@PathVariable("patientId") String patientId) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC X1, parity W1.
        if (isForeign(outpatientLogService.getPatientCompanyId(patientId))) { return forbidden(); } // X1: id tran, chan tenant khac.
        List<Patient> poverty = outpatientLogService.getPoverty(patientId);
        return ResponseEntity.ok(ResultUtil.successJson(poverty));
    }

    /**
     * Truy vấntất cảNgàynhật ký
     */
    @PostMapping(value = "exportExcel")
    public void exportExcel(@RequestBody OutpatientLogRc outpatientLogRc, HttpServletResponse response) {
        if (!hasAnySessionPermission()) { response.setStatus(HttpServletResponse.SC_FORBIDDEN); return; } // San RBAC X1 (void export: 403, khong ghi file), nhu W1.
        forceSessionCompany(outpatientLogRc); // X1: body khong dieu khien tenant, nhu W2.
        try {
            outpatientLogService.exportExcel(outpatientLogRc,response);
        } catch (Exception e) {
            logger.error("XuấtNhật ký khám ngoại trúThất bại", e);
            throw new RuntimeException(e);
        }
    }


}
