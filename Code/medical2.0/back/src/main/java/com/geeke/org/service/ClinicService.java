package com.geeke.org.service;

import cn.hutool.core.util.ObjUtil;
import com.geeke.admin.dao.UserDao;
import com.geeke.admin.dao.UserRoleDao;
import com.geeke.admin.entity.Role;
import com.geeke.admin.entity.User;
import com.geeke.admin.entity.UserRole;
import com.geeke.admin.service.RoleService;
import com.geeke.admin.service.UserService;
import com.geeke.clinic.entity.ClinicVersion;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.org.dao.ClinicDao;
import com.geeke.org.entity.Clinic;
import com.geeke.org.entity.Company;
import com.geeke.sys.service.SysFileService;
import com.geeke.utils.IdGen;
import com.geeke.utils.StringUtils;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Quản lý phòng khámService
 *
 * @author txl
 * @version 2022-05-19
 */

@Service("clinicService")
@Transactional(readOnly = true)
public class ClinicService extends CrudService<ClinicDao, Clinic> {
    @Autowired
    private SysFileService sysFileService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private UserDao userDao;


    @Transactional(readOnly = false)
    public Company save(Company clinic, MultipartFile[] fileIdUploads, String[] deleteIds) throws java.io.IOException {
        // DS82: phoneNumber is NOT NULL in org_company and the controller
        // parses the entity from a String (no @Valid), so a null phone
        // 500d at the company insert (and, had it passed, NPEd at
        // phoneNumber.substring in createUserAndRole). No live company
        // carries a blank phone, so blank is rejected too. Guard first,
        // before any file/company write, so the transaction has nothing
        // to roll back.
        if (StringUtils.isBlank(clinic.getPhoneNumber())) {
            throw new com.geeke.common.service.ServiceException("Số điện thoại phòng khám không được để trống");
        }
        // Provisioning option A, nhom 1 (2026-11-10): mat khau admin =
        // 6 ky tu cuoi so dien thoai (createUserAndRole :187 va nhanh
        // doi so :170 deu substring(len-6)). DS82 chi guard blank;
        // @Length(min=1,max=21) cho phep phone 1-5 ky tu -> substring
        // StringIndexOutOfBounds 500. Guard length<6 o day, truoc moi
        // file/company write, bao ca create lan update (update doi so
        // ngan cung qua nhanh :170). Khong doi hanh vi khi phone >= 6.
        if (clinic.getPhoneNumber().length() < 6) {
            throw new com.geeke.common.service.ServiceException("Số điện thoại phòng khám phải có ít nhất 6 ký tự");
        }
        if (StringUtils.isBlank(clinic.getFileId())) {
            clinic.setFileId(IdGen.uuid());
        }
        // L6 (iteration 59): guard deleteIds truoc moi upload/xoa/ghi, mau K5.
        // ClinicService.save truoc day goi sysFileService.delete(deleteIds)
        // tran truoc khi save clinic; sys_file khong co company_id nen
        // delete tran xoa duoc file logo cua clinic la (probe pre-fix:
        // clinic OWN + deleteIds file la -> 200 + file + content bi xoa).
        // Tap hop cho phep gom fileId dang gui + fileId da luu cua clinic
        // (UI xoa logo cu sau khi doi fileId, nhu K5); file co object_id
        // ngoai tap nay -> 400 truoc moi ghi de transaction rollback toan
        // bo; file khong ton tai giu hanh vi cu (bo qua, nhu R3).
        // Item nay CHI guard deleteIds; floor tao clinic giu nguyen theo
        // quyet dinh owner-decision tu iter22 (uy quyen toan quyen
        // 2026-11-05 cho phep guard nay, khong doi floor).
        // companyService.get la dao.get tran (khong co tenant check),
        // nen doc oldClinic som o day khong doi hanh vi, chi de lay
        // fileId da luu cho tap cho phep va tranh doc trung o duoi.
        Company oldClinic = null;
        if (StringUtils.isNotBlank(clinic.getId())) {
            oldClinic = companyService.get(clinic.getId());
        }
        // DS100: fail-closed 400 truoc moi ghi -- nhanh update NPE 500
        // khi version null (getVersion().getRoleId(), probe C2c) hoac
        // khong co admin user (admin.getId() :120, probe C2b); nhanh
        // create NPE 500 khi version null (saveClinicAndRole:215
        // getVersion().getId(), probe P2, log-proven). Guard o day,
        // truoc moi file/company write, de transaction khong co gi
        // phai rollback; happy path C2d (co admin + version day du)
        // khong doi.
        if (null != oldClinic) {
            if (clinic.getVersion() == null || StringUtils.isBlank(clinic.getVersion().getRoleId())) {
                throw new ServiceException("Phiên bản phòng khám (vai trò) không được để trống");
            }
            if (userService.getAdmin(oldClinic.getId()) == null) {
                throw new ServiceException("Phòng khám chưa có tài khoản quản trị");
            }
        } else {
            if (clinic.getVersion() == null || StringUtils.isBlank(clinic.getVersion().getId())
                    || StringUtils.isBlank(clinic.getVersion().getRoleId())) {
                throw new ServiceException("Phiên bản phòng khám không được để trống");
            }
        }
        if (null != deleteIds && deleteIds.length > 0) {
            Set<String> allowedObjectIds = new HashSet<>();
            if (StringUtils.isNotBlank(clinic.getFileId())) {
                allowedObjectIds.add(clinic.getFileId());
            }
            if (oldClinic != null && StringUtils.isNotBlank(oldClinic.getFileId())) {
                allowedObjectIds.add(oldClinic.getFileId());
            }
            sysFileService.verifyDeleteIdsObjectIds(Arrays.asList(deleteIds), allowedObjectIds);
        }
        // save file
        sysFileService.changeAndSaveSysFileList(fileIdUploads, clinic.getFileId());
        // Theo tệp đính kèmIDXóa thông tin tệp đính kèm
        if (null != deleteIds && deleteIds.length > 0) {
            sysFileService.delete(deleteIds);
        }

        //save clinic
        if (null != oldClinic) {
            //Đã tồn tại, SửaPhòng khám
            companyService.save(clinic);

            //Người dùng và QuyềnCập nhật
            User admin = userService.getAdmin(oldClinic.getId());
            userRoleDao.updateRelationForTenant(clinic.getVersion().getRoleId(), admin.getId());
        } else {
            //Thêm mớiPhòng khám
            Role role = companyService.saveClinicAndRole(clinic);

            //save admin user and admin role
            //is it exist already?
            User admin = userService.getAdmin(clinic.getId());
            if (null == admin) {
                User oldAdmin = userDao.getByLoginName(clinic.getPhoneNumber());
                if (null == oldAdmin) {
                    //Không có và khácPhòng khámliên kết
                    this.createUserAndRole(clinic, role);
                } else {
                    //ĐãcóPhòng khámliên kết
                    userDao.insertUserTenant(IdGen.uuid(), oldAdmin.getId(), clinic.getId(), oldAdmin.getLoginName());
                    UserRole userRole = new UserRole();
                    userRole.setUser(oldAdmin);
                    userRole.setRole(role);
                    userRole.setCompany(clinic);
                    userRole.setId(IdGen.uuid());
                    userRole.preInsert();
                    userRoleDao.insert(userRole);
                }
            } else {
                if (!admin.getLoginName().equals(clinic.getPhoneNumber())) {
                    String uid = admin.getId();
                    String phoneNumber = clinic.getPhoneNumber();
                    String newPassword = phoneNumber.substring(phoneNumber.length() - 6);
                    Md5Hash md5 = new Md5Hash(newPassword, uid, 10000);
                    String md5Password = md5.toHex();
                    userService.updateAdminPhoneAndPass(phoneNumber, md5Password, uid);
                }
                // update user role
                String roleId = role.getId();
                userRoleDao.updateRelationForTenant(roleId, admin.getId());
            }
        }
        return clinic;
    }

