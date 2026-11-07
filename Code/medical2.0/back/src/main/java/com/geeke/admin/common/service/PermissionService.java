package com.geeke.admin.common.service;


import com.geeke.admin.common.dao.PermissionDAO;
import com.geeke.admin.common.entity.DataPermission;
import com.geeke.admin.common.entity.Permission;
import com.geeke.admin.entity.Resource;
import com.geeke.admin.entity.Router;
import com.geeke.common.service.ServiceException;
import com.geeke.utils.IdGen;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;



/**
 * tài nguyêndịch vụTriển khaiLớp
 * 
 * @author lys
 * @date 2019/07/26
 */
@Service("permissionService")
public class PermissionService {

    @Autowired
    private PermissionDAO permissionDAO;
   
    /**
     * Lấy quyền tài nguyên, Không bao gồm mục bị vô hiệu hóa
     * @param userId
     * @param routerId
     * @return
     */
    public List<Resource> listResourcePermission(String userId, String routerId) {
    	return this.permissionDAO.listResourcePermission(userId, routerId);
    }
    
	/**
	 * Lấy tuyến đường mà người dùng có quyền, Không bao gồm mục bị vô hiệu hóa
	 * @param userId
	 * @return
	 */
    public List<Router> listRouterPermission(String userId,String companyId) {
    	return this.permissionDAO.listRouterPermission(userId,companyId);
    }

