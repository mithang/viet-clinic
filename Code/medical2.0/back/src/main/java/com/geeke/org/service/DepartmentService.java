package com.geeke.org.service;

import java.util.List;
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
import com.google.common.collect.Lists;
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

	/**
	 * DS51: chan som id rong o delete don — neu khong, mapper <delete>
	 * nhanh <otherwise> xoa theo company_id va quet sach toan bo phong
	 * cua tenant (probe-proven: POST /org/department/delete
	 * {company:{id:OWN}} tra 200 rows 2, ca 2 phong OWN del_flag=1,
	 * khong dong audit nao vi DS41 bo qua audit khi id rong).
	 * CrudService.delete khong yeu cau id: verifyTenantOwnership chi
	 * so payload company voi session tenant, stored-row check return
	 * som khi id rong. Mau UserService.delete (iteration 29) da chan
	 * tuong tu cho User. Chi chan wipe theo company_id; delete-by-parent
	 * co chu y o cac mapper khac (DictItem theo dict_type_id, ...) khong
	 * bi anh huong vi chung khong qua service nay. bulkDelete cua
	 * service nay (DS50) da bo qua dong khong id nen khong doi.
	 */
	@Override
	@Transactional(readOnly = false)
	public int delete(Department department) {
		if (department == null || StringUtils.isBlank(department.getId())) {
			throw new ServiceException("Không xác định được phòng ban cần xóa.");
		}
		return super.delete(department);
	}

	/**
	 * DS50: bulk* khong dung duong generic CrudService.bulk* (khong co
	 * statement trong DepartmentDao.xml nen moi goi deu 500 "Invalid
	 * bound statement", probe-proven) va cung khong duoc them statement
	 * tho: duong generic bo qua moi bat bien ma save() don ep buoc --
	 * Z3a ep company theo session tenant (bulkInsert voi company trong
	 * hoac cua tenant khac se trong orphan / sai tenant), kiem tra
	 * trung ma code theo company, TreeService.save nap parent (co
	 * kiem tra tenant) va tinh ids tu ids that cua parent roi viet lai
	 * ids ca cay con khi doi parent (duong generic goi preInsert tren
	 * parent tho chi co id nen con se nhan ids "<id>." nhu nut goc, va
	 * doi parent qua bulkUpdate de lai ids cay con cu). Vi vay bulk*
	 * chay vong lap qua save()/delete() don da kiem chung: moi dong
	 * huong day du Z3a + trung ma + parent/ids + audit nhu luu don.
	 * @Transactional(readOnly = false) tuong minh la bat buoc vi service
	 * nay khai bao class-level readOnly = true (mau DS40); mot dong loi
	 * nem ServiceException thi ca list rollback.
	 */
	@Override
	@Transactional(readOnly = false)
	public List<String> bulkInsert(List<Department> entitys) {
		List<String> ids = Lists.newArrayList();
		if (entitys != null) {
			for (Department department : entitys) {
				ids.add(this.save(department).getId());
			}
		}
		return ids;
	}

	@Override
	@Transactional(readOnly = false)
	public List<String> bulkUpdate(List<Department> entitys) {
		List<String> ids = Lists.newArrayList();
		if (entitys != null) {
			for (Department department : entitys) {
				ids.add(this.save(department).getId());
			}
		}
		return ids;
	}

	@Override
	@Transactional(readOnly = false)
	public int bulkDelete(List<Department> entitys) {
		int rows = 0;
		if (entitys != null) {
			for (Department department : entitys) {
				// Dong khong co id khong neu doi tuong nao (mau DS41):
				// bo qua thay vi de mapper <delete> nhanh otherwise
				// xoa theo company_id va quet sach ca cong ty.
				if (department != null && StringUtils.isNotBlank(department.getId())) {
					rows += this.delete(department);
				}
			}
		}
		return rows;
	}

}