    @Transactional(readOnly = false)
    public void createUserAndRole(Company entity, Role role) {
        String companyName = entity.getName();
        String phoneNumber = entity.getPhoneNumber();
        String password = phoneNumber.substring(phoneNumber.length() - 6);

        User user = new User();
        user.setCompany(entity);
        user.setLoginName(phoneNumber);
        user.setLoginPassword(password);
        user.setLoginPasswordUpdate(true);
        user.setIsLocked(0);
        user.setPhone(phoneNumber);
        user.setEmail(phoneNumber + "@newtouch.com");
        user.setName("Quản trị viên phòng khám");
        user.setCreateBy("Quản trị viên hệ thống");
        user.setCreateDate(new Date());
        user.setUpdateBy("Quản trị viên hệ thống");
        user.setUpdateDate(new Date());
        user.setDelFlag("0");
        user.setIsAdmin(1);
        //ĐặtQuyền
        ClinicVersion version = entity.getVersion();
        List<UserRole> userRoleList = new ArrayList<>();
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setCompany(user.getCompany());
        userRoleList.add(userRole);
        userRole.setId(IdGen.uuid());
        userRole.preInsert();
        user.setUserRoleList(userRoleList);
        String id1 = userService.save(user).getId();
        if (user.getLoginPasswordUpdate()) {
            Md5Hash md5 = new Md5Hash(user.getLoginPassword(), user.getId(), 10000);
            String md5Password = md5.toHex();
            userDao.updateLoginPassword(user.getId(), md5Password);
        }
        userRoleDao.insert(userRole);
        userDao.insertUserTenant(IdGen.uuid(), id1, user.getCompanyId(), user.getLoginName());
        logger.info("sau máy tự động Tạo Người dùng: {}", id1);
    }

