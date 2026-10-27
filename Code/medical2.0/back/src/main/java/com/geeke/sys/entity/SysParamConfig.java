package com.geeke.sys.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.geeke.common.persistence.DataEntity;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * Chủ đề hệ thốngEntity
 * @author lys
 * @version 2021-09-19
 */
public class SysParamConfig extends DataEntity<SysParamConfig> {

	private static final long serialVersionUID = 532290982345658937L;
	private String category;        // Lớploại
	private String configName;		// cấu hìnhTên
	private String configValue;		// cấu hìnhGiá trị
	private String companyId;		// Cơ sởId




	public SysParamConfig() {
		super();
	}

	public SysParamConfig(String id){
		super(id);
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@NotNull(message="cấu hìnhhạng mụcTênKhông thể là trống")
	public String getConfigName() {
		return configName;
	}

	public void setConfigName(String configName) {
		this.configName = configName;
	}
	@Length(min=1, max=2000, message="Độ dài chủ đề phải nằm trong khoảng 1 đến 64 ")
	public String getConfigValue() {
		return configValue;
	}

	public void setConfigValue(String configValue) {
		this.configValue = configValue;
	}
	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "sys_param_config";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "532290934221258937";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}