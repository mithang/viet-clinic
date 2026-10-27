package com.geeke.common.persistence;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.geeke.utils.StringUtils;


/**
 * Dữ liệuEntityLớp
 * @author lys
 * @version 2018-05-16
 */
public abstract class TreeEntity<T> extends DataEntity<T> {

	private static final long serialVersionUID = 1L;

	protected T parent;	// chacấp
	protected String ids; // toàn bộmã số
	protected Integer sort;		// Sắp xếp
	protected List<T> children;		//conNút
	
	public TreeEntity() {
		super();
	}
	
	public TreeEntity(String id) {
		super(id);
	}
	
	
	/**
	 * Phương thức thực thi trước khi chèn, Cần gọi thủ công
	 */
	@Override
	public void preInsert(){
		super.preInsert();
		
		TreeEntity<T> treeParent = (TreeEntity<T>)parent;
		if(!StringUtils.isBlank(treeParent.getIds())) {
			setIds(treeParent.getIds()  + id + ".");
		} else {
			setIds(id + ".");
		}		
	}

	/**
	 * Phương thức thực thi trước khi cập nhật, Cần gọi thủ công
	 */
	@Override
	public void preUpdate(){
		super.preUpdate();
		
		TreeEntity<T> treeParent = (TreeEntity<T>)parent;
		if(!StringUtils.isBlank(treeParent.getIds())) {
			setIds(treeParent.getIds()  + id + ".");
		} else {
			setIds(id + ".");
		}	
	}	
	
	//@JsonBackReference
	@NotNull(message="chacấpđịnh danhKhông thể là trống")
	public T getParent() {
		return parent;
	}

	public void setParent(T parent) {
		this.parent = parent;
	}	

	public String getIds() {
		return ids;
	}

	public void setIds(String ids) {
		this.ids = ids;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}
	
	@JsonInclude(value=JsonInclude.Include.NON_NULL) 
	public List<T> getChildren() {
		return children;
	}

	@JsonDeserialize
	public void setChildren(List<T> children) {
		this.children = children;
	}
	
	/*
	 * Khách hàngđầuselecttreenhómtệp  Cần của lableTrường
	 */
	public String getLabel() {
		return this.getName();
	}
}
