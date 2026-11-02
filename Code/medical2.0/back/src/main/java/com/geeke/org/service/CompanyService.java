package com.geeke.org.service;

import com.geeke.admin.common.entity.DataPermission;
import com.geeke.admin.common.entity.Permission;
import com.geeke.admin.common.service.PermissionService;
import com.geeke.admin.entity.Role;
import com.geeke.admin.entity.Router;
import com.geeke.admin.service.RoleService;
import com.geeke.clinic.service.ClinicVersionService;
import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.service.TreeService;
import com.geeke.org.dao.CompanyDao;
import com.geeke.org.entity.Company;
import com.geeke.sys.service.SysFileService;
import com.geeke.common.constants.BizConstants;
import com.geeke.utils.IdGen;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Quản lý công tyService
 * @author lys
 * @version 2022-05-25
 */
 
@Service("companyService")
@Transactional(readOnly = true)
public class CompanyService extends TreeService<CompanyDao, Company> {
    @Autowired
    private SysFileService sysFileService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private ClinicVersionService clinicVersionService;

    @Transactional(readOnly = false)
    public Company saveTenant(Company company) {
        company.setSort(0);
        return save(company);
    }
    @Transactional(readOnly = false)
    public Company save(Company company,
                        MultipartFile[] fileIdUploads,
                        String[] deleteIds) throws java.io.IOException {
        if(StringUtils.isBlank(company.getFileId())) {
            company.setFileId(IdGen.uuid());
        }
        // Lưutệp đính kèm
        sysFileService.changeAndSaveSysFileList(fileIdUploads, company.getFileId());
        // Theo tệp đính kèmIDXóa thông tin tệp đính kèm
        if (null != deleteIds && deleteIds.length > 0) {
            sysFileService.delete(deleteIds);
        }
        return save(company);
    }

    @Transactional(readOnly = false)
    public Role saveClinicAndRole(Company company)
    {
        if(StringUtils.isBlank(company.getFileId()))
        {
            company.setFileId(IdGen.uuid());
        }
        company.setSort(0);
        //LưuCập nhậtPhòng khám
        company.setParent(company.getLesseeId());
        Company c = save(company);
        doUpdate(c);
        //LưuVai trò
        Role role = new Role();
        role.setName(BizConstants.ROLE_ADMIN);
        role.setCode(BizConstants.ROLE_ADMIN);
        role.setCompany(c);
        role.setIsLocked(0);
        String vid = company.getVersion().getId();
        String roleId = clinicVersionService.getRoleIdById(vid);
        role.setParentId(roleId);
        roleService.deleteRoleByCode(BizConstants.ROLE_ADMIN,company.getId());
        Role save = roleService.save(role);
        //LưuQuyềnquan hệ
        savePermisson(company, save);
        return save;
    }

    private void savePermisson(Company company, Role save) {
        String versionRoleId = company.getVersion().getRoleId();
        List<DataPermission> dataPermissions = permissionService.listDataPermissionByRoleId(versionRoleId);
        List<String> resourcePermission = permissionService.listResourcePermission(versionRoleId);
        List<Router> routers = permissionService.listRouterPermissionByRole(versionRoleId);
        List<String> routerIds = routers.stream().map(r -> r.getId()).collect(Collectors.toList());
        Permission permission = new Permission();
        permission.setDataPermissions(dataPermissions);
        permission.setResourceIds(resourcePermission.toArray(new String[resourcePermission.size()]));
        permission.setRouterIds(routerIds.toArray(new String[routers.size()]));
        permissionService.savePermission(save.getId(),permission);
    }



    public Page<Company> listTenantPage(List<Parameter> parameters, int offset, int limit, String orderby) {
        PageRequest pageRequest = buildTenantPageRequest(parameters, offset, limit, orderby);
        return paginate(
            () -> this.dao.countTenant(pageRequest),
            () -> this.dao.listTenantPage(pageRequest)
        );
    }

    public List<Company> listAllTenant(List<Parameter> parameters, String orderby) {
        PageRequest pageRequest = buildTenantPageRequest(parameters, orderby);
        return this.dao.listAllTenant(pageRequest);
    }

