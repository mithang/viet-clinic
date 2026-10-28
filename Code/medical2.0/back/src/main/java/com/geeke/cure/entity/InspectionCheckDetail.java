package com.geeke.cure.entity;

import com.geeke.cure.entity.InspectionCheckInfo;
import javax.validation.constraints.NotNull;
import com.geeke.org.entity.Company;
import com.geeke.treatment.entity.CostItem;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Chi tiết xét nghiệm và kiểm traEntity
 * @author rys
 * @version 2022-10-19
 */
public class InspectionCheckDetail extends DataEntity<InspectionCheckDetail> {

	private static final long serialVersionUID = 1213629183723971393L;
	private InspectionCheckInfo inspectionCheckInfo;      // Chi tiếtbảngid 
	private Company company;      // Phòng khámid 
	private CostItem costItem;      // Hạng mục tương ứngid 
	private String conclusion;		// Kết quả
	private int sign;		// Cờ đánh dấu chỉ số cao/thấp
	private String describes;		// Mô tả
	private int seq;          //Sắp xếp
	
	public InspectionCheckDetail() {
		super();
	}

	public InspectionCheckDetail(String id){
		super(id);
	}
	

	@NotNull(message="Chi tiếtbảngidKhông được để trống")
    public InspectionCheckInfo getInspectionCheckInfo() {
        return inspectionCheckInfo;
    }

    public void setInspectionCheckInfo(InspectionCheckInfo inspectionCheckInfo) {
        this.inspectionCheckInfo = inspectionCheckInfo;
    }
    
	@NotNull(message="Phòng khámidKhông được để trống")
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
	@NotNull(message="Hạng mục tương ứngidKhông được để trống")
    public CostItem getCostItem() {
        return costItem;
    }

    public void setCostItem(CostItem costItem) {
        this.costItem = costItem;
    }
    
	@Length(min=0, max=255, message="Kết quảĐộ dài phải nằm trong khoảng 0 đến 255 ")
	public String getConclusion() {
		return conclusion;
	}
	public void setConclusion(String conclusion) {
		this.conclusion = conclusion;
	}
	
	
	public int getSign() {
		return sign;
	}
	public void setSign(int sign) {
		this.sign = sign;
	}
	
	
	@Length(min=0, max=255, message="Mô tảĐộ dài phải nằm trong khoảng 0 đến 255 ")
	public String getDescribes() {
		return describes;
	}
	public void setDescribes(String describes) {
		this.describes = describes;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "inspection_check_detail";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "1213629183723971393";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return false;
    }
}