package com.geeke.sys.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.admin.dao.UserDao;
import com.geeke.admin.entity.User;
import com.geeke.common.constants.ActionConstants;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * @author: lys
 * @description: Đăng nhậpserviceTriển khaiLớp
 * @date: 2018/10/24 11:53
 */
@Service
public class LoginService extends CrudService<UserDao, User> {
    @Autowired
    private UserDao userDao;

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    /*
     * Xác thựcNgười dùngtên và Mật khẩu
     */
    @Transactional(readOnly = false)
    public HttpStatus authLogin(String username, String password, String tenantId) {
        Subject currentUser = SecurityUtils.getSubject();
        if(currentUser.isAuthenticated())
        	return HttpStatus.OK;
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        try {
            currentUser.login(token);
        } catch (UnknownAccountException e) {
        	logger.info("Người dùng {} Đăng nhập Tài khoản hoặc mật khẩu sai", username);
        	return HttpStatus.UNAUTHORIZED;			// Tài khoản hoặc mật khẩu sai
        }catch(DisabledAccountException e) {
        	logger.info("Người dùng {} Tài khoản bị vô hiệu hóa", username);
        	return HttpStatus.FORBIDDEN;			// Tài khoản bị vô hiệu hóa
        }catch (AuthenticationException e) {
        	logger.info("Người dùng {} Lỗi xác thực", username);
        	return HttpStatus.SERVICE_UNAVAILABLE;			// Lỗi xác thực
        }
        //LưuHiện tạiNgười dùng của tất cảtenantThông tin
        User user = getUserFullClinics();
        // DS208: tenantId do client gui -- chi cho dang nhap khi user co
        // vai tro trong tenant do (sys_user_role.company_id = tenantId).
        // Truoc day bat ky ai co tai khoan hop le cung dang nhap duoc vao
        // BAT KY companyId nao (ke ca id khong ton tai) va nhan session
        // mang danh tenant do (token hop le, loginedtocken tra code 100);
        // user_tenant KHONG phai authority (testfull khong co dong nao).
        // Tenant rong/null (dang nhap don phong kham, wxToken) giu hanh
        // vi cu. Tu choi truoc khi setLoginTenantId de session khong
        // mang tenant gia; logout subject de khong de lai subject da
        // xac thuc; ServiceException -> 400 fail-closed (getToken catch
        // -> E_10012, khong cap token).
        if (StringUtils.isNotBlank(tenantId)
                && userDao.countUserRoleInCompany(user.getId(), tenantId) == 0) {
            logger.info("Người dùng {} Không có quyền truy cập phòng khám {}", username, tenantId);
            currentUser.logout();
            throw new ServiceException("Không có quyền truy cập phòng khám này");
        }
        //Hiện tạiĐăng nhập của tenantThông tinLưuĐếnsession trong 
        SessionUtils.setLoginTenantId(tenantId);
        // LưuĐăng nhậpNgàynhật ký
        this.saveAction(this.createAction(ActionConstants.ACTION_LOGIN, user));
        return HttpStatus.OK;
    }

    private User getUserFullClinics() {
        User user = SessionUtils.getUser();
        List<String> clinics = userDao.getClinicId(user.getId());
        user.setClinics(clinics);
        SessionUtils.setUser(user);
        return user;
    }

    /**
     * Lấy người dùng cần giám sát trạng thái đăng nhập
     * DS207: scope theo tenant của session (SessionUtils.getLoginTenantId).
     * Trước đây UserDao.loginUser quét toàn bộ user của một cơ sở
     * hard-code (parent_id='1639720158872479714'), session tenant A đọc
     * được phone/email của user tenant B. Tenant null -> `= NULL` không
     * khớp dòng nào (fail-closed, trả []).
     * @return
     */
    public List<User> loginUser() {
        return userDao.loginUser(SessionUtils.getLoginTenantId());
    }

    /**
     * Mặc địnhĐăng nhập
     * @return
     */
    public HttpStatus authLogin() {
        Subject currentUser = SecurityUtils.getSubject();
        if(currentUser.isAuthenticated()){
            return HttpStatus.OK;
        }
        return HttpStatus.NON_AUTHORITATIVE_INFORMATION;
    }

    /**
     * ThoátĐăng nhập
     *
     * @return
     */
    @Transactional(readOnly = false)
    public void logout() {
        // LưuĐăng nhậpNgàynhật ký
        User user = SessionUtils.getUser();
        this.saveAction(this.createAction(ActionConstants.ACTION_LOGOUT, user));

        try {
            Subject currentUser = SecurityUtils.getSubject();
            if(currentUser.isAuthenticated())
            	currentUser.logout();
        } catch (Exception e) {
            logger.error("Thoát Đăng nhập Thất bại", e);
        }
    }

    @Transactional(readOnly = false)
    public HttpStatus wxLogin(String loginName, String loginPassword) {
        Subject currentUser = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(loginName, loginPassword);
        try {
            currentUser.login(token);
        } catch (UnknownAccountException e) {
            return HttpStatus.UNAUTHORIZED;			// Tài khoản hoặc mật khẩu sai
        }catch(DisabledAccountException e) {
            return HttpStatus.FORBIDDEN;			// Tài khoản bị vô hiệu hóa
        }catch (AuthenticationException e) {
            return HttpStatus.SERVICE_UNAVAILABLE;			// Lỗi xác thực
        }
        return HttpStatus.OK;
    }
}
