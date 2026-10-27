package com.geeke.common.sequence.entity;

import com.geeke.common.persistence.DataEntity;


/**
 * bảngCộttiêu đề
 * 
 * @author lys
 * @date 2021/4/30
 */
public class Sequence extends DataEntity<Sequence> {

	private static final long serialVersionUID = -5698892194611228984L;
	private String companyId;   //  Công tyId
	private String code;		// quy tắcmã sốcode
	private String prefix;		// tiền tố
	private int val = 1;	// thứ tựCộtGiá trị
	
	public String getCompanyId() {
		return companyId;
	}
	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public int getVal() {
		return val;
	}
	public void setVal(int val) {
		this.val = val;
	}

}
