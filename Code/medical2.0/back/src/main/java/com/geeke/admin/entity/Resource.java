package com.geeke.admin.entity;

import org.hibernate.validator.constraints.Length;
import com.geeke.admin.entity.Router;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Quản lý tài nguyênEntity
 * @author lys
 * @version 2021-11-18
 */
public class Resource extends DataEntity<Resource> {

	private static final long serialVersionUID = 4006L;
	private String code;		// Mã nguồn
	private String url;		// Url
	private String permission;		// Quyền
	private String canPermission;		// có thểủy quyền
	private String isDefault;		// Mặc địnhQuyền
	private Integer isLocked;		// Vô hiệu hóa
	private Router router;      // Định tuyến 

	
	public Resource() {
		super();
	}

	public Resource(String id){
		super(id);
	}
	
	public Resource(Router router){
		this.router = router;
	}

	// DS80: @Length(min=1) cho phep null, nen null qua @Valid roi 500 o cot name NOT NULL; override de fail-closed 400.
	// Khong them @NotNull vao DataEntity.getName vi nhieu entity con khong co cot name / name nullable.
	@Override
	@NotNull(message="Tên tài nguyên không được để trống")
	@Length(min=1, max=128, message="TênĐộ dài phải nằm trong khoảng 1 đến 128 ")
	public String getName() {
		return super.getName();
	}

	@NotNull(message="Mã tài nguyên không được để trống")
	@Length(min=1, max=64, message="Độ dài mã phải nằm trong khoảng 1 đến 64 ")
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	
	@NotNull(message="Url không được để trống")
	@Length(min=1, max=128, message="UrlĐộ dài phải từ 1  và  128 Giữa")
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	
	@Length(min=1, max=32, message="QuyềnĐộ dài phải nằm trong khoảng 1 đến 32 ")
	public String getPermission() {
		return permission;
	}
	public void setPermission(String permission) {
		this.permission = permission;
	}
	
	
	@NotNull(message="Có thể ủy quyền không được để trống")
	@Length(min=1, max=1, message="có thểủy quyềnĐộ dài phải nằm trong khoảng 1 đến 1 ")
	public String getCanPermission() {
		return canPermission;
	}
	public void setCanPermission(String canPermission) {
		this.canPermission = canPermission;
	}
	
	
	@NotNull(message="Mặc định quyền không được để trống")
	@Length(min=1, max=1, message="Mặc địnhQuyềnĐộ dài phải nằm trong khoảng 1 đến 1 ")
	public String getIsDefault() {
		return isDefault;
	}
	public void setIsDefault(String isDefault) {
		this.isDefault = isDefault;
	}
	
	
	public Integer getIsLocked() {
		return isLocked;
	}
	public void setIsLocked(Integer isLocked) {
		this.isLocked = isLocked;
	}
	
	
	@NotNull(message="tuyến đườngKhông thể là trống")
    public Router getRouter() {
        return router;
    }

    public void setRouter(Router router) {
        this.router = router;
    }
    
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "sys_resource";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "4006";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}