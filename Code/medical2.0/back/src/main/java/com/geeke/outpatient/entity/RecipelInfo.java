package com.geeke.outpatient.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
import com.geeke.sys.entity.DictItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
/**
 * Thông tin đơn thuốcEntity
 * @author txl
 * @version 2022-07-20
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RecipelInfo extends DataEntity<RecipelInfo> {

	private static final long serialVersionUID = 1014474470772900008L;
	private Company company;      // Phòng khámid
	private Registration registration;      // Thông tin đăng ký(Tương ứngID)
	private DictItem recipelType;      // Đơn thuốcLớploại
	private String dosage;		// Liều lượng
	private DictItem recipelUse;      // Cách dùng
	private DictItem frequency;      // Tần suất và liều dùng
	private DictItem takeFrequency;      // dùngdùngtần suất lần
	private String singleDosage;		// Liều dùng mỗi lần
	private BigDecimal fee;		// Đơn thuốctổng giá
	private BigDecimal amountReceivedTotal;		// thực thuSố tiềnTổng cộng
	private String isPay;		// Có hay khôngthanh toán
	private Date payDate;		// thanh toánThời gian
	private String isDispension;		// Có hay khôngphát thuốc
	private Date dispensionDate;		// phát thuốcThời gian
	private Integer seq;		// Sắp xếp
	private DictItem smallType;		// nhỏLớpnhỏLớp[Dữ liệu từ điển]
	private String entrust;		// Y lệnh
	private Integer status;		// cơ bảnTrạng thái(1: kêhoàn thành; -1:   hủyĐơn thuốc)
	private Integer chargeStatus;		// Trạng thái thu phí(0: Chờ thu phí; 1: Đã thu phí ; -1: Đã hoàn phí)
	private Integer dispensionStatus;		// phát thuốcTrạng thái(0: chờphát thuốc; 1:  Đãphát thuốc ; -1: ĐãTrả thuốc)
	private Date retreatChargeDate;		// Thời gian hoàn phí
	private Date retreatDispensionDate;		// Thời gian trả thuốc
	private Date invalidDate;		// hủyThời gian
	private Integer isFollowUp;		// Có hay khôngtái khámĐơn thuốc
	private Date followUpDate;		// tái khámThời gian
	private String payUser;		// thanh toánthao tácngười
	private String retreatChargeUser;		// hoàn phíthao tácngười
	private String retreatDispensionUser;		// Trả thuốcthao tácngười
	private String dispensionUser;		// phát thuốcthao tácngười
	private String code;		// Số đơn thuốc
	private Date retreatDate;   //Thời gian hoàn phí
	private String drippingSpeed;  //Truyền dịchtốc độ truyền
	private DictItem infusionDay;    //Truyền dịch ngàysố
	private DictItem infusionFrequency;   //Truyền dịchtần suất lần
	private DictItem infusionUse;			//Cách dùng truyền dịch
	private int cureType;            //Cờ hoàn thành hạng mục khám chữa bệnh
	private int infuseType;          //Truyền dịchhành nghề dònghoàn thànhcờ
	private Boolean chronicDisease;  //Có hay khôngchậmbệnhcờ
	private Integer notShow; //mụctrướcĐối vớingoại trú-công việclàmmáy-khôngđạt chuẩnĐơn thuốcTruy vấn, Có hay khônghiển thị 0 khônghiển thị 1 Hiển thị
	private String diagnosisId; // khám chữa bệnh từ xaBản ghiid
	private String chinessNotes; // trong thuốcGhi chú
	/**Trường BHYT*/
	private  Boolean isPre; // Có hay khôngđiện tửĐơn thuốc
	private  String hiRxno; // BHYTđiện tửSố đơn thuốc

	public String getDiagnosisId() {
		return diagnosisId;
	}

	public void setDiagnosisId(String diagnosisId) {
		this.diagnosisId = diagnosisId;
	}

	public String getChinessNotes() {
		return chinessNotes;
	}

	public void setChinessNotes(String chinessNotes) {
		this.chinessNotes = chinessNotes;
	}

	public Integer getNotShow() {
		return notShow;
	}

	public void setNotShow(Integer notShow) {
		this.notShow = notShow;
	}

	public Boolean getChronicDisease() {
		return chronicDisease;
	}

	public void setChronicDisease(Boolean chronicDisease) {
		this.chronicDisease = chronicDisease;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public RecipelInfo() {
		super();
	}

	public RecipelInfo(String id){
		super(id);
	}


	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public Registration getRegistration() {
		return registration;
	}

	public void setRegistration(Registration registration) {
		this.registration = registration;
	}

	public DictItem getRecipelType() {
		return recipelType;
	}

	public void setRecipelType(DictItem recipelType) {
		this.recipelType = recipelType;
	}

	@Length(min=0, max=45, message="liềulượngĐộ dài phải nằm trong khoảng 0 đến 45 ")
	public String getDosage() {
		return dosage;
	}
	public void setDosage(String dosage) {
		this.dosage = dosage;
	}


	public DictItem getRecipelUse() {
		return recipelUse;
	}

	public void setRecipelUse(DictItem recipelUse) {
		this.recipelUse = recipelUse;
	}

	public DictItem getFrequency() {
		return frequency;
	}

	public void setFrequency(DictItem frequency) {
		this.frequency = frequency;
	}

	public DictItem getTakeFrequency() {
		return takeFrequency;
	}

	public void setTakeFrequency(DictItem takeFrequency) {
		this.takeFrequency = takeFrequency;
	}

	@Length(min=0, max=11, message="Liều dùng mỗi lầnĐộ dài phải nằm trong khoảng 0 đến 11 ")
	public String getSingleDosage() {
		return singleDosage;
	}
	public void setSingleDosage(String singleDosage) {
		this.singleDosage = singleDosage;
	}


	public BigDecimal getFee() {
		return fee;
	}
	public void setFee(BigDecimal fee) {
		this.fee = fee;
	}

	public BigDecimal getAmountReceivedTotal() {
		return amountReceivedTotal;
	}

	public void setAmountReceivedTotal(BigDecimal amountReceivedTotal) {
		this.amountReceivedTotal = amountReceivedTotal;
	}

	@Length(min=0, max=1, message="Có hay khôngthanh toánĐộ dài phải nằm trong khoảng 0 đến 1 ")
	public String getIsPay() {
		return isPay;
	}
	public void setIsPay(String isPay) {
		this.isPay = isPay;
	}


	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getPayDate() {
		return payDate;
	}
	public void setPayDate(Date payDate) {
		this.payDate = payDate;
	}


	@Length(min=0, max=1, message="Có hay khôngphát thuốcĐộ dài phải nằm trong khoảng 0 đến 1 ")
	public String getIsDispension() {
		return isDispension;
	}
	public void setIsDispension(String isDispension) {
		this.isDispension = isDispension;
	}


	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getDispensionDate() {
		return dispensionDate;
	}
	public void setDispensionDate(Date dispensionDate) {
		this.dispensionDate = dispensionDate;
	}


	@NotNull(message="sắp xếpKhông thể là trống")
	public Integer getSeq() {
		return seq;
	}
	public void setSeq(Integer seq) {
		this.seq = seq;
	}


	@NotNull(message="nhỏLớpnhỏLớp[Dữ liệu từ điển]Không được để trống")
	public DictItem getSmallType() {
		return smallType;
	}
	public void setSmallType(DictItem smallType) {
		this.smallType = smallType;
	}


	@Length(min=0, max=4000, message="Y lệnhĐộ dài phải nằm trong khoảng 0 đến 4000 ")
	public String getEntrust() {
		return entrust;
	}
	public void setEntrust(String entrust) {
		this.entrust = entrust;
	}


	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}


	public Integer getChargeStatus() {
		return chargeStatus;
	}
	public void setChargeStatus(Integer chargeStatus) {
		this.chargeStatus = chargeStatus;
	}


	public Integer getDispensionStatus() {
		return dispensionStatus;
	}
	public void setDispensionStatus(Integer dispensionStatus) {
		this.dispensionStatus = dispensionStatus;
	}


	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getRetreatChargeDate() {
		return retreatChargeDate;
	}
	public void setRetreatChargeDate(Date retreatChargeDate) {
		this.retreatChargeDate = retreatChargeDate;
	}


	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getRetreatDispensionDate() {
		return retreatDispensionDate;
	}
	public void setRetreatDispensionDate(Date retreatDispensionDate) {
		this.retreatDispensionDate = retreatDispensionDate;
	}


	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getInvalidDate() {
		return invalidDate;
	}
	public void setInvalidDate(Date invalidDate) {
		this.invalidDate = invalidDate;
	}


	@NotNull(message="Có hay khôngtái khámĐơn thuốcKhông thể là trống")
	public Integer getIsFollowUp() {
		return isFollowUp;
	}
	public void setIsFollowUp(Integer isFollowUp) {
		this.isFollowUp = isFollowUp;
	}


	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getFollowUpDate() {
		return followUpDate;
	}
	public void setFollowUpDate(Date followUpDate) {
		this.followUpDate = followUpDate;
	}


	@Length(min=0, max=100, message="thanh toánthao tácngườiĐộ dài phải nằm trong khoảng 0 đến 100 ")
	public String getPayUser() {
		return payUser;
	}
	public void setPayUser(String payUser) {
		this.payUser = payUser;
	}


	@Length(min=0, max=100, message="hoàn phíthao tácngườiĐộ dài phải nằm trong khoảng 0 đến 100 ")
	public String getRetreatChargeUser() {
		return retreatChargeUser;
	}
	public void setRetreatChargeUser(String retreatChargeUser) {
		this.retreatChargeUser = retreatChargeUser;
	}


	@Length(min=0, max=100, message="Trả thuốcthao tácngườiĐộ dài phải nằm trong khoảng 0 đến 100 ")
	public String getRetreatDispensionUser() {
		return retreatDispensionUser;
	}
	public void setRetreatDispensionUser(String retreatDispensionUser) {
		this.retreatDispensionUser = retreatDispensionUser;
	}


	@Length(min=0, max=100, message="phát thuốcthao tácngườiĐộ dài phải nằm trong khoảng 0 đến 100 ")
	public String getDispensionUser() {
		return dispensionUser;
	}
	public void setDispensionUser(String dispensionUser) {
		this.dispensionUser = dispensionUser;
	}

	public Date getRetreatDate() {
		return retreatDate;
	}

	public void setRetreatDate(Date retreatDate) {
		this.retreatDate = retreatDate;
	}

	public String getDrippingSpeed() {
		return drippingSpeed;
	}

	public void setDrippingSpeed(String drippingSpeed) {
		this.drippingSpeed = drippingSpeed;
	}

	public DictItem getInfusionDay() {
		return infusionDay;
	}

	public void setInfusionDay(DictItem infusionDay) {
		this.infusionDay = infusionDay;
	}

	public DictItem getInfusionFrequency() {
		return infusionFrequency;
	}

	public void setInfusionFrequency(DictItem infusionFrequency) {
		this.infusionFrequency = infusionFrequency;
	}

	public DictItem getInfusionUse() {
		return infusionUse;
	}

	public void setInfusionUse(DictItem infusionUse) {
		this.infusionUse = infusionUse;
	}

	public int getCureType() {
		return cureType;
	}

	public void setCureType(int cureType) {
		this.cureType = cureType;
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
		return "recipel_info";
	}

	/**
	 * Lấy thực thể tương ứngId
	 */
	@Override
	@JsonIgnore
	public String getBusTableId() {
		return "1014474470772900008";
	}


	/**
	 * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
	 */
	@JsonIgnore
	public boolean getBusTableHasDelFlag() {
		return true;
	}
}