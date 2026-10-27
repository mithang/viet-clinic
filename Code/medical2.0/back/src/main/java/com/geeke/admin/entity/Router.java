package com.geeke.admin.entity;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.TreeEntity;
/**
 * Quản lý tuyến đườngEntity
 * @author lys
 * @version 2021-11-18
 */
public class Router extends TreeEntity<Router> {

	private static final long serialVersionUID = 4003L;
	private String code;		// Mã nguồn
	private String url;		// URL
	private String canPermission;		// có thể không  phútphối
	private Integer isLocked;		// Vô hiệu hóa
	private String properties;		// mở rộngthuộc tính

	
	public Router() {
		super();
	}

	public Router(String id){
		super(id);
	}
	

	@Length(min=0, max=64, message="Độ dài mã phải nằm trong khoảng 0 đến 64 ")
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	
	@Length(min=0, max=128, message="URLĐộ dài phải từ 0  và  128 Giữa")
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	
	@Length(min=1, max=1, message="có thể không  phútphốiĐộ dài phải nằm trong khoảng 1 đến 1 ")
	public String getCanPermission() {
		return canPermission;
	}
	public void setCanPermission(String canPermission) {
		this.canPermission = canPermission;
	}
	
	
	public Integer getIsLocked() {
		return isLocked;
	}
	public void setIsLocked(Integer isLocked) {
		this.isLocked = isLocked;
	}
	
	
	@Length(min=1, max=1024, message="mở rộngthuộc tínhĐộ dài phải nằm trong khoảng 1 đến 1024 ")
	public String getProperties() {
		return properties;
	}
	public void setProperties(String properties) {
		this.properties = properties;
	}
	
	
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "sys_router";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "4003";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}