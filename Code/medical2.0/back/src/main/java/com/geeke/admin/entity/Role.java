package com.geeke.admin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
/**
 * Quản lý vai tròEntity
 * @author lys
 * @version 2022-05-24
 */
public class Role extends DataEntity<Role> {

	private static final long serialVersionUID = 4012L;
	private Company company;      // Công ty 
	private String code;		// Mã nguồn
	private Integer isLocked;		// Vô hiệu hóa

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	private String parentId;  //chaLớpid

	public Role() {
		super();
	}

	public Role(String id){
		super(id);
	}
	
	public Role(Company company){
		this.company = company;
	}

	@NotNull(message="Công ty không được để trống")
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
	@Length(min=1, max=32, message="Độ dài mã phải nằm trong khoảng 1 đến 32 ")
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	
	public Integer getIsLocked() {
		return isLocked;
	}
	public void setIsLocked(Integer isLocked) {
		this.isLocked = isLocked;
	}
	
	
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "sys_role";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "4012";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}