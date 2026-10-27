package com.geeke.sys.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.sys.entity.Action;

/**
 * Nhật ký thao tácDAOGiao diện
 * @author lys
 * @version 2019-12-06
 */
@Mapper
public interface ActionDao extends CrudDao<Action> {
	
}