package com.geeke.utils.excel;

import java.lang.annotation.*;

/**
 * ExcelNhậpTrườngánh xạannotation
 * 标注thực thểTrường và  Excel Cột của ánh xạquan hệ
 *
 * <p>Ví dụ sử dụng:</p>
 * <pre>{@code
 * public class Drug {
 *     @ExcelImportField(columnIndex = 0, required = true, maxLength = 128)
 *     private String name;
 *
 *     @ExcelImportField(columnIndex = 3, dictTypeId = "1004078055755374603")
 *     private DictItem type;
 * }
 * }</pre>
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelImportField {

    /** ExcelCộtsố(Từ 0Bắt đầu) */
    int columnIndex();

    /** Có hay khôngbắt buộc, Mặc địnhfalse */
    boolean required() default false;

    /** chuỗiĐộ dài tối đa, Mặc địnhkhônggiới hạn */
    int maxLength() default -1;

    /** Loại từ điểnID(Dùng để tự động phân tích mục từ điểnTênĐếnDictItem) */
    String dictTypeId() default "";

    /** Mặc địnhGiá trị(khiđơn đồngô là trống giờsử dụng) */
    String defaultValue() default "";

    /** Trường trong văn bảntên(dùng đểLỗiđề xuấthiển thị) */
    String label() default "";
}
