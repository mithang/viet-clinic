package com.geeke.utils;

import java.text.ParseException;
import java.util.Date;

import org.quartz.CronExpression;

/**
 * cronbiểu thứcLớp công cụ
 *
 * @author shenzy
 *
 */
public class CronUtils {
    /**
     * Trả vềmộtCáiphát hành尔Giá trịthaybảngmộtCáichochỉ định của Cronbiểu thức của Hiệu lựctính
     *
     * @param cronExpression CronBiểu thức
     * @return boolean biểu thứcCó hay khôngHiệu lực
     */
    public static boolean isValid(String cronExpression) {
        return CronExpression.isValidExpression(cronExpression);
    }

    /**
     * Trả vềmộtCáichuỗiGiá trị,Cho biết thông báo này không hợp lệCronbiểu thứcchoraHiệu lựctính
     *
     * @param cronExpression CronBiểu thức
     * @return String không hợp lệ giờTrả vềbiểu thứcLỗiMô tả,NếuHiệu lựcTrả vềnull
     */
    public static String getInvalidMessage(String cronExpression) {
        try {
            new CronExpression(cronExpression);
            return null;
        }
        catch (ParseException pe) {
            return pe.getMessage();
        }
    }

    /**
     * Trả vềdướimộtCáihành nghề dòngThời gianTheochochỉ định của CronBiểu thức
     *
     * @param cronExpression CronBiểu thức
     * @return Date dưới lầnCronbiểu thứchành nghề dòngThời gian
     */
    public static Date getNextExecution(String cronExpression) {
        try {
            CronExpression cron = new CronExpression(cronExpression);
            return cron.getNextValidTimeAfter(new Date(System.currentTimeMillis()));
        }
        catch (ParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

}
