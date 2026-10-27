package com.geeke.sys.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.sys.entity.Theme;

/**
 * Chủ đề hệ thốngDAOGiao diện
 * @author lys
 * @version 2021-09-19
 */
@Mapper
public interface ThemeDao extends CrudDao<Theme> {
}