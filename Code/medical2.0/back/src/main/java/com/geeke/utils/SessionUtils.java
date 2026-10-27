package com.geeke.utils;

import com.alibaba.fastjson.JSONObject;
import com.geeke.admin.entity.User;
import com.geeke.org.entity.Company;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Người dùngLớp công cụ
 * @author lys
 * @version 2018-12-05
 */
public class SessionUtils extends com.geeke.sys.utils.SessionUtils {
	private static final Logger logger = LoggerFactory.getLogger(SessionUtils.class);

	/**
	 * Lấy người dùng hiện tại
	 * @return Không lấy được thì trả về new User()
	 */
	public static User getUser(){
		JSONObject userJson = getUserJson();
		if (userJson == null) {
			return null;
		}
		return userJson.toJavaObject(User.class);
	}
	
	
    /**
	 * ĐặtHiện tạiNgười dùng
	 * @return Không lấy được thì trả về new User()
	 */
	public static void setUser(User user){
		setUserJson((JSONObject)JSONObject.toJSON(user));
	}

	/**
	 * Đặt phòng khám mà người dùng hiện tại đang đăng nhập
	 * @return Không lấy được thì trả về new User()
	 */
	public static void setLoginTenantId(String tenantId){
		try {
			SecurityUtils.getSubject().getSession().setAttribute("tenantID", tenantId);
		} catch (Exception e) {
			logger.debug("Session Đã hết hạn hoặc không tồn tại", e);
		}
	}

	/**
	 * Đặt phòng khám mà người dùng hiện tại đang đăng nhập
	 * @return Không lấy được thì trả về new User()
	 */
	public static void setLoginTenant(Company company){
		try {
			JSONObject com = (JSONObject) JSONObject.toJSON(company);
			SecurityUtils.getSubject().getSession().setAttribute("tenant", com);
		} catch (Exception e) {
			logger.debug("Session Đã hết hạn hoặc không tồn tại", e);
		}
	}

	/**
	 * Lấy người dùng hiện tạinơiĐăng nhập của Phòng khám
	 * @return Không lấy được thì trả về null
	 */
	public static String getLoginTenantId(){
		try {
			Object attr = SecurityUtils.getSubject().getSession().getAttribute("tenantID");
			if (attr == null) {
				return null;
			}
			// trực tiếpTrả vềchuỗi, TránhString.valueOf(null)Quay lại"null"chuỗi của vấn đề
			return attr.toString();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Đặt phòng khám mà người dùng hiện tại đang đăng nhập
	 * @return Không lấy được thì trả về new User()
	 */
	public static Company getLoginTenant(){
		try {
			JSONObject j = (JSONObject)SecurityUtils.getSubject().getSession().getAttribute("tenant");
			return j.toJavaObject(Company.class);
		} catch (Exception e) {
			return null;
		}
	}

}
