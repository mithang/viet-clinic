package com.geeke.sys.entity;

import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
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
	

	/**
	 * Quy tắc mã là dữ liệu cấp hệ thống: bảng sys_code_rule không có cột
	 * company_id và form gốc cũng không gửi company, nhưng DataEntity bắt
	 * @NotNull company nên @Valid ở CrudController.save luôn trả 400
	 * "Phòng khám ID Không được để trống" — tức Tạo/Sửa codeRule không bao giờ
	 * thành công qua UI, dù mã/quy tắc đã đúng. Sửa theo đúng mẫu
	 * NoticeSend đợt 2: khi company null thì lấy tenant của phiên đăng nhập;
	 * nếu phiên không có tenant (tài khoản hệ thống) thì dùng id "0" cấp hệ
	 * thống để qua @NotNull. Mapper CodeRuleDao không dùng company nên giá
	 * trị này không ghi vào DB và không đổi ngữ nghĩa dữ liệu.
	 */
	// DS80: @Length(min=1) cho phep null, nen null qua @Valid roi 500 o cot name NOT NULL; override de fail-closed 400.
	// Khong them @NotNull vao DataEntity.getName vi nhieu entity con khong co cot name / name nullable.
	@Override
	@NotNull(message="Tên quy tắc mã không được để trống")
	@Length(min=1, max=128, message="Tên Độ dài phải nằm trong khoảng 1 đến 128")
	public String getName() {
		return super.getName();
	}

	@Override
	public Company getCompany() {
		if (company == null) {
			String tenantId = SessionUtils.getLoginTenantId();
			Company fallback = new Company();
			if (StringUtils.isNotBlank(tenantId) && !"null".equals(tenantId)) {
				fallback.setId(tenantId);
			} else {
				fallback.setId("0");
			}
			return fallback;
		}
		return company;
	}

	@NotNull(message="Mã không được để trống")
	@Length(min=1, max=20, message="Độ dài mã phải nằm trong khoảng 1 đến 20 ")
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	
	@NotNull(message="Quy tắc không được để trống")
	@Length(min=1, max=512, message="quy tắc Độ dài phải nằm trong khoảng 1 đến 512")
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