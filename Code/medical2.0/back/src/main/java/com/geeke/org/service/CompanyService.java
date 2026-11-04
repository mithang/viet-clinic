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
import com.geeke.common.service.ServiceException;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    /**
     * Z3h: chan ghi Company xuyen tenant. Company khong co cot company_id
     * rieng (chinh no la tenant) nen verifyTenantOwnership cua CrudService
     * khong bao gio tac dung: companyForm khong gui truong company, va
     * generic save chi kiem tra company body chu khong kiem tra dong dich.
     * Guard nay kiem tra dong cu + dong cha theo nhom goc cua session
     * (cung pham vi cay Z3c), truoc ca thao tac file cua saveWithFile.
     * Tao moi (id rong) giu hanh vi cu de khong pha luong tao phong kham
     * moi cua Clinic; dong khong ton tai / session rong / khong tim thay
     * dong tenant giu hanh vi cu, dung mau Z3a-Z3g.
     */
    @Override
    @Transactional(readOnly = false)
    public Company save(Company company) {
        checkCompanySaveOwnership(company);
        return super.save(company);
    }

    private void checkCompanySaveOwnership(Company company) {
        if (company == null) {
            return;
        }
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return;
        }
        Company tenant = this.dao.get(tenantId);
        if (tenant == null) {
            return;
        }
        String groupPrefix = groupPrefixFor(tenant);
        if (groupPrefix == null) {
            throw new ServiceException("Không xác định được nhóm phòng khám hiện tại");
        }
        if (StringUtils.isNotBlank(company.getId())) {
            Company old = this.dao.get(company.getId());
            if (old != null && (StringUtils.isBlank(old.getIds()) || !old.getIds().startsWith(groupPrefix))) {
                throw new ServiceException("Công ty không thuộc phòng khám hiện tại");
            }
        }
        if (company.getParent() != null && StringUtils.isNotBlank(company.getParent().getId())
                && !"0".equals(company.getParent().getId())) {
            Company parent = this.dao.get(company.getParent().getId());
            if (parent != null && (StringUtils.isBlank(parent.getIds()) || !parent.getIds().startsWith(groupPrefix))) {
                throw new ServiceException("Công ty mẹ không thuộc phòng khám hiện tại");
            }
        }
    }

    private String groupPrefixFor(Company tenant) {
        String ids = tenant.getIds();
        if (StringUtils.isBlank(ids)) {
            return null;
        }
        java.util.List<String> tokens = new ArrayList<String>();
        for (String part : ids.split("\\.")) {
            if (StringUtils.isNotBlank(part)) {
                tokens.add(part);
            }
        }
        if (tokens.isEmpty() || (tokens.size() == 1 && "0".equals(tokens.get(0)))) {
            return null;
        }
        return "0".equals(tokens.get(0)) && tokens.size() >= 2
                ? "0." + tokens.get(1) + "."
                : tokens.get(0) + ".";
    }

    @Transactional(readOnly = false)
    public Company save(Company company,
                        MultipartFile[] fileIdUploads,
                        String[] deleteIds) throws java.io.IOException {
        checkCompanySaveOwnership(company);
        if(StringUtils.isBlank(company.getFileId())) {
            company.setFileId(IdGen.uuid());
        }
        // K5 (iteration 58): guard deleteIds truoc moi upload/xoa/ghi, mau I1/J2.
        // checkCompanySaveOwnership chi guard company payload + company cha,
        // khong guard deleteIds; sys_file khong co company_id nen delete tran
        // truoc day xoa duoc file logo cua company la. Tap hop cho phep gom
        // fileId dang gui + fileId da luu cua company (UI xoa logo cu sau
        // khi doi fileId, nhu I1); file co object_id ngoai tap nay -> 400
        // truoc moi ghi de transaction rollback toan bo; file khong ton tai
        // giu hanh vi cu (bo qua, nhu R3).
        if (deleteIds != null && deleteIds.length > 0) {
            Set<String> allowedObjectIds = new HashSet<>();
            if (StringUtils.isNotBlank(company.getFileId())) {
                allowedObjectIds.add(company.getFileId());
            }
            if (StringUtils.isNotBlank(company.getId())) {
                Company stored = this.dao.get(company.getId());
                if (stored != null && StringUtils.isNotBlank(stored.getFileId())) {
                    allowedObjectIds.add(stored.getFileId());
                }
            }
            sysFileService.verifyDeleteIdsObjectIds(Arrays.asList(deleteIds), allowedObjectIds);
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

    // ITER65 item 2: parent_id cua company, phuc vu guard scope cung-parent.
    @Transactional(readOnly = true)
    public String getCompanyParentId(String id) {
        return this.dao.getCompanyParentId(id);
    }

    public List<Company> getSubordinateClinic(String companyId) {

        return this.dao.getCompanys(companyId);
    }
}