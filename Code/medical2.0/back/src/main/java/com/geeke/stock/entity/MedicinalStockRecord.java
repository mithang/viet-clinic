package com.geeke.stock.entity;

import com.geeke.org.entity.Company;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Bản ghi thao tác tồn khoEntity
 * @author hl
 * @version 2022-09-26
 */
public class MedicinalStockRecord extends DataEntity<MedicinalStockRecord> {

	private static final long serialVersionUID = 1139486400240443467L;
	private Company company;      // Phòng khámID 
	private String drugStuffId;		// Thuốc/Vật tưID
	private String drugStuffName;		// Thuốc/Tên vật tư
	private Integer type;		// Loại(1: Thuốc; 2: Vật tư)
	private Integer operationType;		// Loại thao tác: -2Chi tiết nhập khoĐếnkỳtự độnghủy -1hủynhập khoThông tin   1nhập khothao tác  2chiếm dụng trướcTồn kho   3thực tếchiếm dụng  4Trả thuốctạmChưahoàn phí 6-Xuất khođơnXuất kho
	private BigDecimal operationStock;		// Số lượng tồn kho thao tác
	private String storageId;		// Chi tiết nhập khoID
	private String recipelInfoId;		// Đơn thuốcID
	private String refId;		// liên kếtBản ghiID

	
	public MedicinalStockRecord() {
		super();
	}

	public MedicinalStockRecord(String id){
		super(id);
	}
	

	@NotNull(message="Phòng khámIDKhông được để trống")
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
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
	
	
	public Integer getOperationType() {
		return operationType;
	}
	public void setOperationType(Integer operationType) {
		this.operationType = operationType;
	}
	
	
	@NotNull(message="Số lượng tồn kho thao tácKhông thể là trống")
	public BigDecimal getOperationStock() {
		return operationStock;
	}
	public void setOperationStock(BigDecimal operationStock) {
		this.operationStock = operationStock;
	}
	
	
	@Length(min=1, max=20, message="Chi tiết nhập khoIDĐộ dài phải từ 1  và  20 Giữa")
	public String getStorageId() {
		return storageId;
	}
	public void setStorageId(String storageId) {
		this.storageId = storageId;
	}
	
	
	@Length(min=0, max=20, message="Đơn thuốcIDĐộ dài phải từ 0  và  20 Giữa")
	public String getRecipelInfoId() {
		return recipelInfoId;
	}
	public void setRecipelInfoId(String recipelInfoId) {
		this.recipelInfoId = recipelInfoId;
	}
	
	
	@Length(min=0, max=255, message="liên kếtBản ghiIDĐộ dài phải từ 0  và  255 Giữa")
	public String getRefId() {
		return refId;
	}
	public void setRefId(String refId) {
		this.refId = refId;
	}
	
	
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "medicinal_stock_record";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "1139486400240443467";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}