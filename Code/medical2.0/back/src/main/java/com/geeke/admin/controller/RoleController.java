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

    @PostMapping(value = "listAll")
    public ResponseEntity<JSONObject> listAll(@RequestBody SearchParams searchParams) {
        List<Role> result = roleService.listAllByTenantId(SessionUtils.getLoginTenantId());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

}