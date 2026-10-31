package com.geeke.member.entity;

import com.geeke.org.entity.Company;
import javax.validation.constraints.NotNull;
import com.geeke.member.entity.MemberSet;
import com.geeke.treatment.entity.CostItem;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Chi tiết thẻ hội viênEntity
 * @author rys
 * @version 2022-10-25
 */
public class MemberItem extends DataEntity<MemberItem> {

	private static final long serialVersionUID = 1222698883343517631L;
	private Company company;      // Phòng khámid 
	private MemberSet member;      // Thẻ hội viênid 
	private CostItem costItem;      // tương ứng của hạng mụcid 
	private Integer number;		// tặnggửihạng mụcSố lượng
    private String name;    //tạmlưuhạng mụcTên
    private Integer useNumber;  //sử dụngSố lượng
	
	public MemberItem() {
		super();
	}

	public MemberItem(String id){
		super(id);
	}
	

	@NotNull(message="Phòng khámidKhông được để trống")
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
	@NotNull(message="Thẻ hội viênidKhông được để trống")
    public MemberSet getMember() {
        return member;
    }

    public void setMember(MemberSet member) {
        this.member = member;
    }
    
	@NotNull(message="tương ứng của hạng mụcidKhông được để trống")
    public CostItem getCostItem() {
        return costItem;
    }

    public void setCostItem(CostItem costItem) {
        this.costItem = costItem;
    }
    
	public Integer getNumber() {
		return number;
	}
	public void setNumber(Integer number) {
		this.number = number;
	}

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public Integer getUseNumber() {
        return useNumber;
    }

    public void setUseNumber(Integer useNumber) {
        this.useNumber = useNumber;
    }

    /**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "member_item";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "1222698883343517631";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}