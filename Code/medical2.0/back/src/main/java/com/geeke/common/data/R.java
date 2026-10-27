package com.geeke.common.data;

import com.geeke.utils.constants.Constants;
import com.geeke.utils.constants.ErrorEnum;

/**
 * Đóng gói generic thống nhất cho thân phản hồi
 * từng步thaythay ResultUtil Trả về của  JSONObject, cung cấpLoạian toàn của phản hồicấu trúc. 
 *
 * <p>Ví dụ sử dụng:
 * <pre>
 *   // Thành công
 *   return ResponseEntity.ok(R.ok(entity));
 *   return ResponseEntity.ok(R.ok("LưuThành công", id));
 *
 *   // Cảnh báo nghiệp vụ(biểu mẫuKiểm traThất bại v.v. )
 *   return ResponseEntity.ok(R.warning(ErrorEnum.E_50001));
 *
 *   // Lỗi hệ thống
 *   return ResponseEntity.ok(R.error(ErrorEnum.E_400));
 * </pre>
 *
 * @param <T> phản hồiDữ liệuLoại
 */
public class R<T> {

    private String code;
    private String msg;
    private String type;
    private T data;

    public R() {
    }

    public R(String code, String msg, String type, T data) {
        this.code = code;
        this.msg = msg;
        this.type = type;
        this.data = data;
    }

    // ==================== Thành côngphản hồi ====================

    /**
     * Thành công(khôngDữ liệu)
     */
    public static <T> R<T> ok() {
        return new R<>(Constants.SUCCESS_CODE, Constants.SUCCESS_MSG, Constants.MSG_TYPE_SUCCESS, null);
    }

    /**
     * Thành công(kèmDữ liệu)
     */
    public static <T> R<T> ok(T data) {
        return new R<>(Constants.SUCCESS_CODE, Constants.SUCCESS_MSG, Constants.MSG_TYPE_SUCCESS, data);
    }

    /**
     * Thành công(Thông báo tùy chỉnh + Dữ liệu)
     */
    public static <T> R<T> ok(String msg, T data) {
        return new R<>(Constants.SUCCESS_CODE, msg, Constants.MSG_TYPE_SUCCESS, data);
    }

    /**
     * bản nhápLưuThành công
     */
    public static <T> R<T> okDraft(T data) {
        return new R<>(Constants.SUCCESS_CODE, Constants.SUCCESS_SAVE_DRAFT, Constants.MSG_TYPE_SUCCESS, data);
    }

    // ==================== cảnh báophản hồi ====================

    /**
     * Cảnh báo nghiệp vụ(Sử dụngErrorEnumMặc địnhthông báo)
     */
    public static <T> R<T> warning(ErrorEnum errorEnum) {
        return new R<>(String.valueOf(errorEnum.getErrorCode()), errorEnum.getErrorMsg(), Constants.MSG_TYPE_WARNING, null);
    }

    /**
     * Cảnh báo nghiệp vụ(Thông báo tùy chỉnh)
     */
    public static <T> R<T> warning(ErrorEnum errorEnum, String msg) {
        return new R<>(String.valueOf(errorEnum.getErrorCode()), msg, Constants.MSG_TYPE_WARNING, null);
    }

    // ==================== Lỗiphản hồi ====================

    /**
     * Lỗi hệ thống(Sử dụngErrorEnumMặc địnhthông báo)
     */
    public static <T> R<T> error(ErrorEnum errorEnum) {
        return new R<>(String.valueOf(errorEnum.getErrorCode()), errorEnum.getErrorMsg(), Constants.MSG_TYPE_ERROR, null);
    }

    /**
     * Lỗi hệ thống(Thông báo tùy chỉnh)
     */
    public static <T> R<T> error(ErrorEnum errorEnum, String msg) {
        return new R<>(String.valueOf(errorEnum.getErrorCode()), msg, Constants.MSG_TYPE_ERROR, null);
    }

    // ==================== Getter/Setter ====================

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
