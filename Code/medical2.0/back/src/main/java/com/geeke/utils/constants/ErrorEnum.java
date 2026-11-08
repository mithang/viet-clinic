package com.geeke.utils.constants;

/**
 * @author: lys
 * @date: 2017/10/24 10:16
 */
public enum ErrorEnum {
	
	/*
	 * cảnh báoLớpThông tin
	 */
	E_50001("50001", "Lưu Dữ liệu trùng lặp"),

    //by glh 20230722
    E_50002("60001", "Vi phạm quy tắc nghiệp vụ"),
	
    /*
     * Thông tin lỗi
     * */
    E_400("400", "Xử lý yêu cầu gặp ngoại lệ, vui lòng thử lại sau"),
    E_415("415", "Loại nội dung yêu cầu không được hỗ trợ"),
    E_500("500", "Vui lòngyêu cầucáchcósai,Vui lòng kiểm tra GET/POST"),
    E_501("501", "Vui lòng yêu cầu đường dẫn Không tồn tại"),
    E_502("502", "Quyềnkhông đủ"),
    E_10008("10008", "Xóa vai trò thất bại, vẫn còn người dùng thuộc vai trò này"),
    E_10009("10009", "tài khoản Đã tồn tại"),
    E_10010("10010", "Đăng nhập Tài khoản hoặc Mật khẩu Lỗi"),
    E_10011("10011", "Tài khoản Đã Vô hiệu hóa"),
    E_10012("10012", "tài khoản Xác thực Thất bại"),
    
    E_20011("20011", "đăng nhập Đã hết hạn hoặc Chưa Đăng nhập, Vui lòng Lại đăng nhập"),
    
    E_30001("30001", "luồngquy trìnhenginengoại lệ"),
    E_30002("30002", "Cảnh báo của engine quy trình"),

    E_60001("60001", "Chứa ký tự không hợp lệ, Vui lòng yêu cầu Lỗi"),

    // Dataease giao diệnLỗi
    E_80001("80001", "Dataease giao diện Lỗi"),
    E_80002("80002", "Dataease Dữ liệu Mã hóa Thất bại"),

    E_90003("90003", "thiếu bắt buộc Tham số");
	
    private String errorCode;

    private String errorMsg;

    ErrorEnum() {
    }

    ErrorEnum(String errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
