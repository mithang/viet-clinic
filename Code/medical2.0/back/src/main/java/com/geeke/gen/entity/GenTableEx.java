package com.geeke.gen.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
/**
 * Quản lý bảng nghiệp vụEntity
 * @author lys
 * @version 2019-12-06
 */
public class GenTableEx extends GenTable {
	private static final long serialVersionUID = 57593538386163538L;
	
	private List<GenTable> childList = Lists.newArrayList();	// liên kếtDanh sách bảng con

/*	//@Override
	public List<GenTableColumn> getGenTableColumnList() {
		//return genTableColumnList;
	}*/
	
	@JsonIgnore
	public List<GenTable> getChildList() {
		return childList;
	}
	public void setChildList(List<GenTable> childList) {
		this.childList = childList;
	}

	/**
	 * LấyNhậppackage phụ thuộcchuỗi
	 * @return
	 */
	@JsonIgnore
	public List<String> getImportList(){
		List<String> importList = Lists.newArrayList(); // dẫndùngDanh sách
		
		//List<GenTableColumnEx>  columns = getGenTableColumnList();
		
		for (GenTableColumn c : getGenTableColumnList()){
			
			
			GenTableColumnEx column = (GenTableColumnEx)c;
			
			//tùy chỉnhLoại
			if(column.getJavaType() != null && "Custom".equals(column.getJavaType().getValue())) {
				GenScheme scheme = column.getCustomType();
				if(scheme != null) {
					String customer = scheme.getPackageName() + "." + scheme.getModuleName() ;
					if(!StringUtils.isNullOrEmpty(scheme.getSubModuleName())) {
						customer = customer + "." + scheme.getSubModuleName()+ ".entity";
					}else {
						customer = customer + ".entity" ;
					}
					customer = customer+"."+ scheme.getGenTable().getClassName();
					if (!importList.contains(customer)){
						importList.add(customer);
					}
				}
			}
			
			if (column.getIsNotBaseField() || ("1".equals(column.getIsQuery()) && "between".equals(column.getQueryType().getValue())
							&& ("createDate".equals(column.getSimpleJavaField()) || "updateDate".equals(column.getSimpleJavaField())))){
				// NhậpLoạipackage phụ thuộc,  NếuLoại trong bao gồm“.”, thìCầnNhậpdẫndùng. 
				if (StringUtils.indexOf(column.getJavaType().getValue(), ".") != -1 && !importList.contains(column.getJavaType().getValue())){
					importList.add(column.getJavaType().getValue());
				}
			}
			if (column.getIsNotBaseField()){
				// NhậpJSR303, Json v.v. package phụ thuộc
				for (String ann : column.getAnnotationList()){
					if (!importList.contains(StringUtils.substringBeforeLast(ann, "("))){
						importList.add(StringUtils.substringBefore(ann, "("));
					}
				}
			}
			
			// LongLoạiTrườngthứ tựCộthóaXử lý, giải quyếtchuyểntrướcđầujs溢ravấn đề
			if("Long".equals(column.getJavaType().getValue())) {
				if (!importList.contains("com.fasterxml.jackson.databind.annotation.JsonSerialize")){
					importList.add("com.fasterxml.jackson.databind.annotation.JsonSerialize");
				}
				if (!importList.contains("com.fasterxml.jackson.databind.ser.std.ToStringSerializer")){
					importList.add("com.fasterxml.jackson.databind.ser.std.ToStringSerializer");
				}
			}
		}
		
		// Nếucóconbảng, thìCầnNhậpListtươngđóngdẫndùng
		if (getChildList() != null && getChildList().size() > 0){
			if (!importList.contains("java.util.List")){
				importList.add("java.util.List");
			}
			if (!importList.contains("com.google.common.collect.Lists")){
				importList.add("com.google.common.collect.Lists");
			}
		}
		
		
		return importList;
	}
	
	
	/**
	 * Có tồn tại khôngIdđịnh danhTrường
	 * @return
	 */
	@JsonIgnore
	public Boolean getParentExists(){
		GenTable parentTable = this.getParentTable();
		return parentTable != null && StringUtils.isNotBlank(parentTable.getId()) && StringUtils.isNotBlank(this.getParentTableFk());
	}	
	
