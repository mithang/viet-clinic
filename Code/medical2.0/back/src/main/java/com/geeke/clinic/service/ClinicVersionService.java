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
            role.setId(entity.getRoleId());
            roleService.save(role);
            entity.preUpdate();
            this.doUpdate(entity);
            this.saveAction(this.createAction("updated", entity));
        }

        return entity;
    }

    public int deleteClinic(ClinicVersion entity) {
        String roleId = entity.getRoleId();
        //XóaPhòng khám
        int num = this.delete(entity);
        if(num>0)
        {
            //XóaQuyền và Vai trò
            Role delete = new Role(roleId);
            roleService.delete(delete);
            removePermisson(roleId);
        }
        return num;
    }

    private void removePermisson(String roleId) {
        this.permissionDAO.removeRouterByRoleId(roleId);
        this.permissionDAO.removeResourceByRoleId(roleId);
        this.permissionDAO.removeDataPermssionByRoleId(roleId);
    }

    public int bulkDeleteClinic(List<ClinicVersion> entitys) {
        int num = this.bulkDelete(entitys);
        if(num>0)
        {
            entitys.stream().forEach(e->{
                String roleId = e.getRoleId();
                roleService.delete(new Role(roleId));
                removePermisson(roleId);
            });
        }
        return num;
    }

}