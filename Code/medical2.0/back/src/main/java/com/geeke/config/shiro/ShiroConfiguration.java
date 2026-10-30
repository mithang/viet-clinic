package com.geeke.config.shiro;

import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.MemorySessionDAO;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author: lys
 * @description: shirocấu hìnhLớp
 * @date: 2017/10/24 10:10
 */
@Configuration
public class ShiroConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(ShiroConfiguration.class);

	 /**
	 * Shiro của WebfilterFactory đặt têntên:shiroFilter
	 */
    @Bean(name = "shiroFilter")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager, AjaxPermissionsAuthorizationFilter ajaxFilter) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        //Shiro của 核心an toàngiao diện,nàyCáithuộc tính có Phải của 
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        Map<String, Filter> filterMap = new LinkedHashMap<>();
        filterMap.put("authc", ajaxFilter);
        shiroFilterFactoryBean.setFilters(filterMap);
        
        /*định nghĩashiroquachuỗi filter  Mapcấu trúc
         * Map trong key(xml trong  có chỉvalueGiá trị) của Dòng mộtCái'/'thaybảng của đường dẫn có tươngđối vớitạiHttpServletRequest.getContextPath() của Giá trịđến của 
         * anon: nótương ứng của Bên trong filter có trống của ,Không làm gì cả,Ở đây.do và .jspsaumặt của *cho biếtTham số,so với方说login.jsp?mainnàyloại
         * authc: Trang thuộc filter này phải được xác thực mới được truy cập,nó có Shirotrongđặt của mộtCáiinterceptororg.apache.shiro.web.filter.authc.FormAuthenticationFilter
         */
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
         /* quachuỗi filterđịnh nghĩa, Từ trênhướng tớidướithứ tựhành nghề dòng, mộtthông thườngSẽ  / ** đặttạinhất là dưới边:này có mộtCái坑呢, Sơ ý một chút là code không chạy được nữa;
          authc:Tất cảurlđềuPhảinhậnchứngQuamớiCó thểtruy cập; anon:Tất cảurlđềuđềuCó thể匿têntruy cập */

		// camunda Tải sơ đồ quy trình
        filterChainDefinitionMap.put("/rest/process-definition/*/diagram", "anon");

        filterChainDefinitionMap.put("/websocket/**", "anon");

        // nhậnchứngtươngđónggiao diện(Đăng nhập, LấyToken v.v. )
        filterChainDefinitionMap.put("/auth/token", "anon");
        filterChainDefinitionMap.put("/auth/getToken", "anon");
        filterChainDefinitionMap.put("/auth/getUserTenant", "anon");
        filterChainDefinitionMap.put("/auth/loginedtocken", "anon");
        filterChainDefinitionMap.put("/auth/wxToken", "anon");

        //bản việnLấyPhòng khámThông tinđặt dòng
        filterChainDefinitionMap.put("/hosdata/HosCollectData/listAll", "anon");

