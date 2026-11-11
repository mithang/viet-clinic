package com.geeke.utils;


/**
 * @author: lys
 * @date: 2018/10/24 10:16
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {
	
    private static final char SEPARATOR = '_';

    public static boolean isNullOrEmpty(String str) {
        return null == str || "".equals(str) || "null".equals(str);
    }

    public static boolean isNullOrEmpty(Object obj) {
        return null == obj || "".equals(obj);
    }

    /**
     * DS200: escape ký tự đặc biệt của LIKE (%, _, \) để giá trị tìm kiếm
     * không biến thành wildcard — gõ "%" phải tìm đúng tên chứa "%",
     * không khớp mọi dòng. Mirror DiagnosisService.toLikePattern nhưng
     * KHÔNG bọc % hai đầu (mapper tự concat '%', value, '%'). null giữ
     * null, rỗng giữ rỗng. SQL phải kèm ESCAPE '\' (xem DiagnosisDao).
     */
    public static String escapeLike(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
    
    
	/**
	 * Công cụ đặt tên camelCase
	 * @return
	 * 		toCamelCase("hello_world") == "helloWorld" 
	 * 		toCapitalizeCamelCase("hello_world") == "HelloWorld"
	 * 		toUnderScoreCase("helloWorld") = "hello_world"
	 */
    public static String toCamelCase(String s) {
        if (s == null) {
            return null;
        }

        s = s.toLowerCase();

        StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == SEPARATOR) {
                upperCase = true;
            } else if (upperCase) {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
	 * Công cụ đặt tên camelCase
	 * @return
	 * 		toCamelCase("hello_world") == "helloWorld" 
	 * 		toCapitalizeCamelCase("hello_world") == "HelloWorld"
	 * 		toUnderScoreCase("helloWorld") = "hello_world"
	 */
    public static String toCapitalizeCamelCase(String s) {
        if (s == null) {
            return null;
        }
        s = toCamelCase(s);
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
    
    /**
	 * Công cụ đặt tên camelCase
	 * @return
	 * 		toCamelCase("hello_world") == "helloWorld" 
	 * 		toCapitalizeCamelCase("hello_world") == "HelloWorld"
	 * 		toUnderScoreCase("helloWorld") = "hello_world"
	 */
    public static String toUnderScoreCase(String s) {
        if (s == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            boolean nextUpperCase = true;

            if (i < (s.length() - 1)) {
                nextUpperCase = Character.isUpperCase(s.charAt(i + 1));
            }

            if ((i > 0) && Character.isUpperCase(c)) {
                if (!upperCase || !nextUpperCase) {
                    sb.append(SEPARATOR);
                }
                upperCase = true;
            } else {
                upperCase = false;
            }

            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }
}
