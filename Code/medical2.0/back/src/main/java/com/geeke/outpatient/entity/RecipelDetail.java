package com.geeke.outpatient.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
import com.geeke.sys.entity.DictItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
/**
 * Chi tiết đơn thuốcEntity
 * @author txl
 * @version 2022-07-20
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RecipelDetail extends DataEntity<RecipelDetail> {
	private static final long serialVersionUID = 1014474470772900073L;
	private Company company;      // Phòng khámid 
	private RecipelInfo recipelInfo;      // Đơn thuốc 
	private DrugStuffEvt drugStuffId;		// ThuốcVật tưid
	private BigDecimal singleDosage;		// Liều dùng mỗi lần
	private Integer total;		// Tổng số lượng
	private BigDecimal allFee;		// nên thangtổng giá
	private BigDecimal actualPayment;		// thực thangtổng giá
	private DictItem westernMedicineUse;      // Cách dùng thuốc Tây
	private DictItem frequency;      // Tần suất và liều dùng 
	private DictItem days;      // Số ngày
	private DictItem chineseMedicineUse;      // Cách dùng thuốc Đông y 
	private DictItem infuseUse;      // Cách dùng truyền dịch 
	private String drippingSpeed;		// Tốc độ truyền
	private DictItem skinTest;      // Test da 
	private Integer infuseGroup;		// nhómsố
	private String retailType;		// bán lẻLoại
	private Integer isExtra;		// Có hay khôngphí phụ thêm
	private Integer seq;		// hiển thịthứ tự
	private Integer isUnpackSell;		// Có bán lẻ không
	private BigDecimal unitPrice;		// Đơn giá
	private String unitType;		// đơn giáĐơn vị
	private String stuffType;		// Loại vật liệu
	private Integer minTotal;		// Đơn vị nhỏ nhấttổng lượng
	private BigDecimal executions;			//Tiến độ thực hiện
	private int infuseType;        //Truyền dịchhành nghề dòngTrạng thái
	private int treatment;        //điều trịhành nghề dòngTrạng thái
	private int drugOrder;        //Thuốcthứ tự

	//Trường BHYT
	private PresDrug presDrug; //Thuốc trong đơn thuốc điện tử
	private String drugTracCodg ;//Mã truy xuất thuốc

	public int getTreatment() {
		return treatment;
	}

	public void setTreatment(int treatment) {
		this.treatment = treatment;
	}

	public String getStuffType() {
		return stuffType;
	}

	public void setStuffType(String stuffType) {
		this.stuffType = stuffType;
	}


	public RecipelDetail() {
		super();
	}

	public RecipelDetail(String id){
		super(id);
	}
	
	public RecipelDetail(RecipelInfo recipelInfo){
		this.recipelInfo = recipelInfo;
	}

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
    public RecipelInfo getRecipelInfo() {
        return recipelInfo;
    }

    public void setRecipelInfo(RecipelInfo recipelInfo) {
        this.recipelInfo = recipelInfo;
    }

    public DrugStuffEvt getDrugStuffId() {
        return drugStuffId;
    }

    public void setDrugStuffId(DrugStuffEvt drugStuffId) {
        this.drugStuffId = drugStuffId;
    }

    public BigDecimal getSingleDosage() {
		return singleDosage;
	}
	public void setSingleDosage(BigDecimal singleDosage) {
		this.singleDosage = singleDosage;
	}
	

	
	public BigDecimal getAllFee() {
		return allFee;
	}
	public void setAllFee(BigDecimal allFee) {
		this.allFee = allFee;
	}

	public BigDecimal getActualPayment() {
		return actualPayment;
	}

	public void setActualPayment(BigDecimal actualPayment) {
		this.actualPayment = actualPayment;
	}

	public DictItem getWesternMedicineUse() {
        return westernMedicineUse;
    }

    public void setWesternMedicineUse(DictItem westernMedicineUse) {
        this.westernMedicineUse = westernMedicineUse;
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
    
    public DictItem getChineseMedicineUse() {
        return chineseMedicineUse;
    }

    public void setChineseMedicineUse(DictItem chineseMedicineUse) {
        this.chineseMedicineUse = chineseMedicineUse;
    }
    
    public DictItem getInfuseUse() {
        return infuseUse;
    }

    public void setInfuseUse(DictItem infuseUse) {
        this.infuseUse = infuseUse;
    }
    
	@Length(min=0, max=45, message="tốc độ truyềnĐộ dài phải nằm trong khoảng 0 đến 45 ")
	public String getDrippingSpeed() {
		return drippingSpeed;
	}
	public void setDrippingSpeed(String drippingSpeed) {
		this.drippingSpeed = drippingSpeed;
	}
	
	
    public DictItem getSkinTest() {
        return skinTest;
    }

    public void setSkinTest(DictItem skinTest) {
        this.skinTest = skinTest;
    }
    
	public Integer getInfuseGroup() {
		return infuseGroup;
	}
	public void setInfuseGroup(Integer infuseGroup) {
		this.infuseGroup = infuseGroup;
	}


	@Length(min=0, max=1, message="retail_typeĐộ dài phải từ 0  và  1 Giữa")
	public String getRetailType() {
		return retailType;
	}
	public void setRetailType(String retailType) {
		this.retailType = retailType;
	}


	public Integer getIsExtra() {
		return isExtra;
	}
	public void setIsExtra(Integer isExtra) {
		this.isExtra = isExtra;
	}


	public Integer getSeq() {
		return seq;
	}
	public void setSeq(Integer seq) {
		this.seq = seq;
	}


	@NotNull(message="Có bán lẻ khôngKhông thể là trống")
	public Integer getIsUnpackSell() {
		return isUnpackSell;
	}
	public void setIsUnpackSell(Integer isUnpackSell) {
		this.isUnpackSell = isUnpackSell;
	}


	public BigDecimal getUnitPrice() {
		return unitPrice;
	}
	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}


	@Length(min=0, max=100, message="đơn giáĐơn vịĐộ dài phải nằm trong khoảng 0 đến 100 ")
	public String getUnitType() {
		return unitType;
	}
	public void setUnitType(String unitType) {
		this.unitType = unitType;
	}


	public Integer getMinTotal() {
		return minTotal;
	}

	public void setMinTotal(Integer minTotal) {
		this.minTotal = minTotal;
	}

	public BigDecimal getExecutions() {
		return executions;
	}

	public void setExecutions(BigDecimal executions) {
		this.executions = executions;
	}

	public int getInfuseType() {
		return infuseType;
	}

	public void setInfuseType(int infuseType) {
		this.infuseType = infuseType;
	}

	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "recipel_detail";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "1014474470772900073";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}