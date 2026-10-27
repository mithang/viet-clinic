package com.geeke.utils;

import java.util.UUID;

/**
 * @author: hxy
 * @description: Tạongẫu nhiênmã của Lớp công cụ
 * @date: 2017/10/26 14:20
 */
public class RandomUtil {
    /**
     * Tạotiền tố+32vịMã dùnglàmid
     *
     * @return string
     */
    public static String genarateId(String prefix) {
        String uuid = prefix + UUID.randomUUID().toString().trim().replaceAll("-", "");
        return uuid;
    }
}
