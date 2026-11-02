package com.geeke.org.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geeke.common.service.TreeService;
import com.geeke.common.service.ServiceException;
import com.geeke.org.dao.DepartmentDao;
import com.geeke.org.entity.Company;
import com.geeke.org.entity.Department;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Maps;

/**
 * Quản lý phòng banService
 * @author lys
 * @version 2021-07-13
 */
 
@Service("departmentService")
@Transactional(readOnly = true)
public class DepartmentService extends TreeService<DepartmentDao, Department> {

	@Override
	@Transactional(readOnly = false)
	public Department save(Department department) {
        // Z3a: ep tenant theo session, theo mau ClinicOffice save (setCompany
        // session). Tao moi: company body bi ghi de bang tenant session, khong
        // tao duoc khoa/phong o tenant khac. Sua: dong cu thuoc tenant khac
        // thi fail-closed 400 thay vi bi dichuyen sang tenant session (update
        // cua CrudService chi kiem tra company body, khong kiem tra dong cu).
        // Session rong/"null" giu hanh vi cu, dung mau Z1/Z2.
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isNotBlank(tenantId) && !"null".equals(tenantId)) {
            if (StringUtils.isNotBlank(department.getId())) {
                Department old = dao.get(department.getId());
                if (old != null && old.getCompany() != null
                        && StringUtils.isNotBlank(old.getCompany().getId())
                        && !tenantId.equals(old.getCompany().getId())) {
                    throw new ServiceException("Khong co quyen truy cap tenant khac");
                }
            }
            department.setCompany(new Company(tenantId));
        }
        Map<String, String> colMaps = Maps.newHashMap();
        // code
        colMaps.clear();
        colMaps.put("company_id", "company.id");
        colMaps.put("code", "code");
        
        if(exists(dao, department, colMaps)) {
            throw new ServiceException("Trong cùng một công ty đã tồn tại mã phòng ban giống hệt");
        }

        Department departmentTemp = super.save(department);
        return departmentTemp;
	}

}