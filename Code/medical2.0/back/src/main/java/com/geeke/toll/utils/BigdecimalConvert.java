package com.geeke.toll.utils;

import java.math.BigDecimal;

/**
 * BigDecimal trốngan toànLớp công cụ
 */
public class BigdecimalConvert {

    /**
     * dùng đểKiểm traSố tiềnCó hay không là null,  là nullThì trả về BigDecimal.ZERO
     */
    public static BigDecimal convert(BigDecimal bigDecimal) {
        return bigDecimal != null ? bigDecimal : BigDecimal.ZERO;
    }
}
