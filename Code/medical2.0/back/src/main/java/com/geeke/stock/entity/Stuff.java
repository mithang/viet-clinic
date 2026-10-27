package com.geeke.stock.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.basicdata.entity.ManufactureFactory;
import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
import com.geeke.sys.entity.DictItem;
import com.geeke.utils.excel.ExcelImportField;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
/**
 * Thông tin vật tưEntity
 * @author txl
 * @version 2022-06-22
 */
public class Stuff extends DataEntity<Stuff> {

	private static final long serialVersionUID = 1004462867645374480L;
    private Company company;      // Phòng khámid
    private String syncId;     //đồng bộquađến của ID
    private int syncNum;    //ThuốcCó hay khôngĐãđồng bộ
	private String commonName;		// thườngdùngtên
	private String code;		// Vật tưMã
	private DictItem type;      // Loại vật tư 
	private ManufactureFactory factory;      // Nhà sản xuất 
	private DictItem nature;      // Tính chất 
	private String barCode;		// Mã vạch
	private String registrationName;		// đăng kýchứngTên
	private String registrationCode;		// đăng kýchứngsố
	private String specifications;		// Quy cách
	private Integer packNumber;		// Số lượng đóng gói
	private DictItem minUnit;      // Đơn vị nhỏ nhất 
	private DictItem packUnit;      // Đơn vị đóng gói 
	private String isOutSell;		// Cho phép bán ra ngoài
	private BigDecimal priceOutSell;		// Giá bán ra ngoài
	private String isUnpackSell;		// Cho phép bán lẻ
	private BigDecimal retailPrice;		// Giá bán lẻ sau khi tách
	private String status;		// Cờ bật
	private String pinyinCode;		// Mã pinyin
	private String stockNumber;     //Tồn kho
	private int inventory;  //Tồn kho
	private String locationNumber;   //hàngvịsố
	private String inventoryLimit;   //Tồn khotrêngiới hạn
	private String inventoryFloor;   //Tồn khodướigiới hạn

	private InventoryVerification inventoryVerification;  //Kiểm kê tồn kho
	private String inventoryVerificationDetailId;       //Chi tiết kiểm kê tồn khoid
	private BigDecimal bid;      //Tổng chi phí
	private String indate;        //Thời hạn hiệu lực
	private MedicinalStockControl stock;


	public int getInventory() {
		return inventory;
	}

	public void setInventory(int inventory) {
		this.inventory = inventory;
	}
	public Stuff() {
		super();
	}

	public Stuff(String id){
		super(id);
	}

    public String getSyncId() {
        return syncId;
    }

    public void setSyncId(String syncId) {
        this.syncId = syncId;
    }

    public int getSyncNum() {
        return syncNum;
    }

    public void setSyncNum(int syncNum) {
        this.syncNum = syncNum;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
	@ExcelImportField(columnIndex = 1, label = "Tên chung", maxLength = 45)
	@Length(min=0, max=45, message="thườngdùngtênĐộ dài phải nằm trong khoảng 0 đến 45 ")
	public String getCommonName() {
		return commonName;
	}
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}
	
	
	@Length(min=0, max=45, message="Vật tưĐộ dài mã phải nằm trong khoảng 0 đến 45 ")
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	
    @ExcelImportField(columnIndex = 3, label = "Loại vật tư", dictTypeId = "1004462867645374476")
    public DictItem getType() {
        return type;
    }

    public void setType(DictItem type) {
        this.type = type;
    }
    
    public ManufactureFactory getFactory() {
        return factory;
    }

    public void setFactory(ManufactureFactory factory) {
        this.factory = factory;
    }
    
    @ExcelImportField(columnIndex = 5, label = "Vật tưtính chất", dictTypeId = "1004078055755374607")
    public DictItem getNature() {
        return nature;
    }

    public void setNature(DictItem nature) {
        this.nature = nature;
    }
    
