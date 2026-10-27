package com.geeke.sys.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Chủ đề hệ thốngEntity
 * @author lys
 * @version 2021-09-19
 */
public class Theme extends DataEntity<Theme> {

	private static final long serialVersionUID = 532039984332456179L;
	private String theme;		// Chủ đề
	private String isDefault;		// Mặc định

	
	public Theme() {
		super();
	}

	public Theme(String id){
		super(id);
	}
	

	@Length(min=1, max=2000, message="Độ dài chủ đề phải nằm trong khoảng 1 đến 2000 ")
	public String getTheme() {
		return theme;
	}
	public void setTheme(String theme) {
		this.theme = theme;
	}
	
	
	@Length(min=0, max=1, message="Mặc địnhĐộ dài phải nằm trong khoảng 0 đến 1 ")
	public String getIsDefault() {
		return isDefault;
	}
	public void setIsDefault(String isDefault) {
		this.isDefault = isDefault;
	}
	
	
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "sys_theme";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "532039984332456179";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}