package com.geeke.clinic.service;


import com.geeke.admin.common.dao.PermissionDAO;
import com.geeke.admin.common.entity.DataPermission;
import com.geeke.admin.common.entity.Permission;
import com.geeke.admin.common.service.PermissionService;
import com.geeke.clinic.dao.ClinicPermissionDAO;
import com.geeke.common.service.ServiceException;
import com.geeke.utils.IdGen;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


/**
 * tài nguyêndịch vụTriển khaiLớp
 *
 * @author lys
 * @date 2019/07/26
 */
@Service
public class ClinicPermissionService {

    @Autowired
    private ClinicPermissionDAO clinicPermissionDAO;
    @Autowired
    private PermissionDAO permissionDAO;
    @Autowired
    private PermissionService permissionService;

    /**
     * Lưuphiên bản của Vai tròQuyền
     * @param roleId
     * @return
     */
    @Transactional
    public int savePermission(String roleId, Permission permission) {
        // ITER65 item 1: guard o service duoc vi service nay CHI duoc goi tu
        // ClinicPermissionController (khong co luong noi bo nhu
        // CompanyService.savePermisson ben PermissionService). Phai verify
        // role goc VA TOAN BO role con TRUOC khi xoa bat ky dong nao —
        // xoa role goc roi moi nem loi se de lai role goc trang quyen.
        // Cung quy tac nhu PermissionService.verifyRoleTenant: chi dung
        // session tenant; role khong ton tai / company rong giu hanh vi cu;
        // session khong tenant thi skip.
        verifyRoleTenant(roleId);
        List<String> childIds = clinicPermissionDAO.queryChildNodesByPid(roleId);
        for (String childId : childIds) {
            verifyRoleTenant(childId);
        }

        int rows = saveAllPermission(roleId, permission);

        //Truy vấnconLớpVai tròvàCập nhậtQuyền
        childIds.stream().forEach(id -> saveAllPermission(id,permission));
        return rows;
    }

    private void verifyRoleTenant(String roleId) {
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return;
        }
        String storedCompanyId = this.permissionDAO.getRoleCompanyId(roleId);
        if (StringUtils.isNotBlank(storedCompanyId) && !tenantId.equals(storedCompanyId)) {
            throw new ServiceException("Không có quyền truy cập quyền của vai trò thuộc phòng khám khác");
        }
    }

    private int saveAllPermission(String roleId, Permission permission) {
        // DS118 A: cung validate nhu PermissionService.savePermission
        // (null, role ma, id la, dataPermission thieu truong) truoc khi
        // xoa/ghi; xem PermissionService.validateSavePermission.
        this.permissionService.validateSavePermission(roleId, permission);
        String[] routerIds = permission.getRouterIds();
        String[] resourceIds = permission.getResourceIds();
        List<DataPermission> dataPermissions = permission.getDataPermissions();
        // trướcXóasauThêm
        this.permissionDAO.removeRouterByRoleId(roleId);
        this.permissionDAO.removeResourceByRoleId(roleId);
        this.permissionDAO.removeDataPermssionByRoleId(roleId);

        int rows = 0;
        if(routerIds != null && routerIds.length > 0) {
            Map<String, String> map = Maps.newHashMap();
            for(String routerId: routerIds) {
                map.put(routerId, IdGen.uuid());
            }

            rows += this.permissionDAO.saveRouterPermission(roleId, map);
        }

        if(resourceIds != null && resourceIds.length > 0) {
            Map<String, String> map = Maps.newHashMap();
            for(String resourceId: resourceIds) {
                map.put(resourceId, IdGen.uuid());
            }

            rows += this.permissionDAO.saveResourcePermission(roleId, map);
        }

        if(dataPermissions != null && dataPermissions.size() > 0) {
            String[] ids = new String[dataPermissions.size()];
            for(int idx = 0; idx < dataPermissions.size(); idx++) {
                ids[idx] = IdGen.uuid();
            }

            rows += this.permissionDAO.saveDataPermission(dataPermissions, ids);
        }
        return rows;
    }
}