	/**
	 * Có tồn tại khôngchaLớp
	 * @return
	 */
	@JsonIgnore
	public Boolean getIdExists(){
		for (GenTableColumn c : getGenTableColumnList()){
			if (StringUtils.equalsIgnoreCase("id", c.getName())){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Có tồn tại khôngcreate_dateCột
	 * @return
	 */
	@JsonIgnore
	public Boolean getCreateDateExists(){
		for (GenTableColumn c : getGenTableColumnList()){
			if (StringUtils.equalsIgnoreCase("create_date", c.getName())){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Có tồn tại khôngupdate_dateCột
	 * @return
	 */
	@JsonIgnore
	public Boolean getUpdateDateExists(){
		for (GenTableColumn c : getGenTableColumnList()){
			if (StringUtils.equalsIgnoreCase("update_date", c.getName())){
				return true;
			}
		}
		return false;
	}

	/**
	 * Có tồn tại khôngupdate_byCột
	 * @return
	 */
	@JsonIgnore
	public Boolean getUpdateByExists(){
		for (GenTableColumn c : getGenTableColumnList()){
			if (StringUtils.equalsIgnoreCase("update_by", c.getName())){
				return true;
			}
		}
		return false;
	}

	/**
	 * Có tồn tại khôngsortCột
	 * @return
	 */
	@JsonIgnore
	public Boolean getSortExists(){
		for (GenTableColumn c : getGenTableColumnList()){
			if (StringUtils.equalsIgnoreCase("sort", c.getName())){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Lấy đượcsắp xếpCột
	 * @return
	 */
	@JsonIgnore
	public GenTableColumn getSortColumn(){
		for (GenTableColumn c : getGenTableColumnList()){
			if (StringUtils.equalsIgnoreCase("sort", c.getName())){
				return c;
			}
		}
		return null;
	}
	

	/**
	 * Có tồn tại khôngdel_flagCột
	 * @return
	 */
	@JsonIgnore
	public Boolean getDelFlagExists(){
		for (GenTableColumn c : getGenTableColumnList()){
			if (StringUtils.equalsIgnoreCase("del_flag", c.getName())){
				return true;
			}
		}
		return false;
	}	
	
	/**
	 * Có hay khôngChứa hộp chọn dạng cây--chỉnh sửakhung
	 * @return
	 */
	@JsonIgnore
	public boolean getHasTreeSelectInput() {
		for (GenTableColumn column : getGenTableColumnList()){
			if(column.getShowType() != null 
					&& "SelectTree".equals(column.getShowType().getValue())
					&& "1".equals(column.getIsEdit())){
				return true;
			}
		}
		
		for(GenTable child: getChildList()) {
			for (GenTableColumn column : child.getGenTableColumnList()){
				if(column.getShowType() != null 
						&& "SelectTree".equals(column.getShowType().getValue())
						&& "1".equals(column.getIsEdit())){
					return true;
				}
			}			
		}		
		return false;
	}
	
	/**
	 * Có hay khôngbao gồmStringMultiSelect--chỉnh sửakhung
	 * @return
	 */
	@JsonIgnore
	public boolean getHasStringMultiSelectInput() {
		for (GenTableColumn column : getGenTableColumnList()){
			if(column.getShowType() != null 
					&& "StringMultiSelect".equals(column.getShowType().getValue())
					&& "1".equals(column.getIsEdit())){
				return true;
			}
		}
		
		for(GenTable child: getChildList()) {
			for (GenTableColumn column : child.getGenTableColumnList()){
				if(column.getShowType() != null 
						&& "StringMultiSelect".equals(column.getShowType().getValue())
						&& "1".equals(column.getIsEdit())){
					return true;
				}
			}			
		}
		return false;
	}
	
	/**
	 * Có hay khôngChứa hộp chọn dạng cây--Điều kiện truy vấn
	 * @return
	 */
	@JsonIgnore
	public boolean getHasTreeSelectQuery() {
		for (GenTableColumn column : getGenTableColumnList()){
			if(column.getShowType() != null 
					&& "SelectTree".equals(column.getShowType().getValue())
					&& "1".equals(column.getIsQuery())){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Có hay khôngbao gồmNgàyHộp chọn phạm vi--Điều kiện truy vấn
	 * @return
	 */
	@JsonIgnore
	public boolean getHasDataRangePicker4Query() {
		for (GenTableColumn column : getGenTableColumnList()){
			if(column.getShowType() != null && column.getQueryType() != null
					&& ("DatePicker".equals(column.getShowType().getValue()) || "DateTimePicker".equals(column.getShowType().getValue()))
					&& "between".equals(column.getQueryType().getValue())  
					&& "1".equals(column.getIsQuery())){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Có hay khôngbao gồmsốGiá trịHộp chọn phạm vi--Điều kiện truy vấn
	 * @return
	 */
	@JsonIgnore
	public boolean getHasNumberRangePicker4Query() {
		for (GenTableColumn column : getGenTableColumnList()){
			if(column.getShowType() != null && column.getQueryType() != null
					&& ("SingleInput".equals(column.getShowType().getValue()) || "InputNumber".equals(column.getShowType().getValue()) || "IncNumber".equals(column.getShowType().getValue()))
					&& "between".equals(column.getQueryType().getValue())  
					&& "1".equals(column.getIsQuery())){
				return true;
			}
		}
		return false;
	}
		
	
	/**
	 * Có hay không là câyhìnhbảng
	 * @return
	 */
	@JsonIgnore
	public boolean getIsTreeTable() {
		for (GenTableColumn column : getGenTableColumnList()){
			//javaLoại là This, vàvàTên trường là parent_id
			if(column.getJavaType() != null && "This".equals(column.getJavaType().getValue()) && StringUtils.equalsIgnoreCase("parent_id", column.getName())){
				return true;
			}
		}
		return false;		
	}
	
	/**
	 * LấyparentCột
	 * @return
	 */
	@JsonIgnore
	public GenTableColumn getParentColumn() {
		for (GenTableColumn column : getGenTableColumnList()){
			//javaLoại là This, vàvàTên trường là parent_id
			if(column.getJavaType() != null && "This".equals(column.getJavaType().getValue()) && StringUtils.equalsIgnoreCase("parent_id", column.getName())){
				return column;
			}
		}
		return null;
	}
	
	

	
	/**
	 * Giao diện xử lý loại tùy chỉnh được import từ ô truy vấn của client, Loại bỏ trùng lặp
	 * @return
	 */
	@JsonIgnore
	public List<GenTableColumn> getQueryImportList() {
		List<GenTableColumn> clientImports = Lists.newArrayList();
		getClientImportList(this, clientImports, "query");
		
		for (GenTable genTable : this.getChildList()){
			getClientImportList(genTable, clientImports, "query");
		}
		
		return clientImports;
	}
	
	/**
	 * Giao diện xử lý loại tùy chỉnh được import từ ô chỉnh sửa của client, Loại bỏ trùng lặp
	 * @return
	 */
	@JsonIgnore
	public List<GenTableColumn> getEditImportList() {
		List<GenTableColumn> clientImports = Lists.newArrayList();
		getClientImportList(this, clientImports, "edit");
		
		for (GenTable genTable : this.getChildList()){
			getClientImportList(genTable, clientImports, "edit");
		}
		
		return clientImports;
	}
	
	/**
	 * Có hay khôngbao gồmMật khẩukhung
	 * @return
	 */
	@JsonIgnore
	public boolean getHasPassWordInput() {
		for (GenTableColumn column : getGenTableColumnList()){
			if(column.getShowType() != null && "PassWordInput".equals(column.getShowType().getValue())
					&& "1".equals(column.getIsEdit())){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Có hay khôngnhiềuTệpTải lênnhómtệp
	 * @return
	 */
	@JsonIgnore
	public boolean getMutilFileUploadExists () {
		for (GenTableColumn column : getGenTableColumnList()){
			if(column.getShowType() != null && "MultiFileUpload".equals(column.getShowType().getValue())
					&& "1".equals(column.getIsEdit())){
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Lấy biến liên động của cửa sổ truy vấn và giá trị liên động của nó
	 * @return
	 */
	@JsonIgnore
	public Map<String, List<GenTableColumn>> getQueryInteractions(){
		Map<String, List<GenTableColumn>> map = Maps.newHashMap();
		for (GenTableColumn column : this.getGenTableColumnList()){
			if(!"1".equals(column.getIsQuery())) {
				continue;
			}
			String interaction = column.getInteraction();
			if(!StringUtils.isBlank(interaction)) {
				String arr[] = interaction.split(",");
				for(String key : arr) {
					// kích hoạtviệctệpCột
					GenTableColumnEx actionColumn = getColumByName(key);
					if(actionColumn == null || !"1".equals(actionColumn.getIsQuery())) {
						continue;
					}
					if(map.containsKey(key)) {
						List<GenTableColumn> colList = map.get(key);
						colList.add(column);
					}
					else {
						List<GenTableColumn> colList = Lists.newArrayList();
						colList.add(column);
						map.put(key, colList);
					}
				}
			}
		}
		return map;
	}
	
	/**
	 * Lấy biến liên động của cửa sổ chỉnh sửa và giá trị liên động của nó
	 * @return
	 */
	@JsonIgnore
	public Map<String, List<GenTableColumn>> getEditInteractions(){
		Map<String, List<GenTableColumn>> map = Maps.newHashMap();
		for (GenTableColumn column : this.getGenTableColumnList()){
			if(!"1".equals(column.getIsEdit())) {
				continue;
			}
			String interaction = column.getInteraction();
			if(!StringUtils.isBlank(interaction)) {
				String arr[] = interaction.split(",");
				for(String key : arr) {
					// kích hoạtviệctệpCột
					GenTableColumnEx actionColumn = getColumByName(key);
					if(actionColumn == null || !"1".equals(actionColumn.getIsEdit())) {
						continue;
					}
					if(map.containsKey(key)) {
						List<GenTableColumn> colList = map.get(key);
						colList.add(column);
					}
					else {
						List<GenTableColumn> colList = Lists.newArrayList();
						colList.add(column);
						map.put(key, colList);
					}
				}
			}
		}
		return map;
	}
	
	/**
	 * LấyCộtĐối tượng
	 * @param columnName  Dữ liệubảngCộttên
	 * @return
	 */
	@JsonIgnore
	public GenTableColumnEx getColumByName(String columnName) {
		for (GenTableColumn cc : this.getGenTableColumnList()){
			GenTableColumnEx column = (GenTableColumnEx)cc;
			if(columnName.equals(column.getName())) {
				return column;
			}
		}
		return null;
	}
	
	/**
	 * Khách hàngđầuNhập của tùy chỉnhLoạiXử lýgiao diện, Loại bỏ trùng lặp
	 * @return
	 */
	private void getClientImportList(GenTable genTable, List<GenTableColumn> clientImports, String type) {
		for (GenTableColumn column : genTable.getGenTableColumnList()){
			// làm là Truy vấnkhung của điều khiển, Nếu là This, vàvà là SelectTreeTránhtrùng lặpimportHàm
			if("query".equals(type) && "This".equals(column.getJavaType().getValue()) && "SelectTree".equals(column.getShowType().getValue())) {  
				continue;
			}
			
			if(column.getShowType() != null 
					&& ("Select".equals(column.getShowType().getValue()) || "SelectTree".equals(column.getShowType().getValue()))
					&& (("query".equals(type) && "1".equals(column.getIsQuery())) ||  ("edit".equals(type) && "1".equals(column.getIsEdit())))){
				if(column.getJavaType() != null && "This".equals(column.getJavaType().getValue())) {
					boolean found = false;
					for(GenTableColumn c: clientImports) {
						if(column.getJavaType().getValue().equals(c.getJavaType().getValue()) 
								&& column.getShowType().getValue().equals(c.getShowType().getValue())) {
							found = true;
							break;
						}
					}
					
					if(!found) {
						clientImports.add(column);
					}
				} else if(column.getCustomType() != null && !StringUtils.isNullOrEmpty(column.getCustomType().getId())) {
					boolean found = false;
					for(GenTableColumn c: clientImports) {
						if(column.getCustomType().getId().equals(c.getCustomType().getId()) 
								&& column.getShowType().getValue().equals(c.getShowType().getValue())) {
							found = true;
							break;
						}
					}
					
					if(!found) {
						clientImports.add(column);
					}
				}
			}
		}
	}
	
	/**
	 * Có hay khôngbao gồmluồngquy trìnhinstanceTrường
	 * @return
	 */
	@JsonIgnore
	public boolean getHasProcInst() {
		for (GenTableColumn column : getGenTableColumnList()){
			if(StringUtils.equalsIgnoreCase(column.getName(), "proc_inst")){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Có chứa trường trạng thái phiên bản quy trình không
	 * @return
	 */
	@JsonIgnore
	public boolean getHasProcStatus() {
		for (GenTableColumn column : getGenTableColumnList()){
			if(StringUtils.equalsIgnoreCase(column.getName(), "proc_status")){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Có chứa trường trạng thái phiên bản quy trình không
	 * @return
	 */
	@JsonIgnore
	public boolean getLockedExists() {
		for (GenTableColumn column : getGenTableColumnList()){
			if(StringUtils.equalsIgnoreCase(column.getName(), "is_locked")){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Có hay khôngcókhông phảiliên kết của conbảng
	 * @return
	 */
	@JsonIgnore
	public boolean getHasUnAssChildTable() {
		if(childList != null && childList.size() > 0) {
			for(GenTable child : childList) {
				if("0".equals( child.getIsAssociation())) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Có hay khôngbao gồmtự độngmã sốLoại của Trường
	 * @return
	 */
	@JsonIgnore
	public boolean getAutoSerialExists() {
		for (GenTableColumn column : getGenTableColumnList()){
			if(StringUtils.equalsIgnoreCase(column.getShowType().getValue(), "AutoSerial")){
				return true;
			}
		}
		
		// conbảng trong Có hay khôngcótự độngmã sốLoạiTrường
		if(childList != null && childList.size() > 0) {
			for(GenTable child : childList) {
				for (GenTableColumn column : child.getGenTableColumnList()){
					if(StringUtils.equalsIgnoreCase(column.getShowType().getValue(), "AutoSerial")){
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Có hay khôngChứa bộ định nghĩa thuộc tính của Trường
	 * @return
	 */
	@JsonIgnore
	public boolean getPropertyDefExists() {
		for (GenTableColumn column : getGenTableColumnList()){
			if(StringUtils.equalsIgnoreCase(column.getShowType().getValue(), "PropertyDef")){
				return true;
			}
		}
		return false;
	}

	/**
	 * Có hay khôngbao gồmthuộc tínhNhậpthiết bị của Trường
	 * @return
	 */
	@JsonIgnore
	public boolean getPropertyInputExists() {
		for (GenTableColumn column : getGenTableColumnList()){
			if(StringUtils.equalsIgnoreCase(column.getShowType().getValue(), "PropertyInput")){
				return true;
			}
		}
		return false;
	}
}