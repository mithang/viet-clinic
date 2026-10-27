package com.geeke.outpatient.entity;

import com.geeke.org.entity.Company;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import com.geeke.sys.entity.DictItem;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;

import java.util.List;

/**
 * Đơn thuốc mẫuEntity
 * @author rys
 * @version 2022-10-08
 */
public class Recipetemplate extends DataEntity<Recipetemplate> {

	private static final long serialVersionUID = 1186832625065336477L;
	private Company company;      // Phòng khámid 
	private String recipetemplateName;		// Tên mẫu
	private String code;        //mẫumã số
	private DictItem type;      // Loại mẫu 
	private String category;		// mẫuLớploại
	private String diagnosis;		// Chẩn đoán
	private String singleDosage;		// Liều dùng mỗi lần
	private String remarks;        //mẫuGiải thích
	private String createId;       //Người tạoid
	private String patientId;       //Bệnh nhânid
	private String registerId;       //Đăng ký khámid
	private RecipeTemplateInfo recipeTemplateInfo;
	private List<RecipeTemplateDetail> recipeTemplateDetail;
	
	public Recipetemplate() {
		super();
	}

	public Recipetemplate(String id){
		super(id);
	}

	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}

	public String getRegisterId() {
		return registerId;
	}

	public void setRegisterId(String registerId) {
		this.registerId = registerId;
	}

	@NotNull(message="Phòng khámidKhông được để trống")
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
	@Length(min=1, max=100, message="Tên mẫuĐộ dài phải nằm trong khoảng 1 đến 100 ")
	public String getRecipetemplateName() {
		return recipetemplateName;
	}
	public void setRecipetemplateName(String recipetemplateName) {
		this.recipetemplateName = recipetemplateName;
	}
	
	
	@NotNull(message="Loại mẫuKhông thể là trống")
    public DictItem getType() {
        return type;
    }

    public void setType(DictItem type) {
        this.type = type;
    }
    
	@Length(min=1, max=1, message="mẫuLớploạiĐộ dài phải nằm trong khoảng 1 đến 1 ")
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	
	
	@Length(min=0, max=100, message="Độ dài chẩn đoán phải nằm trong khoảng 0 đến 100 ")
	public String getDiagnosis() {
		return diagnosis;
	}
	public void setDiagnosis(String diagnosis) {
		this.diagnosis = diagnosis;
	}
	
	
	@Length(min=0, max=100, message="Liều dùng mỗi lầnĐộ dài phải nằm trong khoảng 0 đến 100 ")
	public String getSingleDosage() {
		return singleDosage;
	}
	public void setSingleDosage(String singleDosage) {
		this.singleDosage = singleDosage;
	}

	public List<RecipeTemplateDetail> getRecipeTemplateDetail() {
		return recipeTemplateDetail;
	}

	public void setRecipeTemplateDetail(List<RecipeTemplateDetail> recipeTemplateDetail) {
		this.recipeTemplateDetail = recipeTemplateDetail;
	}

	public RecipeTemplateInfo getRecipeTemplateInfo() {
		return recipeTemplateInfo;
	}

	public void setRecipeTemplateInfo(RecipeTemplateInfo recipeTemplateInfo) {
		this.recipeTemplateInfo = recipeTemplateInfo;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getCreateId() {
		return createId;
	}

	public void setCreateId(String createId) {
		this.createId = createId;
	}

	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "recipetemplate";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "1186832625065336477";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}