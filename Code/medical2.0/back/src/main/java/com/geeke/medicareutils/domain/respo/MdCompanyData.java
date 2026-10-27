package com.geeke.medicareutils.domain.respo;

import com.geeke.org.entity.ClinicOffice;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Thông tin khoa
 */
@Data
public class MdCompanyData {
    private String hospDeptCodg;//Bệnh việnKhoaMã
    private String hospDeptName;//Bệnh việnTên khoa
    private LocalDateTime begntime;//Thời gian bắt đầu
    private LocalDateTime endtime;//Thời gian kết thúc
    private String itro;         //giới thiệu
    private String deptResperName;//Khoangười phụ trách
    private String deptResperTel;//Khoangười phụ tráchSố điện thoại
    private String deptMedServScp;
    private String caty;//Khoa
    private LocalDateTime deptEstbdat;//KhoathànhlậpNgày
    private String aprvBedCnt;//lôchuẩngiườngvịSố lượng
    private String hiCrtfBedCnt;//BHYTnhậncó thểgiườngvịsố
    private String poolareaNo;//Mã vùng điều phối
    private String drPsncnt;//Bác sĩsố người
    private String pharPsncnt;//Dược sĩsố người
    private String nursPsncnt;//Y tásố người
    private String tecnPsncnt;//kỹ thuật viênsố người
    private String memo;//Ghi chú
}
