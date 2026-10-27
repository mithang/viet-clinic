package com.geeke.stock.entity;

import com.geeke.org.entity.Company;
import javax.validation.constraints.NotNull;
import com.geeke.stock.entity.InventoryVerification;
import com.geeke.stock.entity.Drug;
import com.geeke.stock.entity.Stuff;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;

import java.math.BigDecimal;

/**
 * Chi tiết kiểm kê tồn khoEntity
 * @author Quản trị viên cấp cao
 * @version 2022-11-02
 */
public class InventoryVerificationDetail extends DataEntity<InventoryVerificationDetail> {

	private static final long serialVersionUID = 1235640942656921614L;
	private Company company;      // Phòng khámid 
	private InventoryVerification inventoryVerification;      // Kiểm kê tồn khoid 
	private Drug drug;      // kiểm kêThuốcid 
	private Stuff stuff;      // kiểm kêVật tưid 
	private Integer currentInventory;		// Tồn kho hiện tại
	private Integer checkInventory;		// Tồn kho kiểm kê
	private Integer profitAndLoss;		// lãi lỗ
    private BigDecimal profitAndLossPrice;  //lãi lỗSố tiền
    private MedicinalStorageControl medicinalStorageControl;  //độngTồn khoChi tiết
    private SupplierStock supplierStock;

	
	public InventoryVerificationDetail() {
		super();
	}

	public InventoryVerificationDetail(String id){
		super(id);
	}
	

	@NotNull(message="Phòng khámidKhông được để trống")
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
	@NotNull(message="Kiểm kê tồn khoidKhông được để trống")
    public InventoryVerification getInventoryVerification() {
        return inventoryVerification;
    }

    public void setInventoryVerification(InventoryVerification inventoryVerification) {
        this.inventoryVerification = inventoryVerification;
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
    
	public Integer getCurrentInventory() {
		return currentInventory;
	}
	public void setCurrentInventory(Integer currentInventory) {
		this.currentInventory = currentInventory;
	}
	
	
	public Integer getCheckInventory() {
		return checkInventory;
	}
	public void setCheckInventory(Integer checkInventory) {
		this.checkInventory = checkInventory;
	}
	
	
	public Integer getProfitAndLoss() {
		return profitAndLoss;
	}
	public void setProfitAndLoss(Integer profitAndLoss) {
		this.profitAndLoss = profitAndLoss;
	}


    public BigDecimal getProfitAndLossPrice() {
        return profitAndLossPrice;
    }

    public void setProfitAndLossPrice(BigDecimal profitAndLossPrice) {
        this.profitAndLossPrice = profitAndLossPrice;
    }

    public MedicinalStorageControl getMedicinalStorageControl() {
        return medicinalStorageControl;
    }

    public void setMedicinalStorageControl(MedicinalStorageControl medicinalStorageControl) {
        this.medicinalStorageControl = medicinalStorageControl;
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
        return "inventory_verification_detail";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "1235640942656921614";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return false;
    }
}