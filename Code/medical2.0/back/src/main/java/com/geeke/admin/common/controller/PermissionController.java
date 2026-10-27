package com.geeke.admin.common.controller;

import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.ObjUtil;
import com.alibaba.fastjson.JSONObject;
import com.geeke.admin.common.entity.DataPermission;
import com.geeke.admin.common.entity.Permission;
import com.geeke.admin.common.service.PermissionService;
import com.geeke.admin.entity.Resource;
import com.geeke.admin.entity.Router;
import com.geeke.admin.entity.User;
import com.geeke.admin.service.ResourceService;
import com.geeke.admin.service.RouterService;
import com.geeke.clinic.entity.ClinicVersion;
import com.geeke.common.data.Parameter;
import com.geeke.common.data.SearchParamsBuilder;
import com.geeke.org.entity.Company;
import com.geeke.org.service.CompanyService;
import com.geeke.sys.controller.BaseController;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * tài nguyênkiểm soátthiết bị
 * 
 * @author Lining
 * @date 2018/2/11
 */
@RestController("PermissionController")
@RequestMapping("/permission")
public class PermissionController extends BaseController {
    
    @Autowired
    private PermissionService permissionService;
    
    @Autowired
    private RouterService routerService;
    
    @Autowired
    private ResourceService resourceService;

    @Autowired
    private CompanyService companyService;
    
    
    @GetMapping("/resources")
    public ResponseEntity<JSONObject> listResourcePermission(@RequestParam(required = false) String routerId) {
    	User user = SessionUtils.getUser();
    	
    	List<Resource> list = this.permissionService.listResourcePermission(user.getId(), routerId);
        return ResponseEntity.ok(ResultUtil.successJson(list));
    }
    
    @GetMapping("/permissions")
    public ResponseEntity<JSONObject> treePermission() {
    	List<Router> listRouter = this.listUnlockedRouter();

    	List<Resource> listResource = this.listUnlockedResource();
    	List<Permission> listPermission = Lists.newArrayList();
    	for(Router router: listRouter ){
    		Permission permission = new Permission();
    		permission.setId(router.getId());
    		permission.setType(Permission.ROUTER_TYPE);
    		permission.setName(router.getName());
    		permission.setCode(router.getCode());
    		permission.setParentId(router.getParent() == null? "": router.getParent().getId());
    		listPermission.add(permission);
    	}
    	
    	for(Resource resource: listResource) {
    		if(resource.getRouter() != null) {
    			Permission permission = new Permission();
    			permission.setId(resource.getId());
    			permission.setType(Permission.RESOURCE_TYPE);
    			permission.setName(resource.getName());
    			permission.setParentId(resource.getRouter().getId());
    			permission.setKey(resource.getId());
    			listPermission.add(permission);
    		}   		    		
    	}
    			
    	List<Permission> trees = this.bulid(listPermission);
    	return ResponseEntity.ok(ResultUtil.successJson(trees));
    }
    
    @GetMapping("/{roleId}/resourcePermissions")
    public ResponseEntity<JSONObject> listResourcePermissionByRoleId(@PathVariable("roleId") String roleId) {
    	List<String> list = this.permissionService.listResourcePermission(roleId);
    	return ResponseEntity.ok(ResultUtil.successJson(list));
    }
    
    @PostMapping("/{roleId}/savePermission")
    public ResponseEntity<JSONObject> savePermission(@PathVariable("roleId") String roleId, @RequestBody Permission permission) {
        int rows = this.permissionService.savePermission(roleId, permission);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }
    
    @GetMapping("/{roleId}/dataPermissions")
    public ResponseEntity<JSONObject> listDataPermissionByRoleId(@PathVariable("roleId") String roleId) {
    	List<DataPermission> list = this.permissionService.listDataPermissionByRoleId(roleId);
    	return ResponseEntity.ok(ResultUtil.successJson(list));
    }
    
    
    
    /**
     * haivòng lặp theo tầngTriển khaixây dựngcây
     * @param list truyền vào của câyNútDanh sách
     * ĐưalistDữ liệuQua id,  và parentIdđủgây ramộtCáicây
     * @return
     */
    private List<Permission> bulid(List<Permission> list) {
        List<Permission> trees = Lists.newArrayList();
        for (Permission entity : list) {
        	//Đã thêm code gỡ lỗi
        	if(Permission.ROUTER_TYPE.equals(entity.getType()) && (StringUtils.isBlank(entity.getParentId()) ||
        			"0".equals(entity.getParentId()))) {
                trees.add(entity);
            }

            for (Permission it : list) {
                if (entity.getId().equals(it.getParentId())) {
                    if (entity.getChildren() == null) {
                    	entity.setChildren(Lists.newArrayList());
                    }                	
                	entity.getChildren().add(it);
                }
            }
        }
        return trees;
    }
    
    /**
     * LấyChưaVô hiệu hóa của tuyến đường
     * @return
     */
    private List<Router> listUnlockedRouter() {
        // Hệ thốngcấptuyến đường chức năngKhông thểbị phútphối:  3006  mãQuản lý;	3007  Quản lý tuyến đường;   3003   Quản lý tài nguyên ;  3011  Dữ liệu từ điển;  3009    Nhật ký thao tác; 3024  Hệ thốngmã sốquy tắc; 3026 Chủ đề hệ thống;
        List<Parameter> parms = SearchParamsBuilder.create()
                .eq("is_locked", "0")
                .eq("can_permission", "1")
                .build();

        Company company = companyService.get(SessionUtils.getLoginTenantId());
        String parentId = Opt.ofNullable(company).map(Company::getParent).map(Company::getId).get();
        if (ObjUtil.isNotNull(company) && ObjUtil.notEqual(parentId, 0) && ObjUtil.notEqual("Nhà cung cấp", company.getName())) {
            //đối vớiPhòng khámgiới hạnchỉ định
            ClinicVersion version = company.getVersion();
            String roleId = version.getRoleId();
            return routerService.getAllRoutersByVersion(roleId);
        }
        return this.routerService.listAll(parms, "sort");
    }
    
    
    /**
     * LấyChưaVô hiệu hóa của tài nguyên
     * @return
     */
    private List<Resource> listUnlockedResource() {
        /* khôngtham và  phútphối của tài nguyên
         * Quản lý công ty: 41105517082353672 Thêmcông ty , 54172422505193473 Xóacông ty
         */
        List<Parameter> parms = SearchParamsBuilder.create()
                .eq("is_locked", "0")
                .eq("is_default", "0")  // Mặc định của khôngdùng phútphối
                .eq("can_permission", "1")
                .build();

        Company company = companyService.get(SessionUtils.getLoginTenantId());
        if (ObjUtil.isNotNull(company) && !"0".equals(company.getParent().getId()) && !"Nhà cung cấp".equals(company.getName())) {
            //đối vớiPhòng khámgiới hạnchỉ định
            ClinicVersion version = company.getVersion();
            String roleId = version.getRoleId();
            return resourceService.listAllByVersion(roleId);
        }
        return this.resourceService.listAll(parms, null);
    }
}