//        đóngtạiWeChatNgười dùngđăng nhập
        filterChainDefinitionMap.put("/wxUser/initLogin","anon");
        filterChainDefinitionMap.put("/wxUser/getCode","anon");
        filterChainDefinitionMap.put("/wxUser/getPhone","anon");


        filterChainDefinitionMap.put("/", "anon");
        filterChainDefinitionMap.put("/sys/sysSeting/getFile/1", "anon");
        filterChainDefinitionMap.put("/sys/sysSeting/getFile/2", "anon");
        filterChainDefinitionMap.put("/sys/sysSeting/getFile/3", "anon");
        filterChainDefinitionMap.put("/sys/sysSeting/getFile/4", "anon");
        filterChainDefinitionMap.put("/sys/sysSeting/getFile/5", "anon");
        filterChainDefinitionMap.put("/sys/sysSeting/getFile/6", "anon");
        // getFile/** chung va filedownload/** KHONG con anon (da xoa):
        // chung roi vao /** authc, nhung van xem/tai duoc qua URL trinh
        // duyet nho ngoai le query token cho hai duong dan nay trong filter
        // va session manager. Chi logo 1-6 va sysSeting/listAll (trang
        // login can truoc khi dang nhap) moi giu anon.
        // Swagger cung KHONG con anon (khong co frontend nao goi).
        filterChainDefinitionMap.put("/sys/sysSeting/listAll", "anon");
        filterChainDefinitionMap.put("/static/**", "anon");
        filterChainDefinitionMap.put("/login/auth", "anon");
        filterChainDefinitionMap.put("/login/logout", "anon");
        filterChainDefinitionMap.put("/error", "anon");
        // ureport/** giu anon: in an mo bang window.open khong kem duoc
        // header X-Token; khoa no se gay in phieu dang ky/hoa don.
        filterChainDefinitionMap.put("/ureport/**", "anon");
       // filterChainDefinitionMap.put("/outpatient/patient/**", "anon");
        // Blocker 8a: CHỈ mở endpoint health cho monitoring, không mở toàn bộ actuator.
        // Các endpoint actuator khác (/actuator/env, /actuator/metrics, ...) vẫn rơi
        // vào "/**" -> authc, và còn bị chặn ở tầng web exposure trong application.yml
        // (management.endpoints.web.exposure.include chỉ có health).
        filterChainDefinitionMap.put("/actuator/health", "anon");

        filterChainDefinitionMap.put("/**", "authc");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }

    @Bean
    public SessionDAO sessionDAO() {
        // sử dụngbộ nhớSessionDAOthaythayRedisSessionDAO
        return new MemorySessionDAO();
    }

    @Bean
    public StatelessSessionManager sessionManager(SessionDAO sessionDAO) {
        StatelessSessionManager sessionManager = new StatelessSessionManager();
        sessionManager.setSessionDAO(sessionDAO);
// Vô hiệu hóaphiênXác thựcthiết bịđiềuđộThời gian
        sessionManager.setSessionValidationSchedulerEnabled(false);
// phiênvượt giờĐặt là 24nhỏ giờ(86400000Mili giây), phòngdừngRedisbộ nhớ泄漏
        sessionManager.setGlobalSessionTimeout(86400000);
// Có hay khôngGọi sau khi phiên hết hạn SessionDAO  của XóaPhương thức, bảo持 là  true khôngsẽ影响hết hạn
        sessionManager.setDeleteInvalidSessions(true);
// Vô hiệu hóa URL viết lại
        sessionManager.setSessionIdUrlRewritingEnabled(false);
// Bậtphiên ID Cookie
        sessionManager.setSessionIdCookieEnabled(true);
        return sessionManager;

    }
    
	
	/**
     * Shiro Realm kế thừatựAuthorizingRealm của tùy chỉnhRealm,tức làchỉchỉ địnhShiroXác thựcNgười dùngĐăng nhập của Lớp là tùy chỉnh của 
     */
    @Bean
    public Realm realm() {
        return new UserRealm();
    }

    
    /**
     * khôngchỉchỉ địnhtênchữ của nói, Tự động tạo một tên phương thức có chữ cái đầu viết thườngbean
     * @param realm
     * @param sessionManager
     * @param
     * @return
     */
    @Bean
    public SecurityManager securityManager(Realm realm, SessionManager sessionManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(realm);
        securityManager.setSessionManager(sessionManager);
        logger.info("Use the StatelessSessionManager in SecurityManager");
        return securityManager;
    }


    /**
     * chứng từkhớpthiết bị
     * (dotạichúng ta của Mật khẩuKiểm tragiaochoShiro của SimpleAuthenticationInfotiến hành dòngXử lýrồi
     * vì vậychúng taCầnSửadướidoGetAuthenticationInfoTrongmã;
     * )
     * Có thểBộ khớp chứng từ mở rộng, Triển khai NhậpMật khẩuLỗi lầnsốsaukhóachỉ định v.v. chức năng, dướimột lần
     */
    @Bean(name = "credentialsMatcher")
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        //phân tánCộtthuật toán:Ở đâysử dụngMD5thuật toán;
        hashedCredentialsMatcher.setHashAlgorithmName("md5");
        //phân tánCột của  lầnsố, Mặc định2 lần
        hashedCredentialsMatcher.setHashIterations(2);
        //storedCredentialsHexEncodedMặc định có true, này giờdùng của  có Mật khẩuMã hóadùng của  có HexMã; false giờdùngBase64Mã
        hashedCredentialsMatcher.setStoredCredentialsHexEncoded(true);
        return hashedCredentialsMatcher;
    }

    /**
     * Shirosinhđặt têntuầnkỳXử lýthiết bị
     */
    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    
    /**
     * mởShiro của annotation(Như @RequiresRoles,@RequiresPermissions),cần借giúpSpringAOPquétsử dụngShiroannotation của Lớp,vàtại必cần giờtiến hành dòngan toànlogicXác thực
     * cấu hìnhbằngdướihaiCáibean(DefaultAdvisorAutoProxyCreator(Tùy chọn) và AuthorizationAttributeSourceAdvisor)tức làcó thểTriển khainàychức năng
     */

    /**
     * Vô hiệu hóa Shiro Filter  của  Servlet Filter đăng ký, do ShiroFilterFactoryBean Quản lý
     */
    @Bean
    public FilterRegistrationBean<AjaxPermissionsAuthorizationFilter> ajaxFilterRegistration(AjaxPermissionsAuthorizationFilter filter) {
        FilterRegistrationBean<AjaxPermissionsAuthorizationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

}
