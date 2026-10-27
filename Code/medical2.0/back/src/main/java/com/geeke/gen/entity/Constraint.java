package com.geeke.gen.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.common.persistence.DataEntity;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
/**
 * Quản lý bảng nghiệp vụEntity
 * @author lys
 * @version 2021-08-26
 */
public class Constraint extends DataEntity<Constraint> {

	private static final long serialVersionUID = 1L;
	private GenTable genTable;      // thuộc vềbảngmã số 
	private String comments;		// Mô tả
	private String constraintColumn;		// Trường ràng buộc

	
	public Constraint() {
		super();
	}

	public Constraint(String id){
		super(id);
	}
	

	public Constraint(GenTable genTable){
		this.genTable = genTable;
	}

	@NotNull(message="thuộc vềbảngmã sốKhông thể là trống")
    public GenTable getGenTable() {
        return genTable;
    }

    public void setGenTable(GenTable genTable) {
        this.genTable = genTable;
    }
    
	@Length(min=0, max=500, message="Mô tảĐộ dài phải nằm trong khoảng 0 đến 500 ")
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	
	
	@Length(min=0, max=500, message="Trường ràng buộcĐộ dài phải nằm trong khoảng 0 đến 500 ")
	public String getConstraintColumn() {
		return constraintColumn;
	}
	public void setConstraintColumn(String constraintColumn) {
		this.constraintColumn = constraintColumn;
	}
	
	
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "gen_table_constraint";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "4009";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}