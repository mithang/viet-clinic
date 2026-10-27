package com.geeke.org.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.clinic.entity.ClinicVersion;
import com.geeke.common.persistence.DataEntity;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.util.Date;
/**
 * Quản lý phòng khámEntity
 * @author txl
 * @version 2022-05-19
 */
@Data
public class Clinic extends DataEntity<Clinic> {

	private static final long serialVersionUID = 986498806151577752L;
	private Lessee lesseeId;      // Đơn vị thuê
	private String addressProvince;		// Địa chỉ(Tỉnh)
	private String addressCity;		// Địa chỉ(Thành phố)
	private String addressRegion;		// Địa chỉ(Quận)
	private String address;		// Địa chỉ
	private String phoneNumber;		// Điện thoại
	private String licenseSubject;		// Môn học trong giấy phép hành nghề
	private String fileId;		// Ảnh giấy phép phòng khám
	private ClinicVersion version;      // sản phẩmphiên bản 
	private Integer status;		// Trạng thái
	private Integer isInstitution;		// Có hay không là Cơ sở
	private Date startUseDate;		// Phòng khámThời gian bắt đầu
	private Date expireDate;		// Phòng khámĐếnkỳThời gian

	private  String fixmedinsCode;// y tế được chỉ địnhCơ sởmã

	
	public Clinic() {
		super();
	}

	public Clinic(String id){
		super(id);
	}
	
	public Clinic(Lessee lesseeId){
		this.lesseeId = lesseeId;
	}

	@NotNull(message="tenantKhông thể là trống")
    public Lessee getLesseeId() {
        return lesseeId;
    }

    public void setLesseeId(Lessee lesseeId) {
        this.lesseeId = lesseeId;
    }
    
	@Length(min=0, max=10, message="Địa chỉ(Tỉnh)Độ dài phải từ 0  và  10 Giữa")
	public String getAddressProvince() {
		return addressProvince;
	}
	public void setAddressProvince(String addressProvince) {
		this.addressProvince = addressProvince;
	}
	
	
	@Length(min=0, max=10, message="Địa chỉ(Thành phố)Độ dài phải từ 0  và  10 Giữa")
	public String getAddressCity() {
		return addressCity;
	}
	public void setAddressCity(String addressCity) {
		this.addressCity = addressCity;
	}
	
	
	@Length(min=0, max=10, message="Địa chỉ(Quận)Độ dài phải từ 0  và  10 Giữa")
	public String getAddressRegion() {
		return addressRegion;
	}
	public void setAddressRegion(String addressRegion) {
		this.addressRegion = addressRegion;
	}
	
	
	@Length(min=0, max=100, message="Độ dài địa chỉ phải nằm trong khoảng 0 đến 100 ")
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
	
	@Length(min=1, max=20, message="Độ dài số điện thoại phải nằm trong khoảng 1 đến 20 ")
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	
	@Length(min=0, max=100, message="Độ dài môn học trong giấy phép hành nghề phải nằm trong khoảng 0 đến 100 ")
	public String getLicenseSubject() {
		return licenseSubject;
	}
	public void setLicenseSubject(String licenseSubject) {
		this.licenseSubject = licenseSubject;
	}
	
	
	@Length(min=0, max=21, message="Độ dài ảnh giấy phép phòng khám phải nằm trong khoảng 0 đến 21 ")
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	
	
	@NotNull(message="sản phẩmphiên bảnKhông thể là trống")
    public ClinicVersion getVersion() {
        return version;
    }

    public void setVersion(ClinicVersion version) {
        this.version = version;
    }
    
	@NotNull(message="Trạng tháiKhông thể là trống")
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}

	@NotNull(message="Có hay không là Cơ sởKhông thể là trống")
	public Integer getIsInstitution() {
		return isInstitution;
	}
	public void setIsInstitution(Integer isInstitution) {
		this.isInstitution = isInstitution;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getStartUseDate() {
		return startUseDate;
	}
	public void setStartUseDate(Date startUseDate) {
		this.startUseDate = startUseDate;
	}
	
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getExpireDate() {
		return expireDate;
	}
	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}
	
	
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "clinic";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "986498806151577752";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}