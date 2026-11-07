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
     * DS120 A: save voi id khong ton tai truoc day roi qua CrudService.save
     * -> doUpdate 0 dong roi van 200 silent no-op (verifyTenantOwnership-
     * AgainstStoredRow bo qua khi stored==null, cung mau DS94/DS99).
     * Kiem tra ton tai truoc moi ghi. Nhanh insert (id rong) khong doi:
     * CompanyService.saveClinicAndRole + ClinicVersionService.save deu
     * tao role moi khong id; nhanh update cua ClinicVersionService truyen
     * roleId da luu (da kiem tra o DS5) nen qua duoc.
     */
    @Override
    @Transactional(readOnly = false)
    public Role save(Role role) {
        if (role != null && StringUtils.isNotBlank(role.getId())
                && dao.get(role.getId()) == null) {
            throw new ServiceException("Vai trò không tồn tại: " + role.getId());
        }
        return super.save(role);
    }

    // DS120 A: bulkInsert/bulkUpdate/bulkDelete generic truoc day de lo:
    // null/rong/[null] -> 500 NPE / empty-foreach (chi vo tinh ve 400 nho
    // handler o mot so duong), thieu truong NOT NULL (name/code/company_id/
    // is_locked) -> 500 DataIntegrityViolation vi bulk khong qua @Valid,
    // bulkUpdate id khong ton tai -> 200 silent no-op / hon hop ghi mot
    // phan. Chan het o service de 400 ro nghia, kiem tra truoc moi ghi de
    // batch hon hop khong doi dong hop le. isLocked null mac dinh 0 (mau
    // Department null sort -> 0, DS80) vi cot NOT NULL khong co default
    // khi bind NULL tuong minh. @Transactional(readOnly = false) tuong minh
    // la bat buoc vi class-level la readOnly = true (mau DepartmentService
    // DS103).
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<Role> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách vai trò không được để trống");
        }
        for (Role role : entitys) {
            validateBulkRole(role);
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<Role> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách vai trò không được để trống");
        }
        for (Role role : entitys) {
            if (role == null || StringUtils.isBlank(role.getId())) {
                throw new ServiceException("Vai trò không xác định được id cần cập nhật");
            }
            if (dao.get(role.getId()) == null) {
                throw new ServiceException("Vai trò không tồn tại: " + role.getId());
            }
            validateBulkRole(role);
        }
        return super.bulkUpdate(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<Role> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách vai trò không được để trống");
        }
        return super.bulkDelete(entitys);
    }

    private void validateBulkRole(Role role) {
        if (role == null) {
            throw new ServiceException("Vai trò không được để trống");
        }
        if (StringUtils.isBlank(role.getName())) {
            throw new ServiceException("Tên vai trò không được để trống");
        }
        if (StringUtils.isBlank(role.getCode())) {
            throw new ServiceException("Mã vai trò không được để trống");
        }
        if (role.getCompany() == null || StringUtils.isBlank(role.getCompany().getId())) {
            throw new ServiceException("Công ty không được để trống");
        }
        if (role.getIsLocked() == null) {
            role.setIsLocked(0);
        }
    }

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