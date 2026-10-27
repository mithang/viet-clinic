package com.geeke.outpatient.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.admin.entity.User;
import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
/**
 * Điền bệnh ánEntity
 * @author txl
 * @version 2022-06-13
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MedicalRecord extends DataEntity<MedicalRecord> {

	private static final long serialVersionUID = 1014786989773332526L;
	private Company company;      // Phòng khámid 
	private String diagnosisId;      // từ xaChẩn đoánid
	private User doctor;      // Bác sĩid
	private Registration registration;      // Đăng ký khámid
	private String patientTell;		// Lý do khám chính
	private String nowHistory;		// Bệnh sử hiện tại
	private String beforeHistory;		// Tiền sử
	private String diagnose;		// Chẩn đoán
	private String personalHistory;		// Tiền sử cá nhân
	private String allergyHistory;		// Tiền sử dị ứng
	private String diseaseHistory;		// Tiền sử bệnh
	private String pregnancyHistory;		// tiền sử hôn nhân và mang thai
	private String infectiousHistory;		// Tiền sử bệnh truyền nhiễm
	private String lunariaHistory;		// Tiền sử kinh nguyệt
	private String familyHistory;		// Tiền sử gia đình
	private String surgeryHistory;		// Tiền sử phẫu thuật
	private String transfusionHistory;		// Tiền sử truyền máu
	private String physiqueCheck;		// Khám thực thể
	private String assistCheck;		// Kiểm tra hỗ trợ
	private String emergencyDiagnose;		// Chẩn đoán cấp cứu
	private String emergencyEffect;		// Hiệu quả cấp cứu
	private String otherCheck;		// Kiểm tra khác
	private String handlingSituation;		// Tình hình xử lý
	private String healthEducation;		// Giáo dục sức khỏe cá thể hóa
	private String epidemicDisease;		// Tiền sử dịch tễ
	private Date diagnoseDate;		// Thời gian chẩn đoán
	private String doctorAdvice;		// Hạng mục y lệnh
	private BigDecimal fee;		// tổngChi phí
	private String fileId;		// Tải lên tệp đính kèm
	private String westernDiagnose; //Chẩn đoán tây y
	private String chinaDiagnose; //Chẩn đoán đông y
	private String diseaseId;//Chẩn đoánid
	private String chinaSyndrome;//Chứng hậu Đông y
	private String syndromeId;//chứngchờid
	private String medic; // khám chữa bệnh từ xaBác sĩ
	private String department; // khám chữa bệnh từ xaBệnh viện
	private String register; // Đăng ký khám

	//Trường BHYT
	private String diagnosisCode; //Mã chẩn đoán



	public String getDiagnosisId() {
		return diagnosisId;
	}

	public void setDiagnosisId(String diagnosisId) {
		this.diagnosisId = diagnosisId;
	}

	public String getMedic() {
		return medic;
	}

	public void setMedic(String medic) {
		this.medic = medic;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getRegister() {
		return register;
	}

	public void setRegister(String register) {
		this.register = register;
	}

	public String getWesternDiagnose() {
		return westernDiagnose;
	}

	public void setWesternDiagnose(String westernDiagnose) {
		this.westernDiagnose = westernDiagnose;
	}

	public String getChinaDiagnose() {
		return chinaDiagnose;
	}

	public void setChinaDiagnose(String chinaDiagnose) {
		this.chinaDiagnose = chinaDiagnose;
	}
	
	public MedicalRecord() {
		super();
	}

	public MedicalRecord(String id){
		super(id);
	}
	
	public MedicalRecord(Registration registration){
		this.registration = registration;
	}

	@NotNull(message="Phòng khámidKhông được để trống")
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
	@NotNull(message="Bác sĩidKhông được để trống")
    public User getDoctor() {
        return doctor;
    }

    public void setDoctor(User doctor) {
        this.doctor = doctor;
    }
    
	@NotNull(message="Đăng ký khámidKhông được để trống")
    public Registration getRegistration() {
        return registration;
    }

    public void setRegistration(Registration registration) {
        this.registration = registration;
    }
    
	@Length(min=1, max=128, message="Lý do khám chínhĐộ dài phải nằm trong khoảng 1 đến 128 ")
	public String getPatientTell() {
		return patientTell;
	}
	public void setPatientTell(String patientTell) {
		this.patientTell = patientTell;
	}
	
	
	@Length(min=0, max=128, message="Bệnh sử hiện tạiĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getNowHistory() {
		return nowHistory;
	}
	public void setNowHistory(String nowHistory) {
		this.nowHistory = nowHistory;
	}
	
	
	@Length(min=0, max=128, message="Tiền sửĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getBeforeHistory() {
		return beforeHistory;
	}
	public void setBeforeHistory(String beforeHistory) {
		this.beforeHistory = beforeHistory;
	}
	
	
	@Length(min=1, max=128, message="Độ dài chẩn đoán phải nằm trong khoảng 1 đến 128 ")
	public String getDiagnose() {
		return diagnose;
	}
	public void setDiagnose(String diagnose) {
		this.diagnose = diagnose;
	}
	
	
	@Length(min=0, max=128, message="Tiền sử cá nhânĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getPersonalHistory() {
		return personalHistory;
	}
	public void setPersonalHistory(String personalHistory) {
		this.personalHistory = personalHistory;
	}
	
	
	@Length(min=0, max=128, message="Tiền sử dị ứngĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getAllergyHistory() {
		return allergyHistory;
	}
	public void setAllergyHistory(String allergyHistory) {
		this.allergyHistory = allergyHistory;
	}
	
	
	@Length(min=0, max=128, message="Tiền sử bệnhĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getDiseaseHistory() {
		return diseaseHistory;
	}
	public void setDiseaseHistory(String diseaseHistory) {
		this.diseaseHistory = diseaseHistory;
	}
	
	
	@Length(min=0, max=128, message="tiền sử hôn nhân và mang thaiĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getPregnancyHistory() {
		return pregnancyHistory;
	}
	public void setPregnancyHistory(String pregnancyHistory) {
		this.pregnancyHistory = pregnancyHistory;
	}
	
	
	@Length(min=0, max=128, message="Tiền sử bệnh truyền nhiễmĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getInfectiousHistory() {
		return infectiousHistory;
	}
	public void setInfectiousHistory(String infectiousHistory) {
		this.infectiousHistory = infectiousHistory;
	}
	
	
	@Length(min=0, max=128, message="Tiền sử kinh nguyệtĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getLunariaHistory() {
		return lunariaHistory;
	}
	public void setLunariaHistory(String lunariaHistory) {
		this.lunariaHistory = lunariaHistory;
	}
	
	
	@Length(min=0, max=128, message="Tiền sử gia đìnhĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getFamilyHistory() {
		return familyHistory;
	}
	public void setFamilyHistory(String familyHistory) {
		this.familyHistory = familyHistory;
	}
	
	
	@Length(min=0, max=128, message="Tiền sử phẫu thuậtĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getSurgeryHistory() {
		return surgeryHistory;
	}
	public void setSurgeryHistory(String surgeryHistory) {
		this.surgeryHistory = surgeryHistory;
	}
	
	
	@Length(min=0, max=128, message="Tiền sử truyền máuĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getTransfusionHistory() {
		return transfusionHistory;
	}
	public void setTransfusionHistory(String transfusionHistory) {
		this.transfusionHistory = transfusionHistory;
	}
	
	
	@Length(min=0, max=128, message="thểôKiểm traĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getPhysiqueCheck() {
		return physiqueCheck;
	}
	public void setPhysiqueCheck(String physiqueCheck) {
		this.physiqueCheck = physiqueCheck;
	}
	
	
	@Length(min=0, max=128, message="Kiểm tra hỗ trợĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getAssistCheck() {
		return assistCheck;
	}
	public void setAssistCheck(String assistCheck) {
		this.assistCheck = assistCheck;
	}
	
	
	@Length(min=0, max=128, message="cấp cứuĐộ dài chẩn đoán phải nằm trong khoảng 0 đến 128 ")
	public String getEmergencyDiagnose() {
		return emergencyDiagnose;
	}
	public void setEmergencyDiagnose(String emergencyDiagnose) {
		this.emergencyDiagnose = emergencyDiagnose;
	}
	
	
	@Length(min=0, max=128, message="Hiệu quả cấp cứuĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getEmergencyEffect() {
		return emergencyEffect;
	}
	public void setEmergencyEffect(String emergencyEffect) {
		this.emergencyEffect = emergencyEffect;
	}
	
	
	@Length(min=0, max=128, message="Kiểm tra khácĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getOtherCheck() {
		return otherCheck;
	}
	public void setOtherCheck(String otherCheck) {
		this.otherCheck = otherCheck;
	}

	@Length(min=0, max=128, message="Tình hình xử lýĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getHandlingSituation() {
		return handlingSituation;
	}
	public void setHandlingSituation(String handlingSituation) {
		this.handlingSituation = handlingSituation;
	}

	@Length(min=0, max=128, message="Giáo dục sức khỏe cá thể hóaĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getHealthEducation() {
		return healthEducation;
	}
	public void setHealthEducation(String healthEducation) {
		this.healthEducation = healthEducation;
	}

	@Length(min=0, max=128, message="Tiền sử dịch tễĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getEpidemicDisease() {
		return epidemicDisease;
	}
	public void setEpidemicDisease(String epidemicDisease) {
		this.epidemicDisease = epidemicDisease;
	}
	
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getDiagnoseDate() {
		return diagnoseDate;
	}
	public void setDiagnoseDate(Date diagnoseDate) {
		this.diagnoseDate = diagnoseDate;
	}
	
	
	@Length(min=0, max=128, message="Y lệnhviệchạng mụcĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getDoctorAdvice() {
		return doctorAdvice;
	}
	public void setDoctorAdvice(String doctorAdvice) {
		this.doctorAdvice = doctorAdvice;
	}
	
	
	public BigDecimal getFee() {
		return fee;
	}
	public void setFee(BigDecimal fee) {
		this.fee = fee;
	}
	
	
	@Length(min=0, max=45, message="Tải lên tệp đính kèmĐộ dài phải nằm trong khoảng 0 đến 45 ")
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	
	
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "medical_record";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "1014786989773332526";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }


}