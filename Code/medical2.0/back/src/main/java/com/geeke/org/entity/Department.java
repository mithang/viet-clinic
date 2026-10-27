package com.geeke.org.entity;

import com.geeke.org.entity.Company;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import com.geeke.admin.entity.User;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.TreeEntity;
/**
 * Quản lý phòng banEntity
 * @author lys
 * @version 2021-07-13
 */
public class Department extends TreeEntity<Department> {

	private static final long serialVersionUID = 1L;
	private Company company;      // Công ty 
	private String code;		// Mã
	private User director;      // tổnggiám sát 
	private User manager;      // qualý 

	
	public Department() {
		super();
	}

	public Department(String id){
		super(id);
	}
	

	public Department(Company company){
		this.company = company;
	}

	@NotNull(message="Công ty không được để trống")
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
	@Length(min=1, max=10, message="Độ dài mã phải nằm trong khoảng 1 đến 10 ")
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	
    public User getDirector() {
        return director;
    }

    public void setDirector(User director) {
        this.director = director;
    }
    
    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }
    
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "org_department";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "52676365136650250";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}