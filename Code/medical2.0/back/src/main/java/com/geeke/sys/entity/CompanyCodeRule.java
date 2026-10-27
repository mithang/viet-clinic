package com.geeke.sys.entity;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.common.persistence.DataEntity;
/**
 * Quy tắc mã công tyEntity
 * @author lys
 * @version 2021-05-17
 */
public class CompanyCodeRule extends DataEntity<CompanyCodeRule> {

	private static final long serialVersionUID = 443292628418674688L;
	private String companyId;      // Công ty 
	private CodeRule sysRule;      // Hệ thốngmã sốquy tắc 
	private String code;		// Mã
	private String ruleDef;		// quy tắc

	
	public CompanyCodeRule() {
		super();
	}

	public CompanyCodeRule(String id){
		super(id);
	}
	
	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	@NotNull(message="Hệ thốngmã sốquy tắcKhông thể là trống")
    public CodeRule getSysRule() {
        return sysRule;
    }

    public void setSysRule(CodeRule sysRule) {
        this.sysRule = sysRule;
    }
    
	@Length(min=1, max=20, message="Độ dài mã phải nằm trong khoảng 1 đến 20 ")
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	
	@Length(min=1, max=512, message="quy tắcĐộ dài phải nằm trong khoảng 1 đến 512 ")
	public String getRuleDef() {
		return ruleDef;
	}
	public void setRuleDef(String ruleDef) {
		this.ruleDef = ruleDef;
	}
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "sys_company_code_rule";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "443292628418674688";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}