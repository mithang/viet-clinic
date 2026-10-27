package com.geeke.org.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.clinic.entity.ClinicVersion;
import com.geeke.common.persistence.TreeEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.util.Date;
/**
 * Quản lý công tyEntity
 * @author lys
 * @version 2022-05-25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Company extends TreeEntity<Company> {

	private static final long serialVersionUID = 41040096140492800L;
	private String fullName;		// toàn bộgọi là
	private String code;		// Mã
	private String addressProvince;		// Địa chỉ(Tỉnh)
	private String addressCity;		// Địa chỉ(Thành phố)
	private String addressRegion;		// Địa chỉ(Quận)
	private String address;		// Địa chỉ
	private String phoneNumber;		// Điện thoại
	private String licenseSubject;		// Môn học trong giấy phép hành nghề
	private String fileId;		// Ảnh giấy phép phòng khám
	private Company lesseeId;      // Đơn vị thuê
	private String contactName;
	private String tenantryId; // Đơn vị thuêid
    //Cấu hình BHYT
	private  String fixmedinsCode;// y tế được chỉ địnhCơ sởmã
	private  String fixmedinsName; //Tên cơ sở y tế được chỉ định
	private  String mdtrtareaAdmvs;//thìyđịa phươngQuậnMã
	//điện tửĐơn thuốccấu hình
	private String appId; // appid
	private String appSecret; //khóa bí mật
	private String privateKey; //khóa riêng
	private String publicKey;  //公钥





	public String getTenantryId() {
		return tenantryId;
	}

	public void setTenantryId(String tenantryId) {
		this.tenantryId = tenantryId;
	}

	public Company getParentId() {
		return parentId;
	}

	public void setParentId(Company parentId) {
		this.parentId = parentId;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	private Company parentId;    //zuhu id
	private ClinicVersion version;      // Phiên bản phòng khám
	private Integer status;		// Trạng thái 1Kích hoạt 0dừngdùng
	private Integer isInstitution;		// Có hay không là Cơ sở 1 có  0không có 
	private Date startUseDate;		// Phòng khámBậtThời gian
	private Date expireDate;		// Phòng khámĐếnkỳThời gian

	
	public Company() {
		super();
	}

	public Company(String id){
		super(id);
	}
	

	@Length(min=1, max=512, message="toàn bộgọi làĐộ dài phải nằm trong khoảng 1 đến 512 ")
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	
	@Length(min=1, max=64, message="Độ dài mã phải nằm trong khoảng 1 đến 64 ")
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	
	@Length(min=1, max=10, message="Địa chỉ(Tỉnh)Độ dài phải từ 1  và  10 Giữa")
	public String getAddressProvince() {
		return addressProvince;
	}
	public void setAddressProvince(String addressProvince) {
		this.addressProvince = addressProvince;
	}
	
	
	@Length(min=1, max=10, message="Địa chỉ(Thành phố)Độ dài phải từ 1  và  10 Giữa")
	public String getAddressCity() {
		return addressCity;
	}
	public void setAddressCity(String addressCity) {
		this.addressCity = addressCity;
	}
	
	
	@Length(min=1, max=10, message="Địa chỉ(Quận)Độ dài phải từ 1  và  10 Giữa")
	public String getAddressRegion() {
		return addressRegion;
	}
	public void setAddressRegion(String addressRegion) {
		this.addressRegion = addressRegion;
	}
	
	
	@Length(min=1, max=100, message="Độ dài địa chỉ phải nằm trong khoảng 1 đến 100 ")
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
	
	@Length(min=1, max=21, message="Độ dài số điện thoại phải nằm trong khoảng 1 đến 21 ")
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
	
	
    public ClinicVersion getVersion() {
        return version;
    }

    public void setVersion(ClinicVersion version) {
        this.version = version;
    }
    
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}

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

	@NotNull(message="tenantKhông thể là trống")
	public Company getLesseeId() {
		return lesseeId;
	}

	public void setLesseeId(Company lesseeId) {
		this.lesseeId = lesseeId;
	}
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "org_company";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "41040096140492800";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}
