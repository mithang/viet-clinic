package com.geeke.cure.entity;

import com.geeke.org.entity.Company;
import javax.validation.constraints.NotNull;
import com.geeke.cure.entity.InspectionCheck;
import com.geeke.treatment.entity.CostItems;
import org.hibernate.validator.constraints.Length;
import com.geeke.outpatient.entity.Patient;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Chi tiết xét nghiệm và kiểm traEntity
 * @author rys
 * @version 2022-10-19
 */
public class InspectionCheckInfo extends DataEntity<InspectionCheckInfo> {

	private static final long serialVersionUID = 1213629183723971377L;
	private Company company;      // Phòng khámid 
	private InspectionCheck inspectionCheck;      // Bảng chínhid 
	private String fileId;		// Tệpid
	private Patient patient;      // Bệnh nhânid 
	private Date samplingDate;		// lấynhưThời gian
	private String samplingNum;		// nhưnàysố
	private String instrument;		// Xét nghiệmthiết bị
	private String checkoutNum;		// Xét nghiệmsố
	private Date checkoutDate;		// Xét nghiệmThời gian
	private String checkoutOffice;		// Xét nghiệmKhoa
	private String reportConclusion;		// kết luận báo cáo
	private String checkType;          //Kiểm traLoại
	private String checkPart;         //Kiểm trabộvị
	private String checkAdvise;      //Kiểm tranhìn thấy
	private String opinion;      //ý kiến
	private String doctor;      //Bác sĩ
	private String checkProject;   //Kiểm trahạng mục
	private List<CostItems> costItemsList;  //Hạng mục khám chữa bệnh
	private List<InspectionCheckDetail> inspectionCheckDetails;

	public InspectionCheckInfo() {
		super();
	}

	public InspectionCheckInfo(String id){
		super(id);
	}
	

	@NotNull(message="Phòng khámidKhông được để trống")
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
	@NotNull(message="Bảng chínhidKhông được để trống")
    public InspectionCheck getInspectionCheck() {
        return inspectionCheck;
    }

    public void setInspectionCheck(InspectionCheck inspectionCheck) {
        this.inspectionCheck = inspectionCheck;
    }
    
	@Length(min=0, max=20, message="TệpidĐộ dài phải từ 0  và  20 Giữa")
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	
	
	@NotNull(message="Bệnh nhânidKhông được để trống")
    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }
    
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getSamplingDate() {
		return samplingDate;
	}
	public void setSamplingDate(Date samplingDate) {
		this.samplingDate = samplingDate;
	}
	
	
	@Length(min=0, max=100, message="nhưnàysốĐộ dài phải nằm trong khoảng 0 đến 100 ")
	public String getSamplingNum() {
		return samplingNum;
	}
	public void setSamplingNum(String samplingNum) {
		this.samplingNum = samplingNum;
	}
	
	
	@Length(min=0, max=100, message="Xét nghiệmthiết bịĐộ dài phải nằm trong khoảng 0 đến 100 ")
	public String getInstrument() {
		return instrument;
	}
	public void setInstrument(String instrument) {
		this.instrument = instrument;
	}
	
	
	@Length(min=0, max=100, message="Xét nghiệmsốĐộ dài phải nằm trong khoảng 0 đến 100 ")
	public String getCheckoutNum() {
		return checkoutNum;
	}
	public void setCheckoutNum(String checkoutNum) {
		this.checkoutNum = checkoutNum;
	}
	
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getCheckoutDate() {
		return checkoutDate;
	}
	public void setCheckoutDate(Date checkoutDate) {
		this.checkoutDate = checkoutDate;
	}
	
	
	@Length(min=0, max=100, message="Xét nghiệmKhoaĐộ dài phải nằm trong khoảng 0 đến 100 ")
	public String getCheckoutOffice() {
		return checkoutOffice;
	}
	public void setCheckoutOffice(String checkoutOffice) {
		this.checkoutOffice = checkoutOffice;
	}
	
	
	@Length(min=0, max=255, message="kết luận báo cáoĐộ dài phải nằm trong khoảng 0 đến 255 ")
	public String getReportConclusion() {
		return reportConclusion;
	}
	public void setReportConclusion(String reportConclusion) {
		this.reportConclusion = reportConclusion;
	}

	public List<CostItems> getCostItemsList() {
		return costItemsList;
	}

	public void setCostItemsList(List<CostItems> costItemsList) {
		this.costItemsList = costItemsList;
	}

	public List<InspectionCheckDetail> getInspectionCheckDetails() {
		return inspectionCheckDetails;
	}

	public void setInspectionCheckDetails(List<InspectionCheckDetail> inspectionCheckDetails) {
		this.inspectionCheckDetails = inspectionCheckDetails;
	}

	public String getCheckAdvise() {
		return checkAdvise;
	}

	public void setCheckAdvise(String checkAdvise) {
		this.checkAdvise = checkAdvise;
	}

	public String getCheckPart() {
		return checkPart;
	}

	public void setCheckPart(String checkPart) {
		this.checkPart = checkPart;
	}

	public String getCheckType() {
		return checkType;
	}

	public void setCheckType(String checkType) {
		this.checkType = checkType;
	}

	public String getOpinion() {
		return opinion;
	}

	public void setOpinion(String opinion) {
		this.opinion = opinion;
	}

	public String getDoctor() {
		return doctor;
	}

	public void setDoctor(String doctor) {
		this.doctor = doctor;
	}

	public String getCheckProject() {
		return checkProject;
	}

	public void setCheckProject(String checkProject) {
		this.checkProject = checkProject;
	}

	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "inspection_check_info";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "1213629183723971377";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return false;
    }
}