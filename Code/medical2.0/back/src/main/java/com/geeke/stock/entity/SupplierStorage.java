package com.geeke.stock.entity;

import com.geeke.org.entity.Company;
import com.geeke.stock.entity.Supplier;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.geeke.sys.entity.DictItem;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Phiếu nhập khoEntity
 * @author txl
 * @version 2022-06-02
 */
public class SupplierStorage extends DataEntity<SupplierStorage> {

	private static final long serialVersionUID = 1007238052174135324L;
	private Company company;      // Phòng khámid 
	private Supplier supplier;      // Nhà cung cấpid 
	private String code;		// Phiếu nhập kho
	private Integer breed;		// Chủng loại
	private Integer number;		// Số lượng
	private BigDecimal allBid;		// Tổng giá nhập
	private BigDecimal allRetailPrice;		// Tổng giá bán lẻ
	private Date cancelDate;		// hủyNgày
	private DictItem examine;      // Trạng thái xét duyệt 
	private Date examineDate;		// phê duyệtThời gian
	private String withGoodsNo;		// 随hàngđơn
	private String invoiceNumber;		// Số hóa đơn
	private int initial;     //Tồn kho ban đầu
	private String type;    //nhập khoLoại
	
	public SupplierStorage() {
		super();
	}

	public SupplierStorage(String id){
		super(id);
	}
	

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
	@NotNull(message="Nhà cung cấpidKhông được để trống")
    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }
    
	@Length(min=0, max=64, message="phiếu nhập khoĐộ dài phải nằm trong khoảng 0 đến 64 ")
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	
	public Integer getBreed() {
		return breed;
	}
	public void setBreed(Integer breed) {
		this.breed = breed;
	}
	
	
	public Integer getNumber() {
		return number;
	}
	public void setNumber(Integer number) {
		this.number = number;
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
	
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getCancelDate() {
		return cancelDate;
	}
	public void setCancelDate(Date cancelDate) {
		this.cancelDate = cancelDate;
	}
	
	
    public DictItem getExamine() {
        return examine;
    }

    public void setExamine(DictItem examine) {
        this.examine = examine;
    }
    
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getExamineDate() {
		return examineDate;
	}
	public void setExamineDate(Date examineDate) {
		this.examineDate = examineDate;
	}
	
	
	@Length(min=0, max=64, message="随hàngđơnĐộ dài phải nằm trong khoảng 0 đến 64 ")
	public String getWithGoodsNo() {
		return withGoodsNo;
	}
	public void setWithGoodsNo(String withGoodsNo) {
		this.withGoodsNo = withGoodsNo;
	}
	
	
	@Length(min=0, max=64, message="phátphiếusốĐộ dài phải nằm trong khoảng 0 đến 64 ")
	public String getInvoiceNumber() {
		return invoiceNumber;
	}
	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public int getInitial() {
		return initial;
	}

	public void setInitial(int initial) {
		this.initial = initial;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "supplier_storage";
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
            return true;
    }
}