package com.geeke.sys.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsonwebtoken.Claims;

import com.alibaba.fastjson.JSONObject;
import com.geeke.utils.JwtUtils;

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
