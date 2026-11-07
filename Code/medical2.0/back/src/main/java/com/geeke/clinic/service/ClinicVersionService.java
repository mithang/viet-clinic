package com.geeke.clinic.service;

import com.geeke.admin.common.dao.PermissionDAO;
import com.geeke.admin.entity.Role;
import com.geeke.admin.service.RoleService;
import com.geeke.clinic.dao.ClinicVersionDao;
import com.geeke.clinic.entity.ClinicVersion;
import com.geeke.common.data.Page;
import com.geeke.common.data.Parameter;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.org.entity.Company;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Phiên bản phòng khámService
 * @author txl
 * @version 2022-05-23
 */
 
@Service("clinicVersionService")
@Transactional(readOnly = false,rollbackFor = Exception.class)
public class ClinicVersionService extends CrudService<ClinicVersionDao, ClinicVersion>{

    @Autowired
    private RoleService roleService;
    @Autowired
    private PermissionDAO permissionDAO;
    @Autowired
    private ClinicVersionDao clinicVersionDao;

    public String getRoleIdById(String id)
    {
       return clinicVersionDao.getRoleIdById(id);
    }

    /**
     * N3b: chặn đọc phiên bản của tenant khác. Gốc rò rỉ: ClinicVersion
     * không khai báo field company riêng (chỉ kế thừa từ DataEntity) nên
     * ensureCompanyFilter của CrudService bỏ qua (hasOwnField dùng
     * getDeclaredField), và mapper ClinicVersionDao.xml cũng không select
     * cột company_id — get()/listPage() trước đây trả mọi tenant (probe
     * C2/C3b: phiên OWN đọc được version FOREIGN + 4 dòng live của tenant
     * thứ ba). Guard này vì vậy không tin entity/company object (mapper
     * không map), chỉ tin 2 lookup SELECT đơn bảng:
     * - clinic_version.company_id: khác rỗng mà khác tenant phiên → 400.
     *   NULL = catalogue dùng chung (mọi version tạo qua save() đều có
     *   role company 0 và company_id NULL vì INSERT không ghi cột này),
     *   nên NULL được đọc như cũ.
     * - sys_role.company_id của role liên kết: khác rỗng, khác "0" (dùng
     *   chung) mà khác tenant phiên → 400. Phòng trường hợp dòng NULL
     *   nhưng role đã bị re-home sang tenant khác.
     * - id không tồn tại (lookup null) hoặc phiên không session: cho qua
     *   như cũ (200 data null) — parity pre-fix.
     * Ghi/xóa ngoài phạm vi: delete/bulkDelete đã bị chặn bởi chuỗi guard
     * trên role (probe C1/C5b), save tạo dòng dùng chung theo thiết kế
     * (role company 0), không tính là ghi chéo tenant.
     */
    public void checkVersionReadTenant(String id) {
        if (StringUtils.isBlank(id)) {
            return;
        }
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return;
        }
        String storedCompany = clinicVersionDao.getCompanyIdById(id);
        if (StringUtils.isNotBlank(storedCompany) && !tenantId.equals(storedCompany)) {
            throw new ServiceException("Không có quyền truy cập tenant khác");
        }
        String roleId = clinicVersionDao.getRoleIdById(id);
        if (StringUtils.isNotBlank(roleId)) {
            String roleCompany = clinicVersionDao.getRoleCompanyIdById(roleId);
            if (StringUtils.isNotBlank(roleCompany) && !"0".equals(roleCompany)
                    && !tenantId.equals(roleCompany)) {
                throw new ServiceException("Không có quyền truy cập tenant khác");
            }
        }
    }

    @Override
    public ClinicVersion get(String id) {
        ClinicVersion entity = super.get(id);
        if (entity != null) {
            checkVersionReadTenant(id);
        }
        return entity;
    }

    /**
     * N3b: ép lọc đọc theo session tenant, giữ lại dòng dùng chung, qua
     * điều kiện tenant trong mapper (institution của PageRequest) thay vì
     * params — vì lọc list phải chặn cả hai trục: clinic_version.company_id
     * (own+NULL) VÀ sys_role.company_id của role liên kết (NULL/0/own).
     * Lọc own-only sẽ ẩn toàn bộ catalogue dùng chung (4 dòng live NULL +
     * mọi version tạo qua save() đều có role company 0 và company_id NULL
     * vì INSERT không ghi cột này), nên own+NULL mới đúng — khác N3e
     * (own-only, không có bằng chứng dùng chung). Tham số body có OR
     * được bọc ngoặc trước theo mẫu AA1 của CrudService, tránh disjunct
     * OR đứng trước thoát khỏi điều kiện tenant; company_id do client
     * gửi bị loại bỏ. Phiên không session: giữ hành vi cũ (institution
     * null → mapper không thêm điều kiện).
     */
    private List<Parameter> tenantFilteredParams(List<Parameter> parameters) {
        List<Parameter> params = parameters == null
                ? Lists.<Parameter>newArrayList() : Lists.newArrayList(parameters);
        Parameter.extractAndRemoveCompanyId(params);
        List<Parameter> rendering = Lists.newArrayList();
        boolean hasOr = false;
        for (Parameter p : params) {
            if (p == null || !isRenderingParam(p)) {
                continue;
            }
            rendering.add(p);
            String logic = p.getLogic();
            if (logic != null && "or".equalsIgnoreCase(logic.trim())) {
                hasOr = true;
            }
        }
        if (!rendering.isEmpty() && hasOr) {
            rendering.get(0).setLogic("");
            List<Parameter> wrapped = Lists.newArrayList();
            wrapped.add(new Parameter(null, "(", null, "AND"));
            wrapped.addAll(rendering);
            wrapped.add(new Parameter(null, ")", null, ""));
            params.clear();
            params.addAll(wrapped);
        }
        return params;
    }

    private boolean isRenderingParam(Parameter p) {
        String queryType = p.getQueryType();
        if (queryType == null) {
            return true;
        }
        String q = queryType.trim();
        if ("(".equals(q) || ")".equals(q)
                || "is null".equalsIgnoreCase(q) || "is not null".equalsIgnoreCase(q)) {
            return true;
        }
        Object value = p.getValue();
        if (value == null) {
            return false;
        }
        if (value instanceof String) {
            return !((String) value).isEmpty();
        }
        if (value instanceof List) {
            if ("in".equalsIgnoreCase(q) || "not in".equalsIgnoreCase(q)) {
                return !((List<?>) value).isEmpty();
            }
            return true;
        }
        return true;
    }

    private String sessionTenantOrNull() {
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return null;
        }
        return tenantId;
    }

    @Override
    public Page<ClinicVersion> listPage(List<Parameter> parameters, int offset, int limit, String orderby) {
        com.geeke.common.data.PageRequest pageRequest = new com.geeke.common.data.PageRequest(
                offset, limit, tenantFilteredParams(parameters), orderby, null, sessionTenantOrNull());
        int total = clinicVersionDao.count(pageRequest);
        List<ClinicVersion> list = total > 0 ? clinicVersionDao.listPage(pageRequest)
                : java.util.Collections.<ClinicVersion>emptyList();
        return new Page<ClinicVersion>(total, list);
    }

    @Override
    public List<ClinicVersion> listAll(List<Parameter> parameters, String orderby) {
        com.geeke.common.data.PageRequest pageRequest = new com.geeke.common.data.PageRequest(
                tenantFilteredParams(parameters), orderby, null, sessionTenantOrNull());
        return clinicVersionDao.listAll(pageRequest);
    }
    public ClinicVersion save(ClinicVersion entity) {
        // DS120 B: code trung lap truoc day roi xuong doInsert -> 500 UNIQUE
        // (code co UNI index), sau khi roleService.save da tao role moi
        // (duoc rollback nho @Transactional cap class, nhung van la 500
        // thay vi 400 ro nghia). Kiem tra TRUOC moi ghi, ca 2 nhanh.
        checkDuplicateCode(entity.getCode(), entity.getId());
        Role role = new Role();
        role.setName(entity.getName());
        role.setCode(entity.getCode());
        role.setIsLocked(0);
        Company comp = new Company();
        comp.setId("0");
        role.setCompany(comp);
        if (StringUtils.isBlank(entity.getId())) {
            Role save = roleService.save(role);
            entity.setRoleId(save.getId());
            entity.preInsert();
            this.doInsert(entity);
            this.saveAction(this.createAction("created", entity));
        } else {
            // DS5: explicit write checks for the update branch. The
            // generic verifies on roleService.save below accidentally
            // block foreign updates today (payload company "0" vs
            // session tenant for clinics; stored role company vs
            // tenant for system sessions), but nothing checks the
            // VERSION row itself — make it explicit so the guard
            // survives any refactor of the role path. Shared rows
            // (version company NULL + role company "0") pass, matching
            // N3b read semantics. The roleId must also match the
            // stored version's roleId: the UI form has no role field
            // and round-trips the stored roleId, so a differing
            // supplied roleId would rename + re-home an arbitrary
            // role through roleService.save below (its update writes
            // company_id from the forced company "0").
            checkVersionReadTenant(entity.getId());
            if (StringUtils.isNotBlank(entity.getRoleId())) {
                String storedRoleId = clinicVersionDao.getRoleIdById(entity.getId());
                if (StringUtils.isBlank(storedRoleId) || !storedRoleId.equals(entity.getRoleId())) {
                    throw new ServiceException("Không có quyền truy cập tenant khácDữ liệu");
                }
            }
            role.setId(entity.getRoleId());
            roleService.save(role);
            entity.preUpdate();
            this.doUpdate(entity);
            this.saveAction(this.createAction("updated", entity));
        }

        return entity;
    }

    // DS5: generic bulkUpdate verifies payload company (absent ->
    // pass) + stored company (the mapper does not map company_id,
    // so stored company is always null -> pass) — any session
    // could rename/reprice foreign version rows (bulkUpdate sets
    // only name/price/description/remarks). Pre-check every row
    // with the N3b read guard before any write (whole list fails
    // closed, N1 pattern). Shared rows pass, as for reads.
    // DS120 B: them fail-closed 400 -- null/rong/[null] -> 400 (truoc
    // day null NPE 500 o super.bulkUpdate, [] 500 o foreach rong),
    // id khong ton tai -> 400 (truoc day 200 silent, checkVersionReadTenant
    // bo qua khi stored lookup tra null), thieu name/price/code -> 400
    // (truoc day 500 NOT NULL vi bulk khong qua @Valid), doi code sang
    // code cua dong khac -> 400 (truoc day 500 UNIQUE). Kiem tra truoc
    // moi ghi de batch hon hop khong doi dong hop le.
    @Override
    public List<String> bulkUpdate(List<ClinicVersion> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách phiên bản không được để trống");
        }
        for (ClinicVersion entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Phiên bản không được để trống");
            }
            checkVersionReadTenant(entity.getId());
            if (StringUtils.isNotBlank(entity.getId())
                    && dao.get(entity.getId()) == null) {
                throw new ServiceException("Phiên bản không tồn tại: " + entity.getId());
            }
            validateBulkVersion(entity);
            checkDuplicateCode(entity.getCode(), entity.getId());
        }
        return super.bulkUpdate(entitys);
    }

    // DS120 B: bulkDelete generic -- null/rong -> 400 (duong chinh cua
    // controller la bulkDeleteClinic, nhung giu parity cho duong generic).
    @Override
    public int bulkDelete(List<ClinicVersion> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách phiên bản không được để trống");
        }
        return super.bulkDelete(entitys);
    }

    private void validateBulkVersion(ClinicVersion entity) {
        if (StringUtils.isBlank(entity.getName())) {
            throw new ServiceException("Tên phiên bản không được để trống");
        }
        if (StringUtils.isBlank(entity.getCode())) {
            throw new ServiceException("Mã phiên bản không được để trống");
        }
        if (entity.getPrice() == null) {
            throw new ServiceException("Giá phiên bản không được để trống");
        }
    }

    private void checkDuplicateCode(String code, String selfId) {
        if (StringUtils.isBlank(code)) {
            return;
        }
        String ownerId = clinicVersionDao.getIdByCode(code);
        if (StringUtils.isNotBlank(ownerId)
                && (StringUtils.isBlank(selfId) || !ownerId.equals(selfId))) {
            throw new ServiceException("Mã phiên bản đã tồn tại: " + code);
        }
    }

    // DS78: bulkInsert bypasses save(), so it is the only path that
    // can plant a version whose roleId points at no sys_role row.
    // A dangling roleId breaks nothing on read (the N3b guard and the
    // listPage LEFT JOIN are null-safe), but it silently breaks clinic
    // provisioning: CompanyService.saveClinicAndRole copies the version
    // role's router/resource/data permissions into the new clinic admin
    // role, and a nonexistent role copies an empty set (probe-proven:
    // 0 resources / 0 routers vs 12 / 5 for a real version role), so
    // the new clinic's admin ends up with no permissions at all.
    // Fail the whole list closed when a non-blank, non-"0" roleId has
    // no sys_role row (role_id is NOT NULL DEFAULT 0; "0" = no role).
    // Single save() needs no check: it always creates a real role and
    // overwrites roleId. bulkUpdate cannot change roleId.
    // DS120 B: them fail-closed 400 -- null/rong/[null] -> 400 (truoc
    // day null NPE 500 o vong DS78, [] 500 o foreach rong), thieu
    // name/price/code -> 400 (truoc day 500 NOT NULL), trung code trong
    // cung list hoac voi dong live -> 400 (truoc day 500 UNIQUE).
    @Override
    public List<String> bulkInsert(List<ClinicVersion> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách phiên bản không được để trống");
        }
        java.util.Set<String> seenCodes = new java.util.HashSet<>();
        for (ClinicVersion entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Phiên bản không được để trống");
            }
            validateBulkVersion(entity);
            if (!seenCodes.add(entity.getCode())) {
                throw new ServiceException("Mã phiên bản trùng trong danh sách: "
                        + entity.getCode());
            }
            checkDuplicateCode(entity.getCode(), null);
            String roleId = entity.getRoleId();
            if (StringUtils.isNotBlank(roleId) && !"0".equals(roleId)
                    && roleService.get(roleId) == null) {
                throw new ServiceException("Vai trò liên kết của phiên bản không tồn tại");
            }
        }
        return super.bulkInsert(entitys);
    }

    // DS129 C: single delete fail-closed 400 -- mau DrugService.delete.
    // Truoc day delete ghost/blank deu 200 data 0 silent no-op (mapper
    // UPDATE clinic_version SET del_flag='1' WHERE id, KHONG loc del_flag;
    // probe DS129 C pre-fix 8/10). Chuoi guard tren role (DS120 B / N3b)
    // KHONG chan duoc ghost/blank: getRoleIdById tra blank nen bo qua het,
    // roi this.delete -> rows 0 -> 200. deleteClinic goi this.delete nen
    // override nay bao ca duong controller duy nhat. dao.get KHONG loc
    // del_flag nen tra ca dong da xoa mem, delFlag phan biet.
    // Double-delete cua version CO role tu truoc da 400 nho guard xoa role
    // DS123 (role da xoa mem); override nay dong lo o tang version, truoc
    // moi buoc role. bulkDelete ghost GIU NGUYEN 200 rows 0 idempotent
    // (DS99/DS106/DS126 B).
    @Override
    public int delete(ClinicVersion entity) {
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được phiên bản cần xóa.");
        }
        ClinicVersion stored = dao.get(entity.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Phiên bản không tồn tại: " + entity.getId());
        }
        verifyTenantOwnershipAgainstStoredRow(entity);
        return super.delete(entity);
    }

    // DS120 B: deleteClinic truoc day tin roleId trong PAYLOAD, khong
    // phai dong da luu -- goi {id: version-cua-minh, roleId: role-nan-nhan}
    // tra 200 rows 1, xoa mem role nan nhan + xoa sach quyen cua no
    // (probe-proven, cung mau DS5 ma nhanh save-update da sua). Nay lay
    // roleId tu dong da luu; payload roleId lech -> 400 truoc moi ghi;
    // roleId rong/"0" (khong co role) thi bo qua buoc xoa role.
    public int deleteClinic(ClinicVersion entity) {
        String storedRoleId = null;
        if (entity != null && StringUtils.isNotBlank(entity.getId())) {
            storedRoleId = clinicVersionDao.getRoleIdById(entity.getId());
            if (StringUtils.isNotBlank(entity.getRoleId())
                    && StringUtils.isNotBlank(storedRoleId)
                    && !storedRoleId.equals(entity.getRoleId())) {
                throw new ServiceException("Vai trò liên kết của phiên bản không khớp");
            }
        }
        //XóaPhòng khám
        int num = this.delete(entity);
        if (num > 0 && StringUtils.isNotBlank(storedRoleId)
                && !"0".equals(storedRoleId)) {
            //XóaQuyền và Vai trò
            Role delete = new Role(storedRoleId);
            roleService.delete(delete);
            removePermisson(storedRoleId);
        }
        return num;
    }

    private void removePermisson(String roleId) {
        this.permissionDAO.removeRouterByRoleId(roleId);
        this.permissionDAO.removeResourceByRoleId(roleId);
        this.permissionDAO.removeDataPermssionByRoleId(roleId);
    }

    // DS120 B: nhu deleteClinic -- bulkDeleteClinic truoc day xoa role theo
    // roleId trong payload tung dong. Nay kiem tra truoc moi ghi (lech ->
    // 400, ca batch khong doi), roi xoa role theo id da luu.
    public int bulkDeleteClinic(List<ClinicVersion> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách phiên bản không được để trống");
        }
        java.util.Map<String, String> storedRoleIds = new java.util.HashMap<>();
        for (ClinicVersion e : entitys) {
            if (e == null || StringUtils.isBlank(e.getId())) {
                throw new ServiceException("Phiên bản không xác định được id cần xóa");
            }
            String storedRoleId = clinicVersionDao.getRoleIdById(e.getId());
            if (StringUtils.isNotBlank(e.getRoleId())
                    && StringUtils.isNotBlank(storedRoleId)
                    && !storedRoleId.equals(e.getRoleId())) {
                throw new ServiceException("Vai trò liên kết của phiên bản không khớp");
            }
            storedRoleIds.put(e.getId(), storedRoleId);
        }
        int num = this.bulkDelete(entitys);
        if (num > 0) {
            for (ClinicVersion e : entitys) {
                String roleId = storedRoleIds.get(e.getId());
                if (StringUtils.isNotBlank(roleId) && !"0".equals(roleId)) {
                    roleService.delete(new Role(roleId));
                    removePermisson(roleId);
                }
            }
        }
        return num;
    }

}