    @Transactional(readOnly = false)
    public void deleteClinic(Company entity) {
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được phòng khám cần xóa");
        }
        // DS136 A (L6 remainder): deleteClinic truoc day tin phoneNumber
        // cua PAYLOAD de tim admin user (userDao.getByPhone). Payload
        // xoa that chi gui {id} nen phoneNumber null -> getByPhone(null)
        // khong khop ai -> user_tenant cua admin KHONG bao gio bi danh
        // co, admin giu quyen tenant cua phong kham da xoa (probe A5
        // pre-fix: company del_flag 1 nhung user_tenant van 0). Doc
        // dong da luu TRUOC khi xoa (companyService.get la dao.get tran,
        // nhu save() da lam) va dung phoneNumber da luu khi payload
        // khong gui; payload co phone thi giu hanh vi cu. Guard them
        // userRoleList / role company null (de phong NPE) -- getByPhone
        // khong nap userRoleList nen nhanh xoa role hien la no-op, chi
        // user_tenant moi la phan lien quan toi quyen truy cap.
        Company stored = companyService.get(entity.getId());
        String phoneNumber = entity.getPhoneNumber();
        if (StringUtils.isBlank(phoneNumber) && stored != null) {
            phoneNumber = stored.getPhoneNumber();
        }
        companyService.delete(entity);
        if (StringUtils.isBlank(phoneNumber)) {
            return;
        }
        User user = userDao.getByPhone(phoneNumber);
        if (ObjUtil.isNotNull(user)) {
            List<UserRole> userRoleList = user.getUserRoleList();
            if (userRoleList != null) {
                List<UserRole> collect = userRoleList.stream().filter(u -> u.getCompany() != null
                        && u.getCompany().getId() != null
                        && u.getCompany().getId().equals(entity.getId())).collect(Collectors.toList());
                if (!collect.isEmpty()) {
                    userRoleDao.delete(collect.get(0));
                }
            }
            // H2: xoa phong kham chi danh co tenant cua phong kham bi xoa,
            // khong danh co tenant khac cua cung user quan tri.
            userDao.updateUserTenantDelFlag(user.getId(), "1", entity.getId());
        }
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<Clinic> entitys) {
        // DS137 B: bulkDelete goc (CrudService -> ClinicDao) chi chay
        // `UPDATE clinic SET del_flag=1` -- bang `clinic` la mirror legacy,
        // khong phai org_company (danh sach phong kham doc tu org_company
        // qua CompanyService.listClinicPage; single create/delete cung ghi
        // org_company). Nen xoa hang loat phong kham dang hien trong UI la
        // no-op lang le 200 rows 0, va khong bao gio don user_tenant nhu
        // deleteClinic (DS136 A) -- probe B1 pre-fix. Ghi de: kiem tra het
        // truoc khi ghi (null/blank id -> 400, giong DS126 B; clinic ngoai
        // tenant -> 400; ghost / da xoa -> bo qua, giu idempotency 200
        // rows 0 cua DS99 G11; id trung lap -> xoa 1 lan), roi xoa that
        // qua deleteClinic cho tung Company con song (soft-delete org_company
        // + don user_tenant cua admin). Khong cham bang `clinic`.
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách xóa không được để trống");
        }
        List<Company> toDelete = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();
        for (Clinic entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Dòng xóa không được để trống");
            }
            if (StringUtils.isBlank(entity.getId())) {
                throw new ServiceException("Dòng xóa không xác định được id cần xóa");
            }
            verifyTenantOwnership(entity);
            verifyTenantOwnershipAgainstStoredRow(entity);
            if (!seenIds.add(entity.getId())) {
                continue;
            }
            Company stored = companyService.get(entity.getId());
            if (stored == null || !"0".equals(stored.getDelFlag())) {
                continue;
            }
            if (!companyService.isCompanyVisibleToSession(stored)) {
                throw new ServiceException("Phòng khám không thuộc phòng khám hiện tại");
            }
            toDelete.add(stored);
        }
        int rows = 0;
        for (Company stored : toDelete) {
            deleteClinic(stored);
            rows++;
        }
        return rows;
    }

}