package com.geeke.stock.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.basicdata.entity.ManufactureFactory;
import com.geeke.common.constants.BizConstants;
import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
import com.geeke.sys.entity.DictItem;
import com.geeke.utils.excel.ExcelImportField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
/**
 * Thông tin thuốcEntity
 * @author txl
 * @version 2022-06-07
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Drug extends DataEntity<Drug> {

	private static final long serialVersionUID = 1004078055755374623L;
	@ExcelImportField(columnIndex = 0, required = true, maxLength = 128, label = "Tên thuốc")
	private String goodsName;		// Tên thuốc

    private String syncId;     //đồng bộquađến của ID

    private int syncNum;    //ThuốcCó hay khôngĐãđồng bộ


	private Company company;      // Phòng khámid 
	@ExcelImportField(columnIndex = 1, maxLength = 128, label = "Tên hàng hóa")
	private String brandName;		// Tên hàng hóa
	@ExcelImportField(columnIndex = 3, required = true, dictTypeId = BizConstants.DICT_TYPE_DRUG_TYPE, label = "Loại thuốc")
	private DictItem type;      // Loại thuốc
	private String code;		// Mã thuốc
	@ExcelImportField(columnIndex = 4, maxLength = 128, label = "Nguồn thuốc")
	private String source;		// Nguồn thuốc
	@ExcelImportField(columnIndex = 5, dictTypeId = BizConstants.DICT_TYPE_DRUG_NATURE, label = "Thuốctính chất")
	private DictItem nature;      // Tính chất
	private ManufactureFactory factory;      // Nhà sản xuất 
	@ExcelImportField(columnIndex = 7, maxLength = 64, label = "Số đăng ký thuốc quốc gia")
	private String standardCode;		// Số đăng ký thuốc quốc gia
	@ExcelImportField(columnIndex = 8, maxLength = 64, label = "nàyvịmã")
	private String bitCode;		// nàyvịmã
	@ExcelImportField(columnIndex = 9, maxLength = 64, label = "Mã vạch")
	private String barCode;		// Mã vạch
	@ExcelImportField(columnIndex = 10, maxLength = 64, label = "Mã BHYT")
	private String insuranceCode;		// Mã BHYT
	@ExcelImportField(columnIndex = 11, maxLength = 45, label = "Liều lượng")
	private String dosis;		// Liều lượng
	@ExcelImportField(columnIndex = 12, dictTypeId = BizConstants.DICT_TYPE_DOSAGE_UNIT, label = "Đơn vị liều lượng")
	private DictItem dosisUnit;      // Đơn vị liều lượng
	@ExcelImportField(columnIndex = 13, maxLength = 45, label = "Chế phẩm")
	private String preparation;		// Chế phẩm
	@ExcelImportField(columnIndex = 14, dictTypeId = BizConstants.DICT_TYPE_PREPARATION_UNIT, label = "Đơn vị bào chế")
	private DictItem preparationUnit;      // Đơn vị bào chế
	@ExcelImportField(columnIndex = 15, dictTypeId = BizConstants.DICT_TYPE_PACK_SPEC, label = "Quy cách đóng gói")
	private DictItem pack;      // Đóng gói
	@ExcelImportField(columnIndex = 16, label = "Giá bán")
	private BigDecimal price;		// Giá bán
	@ExcelImportField(columnIndex = 17, maxLength = 1, label = "Có bán lẻ không")
	private String isUnpackSell;		// Cho phép bán lẻ
	@ExcelImportField(columnIndex = 18, label = "Giá bán lẻ")
	private BigDecimal retailPrice;		// Giá bán lẻ sau khi tách
	private String stockNumber;	//Số lượng tồn kho
	private String stockUnit; //Tồn khoĐơn vị
	@ExcelImportField(columnIndex = 19, maxLength = 1, label = "Trạng thái")
	private String status;   //Cờ bật
	@ExcelImportField(columnIndex = 2, maxLength = 64, label = "Mã pinyin")
	private String pinyinCode;  //Mã pinyin
	private int inventory;  //Tồn kho
	private DictItem westernMedicineUse;      // Cách dùng thuốc Tây
	private DictItem chineseMedicineUse;      // Cách dùng thuốc Đông y
	private Integer singleDosage;		// Liều dùng mỗi lần
	@ExcelImportField(columnIndex = 21, label = "Tổng số lượng")
	private Integer total;		// Tổng số lượng
	private DictItem frequency;      // Tần suất và liều dùng
	private DictItem days;      // Số ngày
	private String locationNumber;   //hàngvịsố
	private String inventoryLimit;   //Tồn khotrêngiới hạn
	private String inventoryFloor;   //Tồn khodướigiới hạn
	private BigDecimal bid;    //giá nhậpTổng cộng

	private InventoryVerification inventoryVerification;  //Kiểm kê tồn kho
	private String inventoryVerificationDetailId;       //Chi tiết kiểm kê tồn khoid

	private String indate;          //Thời hạn hiệu lực
	private MedicinalStockControl stock;
    private  String ypgg ;
	/*Trường BHYT*/
	private  String rxFlag;//Có hay khôngĐơn thuốc 0  1

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

	public int getInventory() {
		return inventory;
	}

	public void setInventory(int inventory) {
		this.inventory = inventory;
	}


	public Drug() {
		super();
	}

	public Drug(String id){
		super(id);
	}
	

	@Length(min=1, max=128, message="ThuốcTênĐộ dài phải nằm trong khoảng 1 đến 128 ")
	public String getGoodsName() {
		return goodsName;
	}
	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}
	
	
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
	@Length(min=0, max=128, message="Tên hàng hóaĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getBrandName() {
		return brandName;
	}
	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}
	
	
	@NotNull(message="Loại thuốcKhông thể là trống")
    public DictItem getType() {
        return type;
    }

    public void setType(DictItem type) {
        this.type = type;
    }
    
	@Length(min=0, max=64, message="ThuốcĐộ dài mã phải nằm trong khoảng 0 đến 64 ")
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	
	@Length(min=0, max=128, message="Nguồn thuốcĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	
	
    public DictItem getNature() {
        return nature;
    }

    public void setNature(DictItem nature) {
        this.nature = nature;
    }
    
    public ManufactureFactory getFactory() {
        return factory;
    }

    public void setFactory(ManufactureFactory factory) {
        this.factory = factory;
    }
    
	@Length(min=0, max=64, message="Độ dài số chuẩn thuốc quốc gia phải nằm trong khoảng 0 đến 64 ")
	public String getStandardCode() {
		return standardCode;
	}
	public void setStandardCode(String standardCode) {
		this.standardCode = standardCode;
	}
	
	
	@Length(min=0, max=64, message="nàyvịmãĐộ dài phải nằm trong khoảng 0 đến 64 ")
	public String getBitCode() {
		return bitCode;
	}
	public void setBitCode(String bitCode) {
		this.bitCode = bitCode;
	}
	
	
	@Length(min=0, max=64, message="mã vạchĐộ dài phải nằm trong khoảng 0 đến 64 ")
	public String getBarCode() {
		return barCode;
	}
	public void setBarCode(String barCode) {
		this.barCode = barCode;
	}
	
	
	@Length(min=0, max=64, message="BHYTĐộ dài mã phải nằm trong khoảng 0 đến 64 ")
	public String getInsuranceCode() {
		return insuranceCode;
	}
	public void setInsuranceCode(String insuranceCode) {
		this.insuranceCode = insuranceCode;
	}
	
	
	@Length(min=0, max=45, message="liềulượngĐộ dài phải nằm trong khoảng 0 đến 45 ")
	public String getDosis() {
		return dosis;
	}
	public void setDosis(String dosis) {
		this.dosis = dosis;
	}
	
	
    public DictItem getDosisUnit() {
        return dosisUnit;
    }

    public void setDosisUnit(DictItem dosisUnit) {
        this.dosisUnit = dosisUnit;
    }
    
	@Length(min=0, max=45, message="bào chếĐộ dài phải nằm trong khoảng 0 đến 45 ")
	public String getPreparation() {
		return preparation;
	}
	public void setPreparation(String preparation) {
		this.preparation = preparation;
	}
	
	
    public DictItem getPreparationUnit() {
        return preparationUnit;
    }

    public void setPreparationUnit(DictItem preparationUnit) {
        this.preparationUnit = preparationUnit;
    }
    
    public DictItem getPack() {
        return pack;
    }

    public void setPack(DictItem pack) {
        this.pack = pack;
    }
    
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	
	
	@Length(min=0, max=45, message="Độ dài cho phép bán lẻ phải nằm trong khoảng 0 đến 45 ")
	public String getIsUnpackSell() {
		return isUnpackSell;
	}
	public void setIsUnpackSell(String isUnpackSell) {
		this.isUnpackSell = isUnpackSell;
	}
	
	
	public BigDecimal getRetailPrice() {
		return retailPrice;
	}
	public void setRetailPrice(BigDecimal retailPrice) {
		this.retailPrice = retailPrice;
	}

	@Length(min=0, max=1, message="Cờ bậtĐộ dài phải nằm trong khoảng 0 đến 1 ")
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public String getStockNumber() {
		return stockNumber;
	}

	public void setStockNumber(String stockNumber) {
		this.stockNumber = stockNumber;
	}

	public String getStockUnit() {
		return stockUnit;
	}

	public void setStockUnit(String stockUnit) {
		this.stockUnit = stockUnit;
	}

	public String getPinyinCode() {
		return pinyinCode;
	}

	public void setPinyinCode(String pinyinCode) {
		this.pinyinCode = pinyinCode;
	}

	public DictItem getWesternMedicineUse() {
		return westernMedicineUse;
	}

	public void setWesternMedicineUse(DictItem westernMedicineUse) {
		this.westernMedicineUse = westernMedicineUse;
	}

	public DictItem getChineseMedicineUse() {
		return chineseMedicineUse;
	}

	public void setChineseMedicineUse(DictItem chineseMedicineUse) {
		this.chineseMedicineUse = chineseMedicineUse;
	}

	public Integer getSingleDosage() {
		return singleDosage;
	}

	public void setSingleDosage(Integer singleDosage) {
		this.singleDosage = singleDosage;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public DictItem getFrequency() {
		return frequency;
	}

	public void setFrequency(DictItem frequency) {
		this.frequency = frequency;
	}

	public DictItem getDays() {
		return days;
	}

	public void setDays(DictItem days) {
		this.days = days;
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

	public String getIndate() {
		return indate;
	}

	public void setIndate(String indate) {
		this.indate = indate;
	}

	public BigDecimal getBid() {
		return bid;
	}

	public void setBid(BigDecimal bid) {
		this.bid = bid;
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
        return "drug";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "1004078055755374623";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}