package com.geeke.sys.entity;

import com.geeke.sys.entity.Action;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Nhật ký thao tácEntity
 * @author lys
 * @version 2019-12-12
 */
public class ActionRecycle extends DataEntity<ActionRecycle> {

	private static final long serialVersionUID = 1L;
	private Action action;      // Nhật ký 
	private String tableName;		// Tên bảng
	private String objectId;		// Đối tượng nghiệp vụ
	private String objectName;		// Tên đối tượnggọi là

	
	public ActionRecycle() {
		super();
	}

	public ActionRecycle(String id){
		super(id);
	}
	

	public ActionRecycle(Action action){
		this.action = action;
	}

	@NotNull(message="Ngàynhật kýKhông thể là trống")
    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
    
	@Length(min=1, max=200, message="bảngtênĐộ dài phải nằm trong khoảng 1 đến 200 ")
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	
	@Length(min=1, max=40, message="Đối tượng nghiệp vụĐộ dài phải nằm trong khoảng 1 đến 40 ")
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	
	
	@Length(min=0, max=200, message="Tên đối tượnggọi làĐộ dài phải nằm trong khoảng 0 đến 200 ")
	public String getObjectName() {
		return objectName;
	}
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}
	
	
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "sys_action_recycle";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "4013";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}