package com.geeke.utils;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SerialNoUtils {
    /** nàyđịa phươngtínhsốthiết bị, key=tiền tố+Ngày, value=Hiện tạisố thứ tự */
    private final Map<String, AtomicInteger> counterMap = new ConcurrentHashMap<>();

    /**
     * Tạo17vịduy nhấtsố thứ tự, "LSH"+yyyyMMdd+6vịSố
     */
    public String generateSerialNo(String codeKey){
        String todayDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String cacheKey = codeKey + todayDate;

        AtomicInteger counter = counterMap.computeIfAbsent(cacheKey, k -> new AtomicInteger(0));
        int codeNum = counter.incrementAndGet();

        return getCodeOfSix(cacheKey, codeNum);
    }

    private String getCodeOfSix(String prefix, int nowNum) {
        StringBuilder numSb = new StringBuilder();
        if (nowNum < 10) {
            numSb.append("00000").append(nowNum);
        } else if (nowNum < 100) {
            numSb.append("0000").append(nowNum);
        } else if (nowNum < 1000) {
            numSb.append("000").append(nowNum);
        } else if (nowNum < 10000) {
            numSb.append("00").append(nowNum);
        } else if (nowNum < 100000) {
            numSb.append("0").append(nowNum);
        } else {
            numSb.append(nowNum);
        }
        return prefix + numSb.toString();
    }
}
