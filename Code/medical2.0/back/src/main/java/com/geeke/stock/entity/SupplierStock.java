package com.geeke.stock.entity;

import com.geeke.org.entity.Company;
import com.geeke.stock.entity.Supplier;
import com.geeke.stock.entity.Drug;
import com.geeke.stock.entity.SupplierStorage;
import com.geeke.stock.entity.Stuff;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import com.geeke.basicdata.entity.ManufactureFactory;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Tồn kho nhà cung cấpEntity
 * @author txl
 * @version 2022-06-09
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SupplierStock extends DataEntity<SupplierStock>  {

	private static final long serialVersionUID = 1005591224273698934L;
	private Company company;      // Phòng khámid 
	private Supplier supplierId;      // Nhà cung cấp 
	private Drug drug;      // Tên thuốc 
	private SupplierStorage supplierStorage;      // Phiếu nhập khoid
	private Stuff stuff;      // Tên vật tư
	private String norms;		// Quy cách
	private ManufactureFactory factory;      // Nhà sản xuất 
	private String batchNo;		// Số lô
	private Date produceDate;		// Ngày sản xuất
	private Date endDate;		// Thời hạn hiệu lực
	private Integer number;		// Số lượng
	private BigDecimal bid;		// Giá nhập
	private BigDecimal leastBid;		// Đơn vị nhỏ nhấtgiá nhập
	private BigDecimal retailPrice;		// Giá bán lẻ
	private BigDecimal allBid;		// Tổng giá nhập
	private BigDecimal allRetailPrice;		// Tổng giá bán lẻ
	private int initial;     //Tồn kho ban đầu
	private String cancellation;  //hủyđánh dấu
	private MedicinalStorageControl medicinalStorageControl;

	/** Trường BHYT */
	private  String drugtracinfo; //truy xuấtmãNútThông tin
	private  String drugTracCodg;//Mã truy xuất thuốc
	private  String cnt;//ybáoSố lượng
	
	public SupplierStock() {
		super();
	}

	public SupplierStock(String id){
		super(id);
	}
	
	public SupplierStock(SupplierStorage supplierStorage){
		this.supplierStorage = supplierStorage;
	}

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
    public Supplier getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Supplier supplierId) {
        this.supplierId = supplierId;
    }
    
    public Drug getDrug() {
        return drug;
    }

    public void setDrug(Drug drug) {
        this.drug = drug;
    }
    
    public SupplierStorage getSupplierStorage() {
        return supplierStorage;
    }

    public void setSupplierStorage(SupplierStorage supplierStorage) {
        this.supplierStorage = supplierStorage;
    }
    
    public Stuff getStuff() {
        return stuff;
    }

	@Length(min=0, max=64, message="Quy cáchĐộ dài phải nằm trong khoảng 0 đến 64 ")
	public String getNorms() {
		return norms;
	}
	public void setNorms(String norms) {
		this.norms = norms;
	}
	
	
    public ManufactureFactory getFactory() {
        return factory;
    }

    public void setFactory(ManufactureFactory factory) {
        this.factory = factory;
    }
    
	@Length(min=0, max=45, message="lôsốĐộ dài phải nằm trong khoảng 0 đến 45 ")
	public String getBatchNo() {
		return batchNo;
	}
	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}
	
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getProduceDate() {
		return produceDate;
	}
	public void setProduceDate(Date produceDate) {
		this.produceDate = produceDate;
	}
	
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	
	public Integer getNumber() {
		return number;
	}
	public void setNumber(Integer number) {
		this.number = number;
	}
	
	
	public BigDecimal getBid() {
		return bid;
	}
	public void setBid(BigDecimal bid) {
		this.bid = bid;
	}

	public BigDecimal getLeastBid() {
		return leastBid;
	}

	public void setLeastBid(BigDecimal leastBid) {
		this.leastBid = leastBid;
	}

	public BigDecimal getRetailPrice() {
		return retailPrice;
	}
	public void setRetailPrice(BigDecimal retailPrice) {
		this.retailPrice = retailPrice;
	}
	
	
	public BigDecimal getAllBid() {
		return allBid;
	}
	public void setAllBid(BigDecimal allBid) {
		this.allBid = allBid;
	}
	
	
	public BigDecimal getAllRetailPrice() {
		return allRetailPrice;
	}
	public void setAllRetailPrice(BigDecimal allRetailPrice) {
		this.allRetailPrice = allRetailPrice;
	}

	public int getInitial() {
		return initial;
	}

	public void setInitial(int initial) {
		this.initial = initial;
	}

	public String getCancellation() {
		return cancellation;
	}

	public void setCancellation(String cancellation) {
		this.cancellation = cancellation;
	}

	public MedicinalStorageControl getMedicinalStorageControl() {
		return medicinalStorageControl;
	}

	public void setMedicinalStorageControl(MedicinalStorageControl medicinalStorageControl) {
		this.medicinalStorageControl = medicinalStorageControl;
	}

	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "supplier_stock";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "1005591224273698934";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}