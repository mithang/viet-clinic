package com.geeke.admin.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.admin.entity.User;
import com.geeke.admin.entity.UserExt;
import com.geeke.admin.service.UserExtService;
import com.geeke.admin.service.UserService;
import com.geeke.common.controller.CrudController;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.common.data.Parameter;
import com.geeke.common.data.SearchParamsBuilder;
import com.geeke.common.service.ServiceException;
import com.geeke.medicareutils.config.MedicareConfigProperties;
import com.geeke.medicareutils.service.MdPsnDataService;
import com.geeke.org.entity.ClinicOffice;
import com.geeke.org.entity.Department;
import com.geeke.org.service.ClinicOfficeService;
import com.geeke.sys.controller.BaseController;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.geeke.utils.constants.ErrorEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý người dùngController
 * @author lys
 * @version 2021-08-25
 */
@RestController
@RequestMapping(value = "/admin/user")
@RequiredArgsConstructor
public class UserController extends CrudController<UserService, User> {

    protected final UserService service;

    private final UserExtService userExtService;

    private final ClinicOfficeService clinicOfficeService;

    private final MedicareConfigProperties medicareConfigProperties;

    private final MdPsnDataService mdPsnDataService;

    @Override
    protected UserService getService() {
        return service;
    }

    /**
     * Đọc hồ sơ user: chính mình, hoặc tài khoản có "user:read" (trang quản
     * trị user). Trước đây mọi token hợp lệ đều đọc được hồ sơ bất kỳ.
     * Trang header chỉ gọi id của chính mình nên không gãy.
     */
    @Override
    @GetMapping("/{id}")
    public ResponseEntity<JSONObject> getById(@PathVariable("id") String id) {
        User sessionUser = SessionUtils.getUser();
        boolean self = sessionUser != null && id != null && id.equals(sessionUser.getId());
        if (!self && !hasSessionPermission("user:read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        User entity = service.get(id);
        // DS42: id la -> service.get tra null (hop dong CrudService.get);
        // truoc day setUserExt tren null NPE 500 (sau khi UserService.get
        // da duoc guard, NPE doi len day). Tra 200 data null nhu
        // CrudController.getById goc.
        if (entity == null) {
            return ResponseEntity.ok(ResultUtil.successJson(entity));
        }
        UserExt extByUserId = userExtService.getExtByUserId(id);
        entity.setUserExt(extByUserId);
        // Không trả về mật khẩu
        entity.setLoginPassword("");
        return ResponseEntity.ok(ResultUtil.successJson(entity));
    }

    /**
     * Tra user theo phone: chỉ userForm quản trị gọi (kiểm tra trùng phone khi
     * tạo/sửa user) nên yêu cầu "user:read". Tra phone là tra cứu PII, không
     * phải lookup của luồng nghiệp vụ chính.
     */
    @GetMapping("/phone/{phone}")
    public ResponseEntity<JSONObject> getByPhone(@PathVariable("phone") String phone) {
        if (!hasSessionPermission("user:read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        User entity = service.CheckNewUserByPhone(phone);
        // DS13: tra phone là tra cứu PII cross-tenant. user:read do nhiều
        // vai trò phòng khám giữ (YS/dược sĩ/admin, không chỉ quản trị
        // nền tảng), mà CheckNewUserByPhone chỉ ném khi phone đã đăng ký
        // ở ĐÚNG clinic gọi (kiểm tra trùng khi tạo user) — phone clinic
        // khác trước đây trả đủ entity. Giới hạn: chỉ trả entity khi
        // cùng tenant hoặc phiên hệ thống ('0'/rỗng — quản trị nền
        // tảng); clinic khác -> null như không tìm thấy. Tạo user
        // cross-clinic vốn được phép (chỉ trùng cùng clinic mới ném)
        // nên null không đổi luồng tạo user; user:list vốn cũng ép
        // tenant session nên giới hạn này là nhất quán, không mới.
        if (entity != null && entity.getCompany() != null) {
            String tenant = SessionUtils.getLoginTenantId();
            String companyId = entity.getCompany().getId();
            if (StringUtils.isNotBlank(tenant) && !"null".equals(tenant) && !"0".equals(tenant)
                    && StringUtils.isNotBlank(companyId) && !tenant.equals(companyId)) {
                return ResponseEntity.ok(ResultUtil.successJson(null));
            }
        }
        if(null!=entity)
        {
            UserExt oldExtInfo = userExtService.getOldExtByUserId(entity.getId());
            if(null != oldExtInfo)
            {
                oldExtInfo.setCompanyId(SessionUtils.getLoginTenantId());
                entity.setUserExt(oldExtInfo);
            }

            // Không trả về mật khẩu
            entity.setLoginPassword("");
        }
        return ResponseEntity.ok(ResultUtil.successJson(entity));
    }

    /**
     * Bảng quản trị user: chỉ "user:list". Chỉ admin/user/index.vue gọi.
     */
    @Override
    @PostMapping(value = {"list", ""})
    public ResponseEntity<JSONObject> listPage(@RequestBody SearchParams searchParams) {
        if (!hasSessionPermission("user:list")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        User user = SessionUtils.getUser();
        //Truy vấntất cảDữ liệu
        Page<User> userPage;
        //Nhà cung cấpnhân viêntheocũlogic
        if("1000".equals(user.getId()) || "1001".equals(user.getId())) {
            userPage = service.listPage(searchParams.getParams(),
                    searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        }
        else
        {

            //Quản lý phòng khámnhân viênTruy vấnTất cảDữ liệu
            List<Parameter> params = searchParams.getParams();
            if (params == null) {
                params = new java.util.ArrayList<>();
            }
            if (params.isEmpty()) {
                params.add(new Parameter());
            } else {
                params.set(0,new Parameter());
            }
            userPage = service.listPageForNoAdmin(params,
            searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        }

        // Không trả về mật khẩu
        if(userPage.getRows() != null) {
            for(User entity: userPage.getRows()){
                entity.setLoginPassword("");
            }
        }
        return ResponseEntity.ok(ResultUtil.successJson(userPage));
    }

    /**
     * Lookup chọn bác sĩ ở form khám, form duyệt đơn, tollDetail…: ĐÃ REVERT
     * gate "user:list" ở vòng này vì kiểm chứng cho thấy vai trò bác sĩ cũ
     * (testdoctor, 34 router gồm cả outpatient/medicalRecord) KHÔNG có
     * user:list nên form khám của nó gãy (403 ở dropdown bác sĩ). Thay bằng
     * sàn hasAnySessionPermission như CrudController generic: tài khoản
     * 0-resource vẫn bị 403, mọi vai trò có menu đều qua được. Muốn chặn
     * đúng bằng user:list thì phải cấp user:list cho vai trò bác sĩ cũ —
     * quyết định cấp quyền vai trò, ngoài phạm vi vòng code này.
     */
    @Override
    @PostMapping(value = "listAll")
    public ResponseEntity<JSONObject> listAll(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        List<User> result = service.listAll(searchParams.getParams(), searchParams.getOrderby());
        // Không trả về mật khẩu
        if(result != null) {
            for(User entity: result){
                entity.setLoginPassword("");
            }
        }
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * Ghi người dùng: chỉ tài khoản có "user:create" hoặc "user:update".
     * Lưu ý: listPage/listAll KHÔNG chặn ở vòng này vì chúng là lookup bắt
     * buộc của trang chính (dropdown chọn bác sĩ ở form khám, form duyệt
     * đơn, form khoa...); chặn chúng theo user:list sẽ gãy các form đó.
     * Ghi thì khác: mọi đường ghi đều chỉ được form quản trị người dùng gọi.
     */
    @Override
    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@javax.validation.Valid @RequestBody User entity) {
        if (!hasSessionPermission("user:create") && !hasSessionPermission("user:update")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        String id = service.save(entity).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    @Override
    @PostMapping(value = "bulkInsert")
    public ResponseEntity<JSONObject> bulkInsert(@RequestBody List<User> entitys) {
        if (!hasSessionPermission("user:create")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        List<String> ids = service.bulkInsert(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(ids));
    }

    @Override
    @PostMapping(value = "bulkUpdate")
    public ResponseEntity<JSONObject> bulkUpdate(@RequestBody List<User> entitys) {
        if (!hasSessionPermission("user:update")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        List<String> ids = service.bulkUpdate(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(ids));
    }

    @Override
    @PostMapping(value = "bulkDelete")
    public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<User> entitys) {
        if (!hasSessionPermission("user:delete")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        int rows = service.bulkDelete(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

    @PostMapping(value = "saveWithFile")
    public ResponseEntity<JSONObject> saveWithFile(@RequestParam("entity") String strUser,
                                           @RequestParam("fileIdUploads") MultipartFile[] fileIdUploads,  // Tệp: Ảnh người dùng
                                           @RequestParam(value = "deleteIds",required = false)String strDeleteIds) throws java.io.IOException {
        if (!hasSessionPermission("user:create") && !hasSessionPermission("user:update")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        User user;
        try {
            user = JSONObject.parseObject(strUser, User.class);
        } catch (com.alibaba.fastjson.JSONException e) {
            // DS86: malformed entity JSON (truncated, or a bare string for
            // the Company-typed field) threw at parseObject -> 500;
            // unparseable input can never succeed -> 400 (DS85 pattern).
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResultUtil.errorJson(ErrorEnum.E_400));
        }
        // DS86: parse from String (no @Valid); a "null" entity NPEd at
        // user.getCompany() below -> 400 early (DS83/DS85 pattern).
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResultUtil.errorJson(ErrorEnum.E_400));
        }
        // DS83: saveWithFile khong chay @Valid; company/userExt/office
        // null se NPE o getCompany().getId() / getUserExt().getOffice()
        // (office null con lam eq("name", null) nem 500 truoc ca guard
        // D1) -> 400 som, khong 500.
        if (user.getCompany() == null || StringUtils.isBlank(user.getCompany().getId())
                || user.getUserExt() == null || StringUtils.isBlank(user.getUserExt().getOffice())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResultUtil.errorJson(ErrorEnum.E_400));
        }
        user.setLoginName(user.getPhone());
        // test
        // ĐặtPhòng banid
        List<Parameter> list = SearchParamsBuilder.create()
                .eq("company_id", user.getCompany().getId())
                .eq("name", user.getUserExt().getOffice())
                .build();
        // D1 (iteration 24): ten khoa khong khop bat ky dong nao -> tra 400
        // thay vi IndexOutOfBounds 500 o get(0); chi truy van mot lan.
        List<ClinicOffice> offices = clinicOfficeService.listAll(list, "");
        if (offices == null || offices.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResultUtil.errorJson(ErrorEnum.E_400));
        }
        String departmentId = offices.get(0).getId();
        Department department = new Department();
        department.setId(departmentId);
        user.setDepartment(department);
        user.getUserExt().setOfficeCode(offices.get(0).getCode());
        String[] deleteIds;
        try {
            deleteIds = JSONObject.parseObject(strDeleteIds, String[].class);
        } catch (com.alibaba.fastjson.JSONException e) {
            // DS86: same as the entity parse above (DS85 pattern).
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResultUtil.errorJson(ErrorEnum.E_400));
        }
        String id = service.save(user,fileIdUploads,deleteIds).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    @PostMapping(value = "update")
    public ResponseEntity<JSONObject> update(@RequestParam("entity") String strUser,
                                             @RequestParam("fileIdUploads") MultipartFile[] fileIdUploads,  // Tệp: Ảnh người dùng
                                             @RequestParam("deleteIds")String strDeleteIds) throws java.io.IOException  {
        if (!hasSessionPermission("user:create") && !hasSessionPermission("user:update")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        User user;
        try {
            user = JSONObject.parseObject(strUser, User.class);
        } catch (com.alibaba.fastjson.JSONException e) {
            // DS86: malformed entity JSON threw at parseObject -> 500;
            // unparseable input can never succeed -> 400 (DS85 pattern).
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResultUtil.errorJson(ErrorEnum.E_400));
        }
        // DS86: parse from String (no @Valid); a "null" entity NPEd at
        // user.getCompany() below -> 400 early (DS83/DS85 pattern).
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResultUtil.errorJson(ErrorEnum.E_400));
        }
        // DS83: nhu saveWithFile — update cung khong chay @Valid;
        // company/userExt/office null se NPE o lookup khoa -> 400 som.
        if (user.getCompany() == null || StringUtils.isBlank(user.getCompany().getId())
                || user.getUserExt() == null || StringUtils.isBlank(user.getUserExt().getOffice())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResultUtil.errorJson(ErrorEnum.E_400));
        }
        List<Parameter> list = SearchParamsBuilder.create()
                .eq("company_id", user.getCompany().getId())
                .eq("name", user.getUserExt().getOffice())
                .build();
        // D1 (iteration 24): nhu saveWithFile, ten khoa khong khop -> 400, khong 500.
        List<ClinicOffice> offices = clinicOfficeService.listAll(list, "");
        if (offices == null || offices.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResultUtil.errorJson(ErrorEnum.E_400));
        }
        ClinicOffice clinicOffice = offices.get(0);
        user.getUserExt().setOfficeCode(clinicOffice.getCode());
        //user.setLoginName(user.getPhone());
        String[] deleteIds;
        try {
            deleteIds = JSONObject.parseObject(strDeleteIds, String[].class);
        } catch (com.alibaba.fastjson.JSONException e) {
            // DS86: same as the entity parse above (DS85 pattern).
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResultUtil.errorJson(ErrorEnum.E_400));
        }
        String id = service.update(user,fileIdUploads,deleteIds).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    @Override
    @PostMapping(value = "delete")
    public ResponseEntity<JSONObject> delete(@RequestBody User entity) {
        if (entity == null) {
            return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_400));
        }
        if (!hasSessionPermission("user:delete")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        if(medicareConfigProperties.getIsDemo().equals("true")){
            throw new ServiceException("demo Hệ thống Vui lòng đừng Xóa Người dùng!");
        }
        // Iteration 29: request khong id khong con di duong fallback dem
        // theo phone (countClinicIdByPhone khong loc tenant, roi
        // delete(user, int) NPE o ImmutableMap vi user_id null). Giai
        // quyet id trong tenant cua session truoc: khong thay (sai
        // tenant, da xoa, hoac khong co phone) thi 400, khong xoa gi.
        // Co id thi giu hanh vi H2: dem theo user_id.
        String entityId = entity.getId();
        if (entityId == null || entityId.isEmpty()) {
            if (entity.getPhone() == null || entity.getPhone().isEmpty()) {
                return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_400));
            }
            entityId = service.findActiveUserIdByPhoneInTenant(
                    entity.getPhone(), SessionUtils.getLoginTenantId());
            if (entityId == null) {
                return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_400));
            }
            entity.setId(entityId);
        }
        int all = service.countActiveTenantsByUserId(entityId);
        service.delete(entity,all);
        return ResponseEntity.ok(ResultUtil.successJson(all));
    }


    /**
     * Sửa Mật khẩu: chỉ chính mình hoặc tài khoản có "user:update" (quản trị
     * reset mật khẩu). Trước đây mọi token hợp lệ đều đổi được mật khẩu của
     * bất kỳ user nào vì service không kiểm tra ownership. Đường đổi mật khẩu
     * chính của UI là PUT /person/me (service đã ép về session user) nên
     * chặn ở đây không gãy luồng tự đổi mật khẩu.
     */
    @PutMapping("/{id}/loginPassword")
    public ResponseEntity<JSONObject> changeLoginPassword(@PathVariable("id") String id, String password) {
        User sessionUser = SessionUtils.getUser();
        boolean self = sessionUser != null && id != null && id.equals(sessionUser.getId());
        if (!self && !hasSessionPermission("user:update")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        int rows = 0;
         if(medicareConfigProperties.getIsDemo().equals("true")){
             throw new ServiceException("demo Hệ thống Vui lòng đừng Sửa Mật khẩu!");
         }
        if(!StringUtils.isBlank(id)) {
            rows = service.changeLoginPassword(id, password);
        }
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

}