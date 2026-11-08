package com.geeke.gen.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.common.persistence.DataEntity;
import com.geeke.sys.entity.DictItem;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
/**
 * Quản lý bảng nghiệp vụEntity
 * @author lys
 * @version 2021-08-26
 */
public class GenTableColumn extends DataEntity<GenTableColumn> {

	private static final long serialVersionUID = 1L;
	private GenTable genTable;      // bảngmã số 
	private String comments;		// Giải thích
	private String jdbcType;		// vật lýLoại
	private DictItem javaType;      // JAVALoại 
	private GenScheme customType;      // tùy chỉnhLoại 
	private String javaField;		// thuộc tínhtên
	private String defVal;		// Mặc địnhGiá trị
	private String isPk;		// chínhkhóa
	private String isNull;		// có thểtrống
	private String isInsert;		// có thểchèn
	private String isEdit;		// có thểbiên
	private String isList;		// Cộthiển thị
	private Integer width;		// Độ rộng cột
	private String isQuery;		// Truy vấn
	private DictItem queryType;      // Truy vấncách 
	private String queryVal;		// Điều kiện truy vấn
	private DictItem showType;      // điều khiển 
	private String interaction;		// liên độngthay đổilượng
	private String interactionVal;		// liên độngGiá trị
	private String parameters;		// liên độngTham số
	private String settings;		// của nónóĐặt(mở rộngTrườngJSON)
	private Integer sort;		// Sắp xếp

	
	public GenTableColumn() {
		super();
	}

	public GenTableColumn(String id){
		super(id);
	}
	

	public GenTableColumn(GenTable genTable){
		this.genTable = genTable;
	}

    // DS80: @Length(min=1) cho phep null, nen null qua @Valid roi 500 o cot name NOT NULL; override de fail-closed 400.
    // Khong them @NotNull vao DataEntity.getName vi nhieu entity con khong co cot name / name nullable.
    @Override
    @NotNull(message="Tên cột không được để trống")
    @Length(min=1, max=128, message="Tên Độ dài phải nằm trong khoảng 1 đến 128")
    public String getName() {
    	return super.getName();
    }

	@NotNull(message="bảng mã số Không thể là trống")
    public GenTable getGenTable() {
        return genTable;
    }

    public void setGenTable(GenTable genTable) {
        this.genTable = genTable;
    }
    
	@NotNull(message="Giải thích không được để trống")
	@Length(min=0, max=64, message="Giải thích Độ dài phải nằm trong khoảng 0 đến 64")
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	
	
	@NotNull(message="Loại vật lý không được để trống")
	@Length(min=1, max=100, message="vật lý Loại Độ dài phải nằm trong khoảng 1 đến 100")
	public String getJdbcType() {
		return jdbcType;
	}
	public void setJdbcType(String jdbcType) {
		this.jdbcType = jdbcType;
	}
	
	
	@NotNull(message="JAVA Loại Không thể là trống")
    public DictItem getJavaType() {
        return javaType;
    }

    public void setJavaType(DictItem javaType) {
        this.javaType = javaType;
    }
    
    public GenScheme getCustomType() {
        return customType;
    }

    public void setCustomType(GenScheme customType) {
        this.customType = customType;
    }
    
	@NotNull(message="Tên thuộc tính không được để trống")
	@Length(min=1, max=200, message="thuộc tính tên Độ dài phải nằm trong khoảng 1 đến 200")
	public String getJavaField() {
		return javaField;
	}
	public void setJavaField(String javaField) {
		this.javaField = javaField;
	}
	
	
	@Length(min=0, max=2000, message="Mặc định Giá trị Độ dài phải nằm trong khoảng 0 đến 2000")
	public String getDefVal() {
		return defVal;
	}
	public void setDefVal(String defVal) {
		this.defVal = defVal;
	}
	
	
	@Length(min=0, max=1, message="chính khóa Độ dài phải nằm trong khoảng 0 đến 1")
	public String getIsPk() {
		return isPk;
	}
	public void setIsPk(String isPk) {
		this.isPk = isPk;
	}
	
	
	@Length(min=0, max=1, message="có thể trống Độ dài phải nằm trong khoảng 0 đến 1")
	public String getIsNull() {
		return isNull;
	}
	public void setIsNull(String isNull) {
		this.isNull = isNull;
	}
	
	
	@Length(min=0, max=1, message="có thể chèn Độ dài phải nằm trong khoảng 0 đến 1")
	public String getIsInsert() {
		return isInsert;
	}
	public void setIsInsert(String isInsert) {
		this.isInsert = isInsert;
	}
	
	
	@NotNull(message="Có thể biên không được để trống")
	@Length(min=0, max=1, message="có thể biên Độ dài phải nằm trong khoảng 0 đến 1")
	public String getIsEdit() {
		return isEdit;
	}
	public void setIsEdit(String isEdit) {
		this.isEdit = isEdit;
	}
	
	
	@Length(min=0, max=1, message="Cột hiển thị Độ dài phải nằm trong khoảng 0 đến 1")
	public String getIsList() {
		return isList;
	}
	public void setIsList(String isList) {
		this.isList = isList;
	}
	
	
	@NotNull(message="Cột rộng Không thể là trống")
	public Integer getWidth() {
		return width;
	}
	public void setWidth(Integer width) {
		this.width = width;
	}
	
	
	@Length(min=0, max=1, message="Truy vấn Độ dài phải nằm trong khoảng 0 đến 1")
	public String getIsQuery() {
		return isQuery;
	}
	public void setIsQuery(String isQuery) {
		this.isQuery = isQuery;
	}
	
	
    public DictItem getQueryType() {
        return queryType;
    }

    public void setQueryType(DictItem queryType) {
        this.queryType = queryType;
    }
    
	@Length(min=0, max=2000, message="Điều kiện truy vấn Độ dài phải nằm trong khoảng 0 đến 2000")
	public String getQueryVal() {
		return queryVal;
	}
	public void setQueryVal(String queryVal) {
		this.queryVal = queryVal;
	}
	
	
    public DictItem getShowType() {
        return showType;
    }

    public void setShowType(DictItem showType) {
        this.showType = showType;
    }
    
	@Length(min=0, max=255, message="liên động thay đổi lượng Độ dài phải nằm trong khoảng 0 đến 255")
	public String getInteraction() {
		return interaction;
	}
	public void setInteraction(String interaction) {
		this.interaction = interaction;
	}
	
	
	@Length(min=0, max=2048, message="liên động Giá trị Độ dài phải nằm trong khoảng 0 đến 2048")
	public String getInteractionVal() {
		return interactionVal;
	}
	public void setInteractionVal(String interactionVal) {
		this.interactionVal = interactionVal;
	}
	
	
	@Length(min=0, max=2000, message="liên động Tham số Độ dài phải nằm trong khoảng 0 đến 2000")
	public String getParameters() {
		return parameters;
	}
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}
	
	
	@Length(min=0, max=2000, message="của nó nó Đặt (mở rộng Trường JSON) Độ dài phải từ 0 và 2000 Giữa")
	public String getSettings() {
		return settings;
	}
	public void setSettings(String settings) {
		this.settings = settings;
	}
	
	
	public Integer getSort() {
		return sort;
	}
	public void setSort(Integer sort) {
		this.sort = sort;
	}
	
	
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "gen_table_column";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "4011";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}