    public Page<Company> listClinicPage(List<Parameter> parameters, int offset, int limit, String orderby) {
        PageRequest pageRequest = buildTenantPageRequest(parameters, offset, limit, orderby);
        return paginate(
            () -> this.dao.countClinic(pageRequest),
            () -> this.dao.listClinicPage(pageRequest)
        );
    }

    public List<Company> listAllClinic(List<Parameter> parameters, String orderby) {
        PageRequest pageRequest = buildTenantPageRequest(parameters, orderby);
        return this.dao.listAllClinic(pageRequest);
    }

    /**TheoĐăng nhậptênTruy vấnPhòng khám*/
    public List<Company> getClinicsByLoginName(String loginName)
    {
        return this.dao.selectClinicsByLoginName(loginName);
    }


    /**
     * Z3c: gioi han cay cong ty theo session tenant. TreeService.tree goi
     * ensureCompanyFilter nhung ham do bo qua khi entity khong co field
     * "company" rieng — Company chi co parentId/lesseeId nen truoc day
     * tree tra TOAN BO org_company cho moi token co quyen (ca POST
     * /org/company/tree lan GET /noticesend/noticeSend/tree/{code}).
     * Pham vi hop le: cay con cua goc toi cao cua tenant (ids bat dau
     * bang "0.<root>."): gom tenant, cac phong kham cung co so va cac
     * cap duoi — dung pham vi nhom nhu getCompanys (sibling cung parent).
     * Param caller duoc GIU NGUYEN (ke ca id = own cua trang department)
     * roi AND them dieu kien ids, nen caller hop le khong doi ket qua,
     * con id la chi tra rong vi ids cua no khong co prefix nhom own.
     * Session rong/"null" hoac khong tim thay dong tenant thi giu hanh
     * vi cu, dung mau Z1/Z2/Z3b. Loi OR trong whereParams van la ton du
     * rieng (da ghi o checkpoint), khong sua trong nhip nay.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Company> tree(List<Parameter> parameters, String orderby) {
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return super.tree(parameters, orderby);
        }
        Company tenant = this.dao.get(tenantId);
        if (tenant == null) {
            return super.tree(parameters, orderby);
        }
        String ids = tenant.getIds();
        if (StringUtils.isBlank(ids)) {
            return new ArrayList<Company>();
        }
        java.util.List<String> tokens = new ArrayList<String>();
        for (String part : ids.split("\\.")) {
            if (StringUtils.isNotBlank(part)) {
                tokens.add(part);
            }
        }
        if (tokens.isEmpty() || (tokens.size() == 1 && "0".equals(tokens.get(0)))) {
            return new ArrayList<Company>();
        }
        // ids that: "0.<root>.<...tenant>."; neu khong co "0" thi token
        // dau la goc. Prefix luon kem dau "." cuoi de khop dung segment.
        String groupPrefix = "0".equals(tokens.get(0)) && tokens.size() >= 2
                ? "0." + tokens.get(1) + "."
                : tokens.get(0) + ".";
        List<Parameter> scoped = parameters == null
                ? new ArrayList<Parameter>()
                : new ArrayList<Parameter>(parameters);
        scoped.add(new Parameter("ids", "right_like", groupPrefix));
        return super.tree(scoped, orderby);
    }

    @Transactional(readOnly = true)
    public List<Company> getCompanys(String id) {
        // Z3b: ep tenant theo session — caller hop le (articlereview) luon
        // truyen currentUser.company.id, nen hanh vi hop le khong doi; id
        // body la chi tra ve sibling cua tenant khac (cung parent) neu
        // khong ep. Session rong/"null" giu hanh vi cu, dung mau Z1/Z2.
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isNotBlank(tenantId) && !"null".equals(tenantId)) {
            id = tenantId;
        }
        return this.dao.getCompanys(id);
    }

    @Transactional(readOnly = true)
    public String getInstitution(String id) {
        return this.dao.getInstitution(id);
    }

    public List<Company> getSubordinateClinic(String companyId) {

        return this.dao.getCompanys(companyId);
    }
}