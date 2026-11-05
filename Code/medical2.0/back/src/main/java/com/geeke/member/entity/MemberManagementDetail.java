package com.geeke.member.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;

import javax.validation.constraints.NotNull;

/**
 * Quản lý thẻ hội viênEntity
 * @author rys
 * @version 2022-10-26
 */

public class MemberManagementDetail extends DataEntity<MemberManagementDetail> {

	private static final long serialVersionUID = 1225301049409470622L;
	private Company company;      // Phòng khámid
	private MemberItem memberItem;      // Thông tin bệnh nhân
	private MemberManagement memberManagement;
	private Integer number;
	private Integer useNumber;

	// DS63: gia tri use_number goc ma frontend da doc truoc khi cong them
	// so luot dung cua don nay. Chi dung de claim o backend, KHONG phai
	// cot DB (khong co trong mapper), KHONG duoc ghi xuong DB. Frontend
	// chooseMember gan truoc khi doi useNumber; backend claim theo gia tri
	// nay khi co, fallback ve gia tri DB da doc (DS62) khi vang.
	private Integer expectedUseNumber;

	public MemberManagementDetail() {
		super();
	}

	public MemberManagementDetail(String id){
		super(id);
	}
	

	@NotNull(message="Phòng khámidKhông được để trống")
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

	public MemberItem getMemberItem() {
		return memberItem;
	}

	public void setMemberItem(MemberItem memberItem) {
		this.memberItem = memberItem;
	}

	public MemberManagement getMemberManagement() {
		return memberManagement;
	}

	public void setMemberManagement(MemberManagement memberManagement) {
		this.memberManagement = memberManagement;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public Integer getUseNumber() {
		return useNumber;
	}

	public void setUseNumber(Integer useNumber) {
		this.useNumber = useNumber;
	}

	public Integer getExpectedUseNumber() {
		return expectedUseNumber;
	}

	public void setExpectedUseNumber(Integer expectedUseNumber) {
		this.expectedUseNumber = expectedUseNumber;
	}

	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "member_management";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "1225301049409470622";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return false;
    }
}