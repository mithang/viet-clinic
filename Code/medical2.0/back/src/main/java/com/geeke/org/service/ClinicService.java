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
        logger.info("saumáytự độngTạoNgười dùng: {}", id1);
    }

    @Transactional(readOnly = false)
    public void deleteClinic(Company entity) {
        companyService.delete(entity);
        String phoneNumber = entity.getPhoneNumber();
        User user = userDao.getByPhone(phoneNumber);
        if (ObjUtil.isNotNull(user)) {
            List<UserRole> userRoleList = user.getUserRoleList();
            List<UserRole> collect = userRoleList.stream().filter(u -> u.getCompany().getId().equals(entity.getId())).collect(Collectors.toList());
            if (!collect.isEmpty()) {
                userRoleDao.delete(collect.get(0));
            }
            // H2: xoa phong kham chi danh co tenant cua phong kham bi xoa,
            // khong danh co tenant khac cua cung user quan tri.
            userDao.updateUserTenantDelFlag(user.getId(), "1", entity.getId());
        }
    }

}