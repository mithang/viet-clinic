package com.geeke.outpatient.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.CrudController;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.common.data.Parameter;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.geeke.medicareutils.config.MedicareConfigProperties;
import com.geeke.medicareutils.domain.respo.MdExamine;
import com.geeke.medicareutils.domain.respo.MdPreDrugData;
import com.geeke.medicareutils.service.MdMedicationOrderService;
import com.geeke.outpatient.entity.MedicalRecord;
import com.geeke.outpatient.entity.RecipelInfo;
import com.geeke.outpatient.entity.RecipelInfoReview;
import com.geeke.outpatient.entity.Registration;
import com.geeke.outpatient.service.MedicalRecordService;
import com.geeke.outpatient.service.RecipelInfoReviewService;
import com.geeke.outpatient.service.RecipelInfoService;
import com.geeke.outpatient.service.RegistrationService;
import com.geeke.outpatient.vo.PrescriptionStatisticsVO;
import com.geeke.outpatient.vo.ReviewVO;
import com.geeke.outpatient.vo.StatementVO;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * Đơn thuốcThông tinxem xétController
 * @author lc
 */
@RestController
@RequestMapping(value = "/outpatient/review")
public class RecipelInfoReviewController extends CrudController<RecipelInfoReviewService, RecipelInfoReview> {

    @Autowired
    protected RecipelInfoReviewService recipelInfoReviewService;

    @Autowired
    private MdMedicationOrderService mdMedicationOrderService;

    @Autowired
    private RecipelInfoService recipelInfoService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private MedicareConfigProperties medicareConfigProperties;

    @Override
    protected RecipelInfoReviewService getService() {
        return recipelInfoReviewService;
    }

