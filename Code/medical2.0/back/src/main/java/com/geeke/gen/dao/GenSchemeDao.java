package com.geeke.gen.dao;

import com.geeke.common.persistence.CrudDao;
import com.geeke.gen.entity.GenScheme;
import org.apache.ibatis.annotations.Mapper;

/**
 * mãphương ánQuản lýDAOGiao diện
 * @author lys
 * @version 2019-12-06
 */
@Mapper
public interface GenSchemeDao extends CrudDao<GenScheme> {
	
}