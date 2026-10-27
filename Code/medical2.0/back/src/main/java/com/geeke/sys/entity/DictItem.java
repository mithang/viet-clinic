package com.geeke.sys.entity;

import com.geeke.sys.entity.DictType;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
/**
 * Mục từ điểnEntity
 * @author lys
 * @version 2021-12-07
 */
public class DictItem extends DataEntity<DictItem> {

	private static final long serialVersionUID = 4005L;
	private Company company;      // thuộc vềtenant
	private DictType dictType;      // Bảng loại từ điểnID
	private String value;		// Giá trị

	
	public DictItem() {
		super();
	}

	public DictItem(String id){
		super(id);
	}
	
	public DictItem(DictType dictType){
		this.dictType = dictType;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	@NotNull(message="Bảng loại từ điểnIDKhông được để trống")
    public DictType getDictType() {
        return dictType;
    }

    public void setDictType(DictType dictType) {
        this.dictType = dictType;
    }
    
	@Length(min=1, max=128, message="Giá trịĐộ dài phải nằm trong khoảng 1 đến 128 ")
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "sys_dict_item";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "4005";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}