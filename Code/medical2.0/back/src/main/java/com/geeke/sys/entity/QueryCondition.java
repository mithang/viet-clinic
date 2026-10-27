package com.geeke.sys.entity;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Điều kiện truy vấnEntity
 * @author lys
 * @version 2021-07-05
 */
public class QueryCondition extends DataEntity<QueryCondition> {

	private static final long serialVersionUID = 1L;
	@JsonSerialize(using= ToStringSerializer.class)
	private Long userId;		// Người dùngId
	@JsonSerialize(using= ToStringSerializer.class)
	private Long routerId;		// Định tuyếnId
	private String conditions;		// Điều kiện

	
	public QueryCondition() {
		super();
	}

	public QueryCondition(String id){
		super(id);
	}
	

	@NotNull(message="Người dùngIdKhông được để trống")
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	
	@NotNull(message="Định tuyếnIdKhông được để trống")
	public Long getRouterId() {
		return routerId;
	}
	public void setRouterId(Long routerId) {
		this.routerId = routerId;
	}
	
	
	@Length(min=1, max=4000, message="Điều kiệnĐộ dài phải nằm trong khoảng 1 đến 4000 ")
	public String getConditions() {
		return conditions;
	}
	public void setConditions(String conditions) {
		this.conditions = conditions;
	}
	
	
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "sys_condition";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "515882179918733313";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}