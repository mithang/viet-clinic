package com.geeke.outpatient.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.CrudController;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.common.data.Parameter;
import com.geeke.common.service.ServiceException;
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
     * N1: chặn ghi review của tenant khác theo đơn cha, TRƯỚC mọi ghi.
     * Review không có company riêng nên guard generic của CrudService luôn
     * pass trên company null (mapper không map company cấp review; mapper
     * update chỉ WHERE id, mapper delete là DELETE vật lý không del_flag).
     * - Dòng đã tồn tại (id khác rỗng): resolve đơn cha của stored row
     *   bằng lookup trực tiếp một cột (KHÔNG dùng get() — get() INNER
     *   JOIN registration nên trả null khi registration cha thiếu, đã
     *   chứng minh live: cả hai recipel test đều thiếu dòng registration
     *   nên get() luôn null và guard dùng get() không bao giờ chặn),
     *   rồi so company của đơn cha với tenant phiên; lạ thì ném 400.
     *   Guard đặt trước save/delete vì delete vật lý không cứu được bằng
     *   guard sau (lỗ A2/A3 đã chứng minh ở M6).
     * - Insert (id rỗng): chặn tường minh bằng isForeignRecipel (đơn
     *   trong body lạ) thay vì chỉ dựa vào tác dụng phụ của chuỗi get()
     *   ở save() — cùng mã 400 như hành vi A1 cũ, không đổi phản hồi;
     *   và phủ luôn saveList, đường gọi thẳng service.save từng dòng
     *   nên trước đây bypass cả chuỗi get() (lỗ A4/A4c).
     * - id không tồn tại (lookup null) hoặc phiên không session: cho qua
     *   như cũ, parity Y1 (service trả null / 0 dòng).
     * Ném ServiceException (400) như verifyTenantOwnership của
     * CrudService, đồng nhất với mã lỗi tenant của A1.
     */
    private void checkReviewWriteTenant(RecipelInfoReview entity) {
        if (entity == null) {
            return;
        }
        if (StringUtils.isNotBlank(entity.getId())) {
            String storedRecipelId = recipelInfoReviewService.getRecipelInfoIdByReviewId(entity.getId());
            if (StringUtils.isNotBlank(storedRecipelId)
                    && isForeign(recipelInfoService.getRecipelInfoCompanyId(storedRecipelId))) {
                throw new ServiceException("Không có quyền truy cập tenant khác");
            }
        }
        if (isForeignRecipel(entity)) {
            throw new ServiceException("Không có quyền truy cập tenant khác");
        }
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

    /**
     * DS19: generic-read ownership, completing the Y1/N1 custom guards
     * (which left the CrudController generics untouched). recipel_info_
     * review has NO company_id column and RecipelInfoReviewDao.get maps
     * only the NESTED recipelInfo.company.id, so the review entity's own
     * DataEntity.company stays null: CrudService.verifyTenantOwnership
     * early-returns and ensureCompanyFilter skips entities without an own
     * "company" field. Probe-proven (tools/ds19-review.py pre 19/19):
     * any permissioned session could GET another institution's review by
     * id (plus joined patient name/phone/card and diagnosis), and
     * listPage/listAll with no params — or an attacker recipelInfo.
     * company_id filter — returned foreign institutions' prescriptions.
     * Scope choice (deliberate, NOT strict own-tenant): this module's
     * established scope is the INSTITUTION GROUP, not the single clinic
     * — the real review UI (review.vue/randomReview.vue) lists with
     * company.parent_id = loginCompany.parent.id and opens the detail
     * via generic getById, and Y1's listPageStatement forces the same
     * parent scope. Forcing own-tenant would regress sibling-clinic
     * detail views inside an institution. So, controller only, reusing
     * isForeign + getRecipelInfoIdByReviewId (direct lookup, NOT get()
     * — get() INNER JOINs registration and returns null when the parent
     * registration is missing, N1):
     * - getById: allow own tenant OR a prescription company whose
     *   parent_id equals the Y1 session scope (parent of session tenant,
     *   or the tenant itself when parentless); anything else -> 403.
     *   Nonexistent (lookup blank) keeps the generic 200+null contract;
     *   no-session passes like the Y1/N1 guards.
     * - listPage/listAll: same parent-scope force as forceSessionParent
     *   (Y1-proven), PLUS a null-params fix: forceSessionParent returns
     *   early when the body carries no params, so a no-params list call
     *   stayed unforced (probe-proven: total 2 with the foreign row in
     *   it). This override creates the param list when absent, then
     *   strips client company/company.parent_id params and forces
     *   company.parent_id = session scope. Legit UI calls already send
     *   that parent value, so their results are unchanged; attacker
     *   filters are ignored. forceSessionParent itself is left untouched
     *   (statement page always sends params).
     * - bulkInsert/bulkUpdate/bulkDelete: STRICT own-tenant (the write
     *   convention of save/delete in this file, not the read group
     *   scope): any item foreign (body prescription for insert/update,
     *   stored row for update/delete) -> 403 fail-closed for the whole
     *   list BEFORE delegating. Delegated own calls keep the existing
     *   mapper-missing 500 (RecipelInfoReviewDao.xml has no bulk
     *   statements), unchanged.
     * No service change, no new resources, no frontend change; the
     * custom Y1/N1 endpoints and listPageStatement are untouched.
     */
    private boolean isForeignReview(String reviewId) {
        if (StringUtils.isBlank(reviewId)) {
            return false;
        }
        String storedRecipelId = recipelInfoReviewService.getRecipelInfoIdByReviewId(reviewId);
        if (StringUtils.isBlank(storedRecipelId)) {
            return false;
        }
        return isForeign(recipelInfoService.getRecipelInfoCompanyId(storedRecipelId));
    }

    private boolean isOutsideReviewGroup(String reviewId) {
        String tenantId = sessionTenantId();
        if (tenantId == null || StringUtils.isBlank(reviewId)) {
            return false;
        }
        String storedRecipelId = recipelInfoReviewService.getRecipelInfoIdByReviewId(reviewId);
        if (StringUtils.isBlank(storedRecipelId)) {
            return false;
        }
        String companyId = recipelInfoService.getRecipelInfoCompanyId(storedRecipelId);
        if (StringUtils.isBlank(companyId)) {
            return false;
        }
        if (tenantId.equals(companyId)) {
            return false;
        }
        String scopeId = sessionStatisticsScopeId();
        if (scopeId == null) {
            return false;
        }
        String parentId = recipelInfoReviewService.getCompanyParentId(companyId);
        return StringUtils.isBlank(parentId) || !scopeId.equals(parentId);
    }

    @Override
    @GetMapping(value = "/{id}")
    public ResponseEntity<JSONObject> getById(@PathVariable("id") String id) {
        if (!hasAnySessionPermission()) { return forbidden(); }
        if (isOutsideReviewGroup(id)) { return forbidden(); }
        RecipelInfoReview entity = getService().get(id);
        return ResponseEntity.ok(ResultUtil.successJson(entity));
    }

    private void forceReviewListScope(SearchParams searchParams) {
        String scopeId = sessionStatisticsScopeId();
        if (scopeId == null) {
            return;
        }
        java.util.List<Parameter> params = searchParams.getNewParams();
        if (params == null) {
            params = new java.util.ArrayList<>();
            searchParams.setParams(params);
        }
        Parameter.extractAndRemoveCompanyId(params);
        params.removeIf(p -> "company.parent_id".equals(p.getColumnName())
                || "`company.parent_id`".equals(p.getColumnName()));
        params.add(new Parameter("company.parent_id", "=", scopeId));
    }

    @Override
    @PostMapping(value = {"list", ""})
    public ResponseEntity<JSONObject> listPage(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); }
        forceReviewListScope(searchParams);
        Page<RecipelInfoReview> result = getService().listPage(searchParams.getParams(), searchParams.getOffset(),
                searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @Override
    @PostMapping(value = "listAll")
    public ResponseEntity<JSONObject> listAll(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); }
        forceReviewListScope(searchParams);
        java.util.List<RecipelInfoReview> result = getService().listAll(searchParams.getParams(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @Override
    @PostMapping(value = "bulkInsert")
    public ResponseEntity<JSONObject> bulkInsert(@RequestBody List<RecipelInfoReview> entitys) {
        if (!hasAnySessionPermission()) { return forbidden(); }
        if (CollectionUtil.isNotEmpty(entitys)) {
            for (RecipelInfoReview e : entitys) {
                if (isForeignReview(e.getId()) || isForeignRecipel(e)) {
                    return forbidden();
                }
            }
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @PostMapping(value = "bulkUpdate")
    public ResponseEntity<JSONObject> bulkUpdate(@RequestBody List<RecipelInfoReview> entitys) {
        if (!hasAnySessionPermission()) { return forbidden(); }
        if (CollectionUtil.isNotEmpty(entitys)) {
            for (RecipelInfoReview e : entitys) {
                if (isForeignReview(e.getId()) || isForeignRecipel(e)) {
                    return forbidden();
                }
            }
        }
        return super.bulkUpdate(entitys);
    }

    @Override
    @PostMapping(value = "bulkDelete")
    public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<RecipelInfoReview> entitys) {
        if (!hasAnySessionPermission()) { return forbidden(); }
        if (CollectionUtil.isNotEmpty(entitys)) {
            for (RecipelInfoReview e : entitys) {
                if (isForeignReview(e.getId())) {
                    return forbidden();
                }
            }
        }
        return super.bulkDelete(entitys);
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
        checkReviewWriteTenant(entity); // N1: chan ghi review tenant khac theo don cha (stored row + don trong body), truoc chuoi get().
        //DS89: guard null truoc chuoi deref — body thieu don/benh an/dang ky
        //truoc day NPE 500 (save:365-367). Chi dong don la deref vo dieu kien
        //(getIsPre); benh an/dang ky chi dung trong nhanh BHYT nen dong stored
        //thieu chi 400 khi nhanh do that su chay — giu nguyen hanh vi cu:
        //dang ky treo (khong co dong registration) van luu duoc khi check=false.
        if (entity.getRecipelInfo() == null || entity.getRecipelInfo().getId() == null) {
            throw new ServiceException("Không tìm thấy đơn thuốc");
        }
        //Lấyxongnguyên của Đơn thuốc, bệnh án, Thông tin đăng ký khám
        RecipelInfo recipelInfo = recipelInfoService.get(entity.getRecipelInfo().getId());
        if (recipelInfo == null) {
            throw new ServiceException("Không tìm thấy đơn thuốc");
        }
        if (entity.getMedicalRecord() == null || entity.getMedicalRecord().getId() == null) {
            throw new ServiceException("Không tìm thấy bệnh án");
        }
        MedicalRecord medicalRecord = medicalRecordService.get(entity.getMedicalRecord().getId());
        if (entity.getRecipelInfo().getRegistration() == null
                || entity.getRecipelInfo().getRegistration().getId() == null) {
            throw new ServiceException("Không tìm thấy đăng ký khám");
        }
        Registration registration = registrationService.get(entity.getRecipelInfo().getRegistration().getId());
        boolean bhytUpload = Boolean.TRUE.equals(recipelInfo.getIsPre())
                && Integer.valueOf(1).equals(entity.getReviewResult())
                && "true".equals(medicareConfigProperties.getCheck());
        if (bhytUpload && (medicalRecord == null || registration == null)) {
            throw new ServiceException("Không tìm thấy bệnh án hoặc đăng ký khám của đơn thuốc");
        }
        if (bhytUpload){
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
            // N1: tien kiem TOAN BO list truoc khi luu dong nao — fail-closed
            // ca list (mau bulkDelete): mot dong la thi khong dong nao duoc ghi.
            entitys.forEach(this::checkReviewWriteTenant);
            entitys.forEach(recipelInfoReviewService::save);
        }
        return ResponseEntity.ok(ResultUtil.successJson("thao tácThành công"));
    }


    @Override
    @DeleteMapping(value = "delete")
    public ResponseEntity<JSONObject> delete(@RequestBody RecipelInfoReview entity) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        checkReviewWriteTenant(entity); // N1: chan xoa vat ly review tenant khac theo don cha cua stored row, truoc delete.
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
        checkReviewWriteTenant(recipelInfoReview); // N1: revoke goi thang service.save (khong qua save() controller) nen can guard stored row rieng; dat truoc cuoc goi BHYT.
         mdMedicationOrderService.cancelElectronicPrescription_Ld7104(recipelInfoReview,undoRea);
         recipelInfoReview.setReviewerId(SessionUtils.getUserJson().getString("id"));
         recipelInfoReview.setReviewStatus(2);
         recipelInfoReviewService.save(recipelInfoReview);
        return ResponseEntity.ok(ResultUtil.successJson("hủyThành công!"));

    }




}
