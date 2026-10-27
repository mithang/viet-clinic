package com.geeke.sys.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.sys.entity.CodeRule;

/**
 * Quy tắc mã hệ thốngDAOGiao diện
 * @author lys
 * @version 2021-05-17
 */
@Mapper
public interface CodeRuleDao extends CrudDao<CodeRule> {
	
	/**
	 * không ởcông ty của quy tắc
	 * @param companyId
	 * @return
	 */
	public List<CodeRule> listNotInCompany(String companyId);
	
	/**
	 * LấyMãquy tắc
	 * @param code
	 * @return
	 */
	public String getRuleDefByCode( @Param("code") String code);
}