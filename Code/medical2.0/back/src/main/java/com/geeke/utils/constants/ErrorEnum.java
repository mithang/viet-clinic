package com.geeke.utils.constants;

/**
 * @author: lys
 * @date: 2017/10/24 10:16
 */
public enum ErrorEnum {
	
	/*
	 * cảnh báoLớpThông tin
	 */
	E_50001("50001", "LưuDữ liệutrùng lặp"),

    //by glh 20230722
    E_50002("60001", "违反nghiệp vụquy tắc"),
	
    /*
     * Thông tin lỗi
     * */
    E_400("400", "Vui lòngyêu cầuXử lýngoại lệ, Vui lòng稍saulại试"),
    E_500("500", "Vui lòngyêu cầucáchcósai,Vui lòng kiểm tra GET/POST"),
    E_501("501", "Vui lòngyêu cầuđường dẫnKhông tồn tại"),
    E_502("502", "Quyềnkhông đủ"),
    E_10008("10008", "Vai tròXóaThất bại,尚cóNgười dùngthuộctạinàyVai trò"),
    E_10009("10009", "tài khoảnĐã tồn tại"),
    E_10010("10010", "Đăng nhậpTài khoản hoặc Mật khẩuLỗi"),
    E_10011("10011", "Tài khoảnĐãVô hiệu hóa"),
    E_10012("10012", "tài khoảnXác thựcThất bại"),
    
    E_20011("20011", "đăng nhậpĐãhết hạn hoặc ChưaĐăng nhập,Vui lòngLạiđăng nhập"),
    
    E_30001("30001", "luồngquy trìnhenginengoại lệ"),
    E_30002("30002", "Cảnh báo của engine quy trình"),

    E_60001("60001", "Chứa ký tự không hợp lệ, Vui lòngyêu cầuLỗi"),

    // Dataease giao diệnLỗi
    E_80001("80001", "Dataease giao diệnLỗi"),
    E_80002("80002", "Dataease Dữ liệuMã hóaThất bại"),

    E_90003("90003", "thiếubắt buộcTham số");
	
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
