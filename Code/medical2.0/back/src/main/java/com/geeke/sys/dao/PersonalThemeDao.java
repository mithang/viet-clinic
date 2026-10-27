package com.geeke.sys.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.sys.entity.PersonalTheme;

/**
 * Chủ đề hệ thốngDAOGiao diện
 * @author lys
 * @version 2021-07-16
 */
@Mapper
public interface PersonalThemeDao extends CrudDao<PersonalTheme> {
}