	@ExcelImportField(columnIndex = 6, label = "mụcmã", maxLength = 64)
	@Length(min=0, max=64, message="mã vạchĐộ dài phải nằm trong khoảng 0 đến 64 ")
	public String getBarCode() {
		return barCode;
	}
	public void setBarCode(String barCode) {
		this.barCode = barCode;
	}
	
	
	@ExcelImportField(columnIndex = 7, label = "đăng kýTên", maxLength = 64)
	@Length(min=0, max=64, message="đăng kýchứngTênĐộ dài phải nằm trong khoảng 0 đến 64 ")
	public String getRegistrationName() {
		return registrationName;
	}
	public void setRegistrationName(String registrationName) {
		this.registrationName = registrationName;
	}
	
	
	@ExcelImportField(columnIndex = 8, label = "đăng kýchứngsố", maxLength = 64)
	@Length(min=0, max=64, message="đăng kýchứngsốĐộ dài phải nằm trong khoảng 0 đến 64 ")
	public String getRegistrationCode() {
		return registrationCode;
	}
	public void setRegistrationCode(String registrationCode) {
		this.registrationCode = registrationCode;
	}
	
	
	@ExcelImportField(columnIndex = 9, label = "Quy cách", maxLength = 45)
	@Length(min=0, max=45, message="Quy cáchĐộ dài phải nằm trong khoảng 0 đến 45 ")
	public String getSpecifications() {
		return specifications;
	}
	public void setSpecifications(String specifications) {
		this.specifications = specifications;
	}
	
	
	@ExcelImportField(columnIndex = 10, label = "Số lượng đóng gói")
	@NotNull(message="Số lượng đóng góiKhông thể là trống")
	public Integer getPackNumber() {
		return packNumber;
	}
	public void setPackNumber(Integer packNumber) {
		this.packNumber = packNumber;
	}
	
	
	@ExcelImportField(columnIndex = 11, label = "Đơn vị bào chế", dictTypeId = "1004406758192578593")
	@NotNull(message="Đơn vị nhỏ nhấtKhông thể là trống")
    public DictItem getMinUnit() {
        return minUnit;
    }

    public void setMinUnit(DictItem minUnit) {
        this.minUnit = minUnit;
    }
    
	@ExcelImportField(columnIndex = 12, label = "Đơn vị đóng gói", dictTypeId = "1004406758192578597")
	@NotNull(message="Đơn vị đóng góiKhông thể là trống")
    public DictItem getPackUnit() {
        return packUnit;
    }

    public void setPackUnit(DictItem packUnit) {
        this.packUnit = packUnit;
    }
    
	@ExcelImportField(columnIndex = 13, label = "Có hay khôngbán ra ngoài", maxLength = 2)
	@Length(min=0, max=2, message="Cho phép bán ra ngoàiĐộ dài phải nằm trong khoảng 0 đến 2 ")
	public String getIsOutSell() {
		return isOutSell;
	}
	public void setIsOutSell(String isOutSell) {
		this.isOutSell = isOutSell;
	}
	
	
	@ExcelImportField(columnIndex = 14, label = "bán ra ngoàiGiá")
	public BigDecimal getPriceOutSell() {
		return priceOutSell;
	}
	public void setPriceOutSell(BigDecimal priceOutSell) {
		this.priceOutSell = priceOutSell;
	}
	
	
	@ExcelImportField(columnIndex = 15, label = "Có bán lẻ không", maxLength = 2)
	@Length(min=0, max=2, message="Độ dài cho phép bán lẻ phải nằm trong khoảng 0 đến 2 ")
	public String getIsUnpackSell() {
		return isUnpackSell;
	}
	public void setIsUnpackSell(String isUnpackSell) {
		this.isUnpackSell = isUnpackSell;
	}
	
	
	@ExcelImportField(columnIndex = 16, label = "Giá bán lẻ")
	public BigDecimal getRetailPrice() {
		return retailPrice;
	}
	public void setRetailPrice(BigDecimal retailPrice) {
		this.retailPrice = retailPrice;
	}


	@ExcelImportField(columnIndex = 19, label = "Trạng thái", maxLength = 1)
	@Length(min=1, max=1, message="Cờ bậtĐộ dài phải nằm trong khoảng 1 đến 1 ")
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}


	@ExcelImportField(columnIndex = 2, label = "Mã pinyin", maxLength = 100)
	@Length(min=0, max=100, message="PinyinmãĐộ dài phải nằm trong khoảng 0 đến 100 ")
	public String getPinyinCode() {
		return pinyinCode;
	}
	public void setPinyinCode(String pinyinCode) {
		this.pinyinCode = pinyinCode;
	}

	public String getStockNumber() {
		return stockNumber;
	}

	public void setStockNumber(String stockNumber) {
		this.stockNumber = stockNumber;
	}

	public String getLocationNumber() {
		return locationNumber;
	}

	public void setLocationNumber(String locationNumber) {
		this.locationNumber = locationNumber;
	}

	public String getInventoryLimit() {
		return inventoryLimit;
	}

	public void setInventoryLimit(String inventoryLimit) {
		this.inventoryLimit = inventoryLimit;
	}

	public String getInventoryFloor() {
		return inventoryFloor;
	}

	public void setInventoryFloor(String inventoryFloor) {
		this.inventoryFloor = inventoryFloor;
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

	public BigDecimal getBid() {
		return bid;
	}

	public void setBid(BigDecimal bid) {
		this.bid = bid;
	}

	public String getIndate() {
		return indate;
	}

	public void setIndate(String indate) {
		this.indate = indate;
	}

	public MedicinalStockControl getStock() {
		return stock;
	}

	public void setStock(MedicinalStockControl stock) {
		this.stock = stock;
	}

	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "stuff";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "1004462867645374480";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}