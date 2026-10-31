package com.geeke.outpatient.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.admin.entity.User;
import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.ClinicOffice;
import com.geeke.org.entity.Company;
import com.geeke.org.entity.Department;
import com.geeke.sys.entity.DictItem;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.util.Date;
/**
 * Thông tin đăng ký khámEntity
 * @author txl
 * @version 2022-06-15
 */
@Data
public class Registration extends DataEntity<Registration> {

	private static final long serialVersionUID = 1008534118685450402L;
	private Company company;      // Phòng khámid 
	private ClinicOffice clinicOffice;      // Khoa
	private Patient patientId;      // Bệnh nhân 
	private User doctor;      // Bác sĩ 
	private DictItem treatType;      // điều trịLoại 
	private DictItem infectType;      // Bệnh truyền nhiễm
	private Date receptionStartDate;		// tiếp nhậnThời gian bắt đầu
	private DictItem source;      // Nguồn 
	private Date receptionEndDate;		// tiếp nhậnThời gian kết thúc
	private String pulse;		// mạch
	private String breathe;		// hô hấp
	private String temperature;		// Nhiệt độ cơ thể
	private String isGoHigharea;		// Có hay khôngđiquadịchQuận
	private String highArea;		// dịchQuậnđịa phươngđiểm
	private String freeRegistrationFee;		// miễnPhí đăng ký khám
	private BigDecimal registrationFee;		// Phí đăng ký khám
	private DictItem payType;      // phương thức thanh toán
	private DictItem status;      // Trạng thái 
	private String dispensingStatus;		// phát thuốcTrạng thái
	private Date dispensingDate;		// phát/Thời gian trả thuốc
	private String chargeStatus;		// Trạng thái thu phí        0- Chờ thu phí   1-Một phầnThu phí   2- Đã thu phí   3-  Một phầnhoàn phí    4- Đã hoàn phí
	private Date chargeDate;		// Thu phíThời gian
	private DictItem refundRegistrationPayType;		// trảPhí đăng ký khámphương thức thanh toán
	private String refundRegistrationRemarks;		// trảPhí đăng ký khámGhi chú
	private  BigDecimal practicleRegistrationFee;   //thực tếthanh toánPhí đăng ký khám
	private String practicalRegistrationPayRemarks;	//thực tếthanh toánPhí đăng ký khámGhi chú
	private String guardianName;      //người giám hộTên
	private String guardianPhone;     //người giám hộSố điện thoại
	private String workplace;     //Đơn vị công tác
	private String nation;     //Dân tộc
	private String card;     //Số CMND/CCCD
	private String occupation;     //Nghề nghiệp
	private Date signedDate;		// kýĐếnThời gian
	private Date exitNumberDate;		// hủy số khámThời gian
	private Date followUpDate;		// tái khámThời gian
	private Date payDate;		// thanh toánThời gian

	private String recipeStatus;//tất cảĐơn thuốchủy của Trạng tháihiển thị

	private String bloodPressure;  //Huyết áp

	private Date subscribeDate;   //đặt lịchThời gian
	private String subscribeProject; //đặt lịchhạng mục

	private Date retreatsDate;    //Thời gian hoàn phí
	private Date returnDate;     //Thời gian trả thuốc
	private Date morbidityTime;     //phátbệnhThời gian

	private Date treatmentDate;  //khámtrị liệuhoàn thànhThời gian
	private String overlook;//Có hay khôngbỏ qua
     /**Trường BHYT**/
	private  String mdtrtId ;// BHYTSố thứ tự duy nhất của giao diện
	//tạmchỉ định11thông thườngngoại trú 12 ngoại trúĐăng ký khám 13 cấp cứu
    private  DictItem medType; //Loại hình y tế
	private  DictItem cardType; //Loại giấy tờ khám bệnh
	private  String iptOtpNo;// Nhập viện/Số khám ngoại trú Số thứ tự duy nhất trong viện
	private  String mdtrtCertNo;//Số giấy tờ khám bệnh, chỉ điền khi là thẻ bảo hiểm xã hội 03
	private  String setlId;//BHYTQuyết toánid
	private  Boolean isPre;//Có hay khôngđiện tửĐơn thuốc

	public Date getPayDate() {
		return payDate;
	}

