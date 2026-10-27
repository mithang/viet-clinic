package com.geeke.stock.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Phiếu xuất khoEntity
 * @author txl
 * @version 2022-06-02/
 */
public class SupplierOutbound extends DataEntity<SupplierOutbound> {

	private static final long serialVersionUID = -2281869736391969855L;
	private Company company;      // Phòng khámid 
	private Supplier supplier;      // Nhà cung cấpid 
	private String code;		// Phiếu xuất kho
	private Integer breed;		// Chủng loại
	private Integer number;		// Số lượng
	private BigDecimal allBid;		// Tổng giá nhập
	private BigDecimal allRetailPrice;		// Tổng giá bán lẻ
	private Date cancelDate;		// hủyNgày
	private String examineBy;      // phê duyệtngười
	private Date examineDate;		// phê duyệtThời gian
	//private Integer initial;     //Tồn kho ban đầu
	private String type;    //Xuất khoLoại 1-Thuốc 2-Vật tư
	private String method;    //Xuất khocách 1-Thuốcbáo损 2-trảcòn供方 3-điều拔Xuất kho
	private String reason;    //Xuất khogốcdo
	private  Integer status;//Trạng thái 1-Chưaxét duyệt  2-Đãxét duyệt 3-Đãhủy

	public SupplierOutbound() {
		super();
	}

	public SupplierOutbound(String id){
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
    
	@Length(min=0, max=64, message="Xuất khođơnĐộ dài phải nằm trong khoảng 0 đến 64 ")
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
	
	
    public String getExamineBy() {
        return examineBy;
    }

    public void setExamineBy(String examineBy) {
        this.examineBy = examineBy;
    }
    
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getExamineDate() {
		return examineDate;
	}
	public void setExamineDate(Date examineDate) {
		this.examineDate = examineDate;
	}



	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "supplier_outbound";
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