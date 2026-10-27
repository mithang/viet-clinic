package com.geeke.collect.entity;

import com.geeke.admin.entity.User;
import javax.validation.constraints.NotNull;
import com.geeke.admin.entity.Router;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Mục yêu thíchEntity
 * @author szy
 * @version 2021-07-28
 */
public class SysCollect extends DataEntity<SysCollect> {

	private static final long serialVersionUID = 1L;
	private User userId;      // Người dùngid 
	private Router routerId;      // Định tuyếnid 
    private Long sort;

    public Long getSort() {
        return sort;
    }

    public void setSort(Long sort) {
        this.sort = sort;
    }

    public SysCollect() {
		super();
	}

	public SysCollect(String id){
		super(id);
	}
	

	@NotNull(message="Người dùngidKhông được để trống")
    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }
    
	@NotNull(message="Định tuyếnidKhông được để trống")
    public Router getRouterId() {
        return routerId;
    }

    public void setRouterId(Router routerId) {
        this.routerId = routerId;
    }
    
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "sys_collect";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "549885676523610115";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}