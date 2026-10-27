package com.geeke.sys.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Chủ đề hệ thốngEntity
 * @author lys
 * @version 2021-09-19
 */
public class PersonalTheme extends DataEntity<PersonalTheme> {

	private static final long serialVersionUID = 532290982221258937L;
    @JsonSerialize(using= ToStringSerializer.class)
	private Long userId;		// Người dùng
	private String theme;		// Chủ đề

	
	public PersonalTheme() {
		super();
	}

	public PersonalTheme(String id){
		super(id);
	}
	

	@NotNull(message="Người dùngKhông thể là trống")
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	
	@Length(min=1, max=2000, message="Độ dài chủ đề phải nằm trong khoảng 1 đến 2000 ")
	public String getTheme() {
		return theme;
	}
	public void setTheme(String theme) {
		this.theme = theme;
	}
	
	
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "sys_personal_theme";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "532290982221258937";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}