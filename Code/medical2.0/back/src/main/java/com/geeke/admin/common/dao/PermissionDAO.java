package com.geeke.admin.common.dao;

import com.geeke.admin.common.entity.DataPermission;
import com.geeke.admin.entity.Resource;
import com.geeke.admin.entity.Router;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

//import com.geeke.sys.entity.Resource;


/**
 * QuyềnQuản lýDAOGiao diện
 * 
 * @author lys
 * @date 2019/7/26
 */
@Mapper
public interface PermissionDAO {

	/**
	 * Theo người dùngidLấy quyền tài nguyên
	 * 
	 * @param userId
	 *            Người dùngId
	 * @return
	 */
	//List<Resource> listResourceByUserId(String userId);
	
	/**
	 * Theotuyến đườngidLấy quyền tài nguyên, Không bao gồm mục bị vô hiệu hóa
	 * @param userId
	 * @param routerId
	 * @return
	 */
	List<Resource> listResourcePermission(@Param("userId")String userId, @Param("routerId")String routerId);
	
	
	/**
	 * Lấy tuyến đường mà người dùng có quyền, Không bao gồm mục bị vô hiệu hóa
	 * @param userId
	 * @return
	 */
	List<Router> listRouterPermission(@Param("userId")String userId,@Param("companyId")String companyId);

	/**
	 * Lấy tuyến đường mà người dùng có quyền, Không bao gồm mục bị vô hiệu hóa
	 * @param roleId
	 * @return
	 */
	List<Router> listRouterPermissionByRoleId(@Param("roleId")String roleId);
	
	/**
	 * Theo người dùngidLấy quyền tài nguyên, Không bao gồm mục bị vô hiệu hóa
	 * @param userId
	 * @return
	 */
	List<String> listResourcePermissionByUserId(@Param("userId")String userId,@Param("companyId")String companyId);
	
	
	/**
	 * Theo vai tròidLấy quyền tài nguyên
	 * @param roleId
	 * @return
	 */
	List<String> listResourcePermissionByRoleId(String roleId);
	
	
    /**
     * Theo vai tròidXóatuyến đườngQuyền
     * @param roleId
     */
    void removeRouterByRoleId(String roleId);
	
	/**
	 * Theo vai tròidXóatài nguyênQuyền
	 * @param roleId
	 * @return
	 */
	int removeResourceByRoleId(String roleId);
    

    /**
     * Lưutuyến đườngQuyền
     * @param roleId
     * @param routerIds
     * @return
     */
    int saveRouterPermission(@Param("roleId")String roleId,  @Param("map")Map<String, String> map);
	

	/**
	 * Lưutài nguyênQuyền
	 * @param roleId
	 * @param resourceIds
	 * @return
	 */
	int saveResourcePermission(@Param("roleId")String roleId, @Param("map")Map<String, String> map);

	
	/**
	 * Theo vai tròidđượcĐếnDữ liệuQuyền
	 * @param roleId
	 * @return
	 */
	List<DataPermission> listDataPermissionByRoleId(String roleId);
	
	
	/**
	 * XóaVai tròDữ liệuQuyền
	 * @param roleId
	 * @return
	 */
	int removeDataPermssionByRoleId(@Param("roleId") String roleId);
	
	
	int saveDataPermission(@Param("dataPermissions") List<DataPermission> dataPermissions, @Param("ids") String[] ids);
	
	
	/**
	 * Theo người dùngidđượcĐếnDữ liệuQuyền
	 * @param userId
	 * @return
	 */
	List<DataPermission> listDataPermissionByUserId(@Param("userId") String userId,@Param("companyId")String companyId);
	
	
	/**
	 * Lấytuyến đường của Mặc địnhtài nguyên
	 * @param roleId
	 * @return
	 */
	List<String> listDefaultResourceByRouters(@Param("routers") List<Router> routers);
}
