package com.geeke.stock.entity;

import com.geeke.org.entity.Company;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;

import java.math.BigDecimal;

/**
 * Kiểm kê tồn khoEntity
 * @author rys
 * @version 2022-11-02
 */
public class InventoryVerification extends DataEntity<InventoryVerification> {

	private static final long serialVersionUID = 1235640942656921602L;
	private Company company;      // Phòng khámid 
	private String code;		// kiểm kêđơnsố
	private String status;		// kiểm kêTrạng thái
	private String type;		//kiểm kêLoại(0: Thuốc, 1: Vật tư)
	private BigDecimal allPrice;   //lãi lỗtổngSố tiền

	
	public InventoryVerification() {
		super();
	}

	public InventoryVerification(String id){
		super(id);
	}
	

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
	@Length(min=1, max=255, message="kiểm kêđơnsốĐộ dài phải nằm trong khoảng 1 đến 255 ")
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	
	@Length(min=0, max=1, message="kiểm kêĐộ dài trạng thái phải nằm trong khoảng 0 đến 1 ")
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public BigDecimal getAllPrice() {
		return allPrice;
	}

	public void setAllPrice(BigDecimal allPrice) {
		this.allPrice = allPrice;
	}

	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "inventory_verification";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "1235640942656921602";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return false;
    }
}