package com.geeke.sys.entity;

import org.hibernate.validator.constraints.Length;
import java.util.List;
import com.google.common.collect.Lists;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
/**
 * Loại từ điểnEntity
 * @author lys
 * @version 2021-12-07
 */
public class DictType extends DataEntity<DictType> {

	private static final long serialVersionUID = 4002L;
	private Company company;      // thuộc vềtenant
	private String code;		// Loại từ điểnMã
	private String isSystem;		// Hệ thốngcấp

	private List<DictItem> dictItemList = Lists.newArrayList();		// Danh sách bảng con
	
	public DictType() {
		super();
	}

	public DictType(String id){
		super(id);
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	@Length(min=1, max=64, message="Độ dài mã loại từ điển phải nằm trong khoảng 1 đến 64 ")
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	
	@Length(min=1, max=1, message="Hệ thốngcấpĐộ dài phải nằm trong khoảng 1 đến 1 ")
	public String getIsSystem() {
		return isSystem;
	}
	public void setIsSystem(String isSystem) {
		this.isSystem = isSystem;
	}
	
	
	
	public List<DictItem> getDictItemList() {
		return dictItemList;
	}

	public void setDictItemList(List<DictItem> dictItemList) {
		this.dictItemList = dictItemList;
	}
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "sys_dict_type";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "4002";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}