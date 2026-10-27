package com.geeke.admin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;

import javax.validation.constraints.NotNull;

/**
 * Quản lý người dùngEntity
 * @author lys
 * @version 2021-08-25
 */
public class UserRole extends DataEntity<UserRole> {

	private static final long serialVersionUID = 1L;
	private User user;      // Định danh người dùng 
	private Role role;      // Vai tròđịnh danh
    private Company company; //nơitenant

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }


	public UserRole() {
		super();
	}

	public UserRole(String id){
		super(id);
	}
	

	public UserRole(User user){
		this.user = user;
	}

	@NotNull(message="Định danh người dùngKhông thể là trống")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
	@NotNull(message="Vai tròđịnh danhKhông thể là trống")
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
    
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "sys_user_role";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "4014";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}