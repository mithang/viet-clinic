package com.geeke.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * Lớp tiện ích ngày tháng, kế thừaorg.apache.commons.lang.time.DateUtilsLớp
 * @author
 * @version 2014-4-15
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {
	
	private final static String[] parsePatterns = {
		"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM", 
		"yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
		"yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

	/**
	 * Lấy chuỗi ngày hiện tại Định dạng(yyyy-MM-dd)
	 */
	public static String getDate() {
		return getDate("yyyy-MM-dd");
	}
	
	/**
	 * Lấy chuỗi ngày hiện tại Định dạng(yyyy-MM-dd) patternCó thể là : "yyyy-MM-dd" "HH:mm:ss" "E"
	 */
	public static String getDate(String pattern) {
		return DateFormatUtils.format(new Date(), pattern);
	}
	
	/**
	 * đượcĐếnNgàychuỗi Mặc địnhđịnh dạng(yyyy-MM-dd) patternCó thể là : "yyyy-MM-dd" "HH:mm:ss" "E"
	 */
	public static String formatDate(Date date, Object... pattern) {
		String formatDate = null;
		if (pattern != null && pattern.length > 0) {
			formatDate = DateFormatUtils.format(date, pattern[0].toString());
		} else {
			formatDate = DateFormatUtils.format(date, "yyyy-MM-dd");
		}
		return formatDate;
	}
	
	/**
	 * đượcĐếnNgàyThời gianchuỗi, chuyển đổiđịnh dạng(yyyy-MM-dd HH:mm:ss)
	 */
	public static String formatDateTime(Date date) {
		return formatDate(date, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * đượcĐếnHiện tạiThời gianchuỗi Định dạng(HH:mm:ss)
	 */
	public static String getTime() {
		return formatDate(new Date(), "HH:mm:ss");
	}

	/**
	 * đượcĐếnHiện tạiNgày và Thời gianchuỗi Định dạng(yyyy-MM-dd HH:mm:ss)
	 */
	public static String getDateTime() {
		return formatDate(new Date(), "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * đượcĐếnHiện tạiNămphầnchuỗi Định dạng(yyyy)
	 */
	public static String getYear() {
		return formatDate(new Date(), "yyyy");
	}

	/**
	 * đượcĐếnHiện tạiThángphầnchuỗi Định dạng(MM)
	 */
	public static String getMonth() {
		return formatDate(new Date(), "MM");
	}

	/**
	 * đượcĐếnkhi ngàychuỗi Định dạng(dd)
	 */
	public static String getDay() {
		return formatDate(new Date(), "dd");
	}

	/**
	 * đượcĐếnHiện tạithứchuỗi Định dạng(E)thứ几
	 */
	public static String getWeek() {
		return formatDate(new Date(), "E");
	}
	
	/**
	 * NgàyChuyển đổi chuỗi kiểu là Ngày Định dạng
	 * { "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", 
	 *   "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm",
	 *   "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm" }
	 */
	public static Date parseDate(Object str) {
		if (str == null){
			return null;
		}
		try {
			return parseDate(str.toString(), parsePatterns);
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * Lấytrước đây của  ngàysố
	 * @param date
	 * @return
	 */
	public static long pastDays(Date date) {
		long t = new Date().getTime()-date.getTime();
		return t/MILLIS_PER_DAY;
	}

	/**
	 * Lấytrước đây của nhỏ giờ
	 * @param date
	 * @return
	 */
	public static long pastHour(Date date) {
		long t = new Date().getTime()-date.getTime();
		return t/MILLIS_PER_HOUR;
	}
	
	/**
	 * Lấytrước đây của  phútphút
	 * @param date
	 * @return
	 */
	public static long pastMinutes(Date date) {
		long t = new Date().getTime()-date.getTime();
		return t/MILLIS_PER_MINUTE;
	}
	
	/**
	 * Chuyển đổi thành thời gian( ngày, giờ: phút:giây.Mili giây)
	 * @param timeMillis
	 * @return
	 */
    public static String formatDateTime(long timeMillis){
		long day = timeMillis / MILLIS_PER_DAY;
		long hour = (timeMillis / MILLIS_PER_HOUR - day * 24);
		long min = ((timeMillis / MILLIS_PER_MINUTE) - day * 24 * 60 - hour * 60);
		long s = (timeMillis / MILLIS_PER_SECOND - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
		long sss = timeMillis % MILLIS_PER_SECOND;
		return (day > 0 ? day + ",":"") + hour + ":" + min + ":" + s + "." + sss;
    }

	/**
	 * Kiểm tra hai ngày có cùng năm không
	 * @param before
	 * @param after
	 * @return
	 */
	public static boolean isSameYear(Date before, Date after) {
		if (null != before && null != after) {
			Calendar beforeCal = Calendar.getInstance();
			beforeCal.setTime(before);
			Calendar afterCal = Calendar.getInstance();
			afterCal.setTime(after);
			return beforeCal.get(Calendar.YEAR) == afterCal.get(Calendar.YEAR);
		}
		return false;
	}

	/**
	 * Kiểm tra hai ngày có cùng năm không
	 * @param before
	 * @param after
	 * @return
	 */
	public static boolean isSameYear(Calendar before, Calendar after) {
		return isSameYear(before.getTime(), after.getTime());
	}

	/**
	 * Kiểm tra hai ngày có cùng năm khôngcùngTháng
	 * @param before
	 * @param after
	 * @return
	 */
	public static boolean isSameMonth(Date before, Date after) {
		if (null != before && null != after) {
			Calendar beforeCal = Calendar.getInstance();
			beforeCal.setTime(before);
			Calendar afterCal = Calendar.getInstance();
			afterCal.setTime(after);
			return beforeCal.get(Calendar.YEAR) == afterCal.get(Calendar.YEAR)
					&& beforeCal.get(Calendar.MONTH) == afterCal.get(Calendar.MONTH);
		}
		return false;
	}

	/**
	 * Kiểm trahaiCáiNgàyCó hay không có cùngTháng
	 * @param before
	 * @param after
	 * @return
	 */
	public static boolean isSameMonth(Calendar before, Calendar after) {
		return isSameMonth(before.getTime(), after.getTime());
	}

	/**
	 * Lấy số ngày giữa hai ngày
	 * 
	 * @param before
	 * @param after
	 * @return
	 */
	public static double getDistanceDays(Date before, Date after) {
		return ((double)getDistanceMillis(before, after)) / MILLIS_PER_DAY;
	}

	/**
	 * Lấy số ngày giữa hai ngày
	 *
	 * @param before
	 * @param after
	 * @return
	 */
	public static double getDistanceDays(Calendar before, Calendar after) {
		return ((double)getDistanceMillis(before.getTime(), after.getTime())) / MILLIS_PER_DAY;
	}

	/**
	 * Lấy số mili giây giữa hai ngày
	 * @param before
	 * @param after
	 * @return
	 */
	public static long getDistanceMillis(Date before, Date after) {
		long beforeTime = before.getTime();
		long afterTime = after.getTime();
		return afterTime - beforeTime;
	}

	/**
	 * Lấy số tháng giữa hai ngày
	 *
	 * @param before
	 * @param after
	 * @return
	 */
	public static double getDistanceMonths(Date before, Date after) {
        double month = 0;
		// LấyThời gian bắt đầukhiTháng của cuối cùng một ngày
		Date endDay = getEndDayOfMonth(before);
		// LấyThời gian kết thúckhiTháng của Dòng một ngày
		Date startDay = getStartDayOfMonth(after);
		// Ngày bắt đầu và Ngày kết thúctạicùngmộtThángtrong
		if (isSameMonth(before, after)) {
            month = getDistanceSameMonth(before, after);
			return month;
		} else {
			// LấyThời gian bắt đầu----Thời gian bắt đầucuối cùng một ngàygiữa của Số tháng
			double startMonth = getDistanceSameMonth(before, endDay);

			// LấyThời gian kết thúc----Thời gian kết thúcDòng một ngàygiữa của Số tháng
			double endMonth = getDistanceSameMonth(startDay, after);

			// LấyThời gian bắt đầukhiThángDòng một ngày,Số tháng + 1(nguyênThángTính, Thời gian bắt đầu)
			Date startDayOfMonth = getStartDayOfMonth(before);
			Calendar startDayMonth = Calendar.getInstance();
			startDayMonth.setTime(startDayOfMonth);
			startDayMonth.add(Calendar.MONTH, 1);

			// LấyThời gian kết thúckhiThángcuối cùng một ngày, Số tháng - 1(nguyênThángTính, Thời gian kết thúc)
			Date endDayOfMonth = getEndDayOfMonth(after);
			Calendar endDayMonth = Calendar.getInstance();
			endDayMonth.setTime(endDayOfMonth);
			endDayMonth.add(Calendar.MONTH, -1);

			// LấyhaiCáiNgàygiữa của Số tháng(Làm tròn)
			int distanceMonth = getDistance(startDayMonth.getTime(), endDayMonth.getTime(), Calendar.MONTH);
			month = startMonth + endMonth + distanceMonth;
			return month;
		}
	}
	/**
	 * Lấy số tháng giữa hai ngày
	 *
	 * @param before
	 * @param after
	 * @return
	 */
	public static double getDistanceMonths(Calendar before, Calendar after) {
		return getDistanceMonths(before.getTime(), after.getTime());
	}

	/**
	 * Lấy số năm giữa hai ngày
	 * 
	 * @param before
	 * @param after
	 * @return
	 */
	public static double getDistanceYears(Date before, Date after) {
		double year = 0;
		// LấyThời gian bắt đầukhiNăm của cuối cùng một ngày
		Date endDay = getEndDayOfYear(before);
		// LấyThời gian kết thúckhiNăm của Dòng một ngày
		Date startDay = getStartDayOfYear(after);
		// Ngày bắt đầu và Ngày kết thúctạicùngmộtNămtrong
		if (isSameYear(before, after)) {
			year = getDistanceSameYear(before, after);
			return year;
		} else { // vượtNămTính
			// LấyThời gian bắt đầu----Thời gian bắt đầucuối cùng một ngàygiữa của Nămsố
			double startYear = getDistanceSameYear(before, endDay);

			// LấyThời gian kết thúc----Thời gian kết thúcDòng một ngàygiữa của Nămsố
			double endYear = getDistanceSameYear(startDay, after);

			// LấyThời gian bắt đầuDòng một ngày,Nămsố + 1(nguyênNămTính, Thời gian bắt đầu)
			Date startDayOfYear = getStartDayOfYear(before);
			Calendar startDayYear = Calendar.getInstance();
			startDayYear.setTime(startDayOfYear);

			// LấyThời gian kết thúccuối cùng một ngày, Nămsố - 1(nguyênNămTính, Thời gian kết thúc)
			Date endDayOfYear = getEndDayOfYear(after);
			Calendar endDayYear = Calendar.getInstance();
			endDayYear.setTime(endDayOfYear);

			int distanceYear = 0;
			// haiCáiNgày là tương邻Năm, khôngCần trong giữavượtNămTính
			if (endDayYear.get(Calendar.YEAR) - startDayYear.get(Calendar.YEAR) > 1) {
				startDayYear.add(Calendar.YEAR, 1);
				endDayYear.add(Calendar.YEAR, -1);
				// LấyhaiCáiNgàygiữa của Nămsốsố(Làm tròn)
				distanceYear = getDistance(startDayYear.getTime(), endDayYear.getTime(), Calendar.YEAR);
			}
			year = startYear + endYear + distanceYear;
			return year;
		}
	}

	/**
	 * Lấy số năm giữa hai ngày
	 *
	 * @param before
	 * @param after
	 * @return
	 */
	public static double getDistanceYears(Calendar before, Calendar after) {
		return getDistanceYears(before.getTime(), after.getTime());
	}

	/**
	 * LấycùngNăm trong haiCáiNgàygiữa của Nămsố
	 * @param before
	 * @param after
	 * @return
	 */
	public static double getDistanceSameYear(Date before, Date after) {
		// Lấy số mili giây giữa hai ngày
		long millis = getDistanceMillis(before, after);
		// LấykhiNăm của Năm ngàysố
		double daysYear = getDaysOfTheYear(before);
		// Thời gian kết thúc---》Thời gian kết thúcDòng một ngàyNămsố
		return (double)millis / MILLIS_PER_DAY / daysYear;
	}

	/**
	 * LấycùngTháng trong haiCáiNgàygiữa của Số tháng
	 * @param before
	 * @param after
	 * @return
	 */
	public static double getDistanceSameMonth(Date before, Date after) {
		// Lấy số mili giây giữa hai ngày
		long millis = getDistanceMillis(before, after);
		// LấykhiTháng của Tháng ngàysố
		double daysMonth = getDaysOfTheMonth(before);
		// Thời gian kết thúc---》Thời gian kết thúckhiThángDòng một ngàySố tháng
		return (double)millis / MILLIS_PER_DAY / daysMonth;
	}

	/**
	 * Lấy ngày và giờ rạng sáng của ngày
	 * 
	 * @param date
	 * @return
	 */
	public static Date getZeroTimeOfDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		setMinTimeOfDay(calendar);
		return calendar.getTime();
	}

	/**
	 * Lấy ngày và giờ rạng sáng của ngày
	 *
	 * @param calendar
	 * @return
	 */
	public static Date getZeroTimeOfDate(Calendar calendar) {
		return getZeroTimeOfDate(calendar.getTime());
	}
	
	/**
	 * Lấy ngày và giờ nửa đêm trong ngày của ngày
	 *
	 * @param date
	 * @return
	 */
	public static Date getEndTimeOfDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		setMaxTimeOfDay(calendar);
		return calendar.getTime();
	}

	/**
	 * Lấy ngày và giờ nửa đêm trong ngày của ngày
	 *
	 * @param calendar
	 * @return
	 */
	public static Date getEndTimeOfDate(Calendar calendar) {
		return getEndTimeOfDate(calendar.getTime());
	}
	
	/**
	 * Lấy ngày và giờ bắt đầu trong tháng của ngày
	 * 
	 * @param date
	 * @return
	 */
	public static Date getStartDayOfMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),1);
		setMinTimeOfDay(calendar);
		return calendar.getTime();
	}

	/**
	 * Lấy ngày và giờ bắt đầu trong tháng của ngày
	 *
	 * @param calendar
	 * @return
	 */
	public static Date getStartDayOfMonth(Calendar calendar) {
		return getStartDayOfMonth(calendar.getTime());
	}

	/**
	 * Lấy ngày và giờ kết thúc trong tháng của ngày
	 * 
	 * @param date
	 * @return
	 */
	public static Date getEndDayOfMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int maxMonthDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),maxMonthDay);
		setMaxTimeOfDay(calendar);
		return calendar.getTime();
	}

	/**
	 * Lấy ngày và giờ kết thúc trong tháng của ngày
	 *
	 * @param calendar
	 * @return
	 */
	public static Date getEndDayOfMonth(Calendar calendar) {
		return getEndDayOfMonth(calendar.getTime());
	}

	/**
	 * Lấy ngày và giờ bắt đầu của tháng sau của ngày
	 *
	 * @param date
	 * @return
	 */
	public static Date getStartDayOfNextMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH,1);
		calendar.add(Calendar.MONTH, 1);
		setMinTimeOfDay(calendar);
		return calendar.getTime();
	}

	/**
	 * Lấy ngày và giờ bắt đầu của tháng sau của ngày
	 *
	 * @param calendar
	 * @return
	 */
	public static Date getStartDayOfNextMonth(Calendar calendar) {
		return getStartDayOfNextMonth(calendar.getTime());
	}

	/**
	 * Lấy ngày và giờ kết thúc của tháng trước của ngày
	 *
	 * @param date
	 * @return
	 */
	public static Date getEndDayOfPreviousMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, -1);
		int maxMonthDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),maxMonthDay);
		setMaxTimeOfDay(calendar);
		return calendar.getTime();
	}

	/**
	 * Lấy ngày và giờ kết thúc của tháng trước của ngày
	 *
	 * @param calendar
	 * @return
	 */
	public static Date getEndDayOfPreviousMonth(Calendar calendar) {
		return getEndDayOfPreviousMonth(calendar.getTime());
	}

	/**
	 * Kiểm tra ngày hiện tại có phải ngày đầu tháng không
	 *
	 * @param date
	 * @return
	 */
	public static boolean isFirstDayOfMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_MONTH) == 1;
	}

	/**
	 * Kiểm tra ngày hiện tại có phải ngày đầu tháng không
	 *
	 * @param calendar
	 * @return
	 */
	public static boolean isFirstDayOfMonth(Calendar calendar) {
		return isFirstDayOfMonth(calendar.getTime());
	}

	/**
	 * Kiểm tra ngày hiện tại có phải ngày cuối tháng không
	 *
	 * @param date
	 * @return
	 */
	public static boolean isLastDayOfMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_MONTH) == calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Kiểm tra ngày hiện tại có phải ngày cuối tháng không
	 *
	 * @param calendar
	 * @return
	 */
	public static boolean isLastDayOfMonth(Calendar calendar) {
		return isLastDayOfMonth(calendar.getTime());
	}

	/**
	 * Lấy ngày và giờ bắt đầu trong năm của ngày
	 * @param date
	 * @return
	 */
	public static Date getStartDayOfYear(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		// chú ýThángphần cần trừ1
		calendar.set(calendar.get(Calendar.YEAR),0,1);
		setMinTimeOfDay(calendar);
		return calendar.getTime();
	}

	/**
	 * Lấy ngày và giờ bắt đầu trong năm của ngày
	 * @param calendar
	 * @return
	 */
	public static Date getStartDayOfYear(Calendar calendar) {
		return getStartDayOfYear(calendar.getTime());
	}
	
	/**
	 * Lấy ngày và giờ kết thúc trong năm của ngày
	 * 
	 * @param date
	 * @return
	 */
	public static Date getEndDayOfYear(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		// chú ýThángphần cần trừ1
		calendar.set(calendar.get(Calendar.YEAR),11,31);
		setMaxTimeOfDay(calendar);
		return calendar.getTime();
	}

	/**
	 * Lấy ngày và giờ kết thúc trong năm của ngày
	 *
	 * @param calendar
	 * @return
	 */
	public static Date getEndDayOfYear(Calendar calendar) {
		return getEndDayOfYear(calendar.getTime());
	}
	
	/**
	 * Lấy số ngày trong cả năm chứa ngày đó
	 * 
	 * @param date
	 * @return
	 */
	public static int getDaysOfTheYear(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
	}

	/**
	 * Lấy số ngày trong cả năm chứa ngày đó
	 *
	 * @param calendar
	 * @return
	 */
	public static int getDaysOfTheYear(Calendar calendar) {
		return calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
	}
	
	/**
	 * Lấy số ngày trong cả tháng chứa ngày đó
	 * 
	 * @param date
	 * @return
	 */
	public static int getDaysOfTheMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Lấy số ngày trong cả tháng chứa ngày đó
	 *
	 * @param calendar
	 * @return
	 */
	public static int getDaysOfTheMonth(Calendar calendar) {
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	/**
	 * LấyhaiCáiNgàygiữa của Nămsố, Số tháng(Làm tròn)
	 * @param before
	 * @param after
	 * @param dateField (YEAR: 1, MONTH: 2)
	 * @return
	 */
	public static int getDistance(Date before, Date after, int dateField) {
		Calendar calBefore = Calendar.getInstance();
		calBefore.setTime(before);
		Calendar calAfter = Calendar.getInstance();
		calAfter.setTime(after);

		// dateField (1: (TínhhaiNgàygiữa của Nămsố), 2: (TínhhaiNgàygiữa của Số tháng))
		if (dateField == Calendar.YEAR) {
			// Ngày kết thúc: khiNămcuối cùng một ngày, cần+1tiến hành dòngTính
			return calAfter.get(Calendar.YEAR) - calBefore.get(Calendar.YEAR) + 1;
		} else if (dateField == Calendar.MONTH) {
			int beforeMonth = calBefore.get(Calendar.YEAR) * 12 + calBefore.get(Calendar.MONTH);
			// Ngày kết thúc: khiThángcuối cùng một ngày, cần+1tiến hành dòngTính
			int afterMonth = calAfter.get(Calendar.YEAR) * 12 + calAfter.get(Calendar.MONTH) + 1;

			return afterMonth - beforeMonth;
		}

		return 0;
	}
	/**
	 * Trả vềnhỏNgày
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static Date min(Date date1, Date date2) {
		return date1 != null && date1.compareTo(date2) == 1 ? date2 : date1;
	}

	/**
	 * Trả vềlớnNgày
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static Date max(Date date1, Date date2) {
		return date1 != null && date1.compareTo(date2) == 1 ? date1 : date2;
	}
	
	
	/**
	 * ĐặtNgày của Thời gian bắt đầu
	 * @param calendar
	 */
	private static void setMinTimeOfDay(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 00);
		calendar.set(Calendar.SECOND, 00);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.MILLISECOND, 000);
	}

	/**
	 * ĐặtNgày của Thời gian kết thúc
	 * @param calendar
	 */
	private static void setMaxTimeOfDay(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.MILLISECOND, 999);
	}
		
}
