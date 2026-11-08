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
        // DS80: sort NOT NULL nhung form co the khong gui; mac dinh 0 theo
        // mau CompanyService (company.setSort(0)) thay vi 500 o cot sort.
        if (department.getSort() == null) {
            department.setSort(0);
        }
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
                    throw new ServiceException("Không có quyền truy cập tenant khác");
                }
            }
            department.setCompany(new Company(tenantId));
        }
        Map<String, String> colMaps = Maps.newHashMap();
        // code
        colMaps.clear();
        colMaps.put("company_id", "company.id");
        colMaps.put("code", "code");

        // DS103: fail-closed 400 khi update id khong ton tai (ho DS94/
        // DS99). Block Z3a tren day pass khi dong khong ton tai (dao.get
        // tra null), TreeService.save sau do doUpdate cham 0 dong ma van
        // 200. Kiem tra sau Z3a (de bao toan thu tu tenant truoc), truoc
        // exists() trung ma (de bao toan C10/C11).
        if (StringUtils.isNotBlank(department.getId())
                && dao.get(department.getId()) == null) {
            throw new ServiceException("Phòng ban không tồn tại: " + department.getId());
        }

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
	 *
	 * DS135 B: ghost / da xoa mem (del_flag '1') -> 400 fail-closed.
	 * Truoc day ghost di qua guard DS51 (id khong rong) roi toi
	 * CrudService.delete, stored-row tenant check bo qua stored row
	 * null (lop DS123) va mapper soft-delete khong loc del_flag nen
	 * ghost -> 200 data 0 im lang, xoa lan 2 -> 200 rows 1. Kiem tra
	 * ton tai qua dao.get (delFlag phan biet) + verifyTenantOwnership-
	 * AgainstStoredRow truoc super.delete (department mang company_id
	 * nen stored check CO tac dung: xoa phong tenant khac -> 400).
	 * checkExists (DS103) KHONG tai dung: cho qua blank id, khong
	 * check delFlag.
	 */
	@Override
	@Transactional(readOnly = false)
	public int delete(Department department) {
		if (department == null || StringUtils.isBlank(department.getId())) {
			throw new ServiceException("Không xác định được phòng ban cần xóa.");
		}
		Department stored = dao.get(department.getId());
		if (stored == null || !"0".equals(stored.getDelFlag())) {
			throw new ServiceException("Phòng ban không tồn tại: " + department.getId());
		}
		verifyTenantOwnershipAgainstStoredRow(department);
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
		// DS103: null/rong -> 400 fail-closed, thay vi 200 [] silent
		// no-op (probe C5; C7 null da 400 o controller, chan them o day
		// de phong duong goi truc tiep service).
		if (entitys == null || entitys.isEmpty()) {
			throw new ServiceException("Danh sách phòng ban không được để trống");
		}
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
		// DS103: null/rong -> 400 fail-closed (probe C4; C7 null da 400
		// o controller). Vong lap qua save(): save() nem ServiceException
		// khi gap id khong ton tai (C2/C3) thi @Transactional rollback ca
		// list -- dong valid khong bi doi (probe C3).
		if (entitys == null || entitys.isEmpty()) {
			throw new ServiceException("Danh sách phòng ban không được để trống");
		}
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
		// DS103: null/rong -> 400 fail-closed (probe C6; C7 null da 400
		// o controller). Dong khong id van bo qua nhu cu (mau DS41).
		if (entitys == null || entitys.isEmpty()) {
			throw new ServiceException("Danh sách phòng ban không được để trống");
		}
		// DS135 B: bulkDelete chay vong lap qua this.delete() (khong co
		// bulkDelete mapper statement -- DS50), nen guard delete() moi
		// (ghost -> 400) se lat bulkDelete [ghost] tu 200 rows 0 thanh
		// 400 va lam [valid, ghost] rollback ca dong valid. Giu
		// idempotency DS99/DS106/DS126 B: pass 1 chi thu thap dong
		// CON SONG (dao.get != null va del_flag '0'), bo qua ghost /
		// da xoa mem / khong id; pass 2 moi goi this.delete() tren
		// dong song (van giu tenant check + audit tung dong). Dong
		// tenant khac van 400 o pass 2 va @Transactional rollback
		// toan bo -- dung bai DS133 B (dong xau khong ghi gi).
		List<Department> live = Lists.newArrayList();
		if (entitys != null) {
			for (Department department : entitys) {
				// Dong khong co id khong neu doi tuong nao (mau DS41):
				// bo qua thay vi de mapper <delete> nhanh otherwise
				// xoa theo company_id va quet sach ca cong ty.
				if (department != null && StringUtils.isNotBlank(department.getId())) {
					Department stored = dao.get(department.getId());
					if (stored != null && "0".equals(stored.getDelFlag())) {
						live.add(department);
					}
				}
			}
		}
		int rows = 0;
		for (Department department : live) {
			rows += this.delete(department);
		}
		return rows;
	}

}