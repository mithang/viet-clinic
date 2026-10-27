package com.geeke.sys.utils;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * Người dùngLớp công cụ
 * @author lys
 * @version 2018-12-05
 * @deprecated Use {@link com.geeke.utils.SessionUtils} instead, which extends this class and provides
 * additional convenience methods (getUser, setUser, tenant management, etc.).
 * This class is kept for backward compatibility with existing imports.
 */
@Deprecated
public class SessionUtils {
    private static final Logger logger = LoggerFactory.getLogger(SessionUtils.class);

    /**
     * session trong lưuđặtNgười dùngThông tin của keyGiá trị
     */
    private static final String SESSION_USER_INFO = "userInfo";
    private static final String SESSION_USER_PERMISSION = "userPermission";
	private static final String SESSION_YEWECHAT_OPENID = "openId";

	
	/**
	 * Lấy người dùng hiện tại
	 * @return Không lấy được thì trả về new User()
	 */
	public static JSONObject getUserJson(){
		try {
			return (JSONObject)SecurityUtils.getSubject().getSession().getAttribute(SESSION_USER_INFO);
		} catch (Exception e) {
			return null;
		}
	}


    /**
     * Lấy người dùng hiện tại
     *
     * @return Không lấy được thì trả về new User()
     */
    public static SessionUserDto getUserDto() {
        try {
            Object object = SecurityUtils.getSubject().getSession().getAttribute(SESSION_USER_INFO);
            return BeanUtil.copyProperties(object, SessionUserDto.class);
        } catch (Exception e) {
            return null;
        }
    }

	/**
	 * Từ chỉchỉ địnhSessionLấyNgười dùngThông tin
	 * @param session
	 * @return
	 */
	public static JSONObject getUserJson(Session session){
		if (session == null) {
			return null;
		}
		return (JSONObject)session.getAttribute(SESSION_USER_INFO);
	}
	
	
    /**
	 * ĐặtHiện tạiNgười dùng
	 * @return Không lấy được thì trả về new User()
	 */
	public static void setUserJson(JSONObject userObj ){
		try {
			SecurityUtils.getSubject().getSession().setAttribute(SESSION_USER_INFO, userObj);
		} catch (Exception e) {
			logger.debug("Session Đã hết hạn hoặc không tồn tại", e);
		}
	}

	@SuppressWarnings("unchecked")
	public static List<String> getUserPermission() {
		try {
			return (List<String>)SecurityUtils.getSubject().getSession().getAttribute(SESSION_USER_PERMISSION);
		} catch (Exception e) {
			return null;
		}
	}

	public static void setUserPermission(List<String> permissionList) {
		try {
			SecurityUtils.getSubject().getSession().setAttribute(SESSION_USER_PERMISSION, permissionList);
		} catch (Exception e) {
			logger.debug("Session Đã hết hạn hoặc không tồn tại", e);
		}
	}


	/**
	 * LấyWeChat doanh nghiệpHiện tạiNgười dùng
	 * @return Không lấy được thì trả về JSONObject
	 */
	public static JSONObject getWeChatUser(){
		try {
			return (JSONObject)SecurityUtils.getSubject().getSession().getAttribute(SESSION_YEWECHAT_OPENID);
		} catch (Exception e) {
			return null;
		}
	}


	/**
	 * ĐặtWeChat doanh nghiệpHiện tạiNgười dùng
	 * @return
	 */
	public static void setWeChatUser(JSONObject jsonObject){
		try {
			SecurityUtils.getSubject().getSession().setAttribute(SESSION_YEWECHAT_OPENID, jsonObject);
		} catch (Exception e) {
			logger.debug("Session Đã hết hạn hoặc không tồn tại", e);
		}
	}
}
