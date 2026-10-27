package com.geeke.org.entity;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Quản lý tenantEntity
 * @author txl
 * @version 2022-05-23
 */
public class Lessee extends DataEntity<Lessee> {

	private static final long serialVersionUID = 985180319911059468L;
	private String userName;		// tenanttên
	private String phoneNumber;		// Số điện thoạisố
	private String company;		// công tyTên
	private String contactName;		// người liên hệTên
	private String addressProvince;		// Địa chỉ(Tỉnh)
	private String addressCity;		// Địa chỉ(Thành phố)
	private String addressRegion;		// Địa chỉ(Quận)
	private String address;		// liên hệĐịa chỉ
	private String isUse;		// Trạng thái

	
	public Lessee() {
		super();
	}

	public Lessee(String id){
		super(id);
	}
	

	@Length(min=1, max=100, message="tenanttênĐộ dài phải nằm trong khoảng 1 đến 100 ")
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	
	@Length(min=1, max=20, message="Số điện thoạisốĐộ dài phải nằm trong khoảng 1 đến 20 ")
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}


	@Length(min=0, max=50, message="người liên hệTênĐộ dài phải nằm trong khoảng 0 đến 50 ")
	public String getContactName() {
		return contactName;
	}
	public void setContactName(String contactName) {
		this.contactName = contactName;
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
	
	
	@Length(min=0, max=100, message="liên hệĐộ dài địa chỉ phải nằm trong khoảng 0 đến 100 ")
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
	
	@Length(min=1, max=1, message="Độ dài trạng thái phải nằm trong khoảng 1 đến 1 ")
	public String getIsUse() {
		return isUse;
	}
	public void setIsUse(String isUse) {
		this.isUse = isUse;
	}
	
	
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "lessee";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "985180319911059468";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}