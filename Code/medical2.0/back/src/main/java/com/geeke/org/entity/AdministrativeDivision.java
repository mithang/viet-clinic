package com.geeke.org.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Phân chia khu vực hành chínhEntity
 * @author txl
 * @version 2022-06-21
 */
public class AdministrativeDivision extends DataEntity<AdministrativeDivision> {

	private static final long serialVersionUID = 986666876811870222L;
    private AdministrativeDivision parten;      // Mã khu vực cấp trên 	        
	private Integer level;		// Cấp khu vực

	
	public AdministrativeDivision() {
		super();
	}

	public AdministrativeDivision(String id){
		super(id);
	}
	

	public AdministrativeDivision getParten() {
		return parten;
	}

	public void setParten(AdministrativeDivision parten) {
		this.parten = parten;
	}
	public Integer getLevel() {
		return level;
	}
	public void setLevel(Integer level) {
		this.level = level;
	}
	
	
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "administrative_division";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "986666876811870222";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return false;
    }
}