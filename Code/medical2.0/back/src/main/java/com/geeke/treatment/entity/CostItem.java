package com.geeke.treatment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.common.persistence.DataEntity;
import com.geeke.cure.entity.InspectionCheckInfoCostItem;
import com.geeke.org.entity.Company;
import com.geeke.outpatient.entity.RecipelDetail;
import com.geeke.sys.entity.DictItem;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
/**
 * Hạng mục khám chữa bệnhEntity
 * @author txl
 * @version 2022-05-30
 */
public class CostItem extends DataEntity<CostItem> {

	private static final long serialVersionUID = 998465736089977637L;
	private Company company;      // Phòng khámid

	private String syncId;  //đồng bộID
	private int syncNum;
	private String itemCode;		// hạng mụcMã
	private String itemName;		// hạng mụcTên
	private DictItem itemType;      // Loại hạng mục 
	private DictItem unit;      // Đơn vị 
	private BigDecimal costPrice;		// Giá vốn
	private BigDecimal salePrice;		// bán hànggiá
	private String isUse;		// Trạng thái
	private String isPackage;   //Có hay không là đánhgóihạng mục
	private String isDeduct;		// Có thực hiện khấu trừ không
	private String itemCountryCode; //quốc giaMã
	private String pinyinCode;     //Mã pinyin
	private List<CostItemPackage> costItemPackages;  //hạng mục conđóng gói
	private RecipelDetail recipelDetail;
	private InspectionCheckInfoCostItem inspectionCheckInfo;
	
	public CostItem() {
		super();
	}

	public CostItem(String id){
		super(id);
	}
	

	@NotNull(message="Phòng khámidKhông được để trống")
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
	@Length(min=0, max=64, message="hạng mụcĐộ dài mã phải nằm trong khoảng 0 đến 64 ")
	public String getItemCode() {
		return itemCode;
	}
	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}
	
	
	@Length(min=1, max=64, message="hạng mụcTênĐộ dài phải nằm trong khoảng 1 đến 64 ")
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
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

	public DictItem getItemType() {
        return itemType;
    }

    public void setItemType(DictItem itemType) {
        this.itemType = itemType;
    }
    
    public DictItem getUnit() {
        return unit;
    }

    public void setUnit(DictItem unit) {
        this.unit = unit;
    }
    
	public BigDecimal getCostPrice() {
		return costPrice;
	}
	public void setCostPrice(BigDecimal costPrice) {
		this.costPrice = costPrice;
	}
	
	
	public BigDecimal getSalePrice() {
		return salePrice;
	}
	public void setSalePrice(BigDecimal salePrice) {
		this.salePrice = salePrice;
	}
	
	
	@Length(min=0, max=2, message="Độ dài trạng thái phải nằm trong khoảng 0 đến 2 ")
	public String getIsUse() {
		return isUse;
	}
	public void setIsUse(String isUse) {
		this.isUse = isUse;
	}
	
	
	@Length(min=0, max=2, message="Độ dài có thực hiện khấu trừ phải nằm trong khoảng 0 đến 2 ")
	public String getIsDeduct() {
		return isDeduct;
	}
	public void setIsDeduct(String isDeduct) {
		this.isDeduct = isDeduct;
	}

	@Length(min=0, max=64, message="quốc giaĐộ dài mã phải nằm trong khoảng 0 đến 64 ")
	public String getItemCountryCode() {
		return itemCountryCode;
	}
	public void setItemCountryCode(String itemCountryCode) {
		this.itemCountryCode = itemCountryCode;
	}

	public String getIsPackage() {
		return isPackage;
	}

	public void setIsPackage(String isPackage) {
		this.isPackage = isPackage;
	}

	public String getPinyinCode() {
		return pinyinCode;
	}

	public void setPinyinCode(String pinyinCode) {
		this.pinyinCode = pinyinCode;
	}

	public List<CostItemPackage> getCostItemPackages() {
		return costItemPackages;
	}

	public void setCostItemPackages(List<CostItemPackage> costItemPackages) {
		this.costItemPackages = costItemPackages;
	}

	public void setRecipelDetail(RecipelDetail recipelDetail) {
		this.recipelDetail = recipelDetail;
	}

	public RecipelDetail getRecipelDetail() {
		return recipelDetail;
	}

	public void setInspectionCheckInfo(InspectionCheckInfoCostItem inspectionCheckInfo) {
		this.inspectionCheckInfo = inspectionCheckInfo;
	}

	public InspectionCheckInfoCostItem getInspectionCheckInfo() {
		return inspectionCheckInfo;
	}

	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "cost_item";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "998465736089977637";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}