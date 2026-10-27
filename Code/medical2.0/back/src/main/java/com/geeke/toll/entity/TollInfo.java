package com.geeke.toll.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.admin.entity.User;
import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
import com.geeke.outpatient.entity.MedicalRecord;
import com.geeke.outpatient.entity.Patient;
import com.geeke.outpatient.entity.RecipelDetail;
import com.geeke.outpatient.entity.RecipelInfo;
import com.geeke.sys.entity.DictItem;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
/**
 * Quản lý thu phíEntity
 * @author lc
 * @version 2022-06-22
 */
public class TollInfo extends DataEntity<TollInfo> {

	private static final long serialVersionUID = 1024846610635145502L;
    private String jgid;
    private String jgmc;
	private Company company;      // Phòng khámid 
	private String department;      // Khoa
	private String doseUnit;      // Liều lượng
	private RecipelInfo recipel;      // Đơn thuốc
	private BigDecimal amountReceivable;		// phải thu(trả)Số tiền
	private BigDecimal amountReceived;		// Số tiền thực thu
	private BigDecimal amountDiscounted;		// Số tiền ưu đãi
	private Float discount;		// 折扣số tiềnđộ 
	private DictItem paymentType;      // thanh toánLoại
	private DictItem amountStatus;      // đơndữ liệuTrạng thái, enum tham khảo 
	private String tollNumber;		// Số phiếu thu phí
	private DictItem tollType;      // Loại thu phí, enum tham khảo 
	private Patient patient;      // Bệnh nhân 
	private MedicalRecord medical;      // ca bệnh 
    private User doctor;	// Khoa lập phiếu/Bác sĩ
    private RecipelDetail recipelDetail;	// Hạng mục thu phí, Thuốc/Vật tư/khámtrị liệuTên&Số lượng
    private String registrationFeeId;   //Phí đăng ký khámBản ghiđăng kýThông tinid
    private int returnType;            //Thu phíđơnhoàn phíđánh dấu
	
	public TollInfo() {
		super();
	}

	public TollInfo(String id){
		super(id);
	}

    public String getDoseUnit() {
        return doseUnit;
    }

    public void setDoseUnit(String doseUnit) {
        this.doseUnit = doseUnit;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
    public RecipelInfo getRecipel() {
        return recipel;
    }

    public void setRecipel(RecipelInfo recipel) {
        this.recipel = recipel;
    }
    
	public BigDecimal getAmountReceivable() {
		return amountReceivable;
	}
	public void setAmountReceivable(BigDecimal amountReceivable) {
		this.amountReceivable = amountReceivable;
	}
	
	
	public BigDecimal getAmountReceived() {
		return amountReceived;
	}
	public void setAmountReceived(BigDecimal amountReceived) {
		this.amountReceived = amountReceived;
	}
	
	
	public BigDecimal getAmountDiscounted() {
		return amountDiscounted;
	}
	public void setAmountDiscounted(BigDecimal amountDiscounted) {
		this.amountDiscounted = amountDiscounted;
	}
	
	
	public Float getDiscount() {
		return discount;
	}
	public void setDiscount(Float discount) {
		this.discount = discount;
	}
	
	
	@NotNull(message="thanh toánLoạiKhông thể là trống")
    public DictItem getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(DictItem paymentType) {
        this.paymentType = paymentType;
    }
    
	@NotNull(message="đơndữ liệuTrạng thái, enum tham khảoKhông thể là trống")
    public DictItem getAmountStatus() {
        return amountStatus;
    }

    public void setAmountStatus(DictItem amountStatus) {
        this.amountStatus = amountStatus;
    }
    
	@Length(min=1, max=64, message="Thu phíđơnsốĐộ dài phải nằm trong khoảng 1 đến 64 ")
	public String getTollNumber() {
		return tollNumber;
	}
	public void setTollNumber(String tollNumber) {
		this.tollNumber = tollNumber;
	}
	
	
	@NotNull(message="Loại thu phí, enum tham khảoKhông thể là trống")
    public DictItem getTollType() {
        return tollType;
    }

    public void setTollType(DictItem tollType) {
        this.tollType = tollType;
    }
    
    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }
    
    public MedicalRecord getMedical() {
        return medical;
    }

    public void setMedical(MedicalRecord medical) {
        this.medical = medical;
    }



    public User getDoctor() {
        return doctor;
    }

    public void setDoctor(User doctor) {
        this.doctor = doctor;
    }

    public RecipelDetail getRecipelDetail() {
        return recipelDetail;
    }

    public void setRecipelDetail(RecipelDetail recipelDetail) {
        this.recipelDetail = recipelDetail;
    }

    public String getRegistrationFeeId() {
        return registrationFeeId;
    }

    public void setRegistrationFeeId(String registrationFeeId) {
        this.registrationFeeId = registrationFeeId;
    }

    public int getReturnType() {
        return returnType;
    }

    public void setReturnType(int returnType) {
        this.returnType = returnType;
    }

    /**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "toll_info";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "1024846610635145502";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}