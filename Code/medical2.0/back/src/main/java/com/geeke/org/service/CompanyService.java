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
    /**
     * DS146 A: @Length tren entity (name/fullName/code/dia-chi/phone/
     * licenseSubject) + remarks theo gioi han cot DB 255 (mau DS141).
     * Generic /save co @Valid, nhung saveWithFile parse tu String
     * (khong @Valid) va bulk* bo qua ca save() lan @Valid nen gia tri
     * qua dai 500 o DB (probe A5: name 129). Guard o day dung chung
     * cho save() (ca saveWithFile qua 1-arg save) va bulk*.
     */
    private static void checkCompanyLengths(Company company) {
        if (company == null) {
            return;
        }
        checkLength(company.getName(), 128, "Tên Độ dài phải nằm trong khoảng 1 đến 128");
        checkLength(company.getFullName(), 512, "toàn bộ gọi là Độ dài phải nằm trong khoảng 1 đến 512");
        checkLength(company.getCode(), 64, "Độ dài mã phải nằm trong khoảng 1 đến 64 ");
        checkLength(company.getAddressProvince(), 10, "Địa chỉ(Tỉnh)Độ dài phải từ 1  và  10 Giữa");
        checkLength(company.getAddressCity(), 10, "Địa chỉ(Thành phố)Độ dài phải từ 1  và  10 Giữa");
        checkLength(company.getAddressRegion(), 10, "Địa chỉ(Quận)Độ dài phải từ 1  và  10 Giữa");
        checkLength(company.getAddress(), 100, "Độ dài địa chỉ phải nằm trong khoảng 1 đến 100 ");
        checkLength(company.getPhoneNumber(), 21, "Độ dài số điện thoại phải nằm trong khoảng 1 đến 21 ");
        checkLength(company.getLicenseSubject(), 100, "Độ dài môn học trong giấy phép hành nghề phải nằm trong khoảng 0 đến 100 ");
        checkLength(company.getRemarks(), 255, "Ghi chú Độ dài phải nằm trong khoảng 0 đến 255");
        // DS147 C: cac truong khong @Length nhung mapper co viet -- gioi han
        // theo cot DB. contactName viet ca o bulkInsert (probe C1); appId/
        // appSecret/fixmedinsName/fixmedinsCode/mdtrtareaAdmvs chi viet o
        // save-insert thuong (probe C2/C3 qua saveWithFile, duong UI that --
        // generic /save @Valid-400 moi create vi @NotNull company/lesseeId
        // nen khong bao gio toi duoc guard do dai). file_id la bigint va
        // private/public key la TEXT nen khong guard do dai.
        checkLength(company.getContactName(), 100, "Tên người liên hệ Độ dài phải nằm trong khoảng 0 đến 100");
        checkLength(company.getAppId(), 255, "Mã ứng dụng Độ dài phải nằm trong khoảng 0 đến 255");
        checkLength(company.getAppSecret(), 255, "Khóa bí mật ứng dụng Độ dài phải nằm trong khoảng 0 đến 255");
        checkLength(company.getFixmedinsName(), 100, "Tên cơ sở y tế được chỉ định Độ dài phải nằm trong khoảng 0 đến 100");
        checkLength(company.getFixmedinsCode(), 12, "Mã cơ sở y tế được chỉ định Độ dài phải nằm trong khoảng 0 đến 12");
        checkLength(company.getMdtrtareaAdmvs(), 255, "Mã địa phương BHYT Độ dài phải nằm trong khoảng 0 đến 255");
    }

    private static void checkLength(String value, int max, String message) {
        if (value != null && value.length() > max) {
            throw new ServiceException(message);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public Company save(Company company) {
        checkCompanySaveOwnership(company);
        // DS146 A: chan do dai (ca duong saveWithFile khong @Valid).
        checkCompanyLengths(company);
        return super.save(company);
    }

    /**
     * DS145 D: controller bulk* chi gate quyen company:create/update/
     * delete roi goi thang CrudService.bulk -- bo qua moi guard cua
     * save(): bulkInsert thieu truong 500 NOT NULL (probe D1),
     * bulkUpdate id ma 200 im lang (probe D2 -- CrudService stored-row
     * check bo qua stored==null theo thiet ke ITER35, va Company khong
     * co cot company_id nen check tenant tren stored row cung bo qua),
     * bulkInsert entry null 500 NPE o vong lap CrudService (probe D3).
     * Ghi de theo mau DS99/DS108: null/rong -> 400, entry null -> 400,
     * bulkUpdate id trong / dong khong ton tai / da xoa mem -> 400;
     * truong bat buoc null-only theo dung hop dong DS85 cua saveWithFile
     * (name/sort/addressProvince/addressCity/addressRegion/address/
     * phoneNumber -- '' duoc chap nhan vi company live mang '');
     * chay checkCompanySaveOwnership tung dong nhu save() (chan tao/cap
     * nhat xuyen nhom goc). bulkDelete ghost giu 200 rows 0 theo parity
     * DS99 G11/DS126 B.
     */
    private void checkCompanyRequired(Company company) {
        if (company == null) {
            throw new ServiceException("Công ty không được để trống");
        }
        if (company.getName() == null) {
            throw new ServiceException("Tên công ty không được để trống");
        }
        if (company.getSort() == null) {
            throw new ServiceException("Sắp xếp không được để trống");
        }
        if (company.getAddressProvince() == null) {
            throw new ServiceException("Địa chỉ (Tỉnh) không được để trống");
        }
        if (company.getAddressCity() == null) {
            throw new ServiceException("Địa chỉ (Thành phố) không được để trống");
        }
        if (company.getAddressRegion() == null) {
            throw new ServiceException("Địa chỉ (Quận) không được để trống");
        }
        if (company.getAddress() == null) {
            throw new ServiceException("Địa chỉ không được để trống");
        }
        if (company.getPhoneNumber() == null) {
            throw new ServiceException("Số điện thoại không được để trống");
        }
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<Company> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách công ty thêm mới không được để trống");
        }
        for (Company company : entitys) {
            checkCompanyRequired(company);
            checkCompanySaveOwnership(company);
            // DS146 A: bulk cung chan do dai nhu save().
            checkCompanyLengths(company);
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<Company> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách công ty cập nhật không được để trống");
        }
        for (Company company : entitys) {
            if (company == null) {
                throw new ServiceException("Công ty không được để trống");
            }
            if (StringUtils.isBlank(company.getId())) {
                throw new ServiceException("Cập nhật hàng loạt yêu cầu id của công ty đã tồn tại");
            }
            Company stored = dao.get(company.getId());
            if (stored == null || !"0".equals(stored.getDelFlag())) {
                throw new ServiceException("Công ty không tồn tại: " + company.getId());
            }
            checkCompanyRequired(company);
            checkCompanySaveOwnership(company);
            // DS146 A: bulk cung chan do dai nhu save().
            checkCompanyLengths(company);
        }
        return super.bulkUpdate(entitys);
    }

    /**
     * DS135 A: single delete fail-closed 400 khi id rong / khong ton tai /
     * da xoa mem (del_flag '1'). Truoc day: ghost -> 200 data 0 im lang,
     * xoa lan 2 -> 200 rows 1, blank id -> 200 data 0 (mapper soft-delete
     * UPDATE org_company SET del_flag='1' WHERE id khong loc del_flag;
     * CrudService stored-row tenant check bo qua stored row null -- lop
     * DS123; TreeService-based services chua bao gio duoc quet vi sweep
     * truoc chi nhin subclass truc tiep cua CrudService). checkExists
     * (DS103) KHONG tai dung: cho qua blank id, khong check delFlag.
     * bulkDelete ghost CO TINH GIU 200 rows 0 (DS99/DS106/DS126 B
     * idempotency -- generic mapper-direct bulkDelete, khong qua
     * delete() guard nay). Company khong co cot company_id nen
     * verifyTenantOwnershipAgainstStoredRow bo qua -- vo hai.
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(Company entity) {
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được công ty cần xóa.");
        }
        Company stored = dao.get(entity.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Công ty không tồn tại: " + entity.getId());
        }
        verifyTenantOwnershipAgainstStoredRow(entity);
        return super.delete(entity);
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
        // DS85: saveWithFile parses the entity from a String (no @Valid),
        // so nulls reach here and 500 (NPE at getFileId() for a null
        // entity; DB NOT NULL at the insert/update for null name/sort/
        // address fields/phone). Guard null-only BEFORE any file op,
        // same contract as the plain-save @NotNull fields (DS80) and
        // the DS83 null-only pattern. '' is NOT rejected: both live
        // companies carry '' address fields, and the UI submits them
        // back on update -- a blank guard would 400 a working update.
        // Clinic creation is unaffected: ClinicService.save calls the
        // 1-arg save(), never this overload.
        if (company == null) {
            throw new ServiceException("Công ty không được để trống");
        }
        if (company.getName() == null) {
            throw new ServiceException("Tên công ty không được để trống");
        }
        if (company.getSort() == null) {
            throw new ServiceException("Sắp xếp không được để trống");
        }
        if (company.getAddressProvince() == null) {
            throw new ServiceException("Địa chỉ (Tỉnh) không được để trống");
        }
        if (company.getAddressCity() == null) {
            throw new ServiceException("Địa chỉ (Thành phố) không được để trống");
        }
        if (company.getAddressRegion() == null) {
            throw new ServiceException("Địa chỉ (Quận) không được để trống");
        }
        if (company.getAddress() == null) {
            throw new ServiceException("Địa chỉ không được để trống");
        }
        if (company.getPhoneNumber() == null) {
            throw new ServiceException("Số điện thoại không được để trống");
        }
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



    /**
     * DS21: ep pham vi nhom co so cho ho list Company — tiep DS16 (chi sua
     * doc don) va Z3c (chi sua tree). listPage/listAll generic (CrudService)
     * goi ensureCompanyFilter nhung ham do bo qua Company (khong co field
     * "company" rieng), va CompanyDao list* khong he tham chieu
     * #{id}/#{institution} cua PageRequest — chi del_flag + whereParams
     * cua caller — nen moi session co 1 quyen deu doc TOAN BO org_company
     * kem appSecret/privateKey/publicKey (da tai hien live 11/11 pre).
     * Pham vi = pham vi cay Z3c / isCompanyVisibleToSession (DS16):
     * ids bat dau bang "0.<root>." (tenant + parent root + sibling cung
     * co so). Session sentinel rong/"null" hoac khong tim thay dong tenant
     * giu hanh vi cu (admin he thong van thay tat ca); tenant ids rong
     * tra rong (parity tree). Caller param giu nguyen, chi AND them ids.
     */
    private String sessionGroupPrefix() {
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return null;
        }
        Company tenant = this.dao.get(tenantId);
        if (tenant == null) {
            return null;
        }
        String ids = tenant.getIds();
        if (StringUtils.isBlank(ids)) {
            return "";
        }
        java.util.List<String> tokens = new ArrayList<String>();
        for (String part : ids.split("\\.")) {
            if (StringUtils.isNotBlank(part)) {
                tokens.add(part);
            }
        }
        if (tokens.isEmpty() || (tokens.size() == 1 && "0".equals(tokens.get(0)))) {
            return "";
        }
        return "0".equals(tokens.get(0)) && tokens.size() >= 2
                ? "0." + tokens.get(1) + "."
                : tokens.get(0) + ".";
    }

    private List<Parameter> scopedParams(List<Parameter> parameters, String groupPrefix) {
        List<Parameter> scoped = parameters == null
                ? new ArrayList<Parameter>()
                : new ArrayList<Parameter>(parameters);
        scoped.add(new Parameter("ids", "right_like", groupPrefix));
        return scoped;
    }

    @Override
    public Page<Company> listPage(List<Parameter> parameters, int offset, int limit, String orderby) {
        String groupPrefix = sessionGroupPrefix();
        if (groupPrefix == null) {
            return super.listPage(parameters, offset, limit, orderby);
        }
        if (groupPrefix.isEmpty()) {
            return new Page<Company>(0, new ArrayList<Company>());
        }
        return super.listPage(scopedParams(parameters, groupPrefix), offset, limit, orderby);
    }

    @Override
    public List<Company> listAll(List<Parameter> parameters, String orderby) {
        String groupPrefix = sessionGroupPrefix();
        if (groupPrefix == null) {
            return super.listAll(parameters, orderby);
        }
        if (groupPrefix.isEmpty()) {
            return new ArrayList<Company>();
        }
        return super.listAll(scopedParams(parameters, groupPrefix), orderby);
    }

    public Page<Company> listTenantPage(List<Parameter> parameters, int offset, int limit, String orderby) {
        String groupPrefix = sessionGroupPrefix();
        if (groupPrefix != null && groupPrefix.isEmpty()) {
            return new Page<Company>(0, new ArrayList<Company>());
        }
        PageRequest pageRequest = buildTenantPageRequest(
                groupPrefix == null ? parameters : scopedParams(parameters, groupPrefix), offset, limit, orderby);
        return paginate(
            () -> this.dao.countTenant(pageRequest),
            () -> this.dao.listTenantPage(pageRequest)
        );
    }

    public List<Company> listAllTenant(List<Parameter> parameters, String orderby) {
        String groupPrefix = sessionGroupPrefix();
        if (groupPrefix != null && groupPrefix.isEmpty()) {
            return new ArrayList<Company>();
        }
        PageRequest pageRequest = buildTenantPageRequest(
                groupPrefix == null ? parameters : scopedParams(parameters, groupPrefix), orderby);
        return this.dao.listAllTenant(pageRequest);
    }

    public Page<Company> listClinicPage(List<Parameter> parameters, int offset, int limit, String orderby) {
        String groupPrefix = sessionGroupPrefix();
        if (groupPrefix != null && groupPrefix.isEmpty()) {
            return new Page<Company>(0, new ArrayList<Company>());
        }
        PageRequest pageRequest = buildTenantPageRequest(
                groupPrefix == null ? parameters : scopedParams(parameters, groupPrefix), offset, limit, orderby);
        return paginate(
            () -> this.dao.countClinic(pageRequest),
            () -> this.dao.listClinicPage(pageRequest)
        );
    }

    public List<Company> listAllClinic(List<Parameter> parameters, String orderby) {
        String groupPrefix = sessionGroupPrefix();
        if (groupPrefix != null && groupPrefix.isEmpty()) {
            return new ArrayList<Company>();
        }
        PageRequest pageRequest = buildTenantPageRequest(
                groupPrefix == null ? parameters : scopedParams(parameters, groupPrefix), orderby);
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

    /**
     * DS16: Company co che rieng — CompanyDao.get khong select company_id
     * nen DataEntity.company luon null va CrudService.verifyTenantOwnership
     * return som, tuc san generic khong bao gio chan Company. Chi duong
     * doc don ban ghi moi can guard nay (list/tree da co scope rieng
     * Z3b/Z3c; write da co floor iter68). Pham vi hop le = pham vi cay
     * Z3c: dong cua chinh tenant + dong trong nhom co so "0.<root>."
     * (gom parent root + sibling cung co so). Session sentinel rong/
     * "null" va tenant khong tim thay dong thi giu hanh vi cu.
     * Row null -> false (caller tu quyet 404/null-data theo hop dong
     * cua endpoint do, khong doan 403 cho id khong ton tai).
     */
    @Transactional(readOnly = true)
    public boolean isCompanyVisibleToSession(Company row) {
        if (row == null) {
            return false;
        }
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return true;
        }
        if (tenantId.equals(row.getId())) {
            return true;
        }
        Company tenant = this.dao.get(tenantId);
        if (tenant == null) {
            return true;
        }
        String ids = tenant.getIds();
        if (StringUtils.isBlank(ids)) {
            return false;
        }
        java.util.List<String> tokens = new ArrayList<String>();
        for (String part : ids.split("\\.")) {
            if (StringUtils.isNotBlank(part)) {
                tokens.add(part);
            }
        }
        if (tokens.isEmpty() || (tokens.size() == 1 && "0".equals(tokens.get(0)))) {
            return false;
        }
        String groupPrefix = "0".equals(tokens.get(0)) && tokens.size() >= 2
                ? "0." + tokens.get(1) + "."
                : tokens.get(0) + ".";
        String rowIds = row.getIds();
        return StringUtils.isNotBlank(rowIds) && rowIds.startsWith(groupPrefix);
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