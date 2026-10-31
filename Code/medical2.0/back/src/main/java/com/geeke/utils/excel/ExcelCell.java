package com.geeke.utils.excel;

import java.lang.annotation.*;

/**
 * 
 * @author skydu
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelCell {

	String name() default "";

	/**
	 * Định dạng ngày
	 * @return
	 */
	String dateFormat() default "yyyy-MM-dd HH:mm:ss";

	/**
	 * liên kếtTrường
	 * @return
	 */
	boolean hyperLink() default false;

	/**
	 * chuyển đổi của key, NếuCần, cầntạiExcelCellProcessor trong đăng ký
	 *  là gìkhôngdùng name, Nếu chẳng may trùng lặp sẽ có nhiều vấn đề, hiệntạicũngkhôngThời giannghĩđógìnhiều, Dự án sắp đưa lên productionrồi
	 * @return
	 */
	String convertKey() default "";
}
