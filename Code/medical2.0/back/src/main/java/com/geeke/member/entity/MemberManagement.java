package com.geeke.member.entity;

import com.geeke.org.entity.Company;
import javax.validation.constraints.NotNull;
import com.geeke.outpatient.entity.Patient;
import com.geeke.member.entity.MemberSet;
import com.geeke.sys.entity.DictItem;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;

import java.util.List;

/**
 * Quản lý thẻ hội viênEntity
 * @author rys
 * @version 2022-10-26
 */
public class MemberManagement extends DataEntity<MemberManagement> {

	private static final long serialVersionUID = 1225301049409470622L;
	private Company company;      // Phòng khámid 
	private Patient patient;      // Thông tin bệnh nhân 
	private MemberSet memberSet;      // Thẻ hội viênid 
	private String age;		// Tuổi
	private DictItem gender;		// Giới tính
	private String card;		// thẻsố
	private DictItem type;		// Hội viênthẻLoại
	private String memberName;		// Hội viênthẻTên
	private String status;		// Trạng thái
	private String identityCard;		// Số CMND/CCCD
	private String phone;   //Điện thoại
	private List<MemberManagementDetail> memberManagementDetails;
	private List<MemberItem> memberItems;
	
	public MemberManagement() {
		super();
	}

	public MemberManagement(String id){
		super(id);
	}
	

	@NotNull(message="Phòng khámidKhông được để trống")
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
	@NotNull(message="Thông tin bệnh nhânKhông thể là trống")
    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }
    
	@NotNull(message="Thẻ hội viênidKhông được để trống")
    public MemberSet getMemberSet() {
        return memberSet;
    }

    public void setMemberSet(MemberSet memberSet) {
        this.memberSet = memberSet;
    }
    
	@Length(min=0, max=100, message="TuổiĐộ dài phải nằm trong khoảng 0 đến 100 ")
	public String getAge() {
		return age;
	}
	public void setAge(String age) {
		this.age = age;
	}
	
	
	@Length(min=0, max=100, message="Giới tínhĐộ dài phải nằm trong khoảng 0 đến 100 ")
	public DictItem getGender() {
		return gender;
	}
	public void setGender(DictItem gender) {
		this.gender = gender;
	}
	
	
	@Length(min=1, max=100, message="thẻsốĐộ dài phải nằm trong khoảng 1 đến 100 ")
	public String getCard() {
		return card;
	}
	public void setCard(String card) {
		this.card = card;
	}
	
	
	@Length(min=1, max=100, message="Hội viênthẻLoạiĐộ dài phải nằm trong khoảng 1 đến 100 ")
	public DictItem getType() {
		return type;
	}
	public void setType(DictItem type) {
		this.type = type;
	}
	
	
	@Length(min=0, max=100, message="Hội viênthẻTênĐộ dài phải nằm trong khoảng 0 đến 100 ")
	public String getMemberName() {
		return memberName;
	}
	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}
	
	
	@Length(min=1, max=1, message="Độ dài trạng thái phải nằm trong khoảng 1 đến 1 ")
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	
	@Length(min=0, max=255, message="số CMND/CCCDĐộ dài phải nằm trong khoảng 0 đến 255 ")
	public String getIdentityCard() {
		return identityCard;
	}
	public void setIdentityCard(String identityCard) {
		this.identityCard = identityCard;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPhone() {
		return phone;
	}

	public List<MemberManagementDetail> getMemberManagementDetails() {
		return memberManagementDetails;
	}

	public void setMemberManagementDetails(List<MemberManagementDetail> memberManagementDetails) {
		this.memberManagementDetails = memberManagementDetails;
	}

	public List<MemberItem> getMemberItems() {
		return memberItems;
	}

	public void setMemberItems(List<MemberItem> memberItems) {
		this.memberItems = memberItems;
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