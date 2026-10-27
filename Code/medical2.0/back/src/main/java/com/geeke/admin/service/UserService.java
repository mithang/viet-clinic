package com.geeke.admin.service;

import com.geeke.admin.dao.UserDao;
import com.geeke.admin.dao.UserExtDao;
import com.geeke.admin.dao.UserRoleDao;
import com.geeke.admin.entity.User;
import com.geeke.admin.entity.UserExt;
import com.geeke.admin.entity.UserRole;
import com.geeke.common.constants.ActionConstants;
import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.medicareutils.config.MedicareConfigProperties;
import com.geeke.org.entity.Clinic;
import com.geeke.org.entity.Company;
import com.geeke.org.service.CompanyService;
import com.geeke.sys.entity.Action;
import com.geeke.sys.entity.ActionRecycle;
import com.geeke.sys.entity.SysFile;
import com.geeke.sys.service.SysFileService;
import com.geeke.utils.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.crypto.hash.Md5Hash;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Quản lý người dùngService
 * @author lys
 * @version 2021-08-25
 */
 
@Service("userService")
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService extends CrudService<UserDao, User>{

    private final UserRoleDao userRoleDao;

    private final UserDao userDao;

    private final SysFileService sysFileService;

    private final UserExtService userExtService;

    private final CompanyService companyService;

    private final UserExtDao userExtDao;

    private final MedicareConfigProperties medicareConfigProperties;

    @Override
    public User get(String id) {
        User user = super.get(id);

        /*Lấy danh sách bảng con   Vai trò người dùng*/
        PageRequest pageRequest = buildPageRequest(com.google.common.collect.ImmutableMap.of(
                "user_id", user.getId(),
                "company_id", SessionUtils.getLoginTenantId()
        ));
        user.setUserRoleList(userRoleDao.listAll(pageRequest));
        UserExt userExt = userExtDao.getUserExtByUserId(user.getId(),SessionUtils.getLoginTenantId());
        user.setUserExt(userExt);
        return user;
    }

    public User CheckNewUserByPhone(String phone) {
        User user = userDao.getByPhone(phone);
        //ĐãtạiHệ thống trong Tồn tại
        if(null != user)
        {
            List<Company> clinicsByLoginName = companyService.getClinicsByLoginName(phone);
            List<String> oldCompanyId = clinicsByLoginName.stream().map(c -> c.getId()).collect(Collectors.toList());
            String newCompanyId = SessionUtils.getLoginTenantId();
          if(oldCompanyId.contains(newCompanyId))
          {
              //cùngPhòng khám của giống hệtNgười dùngbáo lỗi
              throw new ServiceException("đóNgười dùngĐãtạinàyPhòng khámđăng ký. ");
          }
        }
        //Không tồn tạiThì trả vềnull
        return user;
    }


    public void updateAdminPhoneAndPass(String loginName,String pass,String userId)
    {
         userDao.updateAdminPhoneAndPass(loginName,pass,userId);
    }
    @Transactional(readOnly = false)
    public String updatePersonAndPass(User userDetail)
    {
        boolean loginPasswordUpdate = userDetail.getLoginPasswordUpdate();
        User user = SessionUtils.getUser();
        String currentUserId = user.getId();
        User dto = this.get(currentUserId);
        dto.setName(userDetail.getName());
        dto.setLoginName(userDetail.getLoginName());
        dto.setLoginPassword(userDetail.getLoginPassword());
        dto.setLoginPasswordUpdate(loginPasswordUpdate);
        String id = this.save(dto).getId();
        if(loginPasswordUpdate)
        {
            Md5Hash md5 = new Md5Hash(userDetail.getLoginPassword(), user.getId(), 10000);
            String md5Password = md5.toHex();
            this.dao.updateLoginPassword(id,md5Password);
        }
        return id;
    }

    public User getAdmin(String companyId)
    {
        return userDao.getAdmin(companyId);
    }

    @Transactional(readOnly = false)
    public User save(User user,MultipartFile[] fileIdUploads,
                     String[] deleteIds) throws java.io.IOException {
        // Người dùngĐăng nhậptênduy nhấtKiểm tra
        User existUser = getUserByLoginName(user);
        String loginTenant = SessionUtils.getLoginTenantId();
        if(null != existUser)
        {
            List<String> oldCompanyId = existUser.getClinics();
            if(oldCompanyId.contains(loginTenant))
            {
                //cùngPhòng khám của giống hệtNgười dùng
                throw new ServiceException("Đã tồn tại một tên đăng nhập giống hệt. ");
            }
            else
            {
                //khác nhauPhòng khám của giống hệtNgười dùng

                //lưu vào người này của Phòng khámThông tin
                userDao.insertUserTenant(IdGen.uuid(),existUser.getId(),loginTenant,user.getLoginName());
                //lưu vào người này của mở rộngThông tin
                UserExt userExt = user.getUserExt();
                userExt.setCompanyId(SessionUtils.getLoginTenantId());
                userExt.setId(IdGen.uuid());
                //nàyngườiNếuTồn tại, không ởKhi thêm mớiSửaảnh
                userExtService.insert(userExt);
                //lưu vàoHiện tạiNgười dùngtạiPhòng khám của Vai tròThông tin
                saveUserRoleInfo(user);
            }
        }
        else
        {
            //không phảigiống hệtNgười dùng  lưu vàongười
            String id = super.save(user).getId();
            //lưu vào người này của Vai trò
            if (StringUtils.isNoneBlank(id)) {
                // ĐặtMã hóaTrường   Mật khẩu
                if(user.getLoginPasswordUpdate()) {
                    Md5Hash md5 = new Md5Hash(user.getLoginPassword(), user.getId(), 10000);
                    String md5Password = md5.toHex();
                    dao.updateLoginPassword(user.getId(), md5Password);
                }

                saveUserRoleInfo(user);
            }

            //lưu vào người này của Phòng khámThông tin
            userDao.insertUserTenant(IdGen.uuid(),id,loginTenant,user.getLoginName());
            //lưu vào người này của mở rộngDữ liệu
            UserExt userExt = user.getUserExt();
            userExt.setUserId(id);
            userExt.setCompanyId(loginTenant);
            //lưu vào người này của ảnh
            List<SysFile> sysFiles = sysFileService.changeAndSaveSysFileList(fileIdUploads, id);
            if(0 == sysFiles.size())
            {
                logger.debug("Người dùngChưaTải lênảnh");
            }else
            {
                userExt.setPhotoId(sysFiles.get(0).getId());
            }
            // Theo tệp đính kèmIDXóa thông tin tệp đính kèm
            if (null != deleteIds && deleteIds.length > 0) {
                sysFileService.delete(deleteIds);
            }
            userExtService.save(userExt);
        }
//        colMaps.clear();
//        colMaps.put("login_name", "loginName");
//        colMaps.put("company_id", "companyId");
//        if(exists(dao, user, colMaps)) {
//            throw new ServiceException("Đã tồn tại một tên đăng nhập giống hệt. ");
//        }

        return user;
    }

    private void saveUserRoleInfo(User user) {
        /* Xử lý bảng con     Vai trò người dùng */
        PageRequest pageRequest = buildPageRequest(com.google.common.collect.ImmutableMap.of(
                "user_id", user.getId(),
                "company_id", SessionUtils.getLoginTenantId()
        ));
        List<UserRole> list_UserRole = userRoleDao.listAll(pageRequest);
        List<UserRole> deleteUserRoles = Lists.newArrayList(); // Danh sách xóa
        List<UserRole> insertUserRoles = Lists.newArrayList(); // Danh sách thêm
        List<UserRole> updateUserRoles = Lists.newArrayList(); // Danh sách cập nhật
        for(UserRole userRoleSaved: list_UserRole) {
            boolean found = false;
            for (UserRole userRole : user.getUserRoleList()){
                if(userRoleSaved.getId().equals(userRole.getId())){
                    found = true;
                    break;
                }
            }
            if(!found) {
                deleteUserRoles.add(userRoleSaved);
            }
        }
        for (UserRole userRole : user.getUserRoleList()){

            if (StringUtils.isBlank(userRole.getId())) {
                userRole.setUser(user);
                userRole.preInsert();
                userRole.setCompany(SessionUtils.getLoginTenant());
                insertUserRoles.add(userRole);
            } else {
                userRole.preUpdate();
                updateUserRoles.add(userRole);
            }

        }
        if(deleteUserRoles.size() > 0) {
            userRoleDao.bulkDelete(deleteUserRoles);
        }
        if(updateUserRoles.size() > 0) {
            userRoleDao.bulkUpdate(updateUserRoles);
        }
        if(insertUserRoles.size() > 0) {
            userRoleDao.bulkInsert(insertUserRoles);
        }
    }

    private User getUserByLoginName(User user) {
        User existUser = userDao.getByLoginName(user.getLoginName());
        if(null != existUser)
        {
            List<String> clinics = userDao.getClinicIdByPhone(existUser.getId());
            existUser.setClinics(clinics);
        }
        return existUser;
    }

    @Transactional(readOnly = false)
    public User update(User user,MultipartFile[] fileIdUploads,
                       String[] deleteIds) throws java.io.IOException {
        String id = super.save(user).getId();
        if (StringUtils.isNoneBlank(id)) {
            // ĐặtMã hóaTrường   Mật khẩu
            if(user.getLoginPasswordUpdate()) {
                if(medicareConfigProperties.getIsDemo().equals("true")){
                    throw new ServiceException("demoHệ thốngVui lòngđừngSửaMật khẩu!");
                }
                Md5Hash md5 = new Md5Hash(user.getLoginPassword(), user.getId(), 10000);
                String md5Password = md5.toHex();
                dao.updateLoginPassword(user.getId(), md5Password);
            }
            // lạidùng saveUserRoleInfo Xử lý bảng conVai trò người dùng
            saveUserRoleInfo(user);

            UserExt userExt = user.getUserExt();
            //lưu vào người này của ảnh
            List<SysFile> sysFiles = sysFileService.changeAndSaveSysFileList(fileIdUploads, id);
            if(0 != sysFiles.size())
            {
                userExt.setPhotoId(sysFiles.get(0).getId());
            }

            if (null != deleteIds && deleteIds.length > 0) {
                sysFileService.delete(deleteIds);
            }
            userExt.setUserId(user.getId());
            userExt.setCompanyId(SessionUtils.getLoginTenantId());
            userExtService.save(userExt);

        }
        return user;
    }
    /**
     * Xóa
     * @param user
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(User user) {
        /* Xử lý bảng con     Vai trò người dùng, mở rộngThông tin, tenantThông tin */
        PageRequest pageRequest = buildPageRequest(com.google.common.collect.ImmutableMap.of(
                "user_id", user.getId(),
                "company_id", SessionUtils.getLoginTenantId()
        ));
        user.setUserRoleList(userRoleDao.listAll(pageRequest));        
        if(user.getUserRoleList() != null && user.getUserRoleList().size() > 0) {
        	userRoleDao.bulkDelete(user.getUserRoleList());
        }
        UserExt userExt = userExtDao.getUserExtByUserId(user.getId(), SessionUtils.getLoginTenantId());
        if(userExt!=null)
        {
            userExtDao.delete(userExt);
        }
        userDao.updateUserTenantDelFlag(user.getId(),"1");
        int rows = super.delete(user);
        return rows;
    }

    @Transactional(readOnly = false)
    public void delete(User user,int number) {
        /* Xử lý bảng con     Vai trò người dùng, mở rộngThông tin, tenantThông tin */
        PageRequest pageRequest = buildPageRequest(com.google.common.collect.ImmutableMap.of(
                "user_id", user.getId(),
                "company_id", SessionUtils.getLoginTenantId()
        ));
        user.setUserRoleList(userRoleDao.listAll(pageRequest));
        if(user.getUserRoleList() != null && user.getUserRoleList().size() > 0) {
            userRoleDao.bulkDelete(user.getUserRoleList());
        }
        UserExt userExt = userExtDao.getUserExtByUserId(user.getId(), SessionUtils.getLoginTenantId());
        if(userExt!=null)
        {
            userExtDao.delete(userExt);
        }
        userDao.updateUserTenantDelFlag(user.getId(),"1");
        if(1 == number)
        {
            super.delete(user);
        }
    }


    /**
     * Sửa Mật khẩu
     */
    @Transactional(readOnly = false)
    public int changeLoginPassword(String id, String pass) {
        // Md5Mật khẩu — sử dụng và  UserRealm Nhất quán của lặp lầnsố
        Md5Hash md5 = new Md5Hash(pass, id, 10000);
        String md5Password = md5.toHex();

        int rows = dao.updateLoginPassword(id, md5Password);
        return rows;
    }

    /**
     * trực tiếpCập nhậtMật khẩubămGiá trị(dùng đểMật khẩuchuyển đổitrường hợp, truyền vàoĐãbăm của Mật khẩu)
     * @param id Người dùngID
     * @param hashedPassword Đãbăm của Mật khẩu(Bên gọi chịu trách nhiệm bămXử lý)
     */
    @Transactional(readOnly = false)
    public void updatePassword(String id, String hashedPassword) {
        dao.updateLoginPassword(id, hashedPassword);
    }

    /**
     * Tạo nhật ký thao tác
     * @param actionTypeId  Loại thao tácId
     * @param entity        Đối tượng thực thể được thao tác
     * @return
     */
    @Override
    protected Action createAction(String actionTypeId, User entity) {
        Action action = super.createAction(actionTypeId, entity);
        if(action == null) {
            return null;
        }
        // Khi xóa, ghi lại và lưu dữ liệu đã lưu vào thùng rác
        if(ActionConstants.ACTION_DELETED.equals(actionTypeId)) {   
            for(UserRole child: entity.getUserRoleList()) {
                ActionRecycle recycle = new ActionRecycle();
                recycle.setTableName(child.getBusTableName());
                recycle.setObjectId(child.getId());
                recycle.setObjectName((String)Reflections.invokeGetter(child, "name"));
                action.getActionRecycleList().add(recycle);            
            }
        }
        return action;
    }

    public List<User> getListByLoginName(String loginName) {
        return userDao.getListByLoginName(loginName);
    }

    public List<String> getUserIdByCinicId(String clinicId) {
        return userDao.getUserIdByClinicId(clinicId);
    }

    @Transactional(readOnly = false)
    public void updateDelFlag(String id, String delFalg) {
        userDao.updateDelFlag(id, delFalg);
    }

    public Page<User> listPageForNoAdmin(List<Parameter> parameters, int offset, int limit, String orderby) {

        String loginTenant = SessionUtils.getLoginTenantId();
        PageRequestClinic pageRequest = new PageRequestClinic(offset, limit, parameters, orderby,loginTenant);

        return paginate(
            () -> this.dao.countForNoAdmin(pageRequest),
            () -> this.dao.listPageForNoAdmin(pageRequest)
        );
    }

    public List<Clinic> getClinicByLoginName(String loginName)
    {
        return null;
    }

    public int countClinicIdByPhone(String phone)
    {
        return userDao.countClinicIdByPhone(phone);
    }

    /**
     * TheoPhòng khám và Hộp thả xuống chọn người theo chức vụ
     * @param companyId
     * @param jobType
     * @return
     */
    public List<User> getUserByCompanyIdAndJob(String companyId, String jobType){
        return this.dao.getUserByCompanyIdAndJob(companyId,jobType);
    }

    @Transactional
    public void saveWxUser(User user){
        user.setCreateDate(new Date());
        user.setUpdateDate(new Date());
        this.dao.insert(user);
    }

    @Transactional(readOnly = true)
    public User getCompanyIdAndOpenId(String companyId, String openId) {
        User user = this.dao.getCompanyIdAndOpenId(companyId,openId);
        return user;
    }

    @Transactional
    public void updateWxUser(User user) {
        user.setUpdateDate(new Date());
        this.dao.updateWxUser(user);
    }

    public List<User> getUserByWxCompanyIdAndJob(String companyId, String office, String job) {
        List<User> users = this.dao.getUserByWxCompanyIdAndJob(companyId,office,job);
        return users;
    }
    public List<User> getUserByCompanyIdAndJobV2(String companyId, String jobType){
        return this.dao.getUserByCompanyIdAndJobV2(companyId,jobType);
    }
}