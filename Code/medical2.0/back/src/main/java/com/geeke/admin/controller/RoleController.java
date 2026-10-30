package com.geeke.admin.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.admin.entity.Role;
import com.geeke.admin.service.RoleService;
import com.geeke.common.controller.CrudController;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.constants.ErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Quản lý vai tròController
 * @author lys
 * @version 2022-05-24
 */
@RestController
@RequestMapping(value = "/admin/role")
public class RoleController extends CrudController<RoleService, Role> {

	@Autowired
	protected RoleService roleService;

	@Override
	protected RoleService getService() {
		return roleService;
	}

    /**
     * Danh sách vai trò có phân trang: chỉ tài khoản có quyền tài nguyên
     * "role:list" mới được gọi. Trước đây mọi token hợp lệ đều đọc được danh
     * sách vai trò (kể cả tài khoản 0 quyền), vì filter chỉ kiểm tra xác thực.
     * Lọc theo tenant vẫn do service đảm nhiệm (ensureCompanyFilter).
     */
    @Override
    @PostMapping(value = {"list", ""})
    public ResponseEntity<JSONObject> listPage(@RequestBody SearchParams searchParams) {
        if (!hasSessionPermission("role:list")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        Page<Role> result = getService().listPage(searchParams.getParams(), searchParams.getOffset(),
                searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * Danh sách vai trò không phân trang: cùng quyền "role:list" như listPage.
     * Chỉ dùng ở form người dùng (chọn vai trò khi tạo/sửa tài khoản, trang
     * quản trị), không phải lookup bắt buộc của trang nghiệp vụ chính, nên
     * chặn tài khoản 0 quyền ở đây không làm gãy luồng chính.
     */
    @Override
    @PostMapping(value = "listAll")
    public ResponseEntity<JSONObject> listAll(@RequestBody SearchParams searchParams) {
        if (!hasSessionPermission("role:list")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        List<Role> result = roleService.listAllByTenantId(SessionUtils.getLoginTenantId());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * Lưu vai trò: tài khoản phải có "role:create" (thêm mới) hoặc
     * "role:update" (sửa), vì save chung cho cả hai. Chỉ form vai trò ở
     * trang quản trị gọi endpoint này.
     */
    @Override
    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@javax.validation.Valid @RequestBody Role entity) {
        if (!hasSessionPermission("role:create") && !hasSessionPermission("role:update")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        String id = getService().save(entity).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    /**
     * Xóa vai trò: chỉ tài khoản có "role:delete" mới được gọi. Chỉ trang
     * quản trị vai trò gọi endpoint này.
     */
    @Override
    @PostMapping(value = "delete")
    public ResponseEntity<JSONObject> delete(@RequestBody Role entity) {
        if (entity == null) {
            return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_400));
        }
        if (!hasSessionPermission("role:delete")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        int rows = getService().delete(entity);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

}