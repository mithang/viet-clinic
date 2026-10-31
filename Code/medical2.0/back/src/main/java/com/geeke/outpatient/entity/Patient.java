package com.geeke.outpatient.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
import com.geeke.sys.entity.DictItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.util.Date;
/**
 * Thông tin bệnh nhânEntity
 * @author txl
 * @version 2022-06-23
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class Patient extends DataEntity<Patient> {

	private static final long serialVersionUID = 1008489176147648530L;
	private Company company;      // Phòng khámid 
	private DictItem gender;      // Giới tính 
	private String age;		// Tuổi
	private String month;		// Tháng
	private Date birthday;		// Ngày sinh
	private String phone;		// Thông tin liên hệ
	private String card;		// giấy tờsố
	private String cardType;		// Loại giấy tờ
	private DictItem withPatientNexus;      //  và Bệnh nhânquan hệ 
	private String healthCardNo;		// sức khỏethẻsố
	private String province;		// Tỉnh
	private String city;		// Thành phố
	private String area;		// Quận
	private String address;		// Địa chỉ chi tiết
	private String guardianName;		// người giám hộ
	private String guardianPhone;		// người giám hộSố điện thoại
	private String breathe;
	private String temperature;
	private String pulse;
	private String bloodPressure;
	private String openId;
	private String poverty;

	public Patient() {
		super();
	}

	public Patient(String id){
		super(id);
	}
	

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
	@NotNull(message="Giới tínhKhông thể là trống")
    public DictItem getGender() {
        return gender;
    }

    public void setGender(DictItem gender) {
        this.gender = gender;
    }
    
	@Length(min=1, max=45, message="TuổiĐộ dài phải nằm trong khoảng 1 đến 45 ")
	public String getAge() {
		return age;
	}
	public void setAge(String age) {
		this.age = age;
	}
	
	
	@Length(min=1, max=45, message="ThángĐộ dài phải nằm trong khoảng 1 đến 45 ")
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}
	
	
	@JsonFormat(pattern = "yyyy-MM-dd")
	public Date getBirthday() {
		return birthday;
	}
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	
	
	@Length(min=1, max=45, message="liên hệcáchĐộ dài phải nằm trong khoảng 1 đến 45 ")
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	
	@Length(min=1, max=45, message="số CMND/CCCDĐộ dài phải nằm trong khoảng 1 đến 45 ")
	public String getCard() {
		return card;
	}
	public void setCard(String card) {
		this.card = card;
	}
	
	
    public DictItem getWithPatientNexus() {
        return withPatientNexus;
    }

    public void setWithPatientNexus(DictItem withPatientNexus) {
        this.withPatientNexus = withPatientNexus;
    }
    
	@Length(min=0, max=45, message="sức khỏethẻsốĐộ dài phải nằm trong khoảng 0 đến 45 ")
	public String getHealthCardNo() {
		return healthCardNo;
	}
	public void setHealthCardNo(String healthCardNo) {
		this.healthCardNo = healthCardNo;
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
	
	
	@Length(min=0, max=45, message="Độ dài quận phải nằm trong khoảng 0 đến 45 ")
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	
	
	@Length(min=0, max=45, message="Độ dài địa chỉ chi tiết phải nằm trong khoảng 0 đến 45 ")
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
	
	@Length(min=0, max=100, message="người giám hộĐộ dài phải nằm trong khoảng 0 đến 100 ")
	public String getGuardianName(){
		return guardianName;
	}

	public void setGuardianPhone(String guardianPhone) {
		this.guardianPhone = guardianPhone;
	}

	@Length(min=0, max=100, message="người giám hộĐộ dài số điện thoại phải nằm trong khoảng 0 đến 100 ")
	public String getGuardianPhone(){
		return guardianPhone;
	}

	public void setGuardianName(String guardianName) {
		this.guardianName = guardianName;
	}

	public String getBreathe() {
		return breathe;
	}

	public void setBreathe(String breathe) {
		this.breathe = breathe;
	}

	public String getTemperature() {
		return temperature;
	}

	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}

	public String getPulse() {
		return pulse;
	}

	public void setPulse(String pulse) {
		this.pulse = pulse;
	}

	public String getBloodPressure() {
		return bloodPressure;
	}

	public void setBloodPressure(String bloodPressure) {
		this.bloodPressure = bloodPressure;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getPoverty() {
		return poverty;
	}

	public void setPoverty(String poverty) {
		this.poverty = poverty;
	}

	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "patient";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "1008489176147648530";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}