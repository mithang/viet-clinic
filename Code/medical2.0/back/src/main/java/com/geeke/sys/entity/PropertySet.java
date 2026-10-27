package com.geeke.sys.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Quản lý tập thuộc tínhEntity
 * @author lys
 * @version 2021-12-26
 */
public class PropertySet extends DataEntity<PropertySet> {

	private static final long serialVersionUID = 773152267153252816L;
	private String propertiesDef;		// thuộc tínhđịnh nghĩa

	
	public PropertySet() {
		super();
	}

	public PropertySet(String id){
		super(id);
	}
	

	public String getPropertiesDef() {
		return propertiesDef;
	}
	public void setPropertiesDef(String propertiesDef) {
		this.propertiesDef = propertiesDef;
	}
	
	
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "sys_property_set";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "773152267153252816";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}