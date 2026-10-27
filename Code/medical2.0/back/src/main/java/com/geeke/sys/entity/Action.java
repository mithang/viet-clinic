package com.geeke.sys.entity;

import com.geeke.sys.entity.DictItem;
import com.geeke.gen.entity.GenTable;
import org.hibernate.validator.constraints.Length;
import java.util.Date;
import java.util.List;
import com.google.common.collect.Lists;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Nhật ký thao tácEntity
 * @author lys
 * @version 2019-12-11
 */
public class Action extends DataEntity<Action> {

	private static final long serialVersionUID = 1L;
	private DictItem actionType;      // Thao tác 
	private GenTable objectType;      // Đối tượngLoại 
	private String objectId;		// Đối tượng nghiệp vụ
	private String objectName;		// Tên đối tượnggọi là
	private String status;		// khôi phục

	private List<ActionRecycle> actionRecycleList = Lists.newArrayList();		// Danh sách bảng con
	
	public Action() {
		super();
	}

	public Action(String id){
		super(id);
	}
	

    public DictItem getActionType() {
        return actionType;
    }

    public void setActionType(DictItem actionType) {
        this.actionType = actionType;
    }
    
    public GenTable getObjectType() {
        return objectType;
    }

    public void setObjectType(GenTable objectType) {
        this.objectType = objectType;
    }
    
	@Length(min=0, max=40, message="Đối tượng nghiệp vụĐộ dài phải nằm trong khoảng 0 đến 40 ")
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	
	
	@Length(min=0, max=512, message="Tên đối tượnggọi làĐộ dài phải nằm trong khoảng 0 đến 512 ")
	public String getObjectName() {
		return objectName;
	}
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}
	
	
	@Length(min=1, max=1, message="khôi phụcĐộ dài phải nằm trong khoảng 1 đến 1 ")
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	
	
	public List<ActionRecycle> getActionRecycleList() {
		return actionRecycleList;
	}

	public void setActionRecycleList(List<ActionRecycle> actionRecycleList) {
		this.actionRecycleList = actionRecycleList;
	}
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "sys_action";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "4000";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}