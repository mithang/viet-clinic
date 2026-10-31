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
        User user = JSONObject.parseObject(strUser, User.class);
        user.setLoginName(user.getPhone());
        // test
        // ĐặtPhòng banid
        List<Parameter> list = SearchParamsBuilder.create()
                .eq("company_id", user.getCompany().getId())
                .eq("name", user.getUserExt().getOffice())
                .build();
        String departmentId = clinicOfficeService.listAll(list, "").get(0).getId();
        Department department = new Department();
        department.setId(departmentId);
        user.setDepartment(department);
        user.getUserExt().setOfficeCode(clinicOfficeService.listAll(list, "").get(0).getCode());
        String[] deleteIds = JSONObject.parseObject(strDeleteIds, String[].class);
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
        User user = JSONObject.parseObject(strUser, User.class);
        List<Parameter> list = SearchParamsBuilder.create()
                .eq("company_id", user.getCompany().getId())
                .eq("name", user.getUserExt().getOffice())
                .build();
        ClinicOffice clinicOffice = clinicOfficeService.listAll(list, "").get(0);
        user.getUserExt().setOfficeCode(clinicOffice.getCode());
        //user.setLoginName(user.getPhone());
        String[] deleteIds = JSONObject.parseObject(strDeleteIds, String[].class);
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
            throw new ServiceException("demoHệ thốngVui lòngđừngXóaNgười dùng!");
        }
        int all = service.countClinicIdByPhone(entity.getPhone());
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
             throw new ServiceException("demoHệ thốngVui lòngđừngSửaMật khẩu!");
         }
        if(!StringUtils.isBlank(id)) {
            rows = service.changeLoginPassword(id, password);
        }
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

}