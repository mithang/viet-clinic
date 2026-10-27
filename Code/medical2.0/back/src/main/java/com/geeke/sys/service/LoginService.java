package com.geeke.sys.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.admin.dao.UserDao;
import com.geeke.admin.entity.User;
import com.geeke.common.constants.ActionConstants;
import com.geeke.common.service.CrudService;
import com.geeke.utils.SessionUtils;
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
        	logger.info("Người dùng {} Đăng nhậpTài khoản hoặc mật khẩu sai", username);
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
     * @return
     */
    public List<User> loginUser() {
        return userDao.loginUser();
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
            logger.error("ThoátĐăng nhậpThất bại", e);
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
