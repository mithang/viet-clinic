package com.geeke.member.entity;

import com.geeke.org.entity.Company;
import javax.validation.constraints.NotNull;

import com.geeke.sys.entity.DictItem;
import org.hibernate.validator.constraints.Length;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Cài đặt thẻ hội viênEntity
 * @author rys
 * @version 2022-10-25
 */
public class MemberSet extends DataEntity<MemberSet> {

	private static final long serialVersionUID = 1222698883343517159L;
	private Company company;      // Phòng khámid 
	private DictItem type;		// Hội viênthẻLoại
	private BigDecimal money;		// Hội viênthẻSố tiền
	private Integer amount;		// Hội viênthẻtổng số
	private Integer number;		// Số lượng còn lại
	private String status;		// Có bật không(0: Kích hoạt, 1:Vô hiệu hóa)
	private String failure;		// Có hay khônghết hiệu lực(0:  không , 1:  có )
	private Date startDate;		// Thời gian bắt đầu
	private Date endDate;		// Thời gian kết thúc
	private List<MemberItem> memberItem; //đóng góiChi tiết

	
	public MemberSet() {
		super();
	}

	public MemberSet(String id){
		super(id);
	}
	

	@NotNull(message="Phòng khámidKhông được để trống")
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
	@Length(min=1, max=100, message="Hội viênthẻLoạiĐộ dài phải nằm trong khoảng 1 đến 100 ")
	public DictItem getType() {
		return type;
	}
	public void setType(DictItem type) {
		this.type = type;
	}
	
	
	@NotNull(message="Hội viênthẻSố tiềnKhông thể là trống")
	public BigDecimal getMoney() {
		return money;
	}
	public void setMoney(BigDecimal money) {
		this.money = money;
	}
	
	
	public Integer getAmount() {
		return amount;
	}
	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	
	
	public Integer getNumber() {
		return number;
	}
	public void setNumber(Integer number) {
		this.number = number;
	}
	
	
	@Length(min=1, max=1, message="Có bật không(0: Kích hoạt, 1:Vô hiệu hóa)Độ dài phải từ 1  và  1 Giữa")
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	
	@Length(min=1, max=1, message="Có hay khônghết hiệu lực(0:  không , 1:  có )Độ dài phải từ 1  và  1 Giữa")
	public String getFailure() {
		return failure;
	}
	public void setFailure(String failure) {
		this.failure = failure;
	}
	
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public List<MemberItem> getMemberItem() {
		return memberItem;
	}

	public void setMemberItem(List<MemberItem> memberItem) {
		this.memberItem = memberItem;
	}

	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "member_set";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "1222698883343517159";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}