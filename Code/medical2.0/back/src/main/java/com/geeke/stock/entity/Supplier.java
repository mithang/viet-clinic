package com.geeke.stock.entity;

import com.geeke.org.entity.Company;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Quản lý nhà cung cấpEntity
 * @author txl
 * @version 2022-06-22
 */
public class Supplier extends DataEntity<Supplier> {

	private static final long serialVersionUID = 1005526731044757538L;
	private Company company;      // Phòng khámid 
	private String linkman;		// người liên hệ
	private String phone;		// Điện thoại
	private String mail;		// email
	private String province;		// Tỉnh
	private String city;		// Thành phố
	private String creditCode;		// tindùngmã
	private String area;		// Quận
	private String address;		// Địa chỉ
	private String status;      //Có bật không
	private String type;		//Nhà cung cấpLoạicờ
	private String pinyinCode;  //Mã pinyin
	
	public Supplier() {
		super();
	}

	public Supplier(String id){
		super(id);
	}
	

    // DS80: @Length(min=1) cho phep null, nen null qua @Valid roi 500 o cot name NOT NULL; override de fail-closed 400.
    // Khong them @NotNull vao DataEntity.getName vi nhieu entity con khong co cot name / name nullable.
    @Override
    @NotNull(message="Tên nhà cung cấp không được để trống")
    @Length(min=1, max=64, message="TênĐộ dài phải nằm trong khoảng 1 đến 64 ")
    public String getName() {
    	return super.getName();
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
	@Length(min=0, max=45, message="người liên hệĐộ dài phải nằm trong khoảng 0 đến 45 ")
	public String getLinkman() {
		return linkman;
	}
	public void setLinkman(String linkman) {
		this.linkman = linkman;
	}
	
	
	@Length(min=0, max=45, message="Độ dài số điện thoại phải nằm trong khoảng 0 đến 45 ")
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	
	@Length(min=0, max=128, message="emailĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getMail() {
		return mail;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}
	
	
	@Length(min=0, max=45, message="Độ dài tỉnh phải nằm trong khoảng 0 đến 45 ")
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	
	
	@Length(min=0, max=45, message="Độ dài thành phố phải nằm trong khoảng 0 đến 45 ")
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	
	
	@Length(min=0, max=128, message="tindùngĐộ dài mã phải nằm trong khoảng 0 đến 128 ")
	public String getCreditCode() {
		return creditCode;
	}
	public void setCreditCode(String creditCode) {
		this.creditCode = creditCode;
	}
	
	
	@Length(min=0, max=45, message="Độ dài quận phải nằm trong khoảng 0 đến 45 ")
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	
	
	@Length(min=0, max=128, message="Độ dài địa chỉ phải nằm trong khoảng 0 đến 128 ")
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}

	public String getStatus(){
		return status;
	}
	public void setStatus(String status){
		this.status=status;
	}

	@NotNull(message="Loại nhà cung cấp không được để trống")
	@Length(min=1, max=1, message="Độ dài phải nằm trong khoảng 1 đến 1 ")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@NotNull(message="Mã pinyin không được để trống")
	@Length(min=1, max=64, message="Độ dài phải nằm trong khoảng 1 đến 64 ")
	public String getPinyinCode() {
		return pinyinCode;
	}

	public void setPinyinCode(String pinyinCode) {
		this.pinyinCode = pinyinCode;
	}

	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "supplier";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "1005526731044757538";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}