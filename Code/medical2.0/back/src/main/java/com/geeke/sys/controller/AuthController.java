package com.geeke.sys.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.admin.common.entity.DataPermission;
import com.geeke.admin.common.service.PermissionService;
import com.geeke.admin.entity.Router;
import com.geeke.admin.entity.User;
import com.geeke.admin.service.UserService;
import com.geeke.common.data.Parameter;
import com.geeke.common.data.SearchParamsBuilder;


import com.geeke.config.shiro.StatelessSessionManager;
import com.geeke.org.entity.Company;
import com.geeke.org.service.CompanyService;
import com.geeke.sys.entity.PersonalTheme;
import com.geeke.sys.entity.Theme;
import com.geeke.sys.service.LoginService;
import com.geeke.sys.service.PersonalThemeService;
import com.geeke.sys.service.ThemeService;
import com.geeke.utils.JwtUtils;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.geeke.utils.constants.ErrorEnum;
import com.google.common.collect.Lists;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private LoginService loginService;

    @Autowired
    private UserService userService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private PersonalThemeService personalThemeService;

    /**
     * đăng nhậptrướcKiểm tranơinơiPhòng khám
     *
     * @param loginName
     * @return
     */
    @PostMapping("/getUserTenant")
    public ResponseEntity<?> getUserTenant(@RequestBody Map<String, String> body) {
        String loginName = body.get("loginName");
        String password = body.get("password");

        Subject currentUser = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(loginName, password);
        try {
            currentUser.login(token);
        } catch (UnknownAccountException e) {
            logger.info("Người dùng {} Đăng nhậpTài khoản hoặc mật khẩu sai", loginName);
            return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_10010));
        } catch (DisabledAccountException e) {
            logger.info("Người dùng {} Tài khoản bị vô hiệu hóa", loginName);
            return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_10011));
        } catch (AuthenticationException e) {
            logger.info("Người dùng {} Lỗi xác thực", loginName);
            return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_10012));
        }

        List<Company> clinics = companyService.getClinicsByLoginName(loginName);
        return ResponseEntity.ok(clinics);
    }

    @PostMapping("/token")
    public ResponseEntity<?> getToken(HttpServletRequest request, String loginName, String password, String companyId) {
        Map<String, Object> response = null;
        List<String> idList = new ArrayList<>();
        try {
            //Đăng nhập
            HttpStatus httpStatus = loginService.authLogin(loginName, password, companyId);

            if (HttpStatus.UNAUTHORIZED.equals(httpStatus)) {        //Tài khoản hoặc mật khẩu sai
                return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_10010));
            } else if (HttpStatus.FORBIDDEN.equals(httpStatus)) {            //Tài khoản bị vô hiệu hóa
                return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_10011));
            } else if (HttpStatus.SERVICE_UNAVAILABLE.equals(httpStatus)) {  //Lỗi xác thực
                return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_10012));
            }
            User user = SessionUtils.getUser();
            //xác nhậnCó hay không có Phòng khámtạilàm切đổi
            if (!StringUtils.isNullOrEmpty(companyId)) {
                //nhiềuPhòng khámĐăng nhập
                user = changeClinics(user, companyId);
                Company company = companyService.get(companyId);
                SessionUtils.setLoginTenantId(companyId);
                SessionUtils.setLoginTenant(company);
            } else {
                //đơnPhòng khámĐăng nhập
                companyId = user.getCompanyId();
                SessionUtils.setLoginTenantId(companyId);
                SessionUtils.setLoginTenant(user.getCompany());
            }
            //Cập nhậtNgười dùng, Trả vềHiện tạiđăng nhậpngườiThông tin
            response = getLoginUserInfo(request, user, companyId);
        } catch (Exception e) {
            logger.error("Đăng nhậpThất bại", e);
            return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_10012));
        } finally {
            //ĐưaXóa của Sửatrả lạiđến
            if (!idList.isEmpty()) {
                for (String updateId : idList) {
                    userService.updateDelFlag(updateId, "0");
                }
            }
        }
        return ResponseEntity.ok(ResultUtil.successJson(response));
    }

    @PostMapping("/loginedtocken")
    public ResponseEntity<?> getLoginedToken(HttpServletRequest request) {
        // Có hay khôngĐăng nhậpqua
        HttpStatus httpStatus = loginService.authLogin();

        if (!HttpStatus.OK.equals(httpStatus)) {
            return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_20011));
        }
        User user = SessionUtils.getUser();
        String loginTenantId = SessionUtils.getLoginTenantId();
        Map<String, Object> response = getLoginUserInfo(request, user, loginTenantId);
        return ResponseEntity.ok(ResultUtil.successJson(response));
    }


    /**
     * đăngra
     *
     * @return
     */
    @PostMapping("/logout")
    public ResponseEntity<JSONObject> logout() {
        loginService.logout();
        return ResponseEntity.ok(ResultUtil.successJson("ThoátThành công"));
    }

    /**
     * Lấytất cảNgười dùngĐăng nhậpTrạng thái
     *
     * @return
     */
    @GetMapping("/userLoginStatus")
    public List<User> getUserLoginStatus() {
        //TODO: Truy vấnCầnKiểm traĐăng nhậpTrạng thái của Người dùngDanh sách
        List<User> userList = loginService.loginUser();

        //Xử lýtất cảSession, tương ứngĐếnmỗiCáiNgười dùng
        DefaultWebSecurityManager securittyManager = (DefaultWebSecurityManager) SecurityUtils.getSecurityManager();
        StatelessSessionManager sessionManager = (StatelessSessionManager) securittyManager.getSessionManager();
        Collection<Session> sessionList = sessionManager.getActiveSessions();//tất cảHiện tạiĐăng nhậpSession
        Map<String, Session> userSessionCache = new HashMap<>();
        for (Session s : sessionList) {
            //java.util.Date startTime = s.getStartTimestamp();
            //java.util.Date lastAccessTime = s.getLastAccessTime();
            JSONObject userJson = SessionUtils.getUserJson(s);
            if (userJson == null) {
                continue;
            }
            User user = userJson.toJavaObject(User.class);
            if (userSessionCache.containsKey(user.getId())) {
                Session cachedSession = userSessionCache.get(user.getId());
                //cùngmộtNgười dùngcó thểTồn tạihaiCáiSession của tình hình, lấynhấtsautruy cập của Session
                if (s.getLastAccessTime().compareTo(cachedSession.getLastAccessTime()) > 0) {
                    userSessionCache.replace(user.getId(), s);
                }
            } else {
                userSessionCache.put(user.getId(), s);
            }
        }

        //duyệtNgười dùng, TheotrênmặtXử lýsau của SessionCập nhậtcủa nóĐăng nhậpTrạng thái và Đăng nhập giờdài
        java.util.Date now = new java.util.Date();
        for (User u : userList) {
            boolean isLogined = userSessionCache.containsKey(u.getId());//Tồn tạiSessionthìxem là ĐãĐăng nhập
            Long loginedSeconds = 0L;//ĐãĐăng nhậpgiâysố
            if (isLogined) {
                Session session = userSessionCache.get(u.getId());
                java.util.Date startTime = session.getStartTimestamp();//sessionThời gian bắt đầu, tức làĐăng nhậpThời gian
                loginedSeconds = (now.getTime() - startTime.getTime()) / 1000;
            }
            //TODO:Sẽ isLogined, loginedSecondsgánGiá trịĐếnNgười dùngĐăng nhậpThông tin, Trả vềchotrangmặt
            String days = secondToTime(loginedSeconds);
            if (isLogined){
                u.setStatus("ĐãĐăng nhập");
                u.setLoginDuration(days);
            }else {
                u.setStatus("ChưaĐăng nhập");
                u.setLoginDuration("--");
            }
        }
        //TODO: Trả vềGiá trị另ngoàidùngĐối tượngđóng gói
        return userList;
    }
    /**
     * Trả vềNgày giờ phútgiây
     * @param second
     * @return
     */
    private String secondToTime(long second) {

        long days = second / 86400;//chuyển đổi ngàysố
        second = second % 86400;//Số giây còn lại

        long hours = second / 3600;//chuyển đổi thành số giờ
        second = second % 3600;//Số giây còn lại

        long minutes = second / 60;//chuyển đổi phútphút
        second = second % 60;//Số giây còn lại

        if (0 < days){
            return days + " ngày"+hours+" giờ"+minutes+" phút";
        }else {
            // return hours+" giờ"+minutes+" phút, "+second+"giây";
            return hours+" giờ"+minutes+" phút";
        }

    }

    private Map<String, Object> getLoginUserInfo(HttpServletRequest request, User user, String companyId) {
        // Người dùngtruy cậptài nguyênQuyền
        List<String> permissionList = permissionService.listResourcePermissionByUser(user.getId(), companyId);
        SessionUtils.setUserPermission(permissionList);

        // Người dùngtuyến đườngQuyền
        List<Router> listRouter = permissionService.listRouterPermission(user.getId(), companyId);

        // Người dùngDữ liệuQuyền
        List<DataPermission> listDataPermission = permissionService.listDataPermissionByUserId(user.getId(), companyId);

        // Lấychủ đề
        PersonalTheme personalTheme = getPersonalTheme(user.getId());

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("userId", user.getId().toString());
        response.put("username", user.getName());
        response.put("loginname", user.getLoginName());
        response.put("company", user.getCompany());
        response.put("department", user.getDepartment());
        response.put("User", user);
        response.put("routers", listRouter);
        response.put("dataPermisions", listDataPermission);
        response.put("personalTheme", personalTheme);
        JSONObject json = new JSONObject();
        json.put("host", request.getRemoteHost());
        json.put("userId", user.getId());
        json.put("sessionId", SecurityUtils.getSubject().getSession().getId().toString());
        String token = jwtUtils.createJWT(json.toJSONString());
        response.put("token", token);
        logger.info("User's token = {}", token);
        return response;
    }

    private Map<String, Object> LoginUserInfo(HttpServletRequest request, User user, String companyId) {
        // Người dùngtruy cậptài nguyênQuyền
        List<String> permissionList = permissionService.listResourcePermissionByUser(user.getId(), companyId);
        SessionUtils.setUserPermission(permissionList);

        Map<String, Object> response = new HashMap<String, Object>();

        JSONObject json = new JSONObject();
        json.put("host", request.getRemoteHost());
        json.put("userId", user.getId());
        json.put("sessionId", SecurityUtils.getSubject().getSession().getId().toString());
        String token = jwtUtils.createJWT(json.toJSONString());
        response.put("token", token);
        logger.info("User's token = {}", token);
        return response;
    }



    /**
     * 切đổiPhòng khám
     *
     * @param user
     * @param wantCompanyId
     * @return
     */
    private User changeClinics(User user, String wantCompanyId) {
        String nowCompanyId = user.getCompanyId();
        if (!wantCompanyId.equals(nowCompanyId)) {
            Company company = companyService.get(wantCompanyId);
            user.setCompany(company);
        }
        return user;
    }


    private PersonalTheme getPersonalTheme(String userId) {
        // LấyCáitínhhóachủ đề
        List<Parameter> parms = SearchParamsBuilder.create()
                .eq("user_id", userId)
                .build();
        List<PersonalTheme> list = personalThemeService.listAll(parms, null);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }

        // Hệ thốngMặc địnhchủ đề
        PersonalTheme defaultTheme = new PersonalTheme();
        defaultTheme.setUserId(Long.parseUnsignedLong(userId));
        List<Theme> themes = themeService.listAll(Lists.newArrayList(), null);
        if (themes != null && themes.size() > 0) {
            for (Theme t : themes) {
                if ("1".equals(t.getIsDefault())) {
                    defaultTheme.setTheme(t.getTheme());
                    return defaultTheme;
                }
            }
            // ChưaĐặtMặc địnhchủ đề giờ, Dòng mộtCáilàm là Mặc địnhchủ đề
            defaultTheme.setTheme(themes.get(0).getTheme());
        }
        return defaultTheme;
    }

    @PostMapping("/wxToken")
    public ResponseEntity<?> getWxToken(HttpServletRequest request, @RequestBody User user1) {
        Map<String, Object> response = null;
        String loginName = user1.getLoginName();
        String password = user1.getLoginPassword();
        String companyId = null;
        List<String> idList = new ArrayList<>();
        try {
            //Đăng nhập
            HttpStatus httpStatus = loginService.authLogin(loginName, password, companyId);

            if (HttpStatus.UNAUTHORIZED.equals(httpStatus)) {        //Tài khoản hoặc mật khẩu sai
                return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_10010));
            } else if (HttpStatus.FORBIDDEN.equals(httpStatus)) {            //Tài khoản bị vô hiệu hóa
                return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_10011));
            } else if (HttpStatus.SERVICE_UNAVAILABLE.equals(httpStatus)) {  //Lỗi xác thực
                return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_10012));
            }
            User user = SessionUtils.getUser();
            //xác nhậnCó hay không có Phòng khámtạilàm切đổi
            if (!StringUtils.isNullOrEmpty(companyId)) {
                //nhiềuPhòng khámĐăng nhập
                user = changeClinics(user, companyId);
                Company company = companyService.get(companyId);
                SessionUtils.setLoginTenantId(companyId);
                SessionUtils.setLoginTenant(company);
            } else {
                //đơnPhòng khámĐăng nhập
                companyId = user.getCompanyId();
                SessionUtils.setLoginTenantId(companyId);
                SessionUtils.setLoginTenant(user.getCompany());
            }

            //Cập nhậtNgười dùng, Trả vềHiện tạiđăng nhậpngườiThông tin
            response = getLoginUserInfo(request, user, companyId);
        } catch (Exception e) {
            logger.error("Đăng nhậpThất bại", e);
            return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_10012));
        } finally {
            //ĐưaXóa của Sửatrả lạiđến
            if (!idList.isEmpty()) {
                for (String updateId : idList) {
                    userService.updateDelFlag(updateId, "0");
                }
            }
        }
        return ResponseEntity.ok(ResultUtil.successJson(response));
    }
}
