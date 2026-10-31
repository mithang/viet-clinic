package com.geeke.outpatient.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * từ xaChẩn đoánQuản lý
 * @author zh
 * @version 2023-12-06
 */

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RemoteDiagnosisTreatment extends DataEntity<RemoteDiagnosisTreatment> {
    private static final long serialVersionUID = -7912591825814208871L;
    private Company company;        // Phòng khámid
    private String applyId;  // Số thứ tự khám chữa bệnh từ xa(dohisHệ thốngTạo)
    private String registrationId;  // Đăng ký khámid
    private String nameId;      // Bệnh nhânid
    private String name;        // Tên bệnh nhân
	private String gender;      // Giới tính
	private String card;		// Số CMND/CCCD
	private String hospital;	// đăng kýVui lòngBệnh viện
	private String hospitalId;	// đăng kýVui lòngBệnh viện
	private String department;	// đăng kýVui lòngKhoa
	private String departmentId;	// đăng kýVui lòngKhoa
	private String medic;		// đăng kýVui lòngBác sĩ
	private String medicId;		// đăng kýVui lòngBác sĩ
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime diagnosisTime; // khámtrị liệuThời gian
    private String status;		// Trạng thái
    private String medicationStatus;	// phát thuốcTrạng thái thu phí(0: thayThu phí 1: ĐãThu phíchờphát thuốc 2: Đãphát thuốc)
    private String applicationResults;	// đăng kýVui lòngKết quả
    private String recipientName;		// người nhậnTên
    private String recipientPhone;		// người nhậnSố điện thoại
    private String province;		// người nhậnTỉnh
    private String market;		// người nhậnThành phố
    private String distinguish;		// người nhậnQuậnhuyện
    private String recipientAddress;	// người nhậnĐịa chỉ
    private String conferenceId;		// sẽhội nghịsố
    private String cardNo;		// hiskhám bệnhthẻsố
    private String orderNo;		// Chi phíđặtđơnsố
    private String payFee;		// Chi phí

	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "remote_diagnosis";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "-7912591825814208871L";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}