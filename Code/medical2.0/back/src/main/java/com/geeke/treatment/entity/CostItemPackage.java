package com.geeke.treatment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
import com.geeke.sys.entity.DictItem;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CostItemPackage extends DataEntity<CostItemPackage> {
    private static final long serialVersionUID = 998465736089977638L;
    private Company company;      // Phòng khámid
    private String itemCode;		// hạng mụcMã
    private String itemName;		// hạng mụcTên
    private DictItem itemType;      // Loại hạng mục
    private DictItem unit;      // Đơn vị
    private BigDecimal costPrice;		// Giá vốn
    private BigDecimal salePrice;		// bán hànggiá
    private String isUse;		// Trạng thái
    private String  isPackage;  //Có hay không là đánhgóihạng mục
    private String isDeduct;		// Có thực hiện khấu trừ không
    private int quantity;    //hạng mục conSố lượng
    private String costItemId; //Hạng mục khám chữa bệnhid
    private String costItemPkgId; //套餐id
    private String referenceValue;  //tham khảoGiá trị
    private String referenceUnit;  //tham khảoĐơn vị

    public CostItemPackage() {
        super();
    }

    public CostItemPackage(String id){
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

    @NotNull(message="Hạng mục khám chữa bệnhidKhông được để trống")
    public String getCostItemId() {
        return costItemId;
    }

    public void setCostItemId(String costItemId) {
        this.costItemId = costItemId;
    }

    @NotNull(message="套餐idKhông được để trống")
    public String getCostItemPkgId() {
        return costItemPkgId;
    }

    public void setCostItemPkgId(String costItemPkgId) {
        this.costItemPkgId = costItemPkgId;
    }

    public int getQuantity(){return quantity;}

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getIsPackage() {
        return isPackage;
    }

    public void setIsPackage(String isPackage) {
        this.isPackage = isPackage;
    }

    public String getReferenceValue() {
        return referenceValue;
    }

    public void setReferenceValue(String referenceValue) {
        this.referenceValue = referenceValue;
    }

    public String getReferenceUnit() {
        return referenceUnit;
    }

    public void setReferenceUnit(String referenceUnit) {
        this.referenceUnit = referenceUnit;
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
