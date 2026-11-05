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
		// DS9 (round 9): parent null (payload chi co id, vd delete/bulkDelete/
		// bulkInsert) truoc day NPE o treeParent.getIds(). Guard null de roi
		// ve nhanh else cu (nut goc: ids = id + "."), giong hanh vi khi
		// parent co ids rong; parent co ids thi hanh vi khong doi.
		if(treeParent != null && !StringUtils.isBlank(treeParent.getIds())) {
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
		// DS9 (round 9): nhu preInsert — parent null truoc day NPE o day
		// (stack: TreeEntity.preUpdate:58 qua CrudService.delete/bulkDelete),
		// lam single delete va bulkDelete cua Company (va moi TreeEntity)
		// tra 500 cho ca full/system du payload chi co id. Guard null de
		// roi ve nhanh else cu; cac statement delete khong ghi ids nen
		// viec tinh lai ids o day khong anh huong dong da xoa.
		if(treeParent != null && !StringUtils.isBlank(treeParent.getIds())) {
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
