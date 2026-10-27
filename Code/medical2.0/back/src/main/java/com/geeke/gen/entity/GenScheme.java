package com.geeke.gen.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.common.persistence.DataEntity;
import com.geeke.sys.entity.DictItem;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
/**
 * mãphương ánQuản lýEntity
 * @author lys
 * @version 2021-08-26
 */
public class GenScheme extends DataEntity<GenScheme> {

	private static final long serialVersionUID = 1L;
	private DictItem category;      // mẫu phútLớp 
	private String packageName;		// góiđường dẫn
	private String moduleName;		// mô-đuntên
	private String subModuleName;		// conmô-đuntên
	private String functionName;		// chức năngtên
	private String functionNameSimple;		// chức năngtênviết tắt
	private String functionAuthor;		// làmngười
	private String dialogWidth;		// chỉnh sửarộngđộ
	private Integer colCounts;		// chỉnh sửaCộtsố
	private GenTable genTable;      // Bảng nghiệp vụ 

	
	public GenScheme() {
		super();
	}

	public GenScheme(String id){
		super(id);
	}
	

	@NotNull(message="mẫu phútLớpKhông thể là trống")
    public DictItem getCategory() {
        return category;
    }

    public void setCategory(DictItem category) {
        this.category = category;
    }
    
	@Length(min=1, max=500, message="góiđường dẫnĐộ dài phải nằm trong khoảng 1 đến 500 ")
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	
	@Length(min=1, max=30, message="mô-đuntênĐộ dài phải nằm trong khoảng 1 đến 30 ")
	public String getModuleName() {
		return moduleName;
	}
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	
	
	@Length(min=0, max=30, message="conmô-đuntênĐộ dài phải nằm trong khoảng 0 đến 30 ")
	public String getSubModuleName() {
		return subModuleName;
	}
	public void setSubModuleName(String subModuleName) {
		this.subModuleName = subModuleName;
	}
	
	
	@Length(min=1, max=500, message="chức năngtênĐộ dài phải nằm trong khoảng 1 đến 500 ")
	public String getFunctionName() {
		return functionName;
	}
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}
	
	
	@Length(min=1, max=100, message="chức năngtênviết tắtĐộ dài phải nằm trong khoảng 1 đến 100 ")
	public String getFunctionNameSimple() {
		return functionNameSimple;
	}
	public void setFunctionNameSimple(String functionNameSimple) {
		this.functionNameSimple = functionNameSimple;
	}
	
	
	@Length(min=0, max=100, message="làmngườiĐộ dài phải nằm trong khoảng 0 đến 100 ")
	public String getFunctionAuthor() {
		return functionAuthor;
	}
	public void setFunctionAuthor(String functionAuthor) {
		this.functionAuthor = functionAuthor;
	}
	
	
	@Length(min=1, max=20, message="chỉnh sửarộngđộĐộ dài phải nằm trong khoảng 1 đến 20 ")
	public String getDialogWidth() {
		return dialogWidth;
	}
	public void setDialogWidth(String dialogWidth) {
		this.dialogWidth = dialogWidth;
	}
	
	
	@NotNull(message="chỉnh sửaCộtsốKhông thể là trống")
	public Integer getColCounts() {
		return colCounts;
	}
	public void setColCounts(Integer colCounts) {
		this.colCounts = colCounts;
	}
	
	
	@NotNull(message="nghiệp vụbảngKhông thể là trống")
    public GenTable getGenTable() {
        return genTable;
    }

    public void setGenTable(GenTable genTable) {
        this.genTable = genTable;
    }
    
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "gen_scheme";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "4008";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}