    /**
	 * Lấy tuyến đường mà người dùng có quyền, Không bao gồm mục bị vô hiệu hóa
	 * @param roleId
	 * @return
	 */
    public List<Router> listRouterPermissionByRole(String roleId) {
    	return this.permissionDAO.listRouterPermissionByRoleId(roleId);
    }

    
	/**
	 * LấyNgười dùngcóQuyền của tài nguyên, Không bao gồm mục bị vô hiệu hóa
	 * @param userId
	 * @return
	 */
    public List<String> listResourcePermissionByUser(String userId,String companyId) {
    	return this.permissionDAO.listResourcePermissionByUserId(userId,companyId);
    }
    
    
    /**
     * Lấy quyền tài nguyênDanh sách
     * @param roleId
     * @return
     */
    public List<String> listResourcePermission(String roleId) {
        return this.permissionDAO.listResourcePermissionByRoleId(roleId);
    }
    
    
    /**
     * ITER65 item 1: chan doc/ghi quyen cua role thuoc tenant khac.
     * Lookup stored company_id cua role dich (single-column SELECT, tien le
     * X3/N1): foreign (!= session tenant) -> ServiceException 400 fail-closed
     * TRUOC moi lenh xoa/ghi. Parity: role khong ton tai (lookup null) hoac
     * stored company rong giu hanh vi cu (tien le orphan N3c); session khong
     * tenant / "null" thi skip. Chi cho dung session tenant (khong noi cho
     * role cua cong ty cha/institution — khong co bang chung code nao cho
     * thay role la institution-shared, plan iter65 §1(c)).
     * DAT O CONTROLLER (khong dat trong savePermission): CompanyService.
     * savePermisson goi noi bo savePermission de copy quyen mau version
     * sang role moi vua tao cua clinic moi (company = clinic moi, != session
     * tenant cua nguoi tao) — guard o service se lam vo luong tao clinic
     * (L6). Cac duong ngoai duy nhat vao day la 2 controller, ca 2 deu goi
     * verifyRoleTenant truoc khi vao service.
     */
    public void verifyRoleTenant(String roleId) {
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return;
        }
        String storedCompanyId = this.permissionDAO.getRoleCompanyId(roleId);
        if (StringUtils.isNotBlank(storedCompanyId) && !tenantId.equals(storedCompanyId)) {
            throw new ServiceException("Không có quyền truy cập quyền của vai trò thuộc phòng khám khác");
        }
    }

    /**
     * DS118 A: validate toan bo payload TRUOC khi xoa/ghi quyen.
     * - permission null -> 400 (truoc day NPE 500 o getRouterIds()).
     * - roleId khong ton tai -> 400 (truoc day 200 + dong mo coi o
     *   sys_router_permission/sys_resource_permission -- khong co FK;
     *   verifyRoleTenant cho role ma qua theo tien le orphan N3c cua
     *   iter65, nhung do la quy tac phia DOC, khong phai giay phep
     *   GHI cho role khong ton tai).
     * - routerId/resourceId la -> 400 (truoc day 200 + dong mo coi).
     * - dataPermission null / thieu routerId/metaId/roleId -> 400
     *   (truoc day 500 NOT NULL hoac NPE o mapper).
     * - routerId trong dataPermissions cung phai ton tai.
     * CO Y KHONG kiem tra item.roleId == roleId o day:
     * CompanyService.savePermisson goi noi bo de copy quyen mau tu
     * role template sang role moi -- item.roleId mang id role mau
     * (xem ghi chu o verifyRoleTenant). Rang buoc do chi ap o tang
     * controller (2 endpoint ngoai), noi dialog that luon gui
     * item.roleId == role dang sua.
     * Goi truoc moi lenh remove* de role khong bi trang quyen khi
     * payload sai (giong mau pre-check cua DS94/DS99/DS112).
     */
    public void validateSavePermission(String roleId, Permission permission) {
        if (permission == null) {
            throw new ServiceException("Quyền không được để trống");
        }
        if (StringUtils.isBlank(roleId)
                || this.permissionDAO.countRoleById(roleId) == 0) {
            throw new ServiceException("Vai trò không tồn tại");
        }
        Set<String> routerIds = new LinkedHashSet<>();
        if (permission.getRouterIds() != null) {
            for (String id : permission.getRouterIds()) {
                if (StringUtils.isBlank(id)) {
                    throw new ServiceException("Định tuyến không được để trống");
                }
                routerIds.add(id);
            }
            if (!routerIds.isEmpty()
                    && this.permissionDAO.countRoutersByIds(new ArrayList<>(routerIds)) != routerIds.size()) {
                throw new ServiceException("Định tuyến không tồn tại");
            }
        }
        Set<String> resourceIds = new LinkedHashSet<>();
        if (permission.getResourceIds() != null) {
            for (String id : permission.getResourceIds()) {
                if (StringUtils.isBlank(id)) {
                    throw new ServiceException("Tài nguyên không được để trống");
                }
                resourceIds.add(id);
            }
            if (!resourceIds.isEmpty()
                    && this.permissionDAO.countResourcesByIds(new ArrayList<>(resourceIds)) != resourceIds.size()) {
                throw new ServiceException("Tài nguyên không tồn tại");
            }
        }
        if (permission.getDataPermissions() != null) {
            for (DataPermission item : permission.getDataPermissions()) {
                if (item == null) {
                    throw new ServiceException("Quyền dữ liệu không được để trống");
                }
                if (StringUtils.isBlank(item.getRouterId())) {
                    throw new ServiceException("Định tuyến của quyền dữ liệu không được để trống");
                }
                if (StringUtils.isBlank(item.getMetaId())) {
                    throw new ServiceException("Nguồn dữ liệu của quyền dữ liệu không được để trống");
                }
                if (StringUtils.isBlank(item.getRoleId())) {
                    throw new ServiceException("Vai trò của quyền dữ liệu không được để trống");
                }
                routerIds.add(item.getRouterId());
            }
            if (!routerIds.isEmpty()
                    && this.permissionDAO.countRoutersByIds(new ArrayList<>(routerIds)) != routerIds.size()) {
                throw new ServiceException("Định tuyến không tồn tại");
            }
        }
    }

    /**
     * LưuVai tròQuyền
     * @param roleId
     * @return
     */
    @Transactional
    public int savePermission(String roleId, Permission permission) {
        // DS118 A: validate truoc khi xoa/ghi (xem validateSavePermission).
        this.validateSavePermission(roleId, permission);
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

    
    /**
     * Theo vai tròIdđượcĐếnDữ liệuQuyềnDanh sách
     * @param roleId
     * @return
     */
    public List<DataPermission> listDataPermissionByRoleId(String roleId) {
        return this.permissionDAO.listDataPermissionByRoleId(roleId);
    }
    
    
	
	/**
	 * Theo người dùngidđượcĐếnDữ liệuQuyềnDanh sách
	 * @param userId
	 * @return
	 */
    public List<DataPermission> listDataPermissionByUserId(String userId,String companyId) {
		return permissionDAO.listDataPermissionByUserId(userId,companyId);
	}
    
    /**
     * 
     */ 
    public List<String> listDefaultResourceByRouters(List<Router> routers) {
    	return  permissionDAO.listDefaultResourceByRouters(routers);
    }

    
}
