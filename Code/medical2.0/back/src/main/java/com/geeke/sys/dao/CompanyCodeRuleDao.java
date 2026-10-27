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
}