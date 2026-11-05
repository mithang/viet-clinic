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

import java.util.Arrays;
import java.util.Collections;
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
        // DS42: super.get() tra null khi dong khong ton tai (hop dong cua
        // CrudService.get); truoc day user.getId() ben duoi NPE 500 o
        // GET /admin/user/{id} voi id la. Tra null nhu get() goc.
        if (user == null) {
            return null;
        }

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


    // H3 (iteration 28): doi SDT quan tri phong kham (ClinicService) chi
    // doi sys_user.login_name; dong bo user_tenant.login_name cung luc
    // trong cung transaction, neu khong tenant giu ten cu (xem H3).
    @Transactional(readOnly = false)
    public void updateAdminPhoneAndPass(String loginName,String pass,String userId)
    {
         userDao.updateAdminPhoneAndPass(loginName,pass,userId);
         if (StringUtils.isNotBlank(loginName)) {
             userDao.updateUserTenantLoginName(userId, loginName);
         }
    }

    // H3 (iteration 28): dong bo ten dang nhap xuong user_tenant cho moi
    // tenant active cua user (ten la dinh danh chung cua sys_user, khong
    // rieng tenant hien tai). Goi sau khi sys_user da doi ten thanh cong,
    // trong cung transaction cua caller.
    private void syncUserTenantLoginName(String userId, String loginName) {
        if (StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(loginName)) {
            userDao.updateUserTenantLoginName(userId, loginName);
        }
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

    /**
     * G2 (iteration 27): duong JSON POST /admin/user/save cua
     * UserController goi save(User) don — truoc day la CrudService.save,
     * bo qua hoan toan guard tenant/ext/role cua save(User, files,
     * deleteIds) ma saveWithFile dung: tao moi trung loginName cung
     * tenant van 200 va chen them dong sys_user trung (login_name chi
     * co KEY, khong UNIQUE), user moi thi 0 user_tenant + 0 ext.
     * Override nay dua create ve dung duong da guard, giu update-by-id
     * o duong cu (CrudService.save, co verifyTenantOwnership) nhung
     * chan doi loginName sang ten cua user khac — neu khong, update
     * van tao trung login_name nhu create. updatePersonAndPass goi
     * save(dto) voi loginName khong doi nen qua duoc guard doi ten.
     */
    @Override
    @Transactional(readOnly = false)
    public User save(User user) {
        if (StringUtils.isBlank(user.getId())) {
            // Create: dung duong guard nhu saveWithFile, khong file.
            if (user.getUserExt() == null) {
                UserExt ext = new UserExt();
                ext.setPost("");
                ext.setOffice("");
                ext.setCreditNum("");
                user.setUserExt(ext);
            } else {
                if (user.getUserExt().getPost() == null) {
                    user.getUserExt().setPost("");
                }
                if (user.getUserExt().getOffice() == null) {
                    user.getUserExt().setOffice("");
                }
                if (user.getUserExt().getCreditNum() == null) {
                    user.getUserExt().setCreditNum("");
                }
            }
            if (user.getUserRoleList() == null) {
                user.setUserRoleList(Lists.newArrayList());
            }
            try {
                User created = save(user, new MultipartFile[0], new String[0]);
                // H3: neu user cu da tung doi SDT (tenant cu con ten cu),
                // gan them tenant moi khong tu sua cac dong cu — dong bo
                // het ve ten moi sau khi tao/gan xong.
                syncUserTenantLoginName(created.getId(), user.getLoginName());
                return created;
            } catch (java.io.IOException e) {
                throw new ServiceException("Luu nguoi dung that bai: " + e.getMessage());
            }
        }
        // Update-by-id: chi chan doi ten sang loginName cua user khac
        // (cung hoac khac tenant deu chan, vi login tra theo loginName).
        // Ten khong doi, hoac ten moi chua tung co, thi giu duong cu.
        if (StringUtils.isNotBlank(user.getLoginName())) {
            User existUser = getUserByLoginName(user);
            if (existUser != null && !existUser.getId().equals(user.getId())) {
                throw new ServiceException("Đã tồn tại một tên đăng nhập giống hệt. ");
            }
        }
        // H3: super.save chi doi sys_user.login_name, user_tenant giu ten
        // cu lam lech CheckNewUserByPhone/countClinicIdByPhone (dem theo
        // login_name) va delete fallback theo phone — dong bo sau save.
        User saved = super.save(user);
        syncUserTenantLoginName(saved.getId(), user.getLoginName());
        return saved;
    }

    /**
     * H1 (iteration 28): bulkInsert/bulkUpdate/bulkDelete cua User truoc
     * day la CrudService thang, bo qua toan bo guard cua duong don
     * (G1/G2/E1): bulkInsert khong verify tenant, khong chan trung
     * loginName, khong tao user_tenant/ext/role; bulkUpdate qua mat
     * verifyTenantOwnership khi payload thieu company va khong co guard
     * doi ten trung; bulkDelete bo qua cleanup role/ext/tenant cua
     * delete don. Khong co Vue nao goi 3 endpoint nay (chi export o
     * api/admin/user.js) — lo hong tang API, sua o service, khong
     * doi san quyen controller (user:create/update/delete giu nguyen).
     * Ca 3 override deu @Transactional de 1 dong loi rollback ca list.
     */
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<User> entitys) {
        List<String> ids = Lists.newArrayList();
        for (User entity : entitys) {
            // save(User) da guard G2: create trung cung-tenant -> 400,
            // cross-tenant -> gan tenant/ext cho id cu, moi -> du
            // tenant/ext/role. Dong co id san di theo nhanh update
            // cua save don (guard doi ten), khong insert tho nua.
            ids.add(this.save(entity).getId());
        }
        return ids;
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<User> entitys) {
        // Guard truoc khi goi bulk SQL: doi ten trung (nhu G2-update)
        // + tenant theo user_id DA LUU (userDao.getClinicId loc theo
        // user_id + del_flag, mien nhiem voi user_tenant.login_name cu
        // sau doi SDT — xem H3). Payload thieu company khong con qua
        // mat nhu verifyTenantOwnership cua CrudService nua.
        String loginTenant = SessionUtils.getLoginTenantId();
        boolean tenantCheck = StringUtils.isNotBlank(loginTenant) && !"null".equals(loginTenant);
        for (User entity : entitys) {
            if (StringUtils.isBlank(entity.getId())) {
                throw new ServiceException("Thiếu id người dùng để cập nhật hàng loạt. ");
            }
            if (tenantCheck) {
                List<String> clinics = userDao.getClinicId(entity.getId());
                if (clinics == null || !clinics.contains(loginTenant)) {
                    throw new ServiceException("Không có quyền truy cập tenant khácDữ liệu");
                }
            }
            if (StringUtils.isNotBlank(entity.getLoginName())) {
                User probe = new User();
                probe.setLoginName(entity.getLoginName());
                User existUser = getUserByLoginName(probe);
                if (existUser != null && !existUser.getId().equals(entity.getId())) {
                    throw new ServiceException("Đã tồn tại một tên đăng nhập giống hệt. ");
                }
            }
        }
        List<String> ids = super.bulkUpdate(entitys);
        // H3: bulk SQL chi doi sys_user — dong bo tenant sau khi doi ten.
        for (User entity : entitys) {
            syncUserTenantLoginName(entity.getId(), entity.getLoginName());
        }
        return ids;
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<User> entitys) {
        // Theo dung duong delete don cua controller: dem clinic theo
        // phone cua ban DA LUU, cleanup role/ext/tenant cua tenant
        // hien tai, chi super.delete (xoa mem sys_user) khi all == 1.
        // Tenant check nhu bulkUpdate: user khong thuoc tenant hien tai
        // thi nem, tranh delete(user, int) don tenant khac (H2 da loc
        // updateUserTenantDelFlag theo tenant cua session).
        // Tra ve so dong da cleanup (khong phai so dong sys_user khi
        // user con o tenant khac, vi truong hop do sys_user duoc giu).
        String loginTenant = SessionUtils.getLoginTenantId();
        boolean tenantCheck = StringUtils.isNotBlank(loginTenant) && !"null".equals(loginTenant);
        int processed = 0;
        for (User entity : entitys) {
            if (StringUtils.isBlank(entity.getId())) {
                continue;
            }
            User stored = dao.get(entity.getId());
            if (stored == null) {
                continue;
            }
            if (tenantCheck) {
                List<String> clinics = userDao.getClinicId(stored.getId());
                if (clinics == null || !clinics.contains(loginTenant)) {
                    throw new ServiceException("Không có quyền truy cập tenant khácDữ liệu");
                }
            }
            // H2: dem theo user_id da luu thay vi phone — mien nhiem voi
            // user_tenant.login_name cu sau doi SDT.
            int all = this.countActiveTenantsByUserId(stored.getId());
            this.delete(stored, all);
            processed++;
        }
        return processed;
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
                // F1 (iteration 26, lỗi thứ ba trong cùng nhánh): payload
                // tạo mới không có id nên saveUserRoleInfo build
                // ImmutableMap user_id=null → NPE và rollback cả tenant+ext
                // vừa chèn. Gán id đã tồn tại cho payload để role sync
                // đúng user này và controller trả id thật thay vì null.
                user.setId(existUser.getId());

                //lưu vào người này của Phòng khámThông tin
                userDao.insertUserTenant(IdGen.uuid(),existUser.getId(),loginTenant,user.getLoginName());
                //lưu vào người này của mở rộngThông tin
                UserExt userExt = user.getUserExt();
                // F1 (iteration 26): nhánh khác-phòng-khám thiếu userId nên
                // insert ext 500 'user_id cannot be null' — gán đúng id của
                // user đã tồn tại, như nhánh tạo mới ở dưới. Đồng thời
                // preInsert trước insert thô (nhánh tạo mới dùng save() nên
                // có audit, còn insert() thô thì create_by null → 500 tiếp):
                // preInsert giữ id đã set, chỉ điền create_by/update_by/ngày.
                userExt.setUserId(existUser.getId());
                userExt.setCompanyId(SessionUtils.getLoginTenantId());
                userExt.setId(IdGen.uuid());
                userExt.preInsert();
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
            // K7 (iteration 58): guard deleteIds truoc moi upload/xoa
            // (mau J2): file avatar co object_id = id user vua tao;
            // file la (object_id khac) -> 400, transaction rollback
            // ca user vua insert. Nhanh user da ton tai o tenant khac
            // khong xoa file nen khong can guard.
            if (null != deleteIds && deleteIds.length > 0) {
                sysFileService.verifyDeleteIdsObjectIds(Arrays.asList(deleteIds),
                        Collections.singleton(id));
            }
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
            // G1 (iteration 27): getClinicIdByPhone loc theo login_name
            // nhung caller cu truyen existUser.getId() (id so dai, khong
            // bao gio trung SDT) nen clinics luon rong va nhanh
            // cung-phong-kham trong save khong bao gio nem, roi vao
            // nhanh khac-phong-kham chen trung tenant + ext. Dung
            // getClinicId theo user_id (nhu LoginService) — mien nhiem
            // voi user_tenant.login_name cu sau doi SDT. Khong xoa
            // getClinicIdByPhone o DAO trong commit nay.
            List<String> clinics = userDao.getClinicId(existUser.getId());
            existUser.setClinics(clinics);
        }
        return existUser;
    }

    @Transactional(readOnly = false)
    public User update(User user,MultipartFile[] fileIdUploads,
                       String[] deleteIds) throws java.io.IOException {
        // K7 (iteration 58): guard deleteIds truoc moi ghi (mau J2):
        // file avatar co object_id = id user dang sua; file la -> 400
        // truoc super.save. Id rong (khong phai duong UI) thi tap cho
        // phep rong -> file ton tai nao cung bi chan, file khong ton
        // tai giu hanh vi cu nhu I1/R3.
        if (null != deleteIds && deleteIds.length > 0) {
            sysFileService.verifyDeleteIdsObjectIds(Arrays.asList(deleteIds),
                    StringUtils.isNotBlank(user.getId()) ? Collections.singleton(user.getId()) : Collections.<String>emptySet());
        }
        String id = super.save(user).getId();
        // H3: duong sua cua UI (POST /admin/user/update) cung chi doi
        // sys_user — dong bo tenant ngay sau save, truoc khi xu ly role/ext.
        syncUserTenantLoginName(id, user.getLoginName());
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
            // E1 (iteration 25): payload thieu userExt.id thi tai dung dong
            // ext active hien co cua (user, tenant) thay vi insert dong moi
            // — neu khong, moi lan update them 1 dong active va
            // getUserExtByUserId (selectOne) nem TooManyResults lam
            // getById/delete user 500. Khong doi hanh vi khi id da du.
            if (StringUtils.isBlank(userExt.getId())) {
                List<UserExt> existingExts = userExtDao.listActiveUserExtByUserId(user.getId(), SessionUtils.getLoginTenantId());
                if (existingExts != null && !existingExts.isEmpty()) {
                    userExt.setId(existingExts.get(0).getId());
                }
            }
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
        // Iteration 29: chan som id rong — neu khong, ImmutableMap.of
        // ben duoi nem NPE, va listAll khong co loc user_id co the
        // tra ve role cua ca tenant roi bi bulkDelete theo.
        if (user == null || StringUtils.isBlank(user.getId())) {
            throw new ServiceException("Không xác định được người dùng cần xóa. ");
        }
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
        // H2: chi danh co tenant cua session hien tai, khong danh co
        // tenant khac cua cung user (truoc day khong loc tenant).
        userDao.updateUserTenantDelFlag(user.getId(), "1", SessionUtils.getLoginTenantId());
        int rows = super.delete(user);
        return rows;
    }

    @Transactional(readOnly = false)
    public void delete(User user,int number) {
        // Iteration 29: nhu delete don — chan som id rong thay vi NPE.
        if (user == null || StringUtils.isBlank(user.getId())) {
            throw new ServiceException("Không xác định được người dùng cần xóa. ");
        }
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
        // H2: nhu delete don — chi danh co tenant cua session hien tai.
        userDao.updateUserTenantDelFlag(user.getId(), "1", SessionUtils.getLoginTenantId());
        if(1 == number)
        {
            super.delete(user);
        }
    }


    /**
     * Sửa Mật khẩu
     *
     * ITER62 P1 (N4-P1): stored-row tenant check truoc dao.updateLoginPassword.
     * Truoc day controller chi check tu-doi hoac quyen user:update, khong check
     * tenant cua id dich, nen PUT /admin/user/{foreignId}/loginPassword voi
     * quyen user:update o tenant minh doi duoc login_password cua user tenant
     * khac (probe live N4). Guard nay: tu-doi (id == session user) giu nguyen
     * khong check; nguoc lai, neu session co tenant thi user dich phai thuoc
     * tenant do — theo user_tenant active (userDao.getClinicId, tien le
     * bulkUpdate H1 iter28), neu khong co dong user_tenant thi fallback ve
     * sys_user.company_id da luu (dao.get); id khong ton tai / dong mo coi
     * khong company giu hanh vi cu (update tra rows 0 -> 200, nhu tien le
     * orphan cua N3c). ServiceException -> 400 qua GlobalExceptionHandler.
     * Khong dung verifyTenantOwnershipAgainstStoredRow o day vi no chi doc
     * company cua payload, khong biet membership user_tenant cua user.
     */
    @Transactional(readOnly = false)
    public int changeLoginPassword(String id, String pass) {
        User sessionUser = SessionUtils.getUser();
        boolean self = sessionUser != null && id != null && id.equals(sessionUser.getId());
        if (!self) {
            String loginTenant = SessionUtils.getLoginTenantId();
            if (StringUtils.isNotBlank(loginTenant) && !"null".equals(loginTenant)) {
                List<String> clinics = userDao.getClinicId(id);
                if (clinics != null && !clinics.isEmpty()) {
                    if (!clinics.contains(loginTenant)) {
                        throw new ServiceException("Không có quyền truy cập tenant khácDữ liệu");
                    }
                } else {
                    User stored = userDao.get(id);
                    if (stored != null && stored.getCompany() != null) {
                        String storedCompanyId = stored.getCompany().getId();
                        if (StringUtils.isNotBlank(storedCompanyId) && !loginTenant.equals(storedCompanyId)) {
                            throw new ServiceException("Không có quyền truy cập tenant khácDữ liệu");
                        }
                    }
                }
            }
        }
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

    // H2: so tenant con active cua user, dem theo user_id (xem DAO).
    public int countActiveTenantsByUserId(String userId)
    {
        return userDao.countActiveTenantsByUserId(userId);
    }

    // Iteration 29: giai quyet id cho delete khong id — chi trong
    // tenant cua session (xem DAO); tra null khi khong thay.
    public String findActiveUserIdByPhoneInTenant(String phone, String tenantId)
    {
        return userDao.findActiveUserIdByPhoneInTenant(phone, tenantId);
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