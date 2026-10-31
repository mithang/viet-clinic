/**
 * Copyright (c) 2005-2012 springside.org.cn
 */
package com.geeke.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * đóng góiđủ loạiđịnh dạng của MãGiải mãLớp công cụ.
 * 1.Commons-Codec của  hex/base64 Mã
 * 2.tựchế của base62 Mã
 * 3.Commons-Lang của xml/html escape
 * 4.JDKcung cấp của URLEncoder
 * @author calvin
 * @version 2013-01-15
 */
public class Encodes {

	private static final String DEFAULT_URL_ENCODING = "UTF-8";
	private static final char[] BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

	/**
	 * HexMã.
	 */
	public static String encodeHex(byte[] input) {
		return new String(Hex.encodeHex(input));
	}

	/**
	 * HexGiải mã.
	 */
	public static byte[] decodeHex(String input) {
		try {
			return Hex.decodeHex(input.toCharArray());
		} catch (DecoderException e) {
			throw Exceptions.unchecked(e);
		}
	}

	/**
	 * Base64Mã.
	 */
	public static String encodeBase64(byte[] input) {
		return new String(Base64.encodeBase64(input));
	}
	
	/**
	 * Base64Mã.
	 */
	public static String encodeBase64(String input) {
		try {
			return new String(Base64.encodeBase64(input.getBytes(DEFAULT_URL_ENCODING)));
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}

//	/**
//	 * Base64Mã, URLan toàn(Sẽ Base64TrongURLkhông phảiphương phápchữký tự'+' và '/'chuyển là '-' và '_', thấyRFC3548).
//	 */
//	public static String encodeUrlSafeBase64(byte[] input) {
//		return Base64.encodeBase64URLSafe(input);
//	}

	/**
	 * Base64Giải mã.
	 */
	public static byte[] decodeBase64(String input) {
		return Base64.decodeBase64(input.getBytes());
	}
	
	/**
	 * Base64Giải mã.
	 */
	public static String decodeBase64String(String input) {
		try {
			return new String(Base64.decodeBase64(input.getBytes()), DEFAULT_URL_ENCODING);
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}

	/**
	 * Base62Mã. 
	 */
	public static String encodeBase62(byte[] input) {
		char[] chars = new char[input.length];
		for (int i = 0; i < input.length; i++) {
			chars[i] = BASE62[((input[i] & 0xFF) % BASE62.length)];
		}
		return new String(chars);
	}

	/**
	 * Html chuyểnmã.
	 */
	public static String escapeHtml(String html) {
		return StringEscapeUtils.escapeHtml4(html);
	}

	/**
	 * Html Giải mã.
	 */
	public static String unescapeHtml(String htmlEscaped) {
		return StringEscapeUtils.unescapeHtml4(htmlEscaped);
	}

	/**
	 * Xml chuyểnmã.
	 */
	public static String escapeXml(String xml) {
		return StringEscapeUtils.escapeXml10(xml);
	}

	/**
	 * Xml Giải mã.
	 */
	public static String unescapeXml(String xmlEscaped) {
		return StringEscapeUtils.unescapeXml(xmlEscaped);
	}

	/**
	 * URL Mã, EncodeMặc định là UTF-8. 
	 */
	public static String urlEncode(String part) {
		try {
			return URLEncoder.encode(part, DEFAULT_URL_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw Exceptions.unchecked(e);
		}
	}

	/**
	 * URL Giải mã, EncodeMặc định là UTF-8. 
	 */
	public static String urlDecode(String part) {

		try {
			return URLDecoder.decode(part, DEFAULT_URL_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw Exceptions.unchecked(e);
		}
	}
}
