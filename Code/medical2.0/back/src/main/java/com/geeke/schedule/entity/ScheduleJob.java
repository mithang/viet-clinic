package com.geeke.schedule.entity;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Quản lý tác vụ định kỳEntity
 * @author shenzy
 * @version 2021-10-28
 */
public class ScheduleJob extends DataEntity<ScheduleJob> {

	private static final long serialVersionUID = 683898740740005891L;
	private String code;		// tác vụmã số
	private String cron;		// tác vụbiểu thức
	private String status;		// tác vụTrạng thái
	private String isLock;		// Có hay khôngVô hiệu hóa

	
	public ScheduleJob() {
		super();
	}

	public ScheduleJob(String id){
		super(id);
	}
	

	@Length(min=1, max=100, message="tác vụmã sốĐộ dài phải nằm trong khoảng 1 đến 100 ")
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	
	@Length(min=1, max=30, message="tác vụbiểu thứcĐộ dài phải nằm trong khoảng 1 đến 30 ")
	public String getCron() {
		return cron;
	}
	public void setCron(String cron) {
		this.cron = cron;
	}
	
	
	@Length(min=0, max=100, message="tác vụĐộ dài trạng thái phải nằm trong khoảng 0 đến 100 ")
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	
	@Length(min=0, max=1, message="Có hay khôngVô hiệu hóaĐộ dài phải nằm trong khoảng 0 đến 1 ")
	public String getIsLock() {
		return isLock;
	}
	public void setIsLock(String isLock) {
		this.isLock = isLock;
	}
	
	
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "schedule_job";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "683898740740005891";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}