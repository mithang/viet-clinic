package com.geeke.toll.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.toll.entity.TollDetail;

/**
 * Quản lý thu phíDAOGiao diện
 * @author lc
 * @version 2022-06-15
 */
@Mapper
public interface TollDetailDao extends CrudDao<TollDetail> {
}