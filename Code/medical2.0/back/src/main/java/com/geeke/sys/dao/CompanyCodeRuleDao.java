package com.geeke.sys.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.sys.entity.CompanyCodeRule;

/**
 * Quy tắc mã công tyDAOGiao diện
 * @author lys
 * @version 2021-05-17
 */
@Mapper
public interface CompanyCodeRuleDao extends CrudDao<CompanyCodeRule> {
	
	/**
	 * LấyMãquy tắc
	 * @param companyId
	 * @param code
	 * @return
	 */
	public String getRuleDefByCode(@Param("companyId") String companyId, @Param("code") String code);

	/**
	 * N3d: đọc trực tiếp company_id của một dòng rule, không qua get().
	 * Mapper get() INNER JOIN sys_code_rule nên trả null với dòng mồ côi
	 * (sys_rule_id trỏ tới rule đã xóa — live có 2 dòng như vậy), trong khi
	 * update/delete chỉ cần WHERE id nên vẫn ghi được dòng đó. Lookup trực
	 * tiếp một bảng này là nguồn sự thật duy nhất cho guard ghi/đọc.
	 * @param id
	 * @return company_id của dòng, hoặc null nếu id không tồn tại
	 */
	public String getCompanyIdById(@Param("id") String id);
}