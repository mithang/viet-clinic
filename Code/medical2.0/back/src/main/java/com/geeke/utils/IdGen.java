package com.geeke.utils;

import java.security.SecureRandom;

import com.baidu.fsg.uid.UidGenerator;

/**
 * đóng góiđủ loạiTạoduy nhấttínhIDthuật toán của Lớp công cụ.
 * @author lys
 * @version 2018-12-27
 */

public class IdGen {

	private static SecureRandom random = new SecureRandom();

	private static UidGenerator uidGenerator;

	/**
	 * đóng góiJDKtựkèm của UUID, QuaRandomSốTạo,  trong giữakhông- phút割.
	 */
	public static String uuid() {
		// return UUID.randomUUID().toString().replaceAll("-", "");
		return Long.toString(uidGenerator.getUID());
	}
	
	/**
	 * Sử dụngSecureRandomngẫu nhiênTạoLong. 
	 */
	public static long randomLong() {
		return Math.abs(random.nextLong());
	}

	/**
	 * Dựa trênBase62Mã của SecureRandomngẫu nhiênTạobytes.
	 */
	public static String randomBase62(int length) {
		byte[] randomBytes = new byte[length];
		random.nextBytes(randomBytes);
		return Encodes.encodeBase62(randomBytes);
	}

	public static void  setUidGenerator(UidGenerator generator) {
		uidGenerator = generator;
	}
}
