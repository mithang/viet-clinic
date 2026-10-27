package com.geeke.grid.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.grid.entity.UserGrid;

/**
 * Bố cục tùy chỉnhDAOGiao diện
 * @author ycy
 * @version 2021-08-02
 */
@Mapper
public interface UserGridDao extends CrudDao<UserGrid> {
}