package com.geeke.sys.entity;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Quy tắc mã hệ thốngEntity
 * @author lys
 * @version 2021-05-17
 */
public class CodeRule extends DataEntity<CodeRule> {

	private static final long serialVersionUID = 1L;
	private String code;		// Mã
	private String ruleDef;		// quy tắc

	
	public CodeRule() {
		super();
	}

	public CodeRule(String id){
		super(id);
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
        return "sys_code_rule";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "443038039937040385";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}