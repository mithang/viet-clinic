package com.geeke.toll.entity;

import com.geeke.outpatient.entity.RecipelInfo;
import com.geeke.toll.entity.TollInfo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Quản lý thu phíEntity
 * @author lc
 * @version 2022-06-15
 */
public class TollDetail extends DataEntity<TollDetail> {

	private static final long serialVersionUID = 1028225822184669188L;
	private RecipelInfo recipel;      // Đơn thuốc 
	private TollInfo tollId;      // Thu phíBảng chính 

	
	public TollDetail() {
		super();
	}

	public TollDetail(String id){
		super(id);
	}
	
	public TollDetail(TollInfo tollId){
		this.tollId = tollId;
	}

    public RecipelInfo getRecipel() {
        return recipel;
    }

    public void setRecipel(RecipelInfo recipel) {
        this.recipel = recipel;
    }
    
    public TollInfo getTollId() {
        return tollId;
    }

    public void setTollId(TollInfo tollId) {
        this.tollId = tollId;
    }
    
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "toll_detail";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "1028225822184669188";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return false;
    }
}