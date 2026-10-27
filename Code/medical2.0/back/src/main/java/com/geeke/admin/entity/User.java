package com.geeke.admin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.geeke.common.IUser;
import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
import com.geeke.org.entity.Department;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.validator.constraints.Length;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.List;
/**
 * Quản lý người dùngEntity
 * @author lys
 * @version 2021-08-25
 */
@Component
public class User extends DataEntity<User> implements IUser {

	private static final long serialVersionUID = 1L;
	private Company company;      // Công ty 
	private Department department;      // Phòng ban 
	private String loginName;		// Đăng nhậptên
	private String status;		// Đăng nhậpTrạng thái
	private String loginDuration;		// Đăng nhập giờdài
	private boolean  loginPasswordUpdate; // Có hay khôngSửa Mật khẩu
	private String loginPassword;		// Mật khẩu
	private Integer isLocked;		// Vô hiệu hóa
	private String phone;		// số điện thoại di động
	private String email;		// email
	private String qyOpenId;		// doanh nghiệpnghiệp vụopenid
	private List<String> clinics;  // Phòng khámid
	private UserExt userExt; //Người dùng của mở rộngThông tin
	private String office; //thuộc vềKhoa
	private String isWxuser="0";  //Có hay không là WeChatBệnh nhânNgười dùng

	public String getOffice() {
		return office;
	}

	public void setOffice(String office) {
		this.office = office;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getLoginDuration() {
		return loginDuration;
	}

	public void setLoginDuration(String loginDuration) {
		this.loginDuration = loginDuration;
	}

	public UserExt getUserExt() {
		return userExt;
	}

	public void setUserExt(UserExt userExt) {
		this.userExt = userExt;
	}

	public List<String> getClinics() {
		return clinics;
	}

	public void setClinics(List<String> clinics) {
		this.clinics = clinics;
	}

	public Integer getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(Integer isAdmin) {
		this.isAdmin = isAdmin;
	}

	private Integer isAdmin;


	private List<UserRole> userRoleList = Lists.newArrayList();		// Danh sách bảng con
	
	public User() {
		super();
	}

	public User(String id){
		super(id);
	}
	

	public User(Company company){
		this.company = company;
	}

	@NotNull(message="Công ty không được để trống")
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
	@NotNull(message="Phòng banKhông thể là trống")
    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
    
	@Length(min=1, max=32, message="Đăng nhậptênĐộ dài phải nằm trong khoảng 1 đến 32 ")
	public String getLoginName() {
		return loginName;
	}
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	
	
	@Length(min=1, max=64, message="Mật khẩuĐộ dài phải nằm trong khoảng 1 đến 64 ")
	@JsonIgnore
	public String getLoginPassword() {
		return loginPassword;
	}
    @JsonDeserialize
	public void setLoginPassword(String loginPassword) {
		this.loginPassword = loginPassword;
	}
	
	public boolean getLoginPasswordUpdate() {
		return loginPasswordUpdate;
	}
	
	public void setLoginPasswordUpdate(boolean loginPasswordUpdate) {
		this.loginPasswordUpdate = loginPasswordUpdate;
	}	
	
	public Integer getIsLocked() {
		return isLocked;
	}
	public void setIsLocked(Integer isLocked) {
		this.isLocked = isLocked;
	}
	@Length(min=0, max=20, message="số điện thoại di độngĐộ dài phải nằm trong khoảng 0 đến 20 ")
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}


	@Length(min=0, max=100, message="emailĐộ dài phải nằm trong khoảng 0 đến 100 ")
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}


	@Length(min=0, max=100, message="doanh nghiệpnghiệp vụopenidĐộ dài phải từ 0  và  100 Giữa")
	public String getQyOpenId() {
		return qyOpenId;
	}
	public void setQyOpenId(String qyOpenId) {
		this.qyOpenId = qyOpenId;
	}


	public List<UserRole> getUserRoleList() {
		return userRoleList;
	}

	public void setUserRoleList(List<UserRole> userRoleList) {
		this.userRoleList = userRoleList;
	}


	public String getIsWxuser() {
		return isWxuser;
	}

	public void setIsWxuser(String isWxuser) {
		this.isWxuser = isWxuser;
	}

	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "sys_user";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "4004";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }

	@Override
	public String getCompanyId() {
		if(company != null) {
			return company.getId();
		}
		
		return null;
	}
}