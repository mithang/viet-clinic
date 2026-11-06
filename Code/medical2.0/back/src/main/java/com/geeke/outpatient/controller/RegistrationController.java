package com.geeke.outpatient.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.admin.entity.User;
import com.geeke.admin.service.UserService;
import com.geeke.common.constants.BizConstants;
import com.geeke.common.controller.CrudController;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.common.service.ServiceException;
import com.geeke.medicareutils.config.MedicareConfigProperties;
import com.geeke.medicareutils.domain.respo.Output_2201;
import com.geeke.medicareutils.service.MdPsnDataService;
import com.geeke.medicareutils.service.MdRegistrationService;
import com.geeke.outpatient.entity.*;
import com.geeke.org.entity.Company;
import com.geeke.common.data.Parameter;
import com.geeke.outpatient.service.RecipelDetailService;
import com.geeke.outpatient.service.RegistrationService;
import com.geeke.toll.service.TollInfoService;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.geeke.utils.constants.ErrorEnum;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Thông tin đăng ký khámController
 * @author txl
 * @version 2022-06-15
 */
@RestController
@RequestMapping(value = "/outpatient/registration")
public class RegistrationController extends CrudController<RegistrationService, Registration> {

    @Autowired
    protected RegistrationService registrationService;

    @Autowired
    private UserService userService;

    @Autowired
    private TollInfoService tollInfoService;

    @Autowired
    private RecipelDetailService recipelDetailService;

    @Autowired
    private DataBean dataBean;

    @Autowired
    private MdRegistrationService mdRegistrationService;

    @Autowired
    private MdPsnDataService mdPsnDataService;

    @Autowired
    private MedicareConfigProperties medicareConfigProperties;

    @Override
    protected RegistrationService getService() {
        return registrationService;
    }

    /**
     * Truy vấnPhòng khám của Bác sĩ
     * @return
     */
    @GetMapping("/doctor")
    public ResponseEntity<JSONObject> getDocByCompanyId() {
        if (!hasAnySessionPermission()) { return forbidden(); } // U2(b): san RBAC nhu cac duong doc cung controller (T1); co caller UI that listDoctorsAll.
        List<User> doctors = userService.getUserByCompanyIdAndJob(SessionUtils.getLoginTenantId(), "Bác sĩ");
        return ResponseEntity.ok(ResultUtil.successJson(doctors));
    }

    @GetMapping("/doctornew")
    public ResponseEntity<JSONObject> doctornew() {
        if (!hasAnySessionPermission()) { return forbidden(); } // U2(b): san RBAC.
        // V2: statement getUserByCompanyIdAndJobV2 nay dung SQL NEW (bac si
        // co lich truc hom nay) — xem UserDao.xml.
        List<User> doctors = userService.getUserByCompanyIdAndJobV2(SessionUtils.getLoginTenantId(), "Bác sĩ");
        return ResponseEntity.ok(ResultUtil.successJson(doctors));
    }

    // V1 (iteration 16): ep tenant theo session, theo mau T2/ensureCompanyFilter.
    // Body khong duoc dieu khien tenant nua: session rong/"null" (luong noi bo
    // khong session, nhu verifyTenantOwnership) moi giu gia tri body cu.
    private String sessionTenantOr(String fallback) {
        String tenant = SessionUtils.getLoginTenantId();
        return (StringUtils.isNotBlank(tenant) && !"null".equals(tenant)) ? tenant : fallback;
    }

    @PostMapping("/wxDoctor")
    public ResponseEntity<JSONObject> getWxDocByCompanyId(@RequestBody User user) {
        if (!hasAnySessionPermission()) { return forbidden(); } // U2(b): san RBAC.
        // V1: companyId ep theo session, khong lay tu body (truoc day token tenant A
        // gui company tenant B la doc duoc bac si cua B — tai hien tools/v1-repro.py).
        String companyId = sessionTenantOr(user.getCompany() == null ? null : user.getCompany().getId());
        List<User> doctors = userService.getUserByWxCompanyIdAndJob(companyId, user.getOffice(), "Bác sĩ");
        return ResponseEntity.ok(ResultUtil.successJson(doctors));
    }


