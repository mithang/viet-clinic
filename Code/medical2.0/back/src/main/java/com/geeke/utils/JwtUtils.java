package com.geeke.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JwtLớp công cụ
 * 
 * @author Lining
 * @date 2017/11/2
 */
@Component
public class JwtUtils {

	@Value(value = "${jwtUtils.id:jwt}")
	private String id;
	
	@Value(value = "${jwtUtils.secret}")
	private String secret;
	
	/**
	 * lưusốngThời gian(Mili giây)
	 */
	@Value(value = "${jwtUtils.ttlMillis:3600000}")
	private long ttlMillis;
	

	/**
	 * TạoMã hóakey
	 * 
	 * @return
	 */
	private SecretKey generalKey() {
		if (secret == null || secret.trim().isEmpty()) {
			throw new IllegalStateException("JWT secret is not configured. Set 'jwtUtils.secret' in application config.");
		}
		byte[] encodedKey = Base64.getDecoder().decode(secret);
		SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
		return key;
	}

	/**
	 * Tạojwt
	 * 
	 * @param subject
	 * @return
	 */
	public String createJWT(String subject) {
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);
		SecretKey key = generalKey();
		JwtBuilder builder = Jwts.builder().setId(id).setIssuedAt(now).setSubject(subject).signWith(signatureAlgorithm,
				key);
		if (ttlMillis >= 0) {
			long expMillis = nowMillis + ttlMillis;
			Date exp = new Date(expMillis);
			builder.setExpiration(exp);

			SecurityUtils.getSubject().getSession().setTimeout(ttlMillis);  // sessionĐếnkỳThời gian
		}
		return builder.compact();
	}

	/**
	 * Giải mãjwt
	 *
	 * @param jwt
	 * @return
	 * @throws ExpiredJwtException tokenhết hạn giờném ra
	 * @throws io.jsonwebtoken.SignatureException chữ kýXác thựcThất bại giờném ra
	 */
	public Claims parseJWT(String jwt) {
		SecretKey key = generalKey();
		// khônglạinuốtrơi ExpiredJwtException — hết hạntokennênbịxem là không hợp lệ
		Claims claims = Jwts.parser()
				.setSigningKey(key)
				.parseClaimsJws(jwt)
				.getBody();
		return claims;
	}

	/**
	 * Giải mãjwt, hết hạn giờTrả vềnullmàkhông phảiném rangoại lệ
	 * dùng đểCầnlặngimXử lýhết hạntoken của trường hợp(Như Từ hết hạntoken trong đề xuấtlấysessionIdlàmsạchlý)
	 *
	 * @param jwt
	 * @return claims, hết hạn giờTrả vềnull
	 */
	public Claims parseJWTLenient(String jwt) {
		SecretKey key = generalKey();
		try {
			return Jwts.parser()
					.setSigningKey(key)
					.parseClaimsJws(jwt)
					.getBody();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		}
	}

	public long getTtlMillis() {
		return ttlMillis;
	}

}
