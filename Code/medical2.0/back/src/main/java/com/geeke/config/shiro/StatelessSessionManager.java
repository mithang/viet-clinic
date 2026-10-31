package com.geeke.config.shiro;

/**
 * @Author lys
 * @Description redisdịch vụcấu hình
 * @Date 15:18 2019/12/20
 **/

import com.geeke.utils.SessionUtils;
import io.jsonwebtoken.Claims;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.session.Session;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.geeke.utils.JwtUtils;
import com.geeke.utils.StringUtils;
import org.springframework.util.ObjectUtils;


public class StatelessSessionManager extends DefaultWebSessionManager {
    private final static Logger logger = LoggerFactory.getLogger(StatelessSessionManager.class);

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * nàyCái có Khách hàngđầuVui lòngyêu cầuchodịch vụđầukèm của header
     */
    private final static String HEADER_TOKEN_NAME = "X-Token";

    @Override
    protected Serializable getSessionId(ServletRequest servletRequest, ServletResponse servletResponse) {
        // Từ Vui lòngtiêu đề yêu cầu trong Lấytoken
        HttpServletRequest httpRequest = WebUtils.toHttp(servletRequest);
        String token = httpRequest.getHeader(HEADER_TOKEN_NAME);
        // Giong filter xac thuc: token qua query param chi duoc chap nhan
        // cho hai duong dan tai tep/xem truoc qua URL trinh duyet (khong
        // dat duoc header). API thong thuong bat buoc dung header X-Token,
        // neu khong session se khong giai quyet duoc tu query token.
        if (token == null && isDownloadUrl(httpRequest.getRequestURI())) {
            token = httpRequest.getParameter("token");
        }
        // Kiểm traCó hay khôngcóGiá trị
        if (StringUtils.isNoneBlank(token)) {
            // ĐặtHiện tạisessionTrạng thái
            servletRequest.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE, "url");
            servletRequest.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID, token);
            servletRequest.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID, Boolean.TRUE);

            try {
                Claims claims = jwtUtils.parseJWT(token);
                JSONObject json = JSONObject.parseObject(claims.getSubject());

                String sessionId = json.getString("sessionId");
                logger.info("Reuest {} get sessionId from request header {}", httpRequest.getRequestURI(), sessionId);
                return sessionId;

            } catch (Exception e) {
                logger.error("Get session id by parse token  error.  url: {}", httpRequest.getRequestURI(), e);
            }
            logger.info("Request {} get tokenId from request header {}", httpRequest.getRequestURI(), token);
            return token;
        }
        // nếuheaderLấykhôngĐếntokenthìthửthửTừ cookie trong Lấy
        // Serializable serializable = super.getSessionId(servletRequest, servletResponse);
        Serializable serializable = UUID.randomUUID();  // Hỗ trợ trình duyệt mở hai trang đăng nhập bằng người dùng khác nhau
        logger.info("Request {} get sessionId from super get getSessionId {}", httpRequest.getRequestURI(), serializable.toString());
        return serializable;
    }

    /**
     * Chi hai duong dan tai tep/xem truoc qua URL trinh duyet moi duoc
     * lay token tu query param (xem AjaxPermissionsAuthorizationFilter).
     */
    private boolean isDownloadUrl(String uri) {
        return uri != null
                && (uri.contains("/sys/sysSeting/getFile/")
                        || uri.contains("/sys/sysSeting/filedownload"));
    }

    /**
     * Lấytất cảsốngđộngTrongSession
     * @return
     */
    public Collection<Session> getActiveSessions() {
        Collection<Session> sessionList = super.getActiveSessions();
        return sessionList;
    }
}