    /**
     * Y1: tenant của phiên đăng nhập; rỗng hoặc "null" (luồng nội bộ
     * không session) thì giữ hành vi cũ, như verifyTenantOwnership (mẫu X3).
     */
    private String sessionTenantId() {
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return null;
        }
        return tenantId;
    }

    /**
     * Y1: kiểm tra sở hữu cho đường theo id trần. id không tồn tại
     * (company null) thì cho qua như cũ — service trả null/rỗng.
     */
    private boolean isForeign(String ownerCompanyId) {
        String tenantId = sessionTenantId();
        return tenantId != null && ownerCompanyId != null && !tenantId.equals(ownerCompanyId);
    }

    /**
     * Y1: company của đơn trong body (nếu có) có thuộc tenant phiên không.
     * Body không mang id đơn thì cho qua — sàn 403 đã chặn tài khoản 0 quyền.
     */
    private boolean isForeignRecipel(RecipelInfoReview entity) {
        if (entity == null || entity.getRecipelInfo() == null || entity.getRecipelInfo().getId() == null) {
            return false;
        }
        return isForeign(recipelInfoService.getRecipelInfoCompanyId(entity.getRecipelInfo().getId()));
    }

    /**
     * Y1: phạm vi thống kê hợp lệ của phiên — parent của tenant phiên
     * (trang thật gửi company.parent_id = parent của công ty đăng nhập),
     * hoặc chính tenant phiên khi tenant không có parent (parent 0/null:
     * tenant là gốc thì phạm vi là các công ty con của nó; phòng khám
     * độc lập không có con thì trả rỗng, fail-closed thay vì toàn hệ thống
     * như trước khi frontend bỏ qua điều kiện parent).
     */
    private String sessionStatisticsScopeId() {
        String tenantId = sessionTenantId();
        if (tenantId == null) {
            return null;
        }
        String parentId = recipelInfoReviewService.getCompanyParentId(tenantId);
        if (StringUtils.isBlank(parentId) || "0".equals(parentId) || "null".equals(parentId)) {
            return tenantId;
        }
        return parentId;
    }

    /**
     * Y1: listPageStatement đẩy thẳng params xuống DAO (Common.whereParams)
     * nên body điều khiển tenant như W2 — ép company.parent_id theo session:
     * xóa company.parent_id và company_id của body rồi thêm phạm vi phiên.
     * Trang thật vốn chỉ gửi parent của công ty mình nên không đổi luồng chính.
     * Dùng getNewParams() (danh sách thô, chưa bọc backtick) thay vì
     * getParams(): getParams() bọc backtick vào tên cột, gọi lần 2 ở đối số
     * service sẽ văng 400 "tên trường chứa ký tự không hợp lệ" — getParams()
     * chỉ được chạy đúng một lần, ở lời gọi service dưới đây.
     */
    private void forceSessionParent(SearchParams searchParams) {
        String scopeId = sessionStatisticsScopeId();
        if (scopeId == null) {
            return;
        }
        java.util.List<Parameter> params = searchParams.getNewParams();
        if (params == null) {
            return;
        }
        Parameter.extractAndRemoveCompanyId(params);
        params.removeIf(p -> "company.parent_id".equals(p.getColumnName())
                || "`company.parent_id`".equals(p.getColumnName()));
        params.add(new Parameter("company.parent_id", "=", scopeId));
    }

    @GetMapping("/recipelInfo/{recipelInfoId}")
    public ResponseEntity<JSONObject> getByRecipelInfoId(@PathVariable("recipelInfoId") String recipelInfoId) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC Y1, parity save/saveList/delete cùng file.
        if (isForeign(recipelInfoService.getRecipelInfoCompanyId(recipelInfoId))) { return forbidden(); } // Y1: id don tran, chan tenant khac (DAO truoc day WHERE chi theo recipel_info_id, khong loc company).
        RecipelInfoReview entity = recipelInfoReviewService.getByRecipelInfoId(recipelInfoId);
        return ResponseEntity.ok(ResultUtil.successJson(entity));
    }

    @Override
    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@RequestBody RecipelInfoReview entity) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        //Lấyxongnguyên của Đơn thuốc, bệnh án, Thông tin đăng ký khám
        RecipelInfo recipelInfo = recipelInfoService.get(entity.getRecipelInfo().getId());
        MedicalRecord medicalRecord = medicalRecordService.get(entity.getMedicalRecord().getId());
        Registration registration = registrationService.get(entity.getRecipelInfo().getRegistration().getId());
        if(recipelInfo.getIsPre() && entity.getReviewResult().equals(1) && medicareConfigProperties.getCheck().equals("true") ){
            //điện tửĐơn thuốcxét duyệt
            recipelInfo.setRegistration(registration);
            entity.setRecipelInfo(recipelInfo);
            entity.setMedicalRecord(medicalRecord);
            //Tải lênkiểm tra trước
            JSONObject jsonObject = mdMedicationOrderService.validateElectronicPrescriptionUpload_Ld7801(entity);
            //chữ kýđiện tửĐơn thuốc
            JSONObject signResult = mdMedicationOrderService.signElectronicPrescriptionWithInsurance_Ld7802(entity, SessionUtils.getUserJson().getString("id"));
            //Tải lên đơn thuốc điện tử
            JSONObject uploadResult = mdMedicationOrderService.uploadElectronicPrescription_Ld7101(entity,SessionUtils.getUserJson().getString("id"));
        }
        String id = recipelInfoReviewService.save(entity).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    @PostMapping(value = "save/list")
    public ResponseEntity<JSONObject> saveList(@RequestBody List<RecipelInfoReview> entitys) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        if (CollectionUtil.isNotEmpty(entitys)) {
            entitys.forEach(recipelInfoReviewService::save);
        }
        return ResponseEntity.ok(ResultUtil.successJson("thao tácThành công"));
    }


    @Override
    @DeleteMapping(value = "delete")
    public ResponseEntity<JSONObject> delete(@RequestBody RecipelInfoReview entity) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        int rows = recipelInfoReviewService.delete(entity);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

    @GetMapping("/form/{recipelInfoId}")
    public ResponseEntity<JSONObject> reviewForm(@PathVariable("recipelInfoId") String recipelInfoId) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC Y1.
        if (isForeign(recipelInfoService.getRecipelInfoCompanyId(recipelInfoId))) { return forbidden(); } // Y1: cung ownership voi getByRecipelInfoId (reviewForm gan them chan doan + chi tiet don tu chinh id do).
        ReviewVO vo = recipelInfoReviewService.reviewForm(recipelInfoId);
        return ResponseEntity.ok(ResultUtil.successJson(vo));
    }

    @PostMapping(value = {"list/statement", ""})
    public ResponseEntity<JSONObject> listPageStatement(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC Y1.
        forceSessionParent(searchParams); // Y1: ep company.parent_id theo session, body khong dieu khien tenant duoc nua.
        Page<StatementVO> result = recipelInfoReviewService.listPageStatement(searchParams.getParams(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @GetMapping("/prescription/statistics/{id}")
    public ResponseEntity<JSONObject> prescriptionStatistics(@PathVariable("id") String id,
                                                             @RequestParam String startTime,
                                                             @RequestParam String endTime) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC Y1.
        String scopeId = sessionStatisticsScopeId(); // Y1: id tren path la parent company do caller chi dinh — chi chap nhan tenant phien hoac parent cua no.
        if (scopeId != null && !scopeId.equals(id) && !sessionTenantId().equals(id)) { return forbidden(); }
        PrescriptionStatisticsVO entity = recipelInfoReviewService.prescriptionStatistics(id,startTime,endTime);
        return ResponseEntity.ok(ResultUtil.successJson(entity));
    }


    /**
     * BHYTđiện tửĐơn thuốclấy thuốcTruy vấn
     * @param entity
     * @return
     */
    @PostMapping("/pre/getMdMedicineInfo")
    public ResponseEntity<JSONObject> getMdMedicineInfo(@RequestBody RecipelInfoReview entity) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC Y1.
        if (isForeignRecipel(entity)) { return forbidden(); } // Y1: chan tra cuu BHYT theo don tenant khac.
        JSONObject jsonObject = mdMedicationOrderService.queryPrescriptionDispensingResult_Ld7804(entity);
        MdPreDrugData mdPreDrugData = JSONObject.parseObject(jsonObject.getString("data"), MdPreDrugData.class);
        return ResponseEntity.ok(ResultUtil.successJson(mdPreDrugData));
    }

    /**
     * BHYTxét duyệtTruy vấn
     * @param entity
     * @return
     */
    @PostMapping("/pre/getMdExamineInfo")
    public ResponseEntity<JSONObject> getMdExamineInfo(@RequestBody RecipelInfoReview entity) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC Y1.
        if (isForeignRecipel(entity)) { return forbidden(); } // Y1: chan tra cuu BHYT theo don tenant khac.
        JSONObject jsonObject = mdMedicationOrderService.queryElectronicPrescriptionReviewResult_Ld7805(entity);
        MdExamine mdExamine = JSONObject.parseObject(jsonObject.getString("data"), MdExamine.class);
        return ResponseEntity.ok(ResultUtil.successJson(mdExamine));
    }

    /**
     * Đơn thuốcThông tinTruy vấn
     * @param entity
     * @return
     */
    @PostMapping("/pre/getMdRecInfo")
    public ResponseEntity<JSONObject> getMdPreInfo(@RequestBody RecipelInfoReview entity) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC Y1 (than ham chi tra rong, them san cho dong mau).

        return ResponseEntity.ok(ResultUtil.successJson());
    }


    /**
     * Hủy đơn thuốc điện tử
     * @param
     * @return
     */
    @PostMapping("/pre/revoke")
    public ResponseEntity<JSONObject> revokePre(@RequestBody RecipelInfoReview recipelInfoReview, @RequestParam String undoRea) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC Y1.
        if (isForeignRecipel(recipelInfoReview)) { return forbidden(); } // Y1: chan huy don tenant khac — kiem tra TRUOC khi goi dich vu huy BHYT.
         mdMedicationOrderService.cancelElectronicPrescription_Ld7104(recipelInfoReview,undoRea);
         recipelInfoReview.setReviewerId(SessionUtils.getUserJson().getString("id"));
         recipelInfoReview.setReviewStatus(2);
         recipelInfoReviewService.save(recipelInfoReview);
        return ResponseEntity.ok(ResultUtil.successJson("hủyThành công!"));

    }




}
