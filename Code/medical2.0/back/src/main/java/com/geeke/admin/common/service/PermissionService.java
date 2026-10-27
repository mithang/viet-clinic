package com.geeke.admin.common.service;


import com.geeke.admin.common.dao.PermissionDAO;
import com.geeke.admin.common.entity.DataPermission;
import com.geeke.admin.common.entity.Permission;
import com.geeke.admin.entity.Resource;
import com.geeke.admin.entity.Router;
import com.geeke.utils.IdGen;
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
     * LưuVai tròQuyền
     * @param roleId
     * @return
     */
    @Transactional
    public int savePermission(String roleId, Permission permission) {
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
