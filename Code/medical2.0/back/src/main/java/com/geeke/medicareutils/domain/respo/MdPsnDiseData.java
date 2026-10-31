package com.geeke.medicareutils.domain.respo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Thông tin chẩn đoán
 */
@Data
public class MdPsnDiseData {
    /** Thông tin chẩn đoán ID */
    private String diagInfoId;  // Kiểu ký tự, 30Ký tự

    /** Khám bệnh ID */
    private String mdtrtId;  // Kiểu ký tự, 30Ký tự

    /** Mã nhân viên */
    private String psnNo;  // Kiểu ký tự, 30Ký tự

    /** ranhập việnLoại chẩn đoán */
    private String inoutDiagType;  // Kiểu ký tự, 3Ký tự

    /** Loại chẩn đoán */
    private String diagType;  // Kiểu ký tự, 3Ký tự

    /** Cờ chẩn đoán chính */
    private String maindiagFlag;  // Kiểu ký tự, 3Ký tự

    /** Chẩn đoánxếpsố thứ tự */
    private Integer diagSrtNo;  // Kiểu số, 2Ký tự

    /** Mã chẩn đoán */
    private String diagCode;  // Kiểu ký tự, 20Ký tự

    /** Tên chẩn đoán */
    private String diagName;  // Kiểu ký tự, 100Ký tự

    /** nhập việnbệnhtình */
    private String admCond;  // Kiểu ký tự, 500Ký tự

    /** Chẩn đoánKhoa */
    private String diagDept;  // Kiểu ký tự, 50Ký tự

    /** Chẩn đoánBác sĩMã */
    private String diseDorNo;  // Kiểu ký tự, 30Ký tự

    /** Chẩn đoánBác sĩTên */
    private String diseDorName;  // Kiểu ký tự, 50Ký tự

    /** Thời gian chẩn đoán */
    private LocalDateTime diagTime;  // Kiểu ngày giờ, Định dạng: yyyy-MM-dd HH:mm:ss

    /** Người thực hiện ID */
    private String opterId;  // Kiểu ký tự, 20Ký tự

    /** Tên người thực hiện */
    private String opterName;  // Kiểu ký tự, 50Ký tự

    /** qualàmThời gian */
    private LocalDateTime optTime;  // Kiểu ngày giờ, Định dạng: yyyy-MM-dd HH:mm:ss
}
