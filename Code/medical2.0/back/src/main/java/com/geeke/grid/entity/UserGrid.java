package com.geeke.grid.entity;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Bố cục tùy chỉnhEntity
 * @author ycy
 * @version 2021-08-02
 */
public class UserGrid extends DataEntity<UserGrid> {

	private static final long serialVersionUID = 1L;

	@JsonSerialize(using= ToStringSerializer.class)
	private Long userId;		// Người dùngid
	@JsonSerialize(using= ToStringSerializer.class)
	private Long routerId;		//Định tuyếnid
	private String gridInfo;		// Bố cục tùy chỉnhThông tin
	private String defaultCheck;		// chọn trong hiển thịhiển thị của mô-đun

	
	public UserGrid() {
		super();
	}

	public UserGrid(String id){
		super(id);
	}
	

	@NotNull(message="Người dùngidKhông được để trống")
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	
	@Length(min=0, max=1000, message="Bố cục tùy chỉnhThông tinĐộ dài phải nằm trong khoảng 0 đến 1000 ")
	public String getGridInfo() {
		return gridInfo;
	}
	public void setGridInfo(String gridInfo) {
		this.gridInfo = gridInfo;
	}
	
	
	@Length(min=0, max=200, message="Độ dài mô-đun được chọn hiển thị phải nằm trong khoảng 0 đến 200 ")
	public String getDefaultCheck() {
		return defaultCheck;
	}
	public void setDefaultCheck(String defaultCheck) {
		this.defaultCheck = defaultCheck;
	}


	public Long getRouterId() {
		return routerId;
	}

	public void setRouterId(Long routerId) {
		this.routerId = routerId;
	}

	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "user_grid";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "570619064204943377";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}