	public void setPayDate(Date payDate) {
		this.payDate = payDate;
	}

	public Date getSignedDate() {
		return signedDate;
	}

	public void setSignedDate(Date signedDate) {
		this.signedDate = signedDate;
	}

	public Date getExitNumberDate() {
		return exitNumberDate;
	}

	public void setExitNumberDate(Date exitNumberDate) {
		this.exitNumberDate = exitNumberDate;
	}

	public Date getFollowUpDate() {
		return followUpDate;
	}

	public void setFollowUpDate(Date followUpDate) {
		this.followUpDate = followUpDate;
	}


	public Registration() {
		super();
	}

	public Registration(String id){
		super(id);
	}

	public Registration(Patient patientId){
		this.patientId = patientId;
	}

	public DictItem getRefundRegistrationPayType() {
		return refundRegistrationPayType;
	}

	public void setRefundRegistrationPayType(DictItem refundRegistrationPayType) {
		this.refundRegistrationPayType = refundRegistrationPayType;
	}

	public String getRefundRegistrationRemarks() {
		return refundRegistrationRemarks;
	}

	public void setRefundRegistrationRemarks(String refundRegistrationRemarks) {
		this.refundRegistrationRemarks = refundRegistrationRemarks;
	}

	public BigDecimal getPracticleRegistrationFee() {
		return practicleRegistrationFee;
	}

	public void setPracticleRegistrationFee(BigDecimal practicleRegistrationFee) {
		this.practicleRegistrationFee = practicleRegistrationFee;
	}

	public String getPracticalRegistrationPayRemarks() {
		return practicalRegistrationPayRemarks;
	}

	public void setPracticalRegistrationPayRemarks(String practicalRegistrationPayRemarks) {
		this.practicalRegistrationPayRemarks = practicalRegistrationPayRemarks;
	}

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

	public ClinicOffice getClinicOffice() {
		return clinicOffice;
	}

	public void setClinicOffice(ClinicOffice clinicOffice) {
		this.clinicOffice = clinicOffice;
	}

	public Patient getPatientId() {
        return patientId;
    }

    public void setPatientId(Patient patientId) {
        this.patientId = patientId;
    }
    
    public User getDoctor() {
        return doctor;
    }

    public void setDoctor(User doctor) {
        this.doctor = doctor;
    }
    
    public DictItem getTreatType() {
        return treatType;
    }

    public void setTreatType(DictItem treatType) {
        this.treatType = treatType;
    }

    public DictItem getInfectType() {
        return infectType;
    }

    public void setInfectType(DictItem infectType) {
        this.infectType = infectType;
    }
    
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getReceptionStartDate() {
		return receptionStartDate;
	}
	public void setReceptionStartDate(Date receptionStartDate) {
		this.receptionStartDate = receptionStartDate;
	}
	
	
    public DictItem getSource() {
        return source;
    }

