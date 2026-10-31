package com.geeke.common.entity;

import com.geeke.common.persistence.BaseEntity;

/**
 * bảngCộttiêu đề
 * 
 * @author lys
 * @date 2019/2/2
 */
public class ColumnView extends BaseEntity<ColumnView> implements Comparable<ColumnView> {
	private static final long serialVersionUID = -4859316271465038916L;

	private String userId;		//Người dùngID
	private String routerId;	//Định tuyếnID
	private String tableId;		//bảngID
	
	private String prop;		// thuộc tínhtên
	private String label;		// Cộttiêu đề	
	private String align;		// đơn đồngôđối vớiđềucách
	private Integer miniWidth;		// Nhỏ nhấtrộngđộ
	private Integer width;		// Độ rộng cột
	private Boolean display;		// Danh sách trong Có hay khônghiển thị 	
	private Integer sort;		// Sắp xếp
	private String showType;	//hiển thịLoại
	private String javaType;	// javaLoại

	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getRouterId() {
		return routerId;
	}
	public void setRouterId(String routerId) {
		this.routerId = routerId;
	}
	public String getTableId() {
		return tableId;
	}
	public void setTableId(String tableId) {
		this.tableId = tableId;
	}
	public String getProp() {
		return prop;
	}
	public void setProp(String prop) {
		this.prop = prop;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getAlign() {
		return align;
	}
	public void setAlign(String align) {
		this.align = align;
	}	
	public Integer getMiniWidth() {
		return miniWidth;
	}
	public void setMiniWidth(Integer miniWidth) {
		this.miniWidth = miniWidth;
	}
	public Integer getWidth() {
		return width;
	}
	public void setWidth(Integer width) {
		this.width = width;
	}
	public Boolean getDisplay() {
		return display;
	}
	public void setDisplay(Boolean display) {
		this.display = display;
	}
	public Integer getSort() {
		return sort;
	}
	public void setSort(Integer sort) {
		this.sort = sort;
	}
	public String getShowType() {
		return showType;
	}
	public void setShowType(String showType) {
		this.showType = showType;
	}
	public String getJavaType() {
		return javaType;
	}
	public void setJavaType(String javaType) {
		this.javaType = javaType;
	}
	@Override
	public int compareTo(ColumnView o) {
		//if(this.getDisplay() == o.getDisplay()) {
			return this.getSort() - o.getSort();
/*		}else if(this.getDisplay()) {
			return -1;
		}*/
		//return 1;
	}
}
