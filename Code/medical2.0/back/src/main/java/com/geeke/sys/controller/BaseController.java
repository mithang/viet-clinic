package com.geeke.sys.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsonwebtoken.Claims;

import com.alibaba.fastjson.JSONObject;
import com.geeke.utils.JwtUtils;
import com.geeke.utils.SessionUtils;

/**
 * ControllerLớp cơ sở
 * @author lys
 * @date 2017/11/15
 */
public class BaseController {
	
	/**
	 * Ngàynhật kýĐối tượng
	 */
	protected static final Logger logger = LoggerFactory.getLogger(BaseController.class);
	
	/**
	 * Kiểm tra phiên đăng nhập hiện tại có quyền tài nguyên hay không.
	 * Quyền tài nguyên là danh sách chuỗi dạng "entity:action" (ví dụ "role:list")
	 * được nạp vào phiên khi đăng nhập (xem AuthController.getLoginUserInfo).
	 * Dùng để chặn API ở backend, không chỉ ẩn nút ở frontend: nếu phiên không
	 * có quyền thì từ chối, kể cả khi token còn hợp lệ. Trả về false khi không
	 * lấy được danh sách quyền (fail closed).
	 */
	protected boolean hasSessionPermission(String permission) {
		try {
			List<String> permissions = SessionUtils.getUserPermission();
			return permissions != null && permissions.contains(permission);
		} catch (Exception e) {
			logger.debug("Không kiểm tra được quyền phiên: {}", e.getMessage());
			return false;
		}
	}

	/**
	 * Sàn fail-closed cho các endpoint generic chưa có permission riêng theo
	 * entity (xem CrudController): phiên phải có ít nhất một resource thì mới
	 * được gọi. Tài khoản 0-resource (0 router, 0 resource, vd testflag) bị
	 * chặn; mọi vai trò nghiệp vụ đều có hàng chục resource nên sàn này không
	 * thay đổi luồng chính. Đây là sàn, không phải RBAC đầy đủ theo entity:
	 * tài khoản có 1 resource lẻ vẫn qua sàn, permission cụ thể theo entity
	 * là việc của vòng map 61 controller riêng. Trả về false khi không lấy
	 * được danh sách quyền (fail closed).
	 */
	protected boolean hasAnySessionPermission() {
		try {
			List<String> permissions = SessionUtils.getUserPermission();
			return permissions != null && !permissions.isEmpty();
		} catch (Exception e) {
			logger.debug("Không kiểm tra được quyền phiên: {}", e.getMessage());
			return false;
		}
	}

	/**
	 * Từ jwt trong LấysubjectThông tin
	 * @param jwtUtils
	 * @param jwt
	 * @param key
	 * @return
	 */
	protected String getSubjectFromJwt(JwtUtils jwtUtils, String jwt, String key) {
		try {
			Claims claims = jwtUtils.parseJWT(jwt);
			String subject = claims.getSubject();
			if(key != null && !key.trim().equals("")) {
				JSONObject json = JSONObject.parseObject(subject);
				return json.getString(key);
			} else {
				return subject;
			}
		} catch (Exception e) {
			logger.warn("Failed to parse JWT: {}", e.getMessage());
			return null;
		}
	}

}
