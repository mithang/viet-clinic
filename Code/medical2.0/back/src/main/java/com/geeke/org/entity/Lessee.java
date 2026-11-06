package com.geeke.org.entity;

import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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
	// ITER63 item 2: tên công ty của đơn vị thuê — CHUỖI tên, KHÔNG phải
	// tenant id. Trước đây field này tên `company`, che (shadow) field
	// `Company company` của DataEntity nhưng không có getter/setter String,
	// nên property `company` luôn resolve qua getCompany()/setCompany(Company)
	// của lớp cha: Jackson biến chuỗi frontend thành Company(id=chuỗi tên),
	// MyBatis đưa Company object vào cột varchar (500), get trả company null,
	// và Reflections.hasOwnField(Lessee,"company")=true khiến
	// ensureCompanyFilter chèn lọc company_id lên bảng không có cột đó
	// (listPage/listAll 500). Đổi tên thành companyName để hết shadow;
	// wire name giữ nguyên "company" qua @JsonProperty để frontend
	// (lesseeForm.vue: input text `company`) không phải đổi gì.
	private String companyName;		// công tyTên (chuỗi tên, xem trên)
	private String contactName;		// người liên hệTên
	private String addressProvince;		// Địa chỉ(Tỉnh)
	private String addressCity;		// Địa chỉ(Thành phố)
	private String addressRegion;		// Địa chỉ(Quận)
	private String address;		// liên hệĐịa chỉ
	private String isUse;		// Trạng thái

	
	public Lessee() {
		super();
		// ITER63 item 2: Lessee không dùng company kiểu Company của
		// DataEntity (không có tenant id — chuỗi tên nằm ở companyName),
		// nhưng CrudController.save có @Valid và DataEntity.getCompany()
		// mang @NotNull (constraint này Hibernate Validator vẫn áp cho
		// cả override không annotation, theo tiền lệ Department/
		// ClinicOffice re-declare @NotNull). Để save đơn qua được @Valid
		// mà không cần payload company object (frontend chỉ gửi chuỗi
		// tên), giữ sẵn một Company rỗng: id rỗng nên generic guard
		// verifyTenantOwnership vẫn cho qua (id rỗng -> return sớm),
		// và nó không bao giờ chạm tới SQL (mapper chỉ dùng companyName)
		// hay JSON (getCompany() đã @JsonIgnore).
		this.company = new com.geeke.org.entity.Company();
	}

	public Lessee(String id){
		super(id);
	}
	

	@NotNull(message="Tên tenant không được để trống")
	@Length(min=1, max=100, message="tenanttênĐộ dài phải nằm trong khoảng 1 đến 100 ")
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	
	@NotNull(message="Số điện thoại không được để trống")
	@Length(min=1, max=20, message="Số điện thoạisốĐộ dài phải nằm trong khoảng 1 đến 20 ")
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	/**
	 * Tên công ty (chuỗi) của đơn vị thuê — wire name "company" như frontend
	 * gửi/nhận, nhưng KHÔNG dùng property `company` của DataEntity (kiểu
	 * Company, tenant id) để tránh shadow và tránh Jackson/MyBatis lẫn lộn.
	 */
	@Length(min=0, max=100, message="Tên công tyĐộ dài phải nằm trong khoảng 0 đến 100 ")
	@JsonProperty("company")
	public String getCompanyName() {
		return companyName;
	}
	@JsonProperty("company")
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	/**
	 * ITER63 item 2: ẩn property `company` kiểu Company của DataEntity khỏi
	 * JSON — Lessee không có tenant id, chuỗi tên nằm ở companyName.
	 * Không có override này, Jackson sẽ nhận property "company" thứ hai
	 * (kiểu Company) xung đột với companyName-as-"company" ở trên.
	 * Generic guard của CrudService vẫn đọc getCompany() (luôn null ở đây)
	 * và cho qua như trước; guard thật của Lessee là checkLesseeTenant
	 * theo chuỗi tên (N3c, giữ nguyên).
	 */
	@Override
	@JsonIgnore
	public com.geeke.org.entity.Company getCompany() {
		return super.getCompany();
	}
	@Override
	@JsonIgnore
	public void setCompany(com.geeke.org.entity.Company company) {
		super.setCompany(company);
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
	
	
	@NotNull(message="Trạng thái không được để trống")
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