package com.geeke.admin.service;

import com.geeke.admin.dao.RoleDao;
import com.geeke.admin.entity.Role;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Quản lý vai tròService
 * @author lys
 * @version 2022-05-24
 */
 
@Service("roleService")
@Transactional(readOnly = true)
public class RoleService extends CrudService<RoleDao, Role>{
    @Autowired
    private RoleDao roleDao;

    /**
     * DS51: chan som id rong o delete don — neu khong, mapper <delete>
     * nhanh <otherwise> xoa theo company_id va quet sach toan bo vai tro
     * cua tenant (probe-proven: POST /admin/role/delete {company:{id:OWN}}
     * tra 200 rows 6, ca 4 vai tro live + 2 vai tro fixture cua OWN
     * del_flag=1, khong dong audit nao vi DS41 bo qua audit khi id rong).
     * CrudService.delete khong yeu cau id: verifyTenantOwnership chi so
     * payload company voi session tenant, stored-row check return som
     * khi id rong. Mau UserService.delete (iteration 29) da chan tuong
     * tu cho User. Cac caller noi bo (ClinicVersionService.deleteClinic /
     * bulkDeleteClinic) deu truyen new Role(roleId) co id nen khong doi.
     * Chi chan wipe theo company_id; delete-by-parent co chu y o cac
     * mapper khac khong bi anh huong vi chung khong qua service nay.
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(Role role) {
        if (role == null || StringUtils.isBlank(role.getId())) {
            throw new ServiceException("Không xác định được vai trò cần xóa.");
        }
        return super.delete(role);
    }

    public int deleteRoleByCode(String roleCode,String tenantId){
      return roleDao.deleteRoleByRoleCode(roleCode,tenantId);
    }

    /**
     * Truy vấnHiện tạiPhòng khámnơisở hữucó của Vai trò
     * @param tenantId
     * @return
     */
    public List<Role> listAllByTenantId(String tenantId)
    {
        return roleDao.listAllByTenantId(tenantId);
    }

}