    public void setSource(DictItem source) {
        this.source = source;
    }
    
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getReceptionEndDate() {
		return receptionEndDate;
	}
	public void setReceptionEndDate(Date receptionEndDate) {
		this.receptionEndDate = receptionEndDate;
	}
	
	
	@Length(min=0, max=45, message="mạchĐộ dài phải nằm trong khoảng 0 đến 45 ")
	public String getPulse() {
		return pulse;
	}
	public void setPulse(String pulse) {
		this.pulse = pulse;
	}
	
	
	@Length(min=0, max=45, message="hô hấpĐộ dài phải nằm trong khoảng 0 đến 45 ")
	public String getBreathe() {
		return breathe;
	}
	public void setBreathe(String breathe) {
		this.breathe = breathe;
	}
	
	
	@Length(min=0, max=45, message="Nhiệt độ cơ thểĐộ dài phải nằm trong khoảng 0 đến 45 ")
	public String getTemperature() {
		return temperature;
	}
	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}
	
	
	@Length(min=0, max=64, message="Có hay khôngđiquadịchĐộ dài quận phải nằm trong khoảng 0 đến 64 ")
	public String getIsGoHigharea() {
		return isGoHigharea;
	}
	public void setIsGoHigharea(String isGoHigharea) {
		this.isGoHigharea = isGoHigharea;
	}
	
	
	@Length(min=0, max=128, message="dịchQuậnđịa phươngđiểmĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getHighArea() {
		return highArea;
	}
	public void setHighArea(String highArea) {
		this.highArea = highArea;
	}
	
	
	@Length(min=0, max=64, message="miễnPhí đăng ký khámĐộ dài phải nằm trong khoảng 0 đến 64 ")
	public String getFreeRegistrationFee() {
		return freeRegistrationFee;
	}
	public void setFreeRegistrationFee(String freeRegistrationFee) {
		this.freeRegistrationFee = freeRegistrationFee;
	}
	
	
	public BigDecimal getRegistrationFee() {
		return registrationFee;
	}
	public void setRegistrationFee(BigDecimal registrationFee) {
		this.registrationFee = registrationFee;
	}


    public DictItem getPayType() {
        return payType;
    }

    public void setPayType(DictItem payType) {
        this.payType = payType;
    }
    
    public DictItem getStatus() {
        return status;
    }

    public void setStatus(DictItem status) {
        this.status = status;
    }
    
	@Length(min=0, max=1, message="phát thuốcĐộ dài trạng thái phải nằm trong khoảng 0 đến 1 ")
	public String getDispensingStatus() {
		return dispensingStatus;
	}
	public void setDispensingStatus(String dispensingStatus) {
		this.dispensingStatus = dispensingStatus;
	}
	
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getDispensingDate() {
		return dispensingDate;
	}
	public void setDispensingDate(Date dispensingDate) {
		this.dispensingDate = dispensingDate;
	}
	
	
	@Length(min=0, max=1, message="Thu phíĐộ dài trạng thái phải nằm trong khoảng 0 đến 1 ")
	public String getChargeStatus() {
		return chargeStatus;
	}
	public void setChargeStatus(String chargeStatus) {
		this.chargeStatus = chargeStatus;
	}
	
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getChargeDate() {
		return chargeDate;
	}
	public void setChargeDate(Date chargeDate) {
		this.chargeDate = chargeDate;
	}

	public String getGuardianName() {
		return guardianName;
	}

	public void setGuardianName(String guardianName) {
		this.guardianName = guardianName;
	}

	public String getGuardianPhone() {
		return guardianPhone;
	}

	public void setGuardianPhone(String guardianPhone) {
		this.guardianPhone = guardianPhone;
	}

	public String getWorkplace() {
		return workplace;
	}

	public void setWorkplace(String workplace) {
		this.workplace = workplace;
	}

	public String getNation() {
		return nation;
	}

	public void setNation(String nation) {
		this.nation = nation;
	}

	public String getCard() {
		return card;
	}

	public void setCard(String card) {
		this.card = card;
	}

	public String getOccupation() {
		return occupation;
	}

	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}

	public String getRecipeStatus() {
		return recipeStatus;
	}

	public void setRecipeStatus(String recipeStatus) {
		this.recipeStatus = recipeStatus;
	}

	public String getBloodPressure() {
		return bloodPressure;
	}

	public void setBloodPressure(String bloodPressure) {
		this.bloodPressure = bloodPressure;
	}
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	public Date getSubscribeDate() {
		return subscribeDate;
	}

	public void setSubscribeDate(Date subscribeDate) {
		this.subscribeDate = subscribeDate;
	}

	public String getSubscribeProject() {
		return subscribeProject;
	}

	public void setSubscribeProject(String subscribeProject) {
		this.subscribeProject = subscribeProject;
	}

	public Date getretreatsDate() {
		return retreatsDate;
	}

	public void setretreatsDate(Date retreatsDate) {
		this.retreatsDate = retreatsDate;
	}

	public Date getReturnDate() {
		return returnDate;
	}

	public void setReturnDate(Date returnDate) {
		this.returnDate = returnDate;
	}

	public Date getMorbidityTime() {
		return morbidityTime;
	}

	public void setMorbidityTime(Date morbidityTime) {
		this.morbidityTime = morbidityTime;
	}


	public Date getTreatmentDate() {
		return treatmentDate;
	}

	public void setTreatmentDate(Date treatmentDate) {
		this.treatmentDate = treatmentDate;
	}

	public String getOverlook(){return overlook;}

	public void setOverlook(String overlook){this.overlook=overlook;}

	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "registration";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "1008534118685450402";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}