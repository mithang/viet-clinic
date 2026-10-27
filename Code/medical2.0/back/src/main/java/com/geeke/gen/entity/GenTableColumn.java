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

	@NotNull(message="bảngmã sốKhông thể là trống")
    public GenTable getGenTable() {
        return genTable;
    }

    public void setGenTable(GenTable genTable) {
        this.genTable = genTable;
    }
    
	@Length(min=0, max=500, message="Giải thíchĐộ dài phải nằm trong khoảng 0 đến 500 ")
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	
	
	@Length(min=1, max=100, message="vật lýLoạiĐộ dài phải nằm trong khoảng 1 đến 100 ")
	public String getJdbcType() {
		return jdbcType;
	}
	public void setJdbcType(String jdbcType) {
		this.jdbcType = jdbcType;
	}
	
	
	@NotNull(message="JAVALoạiKhông thể là trống")
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
    
	@Length(min=1, max=200, message="thuộc tínhtênĐộ dài phải nằm trong khoảng 1 đến 200 ")
	public String getJavaField() {
		return javaField;
	}
	public void setJavaField(String javaField) {
		this.javaField = javaField;
	}
	
	
	@Length(min=0, max=2000, message="Mặc địnhGiá trịĐộ dài phải nằm trong khoảng 0 đến 2000 ")
	public String getDefVal() {
		return defVal;
	}
	public void setDefVal(String defVal) {
		this.defVal = defVal;
	}
	
	
	@Length(min=0, max=1, message="chínhkhóaĐộ dài phải nằm trong khoảng 0 đến 1 ")
	public String getIsPk() {
		return isPk;
	}
	public void setIsPk(String isPk) {
		this.isPk = isPk;
	}
	
	
	@Length(min=0, max=1, message="có thểtrốngĐộ dài phải nằm trong khoảng 0 đến 1 ")
	public String getIsNull() {
		return isNull;
	}
	public void setIsNull(String isNull) {
		this.isNull = isNull;
	}
	
	
	@Length(min=0, max=1, message="có thểchènĐộ dài phải nằm trong khoảng 0 đến 1 ")
	public String getIsInsert() {
		return isInsert;
	}
	public void setIsInsert(String isInsert) {
		this.isInsert = isInsert;
	}
	
	
	@Length(min=0, max=1, message="có thểbiênĐộ dài phải nằm trong khoảng 0 đến 1 ")
	public String getIsEdit() {
		return isEdit;
	}
	public void setIsEdit(String isEdit) {
		this.isEdit = isEdit;
	}
	
	
	@Length(min=0, max=1, message="Cộthiển thịĐộ dài phải nằm trong khoảng 0 đến 1 ")
	public String getIsList() {
		return isList;
	}
	public void setIsList(String isList) {
		this.isList = isList;
	}
	
	
	@NotNull(message="CộtrộngKhông thể là trống")
	public Integer getWidth() {
		return width;
	}
	public void setWidth(Integer width) {
		this.width = width;
	}
	
	
	@Length(min=0, max=1, message="Truy vấnĐộ dài phải nằm trong khoảng 0 đến 1 ")
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
    
	@Length(min=0, max=2000, message="Điều kiện truy vấnĐộ dài phải nằm trong khoảng 0 đến 2000 ")
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
    
	@Length(min=0, max=255, message="liên độngthay đổilượngĐộ dài phải nằm trong khoảng 0 đến 255 ")
	public String getInteraction() {
		return interaction;
	}
	public void setInteraction(String interaction) {
		this.interaction = interaction;
	}
	
	
	@Length(min=0, max=2048, message="liên độngGiá trịĐộ dài phải nằm trong khoảng 0 đến 2048 ")
	public String getInteractionVal() {
		return interactionVal;
	}
	public void setInteractionVal(String interactionVal) {
		this.interactionVal = interactionVal;
	}
	
	
	@Length(min=0, max=2000, message="liên độngTham sốĐộ dài phải nằm trong khoảng 0 đến 2000 ")
	public String getParameters() {
		return parameters;
	}
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}
	
	
	@Length(min=0, max=2000, message="của nónóĐặt(mở rộngTrườngJSON)Độ dài phải từ 0  và  2000 Giữa")
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