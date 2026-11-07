package com.geeke.clinic.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.admin.common.entity.DataPermission;
import com.geeke.admin.common.entity.Permission;
import com.geeke.clinic.service.ClinicPermissionService;
import com.geeke.sys.controller.BaseController;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.StringUtils;
import com.geeke.utils.constants.ErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * tài nguyênkiểm soátthiết bị
 *
 * @author Lining
 * @date 2018/2/11
 */
@RestController("ClinicPermissionController")
@RequestMapping("/clinic")
public class ClinicPermissionController extends BaseController {

    @Autowired
    private ClinicPermissionService permissionService;

    /**
     * Y2a: cùng sàn ghi như /permission savePermission (role:update hoặc
     * clinicVersion:update). Endpoint này không tìm thấy caller nào trong
     * client/src (cả 2 dialog phân quyền đều gọi /permission/), nhưng
     * không xóa khi chưa có bằng chứng là code chết — service của nó còn
     * lan quyền xuống các role con, nên khóa lại thay vì để mở.
     */
    @PostMapping("/{roleId}/savePermission")
    public ResponseEntity<JSONObject> savePermission(@PathVariable("roleId") String roleId, @RequestBody Permission permission) {
        if (!hasSessionPermission("role:update") && !hasSessionPermission("clinicVersion:update")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        // DS118 A: nhu PermissionController.savePermission -- chan
        // dataPermission mang roleId cua role khac (mapper chen
        // #{item.roleId}); luong copy noi bo cua CompanyService khong
        // qua controller nen khong anh huong.
        if (permission != null && permission.getDataPermissions() != null) {
            for (DataPermission item : permission.getDataPermissions()) {
                if (item != null && StringUtils.isNotBlank(item.getRoleId())
                        && !roleId.equals(item.getRoleId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ResultUtil.errorJson(ErrorEnum.E_502));
                }
            }
        }
        int rows = permissionService.savePermission(roleId, permission);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

}
