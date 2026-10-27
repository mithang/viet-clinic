package com.geeke.cure.entity;

import com.geeke.org.entity.Company;
import javax.validation.constraints.NotNull;

import com.geeke.outpatient.entity.Patient;
import com.geeke.outpatient.entity.RecipelDetail;
import com.geeke.outpatient.entity.RecipelInfo;
import com.geeke.outpatient.entity.Registration;
import com.geeke.treatment.entity.CostItem;
import org.hibernate.validator.constraints.Length;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Xét nghiệm và kiểm traEntity
 * @author rys
 * @version 2022-10-18
 */
public class InspectionCheck extends DataEntity<InspectionCheck> {

	private static final long serialVersionUID = 1212102786706588488L;
	private Company company;      // Phòng khámid 
	private Patient patient;		// Bệnh nhânid
	private Registration registration;		// Thông tin đăng kýid
	private CostItem costItem;		// Hạng mục khám chữa bệnhid
	private String type;		// Loại(0: Xét nghiệm; 1: Kiểm tra)
	private String status;		// điềnTrạng thái(0: Chưađiền; 1: Đãđiền)
	private String patientName;		// Tên bệnh nhân
	private String sex;		// Giới tính
	private String phone;		// số điện thoại di động
	private String diagnose;		// Chẩn đoán
	private String completeBy;		// lập đơnBác sĩ
	private Date completeDate;		// Thời gian lập phiếu
	private RecipelInfo recipelInfo;  //Đơn thuốc
	private RecipelDetail recipelDetail;  //Chi tiết đơn thuốc

	
	public InspectionCheck() {
		super();
	}

	public InspectionCheck(String id){
		super(id);
	}
	

	@NotNull(message="Phòng khámidKhông được để trống")
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public Registration getRegistration() {
		return registration;
	}

	public void setRegistration(Registration registration) {
		this.registration = registration;
	}

	public CostItem getCostItem() {
		return costItem;
	}

	public void setCostItem(CostItem costItem) {
		this.costItem = costItem;
	}

	@Length(min=1, max=1, message="Loại(0: Xét nghiệm; 1: Kiểm tra)Độ dài phải từ 1  và  1 Giữa")
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	
	@Length(min=1, max=1, message="điềnTrạng thái(0: Chưađiền; 1: Đãđiền)Độ dài phải từ 1  và  1 Giữa")
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	
	@Length(min=0, max=100, message="Bệnh nhânTênĐộ dài phải nằm trong khoảng 0 đến 100 ")
	public String getPatientName() {
		return patientName;
	}
	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}
	
	
	@Length(min=0, max=100, message="Giới tínhĐộ dài phải nằm trong khoảng 0 đến 100 ")
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	
	
	@Length(min=0, max=100, message="số điện thoại di độngĐộ dài phải nằm trong khoảng 0 đến 100 ")
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	
	@Length(min=0, max=100, message="Độ dài chẩn đoán phải nằm trong khoảng 0 đến 100 ")
	public String getDiagnose() {
		return diagnose;
	}
	public void setDiagnose(String diagnose) {
		this.diagnose = diagnose;
	}
	
	
	@Length(min=0, max=100, message="lập đơnBác sĩĐộ dài phải nằm trong khoảng 0 đến 100 ")
	public String getCompleteBy() {
		return completeBy;
	}
	public void setCompleteBy(String completeBy) {
		this.completeBy = completeBy;
	}
	
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getCompleteDate() {
		return completeDate;
	}
	public void setCompleteDate(Date completeDate) {
		this.completeDate = completeDate;
	}

	public RecipelInfo getRecipelInfo() {
		return recipelInfo;
	}

	public void setRecipelInfo(RecipelInfo recipelInfo) {
		this.recipelInfo = recipelInfo;
	}

	public RecipelDetail getRecipelDetail() {
		return recipelDetail;
	}

	public void setRecipelDetail(RecipelDetail recipelDetail) {
		this.recipelDetail = recipelDetail;
	}

	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "inspection_check";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "1212102786706588488";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}