package com.geeke.stock.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Phiếu xuất khoEntity
 * @author txl
 * @version 2022-06-02/
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SupplierOutboundDetail extends DataEntity<SupplierOutboundDetail> {

	private static final long serialVersionUID = -2281869736391969854L;
	private Company company;      // Phòng khámid
	private Supplier supplier;      // Nhà cung cấpid
	private SupplierOutbound supplierOutbound;      // Phiếu xuất khoid
	private Drug drug;      // Tên thuốc
	private Stuff stuff;      // Tên vật tư
	private  Integer currentStock;//Tồn kho hiện tại
	private BigDecimal number;		// Xuất khoSố lượng
	private MedicinalStorageControl medicinalStorage;  //độngTồn khoChi tiết
	private  String remark;//Ghi chú
	private SupplierStock supplierStock;
	//Trường BHYT
	private  String cnt;//ybáoSố lượng
	private  String drugTracCodg;

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public Supplier getSupplier() {
		return supplier;
	}

	public void setSupplier(Supplier supplier) {
		this.supplier = supplier;
	}

	public SupplierOutbound getSupplierOutbound() {
		return supplierOutbound;
	}

	public void setSupplierOutbound(SupplierOutbound supplierOutbound) {
		this.supplierOutbound = supplierOutbound;
	}

	public Drug getDrug() {
		return drug;
	}

	public void setDrug(Drug drug) {
		this.drug = drug;
	}

	public Stuff getStuff() {
		return stuff;
	}

	public void setStuff(Stuff stuff) {
		this.stuff = stuff;
	}

	public Integer getCurrentStock() {
		return currentStock;
	}

	public void setCurrentStock(Integer currentStock) {
		this.currentStock = currentStock;
	}

	public BigDecimal getNumber() {
		return number;
	}

	public void setNumber(BigDecimal number) {
		this.number = number;
	}

	public MedicinalStorageControl getMedicinalStorage() {
		return medicinalStorage;
	}

	public void setMedicinalStorage(MedicinalStorageControl medicinalStorageControl) {
		this.medicinalStorage = medicinalStorageControl;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public SupplierStock getSupplierStock() {
		return supplierStock;
	}

	public void setSupplierStock(SupplierStock supplierStock) {
		this.supplierStock = supplierStock;
	}

	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "supplier_outbound_detail";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "1007238052174135324";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return false;
    }
}