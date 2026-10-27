package com.geeke.sys.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.sys.entity.QueryCondition;

/**
 * Điều kiện truy vấnDAOGiao diện
 * @author lys
 * @version 2021-07-05
 */
@Mapper
public interface QueryConditionDao extends CrudDao<QueryCondition> {
	
}