    // Frontend gọi POST (xem api/outpatient/registration.js: updateStatus
    // dùng method 'post'), nhưng trước đây backend chỉ có @GetMapping nên
    // Spring trả 405 "Phương thức không hỗ trợ: POST". Giữ GET để tương
    // thích các caller cũ, thêm POST cho frontend web.
    @RequestMapping(value = "/status", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<JSONObject> updateStatus(@RequestParam("id") String id, @RequestParam("status") String status, @RequestParam("departmentId") String departmentId, @RequestParam("doctorId") String doctorId) {
        if (!hasAnySessionPermission()) { return forbidden(); } // U2(a): san RBAC nhu cac duong ghi cung controller.
        int res = registrationService.updateStatus(id, status, departmentId, doctorId);
        return ResponseEntity.ok(ResultUtil.successJson(res));
    }

    // Hủy đăng ký (trảsố): frontend gọi POST với params
    // (api/outpatient/registration.js: refundRegistrationPay dùng method
    // 'post'), nhưng trước đây backend chỉ có @GetMapping nên Spring trả
    // 405 "Phương thức không hỗ trợ: POST". Giữ GET cho các caller cũ,
    // thêm POST cho frontend web. Đây KHÔNG phải xóa cứng: service chỉ
    // UPDATE status (mặc định frontend gửi registrationStatus_2 = đã hủy),
    // ghi refund_registration_pay_type/remarks và exit_number_date; bản
    // ghi vẫn còn trong bảng registration và vẫn xem được, khác với
    // POST /delete của CrudController (xóa logic: del_flag=1, vì
    // Registration.getBusTableHasDelFlag() = true). Hủy không tự khôi
    // phục qua API này — muốn dùng lại phải tạo đăng ký mới hoặc sửa
    // status bằng nghiệp vụ khác; controller vẫn chặn hủy lại khi đã ở
    // trạng thái đã hủy/đã khám/đã hoàn phí (xem nhánh kiểm tra status
    // trong method này, áp dụng khi refundRegistrationRemarks mang mã
    // trạng thái đã hủy).
    @RequestMapping(value = "/refundRegistrationPay", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<JSONObject> refundRegistrationPay(String id, String status,
                                                            String refundRegistrationPayType, String refundRegistrationRemarks
    ) {
        if (!hasAnySessionPermission()) { return forbidden(); } // U2(a): san RBAC nhu cac duong ghi cung controller.
        int res;
        Date exitNumberDate = new Date();
        if (BizConstants.REG_STATUS_REFUNDED.equals(refundRegistrationRemarks)) {
            Registration registration = registrationService.get(id);
            if (BizConstants.REG_STATUS_PENDING.equals(registration.getStatus().getValue())) {
                String message = "bạnĐãhoàn thànhkýĐến, Như cầnhủy số khámVui lòngĐếnBệnh việntiến hành dòng!";
                return ResponseEntity.ok(ResultUtil.successJson(message));
            }
            if (BizConstants.REG_STATUS_VISITED.equals(registration.getStatus().getValue())) {
                String message = "bạnĐãhoàn thànhkhám bệnh, Nếu có thắc mắc, vui lòng đến bệnh viện để tìm hiểu chi tiết!";
                return ResponseEntity.ok(ResultUtil.successJson(message));
            }
            if (BizConstants.REG_STATUS_REFUNDED.equals(registration.getStatus().getValue())) {
                String message = "bạnĐãlấyhủyđặt lịch, khôngcó thểlại lầnlấyhủy!";
                return ResponseEntity.ok(ResultUtil.successJson(message));
            }
            if (BizConstants.REG_STATUS_FEE_REFUNDED.equals(registration.getStatus().getValue())) {
                String message = "đóđặt lịchĐãhết hiệu lực!";
                return ResponseEntity.ok(ResultUtil.successJson(message));
            }
            String remarks = "";
            res = registrationService.refundRegistrationPay(id, status, refundRegistrationPayType, remarks, exitNumberDate);
        } else {
            res = registrationService.refundRegistrationPay(id, status, refundRegistrationPayType, refundRegistrationRemarks, exitNumberDate);
        }
        if ("true".equals(medicareConfigProperties.getCheck())){
            //mởBHYThủy số khám
            Registration registration = registrationService.get(id);
            mdRegistrationService.revokeRegistrationInfo_2202(registration);
        }
        return ResponseEntity.ok(ResultUtil.successJson(res));
    }

    @PostMapping("/wxReturnPay")
    public ResponseEntity<JSONObject> wxReturnPay(@RequestBody Registration registration) {
        if (!hasAnySessionPermission()) { return forbidden(); } // U2(a): san RBAC nhu cac duong ghi cung controller, truoc khi service chay.
        int res;
        Date exitNumberDate = new Date();
        Registration registrations = registrationService.get(registration.getId());
        if (BizConstants.REG_STATUS_PENDING.equals(registrations.getStatus().getValue())) {
            String message = "bạnĐãhoàn thànhkýĐến, Như cầnhủy số khámVui lòngĐếnBệnh việntiến hành dòng!";
            throw new ServiceException(message);
        }
        if (BizConstants.REG_STATUS_VISITED.equals(registrations.getStatus().getValue())) {
            String message = "bạnĐãhoàn thànhkhám bệnh, Nếu có thắc mắc, vui lòng đến bệnh viện để tìm hiểu chi tiết!";
            throw new ServiceException(message);
        }
        if (BizConstants.REG_STATUS_REFUNDED.equals(registrations.getStatus().getValue())) {
            String message = "bạnĐãlấyhủyđặt lịch, khôngcó thểlại lầnlấyhủy!";
            throw new ServiceException(message);
        }
        if (BizConstants.REG_STATUS_FEE_REFUNDED.equals(registrations.getStatus().getValue())) {
            String message = "đóđặt lịchĐãhết hiệu lực!";
            throw new ServiceException(message);
        }
        registrations.setExitNumberDate(exitNumberDate);
        registrations.setStatus(registration.getStatus());
        registrations.setRefundRegistrationRemarks(registration.getRefundRegistrationRemarks());
        registrations.setRefundRegistrationPayType(registration.getRefundRegistrationPayType());
        res = registrationService.wxReturnPay(registrations);

        return ResponseEntity.ok(ResultUtil.successJson(res));
    }

    @Override
    @PostMapping(value = {"list", ""})
    public ResponseEntity<JSONObject> listPage(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        Page<Registration> result = registrationService.listPage(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        registrationService.updateExpiredStatusFromPage(result, row -> row.getCompany().getId());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @Override
    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@RequestBody Registration entity) {
        String id ="";
        if(Objects.equals(entity.getCreateBy(),"WeChat")){
            // C1 (iteration 50): san RBAC parity nhanh web — truoc day nhanh
            // WeChat khong co san, token 0 quyen cung goi duoc wxSave.
            if (!hasAnySessionPermission()) { return forbidden(); }
            id = registrationService.wxSave(entity).getId();
        }else {
            if (!hasAnySessionPermission()) { return forbidden(); } // H4: san RBAC cho duong luu web (lop cha CrudController.save co san, override nay truoc day bo qua).
            // K1 (iteration 58): ep tenant theo session cho NHANH INSERT
            // (id trong), mau D1/E1, truoc moi ghi. Truoc day payload id
            // trong + company tenant la tao that luot kham o tenant la
            // (nhanh insert cua CrudService.save khong verify tenant) va
            // assignQueueNumber con an 1 so cua counter tenant la khi co
            // doctor (tai hien tools/iter58-k1.py). Session co tenant thi
            // bo qua company payload; session rong/"null" giu fallback
            // body cu qua sessionTenantOr. Nhanh UPDATE KHONG ep: guard
            // stored-row cua CrudService.save van chan payload company la
            // (case C van 400 + dong van OWN) va dong la van bi chan.
            // Nhanh WeChat khong doi (C1 da guard wxSave + san).
            // Booking confirm goi RegistrationService.save truc tiep, khong
            // qua controller nay, nen khong anh huong. UI that gui company
            // own nen khong doi hanh vi hop le.
            if (StringUtils.isBlank(entity.getId())) {
                String tenant = sessionTenantOr(entity.getCompany() == null ? null : entity.getCompany().getId());
                if (tenant != null) {
                    entity.setCompany(new Company(tenant));
                }
            }
            //mởBHYTgiao diện
            if("true".equals(medicareConfigProperties.getCheck())){
                //LưuNgười dùngBHYTThông tin
                if (mdPsnDataService.getAndSetPsnData(entity)) {
                    //Đăng ký khám BHYT
                    String registrationInfo = mdRegistrationService.getRegistrationInfo_2201(entity);
                    Output_2201 output_2201 = JSONObject.parseObject(registrationInfo, Output_2201.class);
                    entity.setMdtrtId(output_2201.getData().getMdtrt_id());
                    entity.setIptOtpNo(output_2201.getData().getIpt_otp_no());
                }
            }
            id = registrationService.save(entity).getId();
        }

        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    @PostMapping(value = "registationupdate")
    public ResponseEntity<JSONObject> registationupdate(@RequestBody RegistrationMedicalrecordlist entitys) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        // DS82: registration null NPEd at getRegistration().setUpdateDate
        // -> 500; fail closed 400 like the print path below.
        if (entitys.getRegistration() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResultUtil.errorJson(ErrorEnum.E_400));
        }
        entitys.getRegistration().setUpdateDate(new Date());
        int row = registrationService.registrationupdate(entitys.getRegistration());
        if(entitys.getMedicalRecord()!=null) {
            if (entitys.getMedicalRecord().getPatientTell() != null && entitys.getMedicalRecord().getDoctor() != null) {
                entitys.getMedicalRecord().setRegistration(entitys.getRegistration());
                row = registrationService.medicalRecordInserts(entitys.getMedicalRecord());
            }
        }
        return ResponseEntity.ok(ResultUtil.successJson(row));
    }

    @PostMapping(value = {"conditionList", ""})
    public ResponseEntity<JSONObject> conditionList(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        List<Parameter> params = searchParams.getParams();
        Page<Registration> result;
        if (null != params && 0 != params.size()) {
            result = registrationService.listConditionPage(searchParams.getParams().get(0).getColumnName(), searchParams.getOffset(), searchParams.getLimit());
        } else {
            result = registrationService.listPage(params, searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        }
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    //in ấnĐơn thuốc
    @PostMapping("/print")
    public ResponseEntity<JSONObject> print(@RequestBody JSONObject inJson) {
        if (!hasAnySessionPermission()) { return forbidden(); } // F3: san RBAC nhu cac duong cung controller; truoc day 0 quyen van chay nhu testfull.
        String id = inJson.getString("id");
        if (StringUtils.isNotBlank(id)) {
            RecipelDetail recipelDetail = recipelDetailService.get(id);
            // F3: get() tra null khi id la (khong phai 500) — id da cho nhung
            // khong co chi tiet don thi tra 400 loi that, theo mau D1.
            if (recipelDetail == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ResultUtil.errorJson(ErrorEnum.E_400));
            }
            Map<String, Object> map = new HashMap<>();
            map.put("buildReport", recipelDetail);
            // F3: dung bean DataBean da inject thay vi new DataBean(): bean
            // moi co @Autowired service; new lam recipelInfoService null nen
            // buildReport NPE voi moi id khong rong (tai hien f3-repro.py).
            // Ket qua buildReport von bi bo qua o day (endpoint chi tra
            // success rong) nen giu nguyen map/key cu, khong ve them
            // hanh vi in that — print that la tinh nang, ngoai pham vi F3.
            dataBean.buildReport("", "", map);
        }
        return ResponseEntity.ok(ResultUtil.successJson());
    }

    //LấyBệnh nhânđăng kýThông tin
    @PostMapping("/v2/list")
    public ResponseEntity<JSONObject> listPages(@RequestBody PageRegistration pageRegistration) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        Page<Registration> result = registrationService.listPages(pageRegistration);
        registrationService.updateExpiredStatusFromPage(result, row -> row.getCompany().getId());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    //LấyChi phíThông tin
    @PostMapping("/wx/list")
    public ResponseEntity<JSONObject> wxListPages(@RequestBody PageRegistration pageRegistration) {
        if (!hasAnySessionPermission()) { return forbidden(); } // U2(b): san RBAC.
        // V1: companyId ep theo session (truoc day body tenant B + openId that khien
        // total>0 roi allNewQuery nem 400 verifyTenantOwnership — oracle ton tai
        // luot don tenant khac; nay body la chi con total 0).
        pageRegistration.setCompanyId(sessionTenantOr(pageRegistration.getCompanyId()));
        Page<ReceptionEvt> result = registrationService.wxListPages(pageRegistration);
        registrationService.updateExpiredStatusFromPage(result, row -> row.getRegistration().getCompany().getId());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    //Lấyphát thuốcBản ghi
    @PostMapping("/wx/dispensingList")
    public ResponseEntity<JSONObject> wxDispensingListPages(@RequestBody PageRegistration pageRegistration) {
        if (!hasAnySessionPermission()) { return forbidden(); } // U2(b): san RBAC, nhu wx/list.
        // V1: nhu wx/list — companyId ep theo session.
        pageRegistration.setCompanyId(sessionTenantOr(pageRegistration.getCompanyId()));
        Page<ReceptionEvt> result = registrationService.wxDispensingListPages(pageRegistration);
        registrationService.updateExpiredStatusFromPage(result, row -> row.getRegistration().getCompany().getId());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    //WeChatđầuQuaopenidLấykhám bệnhThông tin
    @PostMapping("getRegistrationByOpenId")
    public ResponseEntity<JSONObject> getRegistrationByOpenId(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // U2(b): san RBAC.
        List<Parameter> params = searchParams.getParams();
        // V1: company_id ep theo session, giu nguyen vi tri params (service doc
        // params[0]=companyId, params[1]=openId theo vi tri).
        // V2: getParams() da boc columnName trong backtick (`company_id`) nen
        // phai khop ca hai dang, neu khong vong nay lang lang khong ep duoc
        // va body lai dieu khien tenant (V1 chi co tac dung sau khi V2 sua NPE).
        String tenant = SessionUtils.getLoginTenantId();
        if (StringUtils.isNotBlank(tenant) && !"null".equals(tenant) && params != null) {
            for (Parameter p : params) {
                String col = p.getColumnName();
                if ("company_id".equals(col) || "`company_id`".equals(col)) { p.setValue(tenant); }
            }
        }
        Page<Registration> registrationByOpenId = registrationService.getRegistrationByOpenId(params, searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(registrationByOpenId));
    }
}
