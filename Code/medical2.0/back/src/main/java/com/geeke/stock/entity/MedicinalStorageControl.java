package com.geeke.stock.entity;

import com.geeke.org.entity.Company;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Thuốc/Kiểm soát chi tiết nhập kho vật tưEntity
 * @author hl
 * @version 2022-09-26
 */
public class MedicinalStorageControl extends DataEntity<MedicinalStorageControl> {

	private static final long serialVersionUID = 1139486400240443448L;
	private Company company;      // Phòng khámID 
	private String storageId;		// Chi tiết nhập khoID
	private String drugStuffId;		// Thuốc/Vật tưID
	private String drugStuffName;		// Thuốc/Tên vật tư
	private Integer type;		// Loại(1: Thuốc; 2: Vật tư)
	private BigDecimal storageStock;		// Tổng lượng nhập kho
	private BigDecimal usedStock;		// Số lượng tồn kho đã sử dụng
	private BigDecimal occupyStock;		// Số lượng tồn kho đang bị chiếm dụng
	private BigDecimal surplusStock;		// Số lượng tồn kho còn khả dụng
	private BigDecimal reimburseStock;		// Số lượng tồn kho báo hỏng
	private BigDecimal returnTodoFeeStock;		// Trả thuốcChưahoàn phílượng
	private Drug drug;
	private Stuff stuff;
	private InventoryVerification inventoryVerification;
	private String inventoryVerificationDetailId;       //Chi tiết kiểm kê tồn khoid
	private SupplierStock supplierStock;

	
	public MedicinalStorageControl() {
		super();
	}

	public MedicinalStorageControl(String id){
		super(id);
	}
	

	@NotNull(message="Phòng khámIDKhông được để trống")
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
	@Length(min=1, max=20, message="Chi tiết nhập khoIDĐộ dài phải từ 1  và  20 Giữa")
	public String getStorageId() {
		return storageId;
	}
	public void setStorageId(String storageId) {
		this.storageId = storageId;
	}
	
	
	@Length(min=1, max=20, message="Thuốc/Vật tưIDĐộ dài phải từ 1  và  20 Giữa")
	public String getDrugStuffId() {
		return drugStuffId;
	}
	public void setDrugStuffId(String drugStuffId) {
		this.drugStuffId = drugStuffId;
	}
	
	
	@Length(min=1, max=255, message="Thuốc/Độ dài tên vật tư phải nằm trong khoảng 1 đến 255 ")
	public String getDrugStuffName() {
		return drugStuffName;
	}
	public void setDrugStuffName(String drugStuffName) {
		this.drugStuffName = drugStuffName;
	}
	
	
	@NotNull(message="Loại(1: Thuốc; 2: Vật tư)Không được để trống")
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	
	
	@NotNull(message="Tổng lượng nhập khoKhông thể là trống")
	public BigDecimal getStorageStock() {
		return storageStock;
	}
	public void setStorageStock(BigDecimal storageStock) {
		this.storageStock = storageStock;
	}
	
	
	@NotNull(message="Số lượng tồn kho đã sử dụngKhông thể là trống")
	public BigDecimal getUsedStock() {
		return usedStock;
	}
	public void setUsedStock(BigDecimal usedStock) {
		this.usedStock = usedStock;
	}
	
	
	@NotNull(message="Số lượng tồn kho đang bị chiếm dụngKhông thể là trống")
	public BigDecimal getOccupyStock() {
		return occupyStock;
	}
	public void setOccupyStock(BigDecimal occupyStock) {
		this.occupyStock = occupyStock;
	}
	
	
	@NotNull(message="Số lượng tồn kho còn khả dụngKhông thể là trống")
	public BigDecimal getSurplusStock() {
		return surplusStock;
	}
	public void setSurplusStock(BigDecimal surplusStock) {
		this.surplusStock = surplusStock;
	}
	
	
	@NotNull(message="Số lượng tồn kho báo hỏngKhông thể là trống")
	public BigDecimal getReimburseStock() {
		return reimburseStock;
	}
	public void setReimburseStock(BigDecimal reimburseStock) {
		this.reimburseStock = reimburseStock;
	}

	public BigDecimal getReturnTodoFeeStock() {
		return returnTodoFeeStock;
	}

	public void setReturnTodoFeeStock(BigDecimal returnTodoFeeStock) {
		this.returnTodoFeeStock = returnTodoFeeStock;
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


	public InventoryVerification getInventoryVerification() {
		return inventoryVerification;
	}

	public void setInventoryVerification(InventoryVerification inventoryVerification) {
		this.inventoryVerification = inventoryVerification;
	}

	public String getInventoryVerificationDetailId() {
		return inventoryVerificationDetailId;
	}

	public void setInventoryVerificationDetailId(String inventoryVerificationDetailId) {
		this.inventoryVerificationDetailId = inventoryVerificationDetailId;
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
        return "medicinal_storage_control";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "1139486400240443448";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}