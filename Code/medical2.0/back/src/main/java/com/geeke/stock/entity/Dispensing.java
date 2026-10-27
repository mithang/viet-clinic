package com.geeke.stock.entity;

import com.geeke.org.entity.Company;
import com.geeke.outpatient.entity.Registration;
import javax.validation.constraints.NotNull;
import com.geeke.stock.entity.SupplierStock;
import com.geeke.outpatient.entity.RecipelInfo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Chi tiết phát thuốcEntity
 * @author txl
 * @version 2022-08-11
 */
public class Dispensing extends DataEntity<Dispensing> {

	private static final long serialVersionUID = 1025152944882663511L;
	private Company company;      // Phòng khámid 
	private Registration registration;      // Đăng ký khámid 
	private SupplierStock supplierStock;      // Tồn khoid 
	private RecipelInfo recipelInfo;      // Thông tin đơn thuốcid 
	private Integer number;		// Số lượng

	
	public Dispensing() {
		super();
	}

	public Dispensing(String id){
		super(id);
	}
	

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
	@NotNull(message="Đăng ký khámidKhông được để trống")
    public Registration getRegistration() {
        return registration;
    }

    public void setRegistration(Registration registration) {
        this.registration = registration;
    }
    
	@NotNull(message="Tồn khoidKhông được để trống")
    public SupplierStock getSupplierStock() {
        return supplierStock;
    }

    public void setSupplierStock(SupplierStock supplierStock) {
        this.supplierStock = supplierStock;
    }
    
    public RecipelInfo getRecipelInfo() {
        return recipelInfo;
    }

    public void setRecipelInfo(RecipelInfo recipelInfo) {
        this.recipelInfo = recipelInfo;
    }
    
	public Integer getNumber() {
		return number;
	}
	public void setNumber(Integer number) {
		this.number = number;
	}
	
	
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "dispensing";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "1025152944882663511";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}