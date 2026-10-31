package com.geeke.medicareutils.domain.respo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * nhân viên hành nghềThông tin
 */
@Data
public class MdStaffData {
    private String psnCertType;//Loại giấy tờ nhân viên
    private String certno;//Số giấy tờ
    private String pracPsnNo;//hành nghềnghiệp vụMã nhân viên
    private String pracPsnCode;//Mã nhân viên hành nghề
    private String pracPsnName;//hành nghềnghiệp vụTên nhân viên
    private String pracPsnType;//nhân viên hành nghề phútLớp
    private String pracPsnCert;//Chứng chỉ hành nghề của nhân viên hành nghềMã
    private String pracCertNo;//hành nghềnghiệp vụchứngsáchmã số
    private String hiDrFlag;//BHYTBác sĩcờ
    private LocalDateTime begntime;//Thời gian bắt đầu
    private LocalDateTime endtime;//Thời gian kết thúc
    private String chgRea;//thay đổigốcdo

}
