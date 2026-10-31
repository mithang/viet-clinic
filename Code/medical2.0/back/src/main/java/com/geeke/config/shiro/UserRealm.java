package com.geeke.config.shiro;

import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import com.geeke.admin.entity.User;
import com.geeke.admin.service.UserService;
import com.geeke.common.data.Parameter;
import com.geeke.common.data.SearchParamsBuilder;
import com.geeke.utils.SessionUtils;
import com.google.common.collect.Lists;

/**
 * @author: lys
 * @description: Tùy chỉnhRealm
 * @date: 2019/10/24 10:06
 */
public class UserRealm extends AuthorizingRealm {
    private static final Logger logger = LoggerFactory.getLogger(UserRealm.class);

    @Autowired
    @Lazy
    private UserService userService;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        //Truy vấnNgười dùng của Quyền
    	List<String> permissions = SessionUtils.getUserPermission();
        logger.debug("permission của Giá trị là : {}", permissions);
        // là Hiện tạiNgười dùngĐặtVai trò và Quyền
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        authorizationInfo.addStringPermissions(permissions);        
        
        return authorizationInfo;
    }

    /**
     * Mật khẩubămlặp lầnsố — đề xuấtnângđến10000 lầnbằngtăngmạnhan toàntính(gốc là 6 lần)
     *  và  ShiroConfiguration Trong HashedCredentialsMatcher Giữ nhất quán
     */
    private static final int PASSWORD_HASH_ITERATIONS = 10000;

    /**
     * cũbảnMật khẩubămlặp lầnsố — dùng đểdầntiến hànhkiểuMật khẩuchuyển đổi
     * Đăng nhập giờtrướcdùngmớilặp lầnsốXác thực, Thất bạisautrả lạitrảĐếnlần lặp cũ lầnsốXác thực; 
     * lần lặp cũ lầnsốXác thựcThành côngsau, Băm lại mật khẩu với số lần lặp mới và cập nhật cơ sở dữ liệu. 
     */
    private static final int LEGACY_PASSWORD_HASH_ITERATIONS = 6;

    /**
     * Xác thựcHiện tạiĐăng nhập của Subject
     * LoginController.login()Phương thức trong hành nghề dòngSubject.login() giờ hành nghề dòngnàyPhương thức
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {
        String loginName = (String) authcToken.getPrincipal();
        // LấyNgười dùngMật khẩu
        String password = new String((char[]) authcToken.getCredentials());

        User user = this.getUserByLoginName(loginName);
        if (user == null) {
            //khôngtìmĐếntài khoảnsố
            throw new UnknownAccountException();
        }

        // Xác thựcMật khẩu — trướcdùngmớilặp lầnsốXác thực, Thất bạisautrả lạitrảĐếnlần lặp cũ lầnsố
        boolean passwordMatched = false;
        Md5Hash md5New = new Md5Hash(password, user.getId(), PASSWORD_HASH_ITERATIONS);
        if (md5New.toHex().equals(user.getLoginPassword())) {
            passwordMatched = true;
        } else {
            // trả lạitrảĐếnlần lặp cũ lầnsốXác thực(Giai đoạn chuyển đổi tương thích)
            Md5Hash md5Legacy = new Md5Hash(password, user.getId(), LEGACY_PASSWORD_HASH_ITERATIONS);
            if (md5Legacy.toHex().equals(user.getLoginPassword())) {
                passwordMatched = true;
                // Băm lại với số lần lặp mới và cập nhật cơ sở dữ liệu
                try {
                    user.setLoginPassword(md5New.toHex());
                    userService.updatePassword(user.getId(), md5New.toHex());
                    logger.info("Người dùng {} Mật khẩu đã tự động chuyển sang số lần lặp băm mới", loginName);
                } catch (Exception e) {
                    logger.warn("Người dùng {} Mật khẩutự độngchuyển đổiThất bại: {}", loginName, e.getMessage());
                }
            }
        }

        if (!passwordMatched) {
            throw new UnknownAccountException();
        }

        // Người dùngTài khoảnbịVô hiệu hóa
        if(user.getIsLocked() != 0) {
        	throw new DisabledAccountException();
        }

        //giaochoAuthenticatingRealmSử dụngCredentialsMatchertiến hành dòngMật khẩukhớp, Nếu thấy cách của người khác không tốt thì có thể tự triển khai
        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
                user.getLoginName(),
                password,
                getName()
        );

        //session trong khôngCầnLưuMật khẩu
        user.setLoginPassword("");

        // ĐặtvượtcấpNgười dùng, Hệ thốngQuản lýnhân viênNgười dùngThông tin
        if("1000".equals(user.getId()) || "1001".equals(user.getId())) {
        	user.getCompany().setIds("0.");    // Đặtcông ty của toàn bộmã số

        }

        //Sẽ Người dùngThông tinđặtvàosession trong 
        SessionUtils.setUser(user);
        return authenticationInfo;
    }
    
    private User getUserByLoginName(String loginName) {
    	List<Parameter> parms = SearchParamsBuilder.create()
    	        .eq("login_name", loginName)
    	        .build();
        List<User> list = userService.listAll(parms, null);
        if (list == null || list.size() < 1) {
            logger.info("{} Người dùngTài khoảnKhông tồn tại", loginName);
            return null;
        }
        return list.get(0);
    }
}
