package com.geeke.gen.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.common.persistence.DataEntity;
import com.google.common.collect.Lists;
import org.hibernate.validator.constraints.Length;

import java.util.List;
/**
 * Quản lý bảng nghiệp vụEntity
 * @author lys
 * @version 2021-08-26
 */
public class GenTable extends DataEntity<GenTable> {

	private static final long serialVersionUID = 1L;
	private String comments;		// Giải thích
	private String source;		// Nguồn
	private String className;		// thực thểLớpTên
	private GenTable parentTable;      // liên kếtchabảng 
	private String parentTableFk;		// chabảngngoàikhóa
	private String isAssociation;		//  có liên kếtbảng
	private String filter;		// lọcĐiều kiện
	private String orderColumns;	// sắp xếpTrường

	private List<Constraint> constraintList = Lists.newArrayList();		// Danh sách bảng con
	private List<GenTableColumn> genTableColumnList = Lists.newArrayList();		// Danh sách bảng con
	
	public GenTable() {
		super();
	}

	public GenTable(String id){
		super(id);
	}
	

	@Length(min=0, max=500, message="Giải thíchĐộ dài phải nằm trong khoảng 0 đến 500 ")
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	
	
	@Length(min=0, max=128, message="đếnnguồnĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	
	
	@Length(min=1, max=100, message="thực thểLớpTênĐộ dài phải nằm trong khoảng 1 đến 100 ")
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	
	
    public GenTable getParentTable() {
        return parentTable;
    }

    public void setParentTable(GenTable parentTable) {
        this.parentTable = parentTable;
    }
    
	@Length(min=0, max=100, message="chabảngngoàikhóaĐộ dài phải nằm trong khoảng 0 đến 100 ")
	public String getParentTableFk() {
		return parentTableFk;
	}
	public void setParentTableFk(String parentTableFk) {
		this.parentTableFk = parentTableFk;
	}
	
	
	@Length(min=1, max=1, message=" có liên kếtbảngĐộ dài phải nằm trong khoảng 1 đến 1 ")
	public String getIsAssociation() {
		return isAssociation;
	}
	public void setIsAssociation(String isAssociation) {
		this.isAssociation = isAssociation;
	}
	
	
	@Length(min=0, max=2000, message="lọcĐiều kiệnĐộ dài phải nằm trong khoảng 0 đến 2000 ")
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}
	
	public String getOrderColumns() {
		return orderColumns;
	}
	
	public void setOrderColumns(String orderColumns) {
		this.orderColumns = orderColumns;
	}

	public List<Constraint> getConstraintList() {
		return constraintList;
	}

	public void setConstraintList(List<Constraint> constraintList) {
		this.constraintList = constraintList;
	}
	public List<GenTableColumn> getGenTableColumnList() {
		return genTableColumnList;
	}

	public void setGenTableColumnList(List<GenTableColumn> genTableColumnList) {
		this.genTableColumnList = genTableColumnList;
	}
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "gen_table";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "4001";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}