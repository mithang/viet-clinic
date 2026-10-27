package com.geeke.config.shiro;

import io.jsonwebtoken.Claims;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.IUser;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.JwtUtils;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.constants.ErrorEnum;

/**
 * @author: lys
 * @description: đối vớiKhông cóĐăng nhậpYêu cầutiến hành dòngchặn, Tất cảTrả vềjsonThông tin. ghi đèrơishirogốcnày của 跳chuyểnlogin.jsp của chặncách
 * @date: 2017/10/24 10:11
 */
@Component
public class AjaxPermissionsAuthorizationFilter extends FormAuthenticationFilter {
    private final static Logger logger = LoggerFactory.getLogger(AjaxPermissionsAuthorizationFilter.class);
    
	private JwtUtils jwtUtils;

	public AjaxPermissionsAuthorizationFilter(JwtUtils jwtUtils) {
		this.jwtUtils = jwtUtils;
	}
	
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
    	HttpServletRequest httpRequest = WebUtils.toHttp(request);
		String token = httpRequest.getHeader("X-Token");

		// chú ý: QuaURLTham sốtruyềntokenTồn tạian toàn风险(tokensẽrahiệntạitruy cậpNgàynhật ký, Lịch sử trình duyệt trong )
		// chỉtạikhôngphương phápĐặt tiêu đề yêu cầu của trường hợp(Như TệpTải xuống) trong sử dụng
		if( token == null ) {
			token = httpRequest.getParameter("token");
		}

    	if(token == null ) {
    		logger.info("Can not get token from the hearder of {} ", httpRequest.getRequestURI());
    		return false;
    	}
    	try {

	    	Claims claims = jwtUtils.parseJWT(token);
	    	// JWThết hạnXác thựcĐãQuaJwtUtilsTrongsetExpirationTriển khai
	    	String host = request.getRemoteHost();
			JSONObject json = JSONObject.parseObject(claims.getSubject());
			if(!host.equals(json.getString("host"))) {
				logger.info("The host of the token is inconsistent. url: {} ", httpRequest.getRequestURI());
				return false;
			}

			// Kiểm traNgười dùngId
			IUser user = SessionUtils.getUser();
			if(user == null) {
				logger.info("Session is expired. {}", httpRequest.getRequestURI());
				return false;
			}
			if(user.getId() == null || !user.getId().equals(json.getString("userId"))) {
				logger.info("The user of the token is inconsistent. url: {} ", httpRequest.getRequestURI());
				return false;
			}
    	}catch(Exception e){
    		logger.error("Access allowed error.  url: {}", httpRequest.getRequestURI(), e);
    		return false;
    	}
        return true;
    }
	
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        JSONObject jsonObject = ResultUtil.errorJson(ErrorEnum.E_20011);
        PrintWriter out = null;
        HttpServletResponse res = (HttpServletResponse) response;
        try {
            res.setCharacterEncoding("UTF-8");
            res.setContentType("application/json");
            out = response.getWriter();
            out.println(jsonObject);
        } catch (Exception e) {
            logger.debug("Response write error ignored", e);
        } finally {
            if (null != out) {
                out.flush();
                out.close();
            }
        }
        return false;
    }
}
