package com.geeke.sys.dao;

import com.geeke.common.persistence.CrudDao;
import com.geeke.sys.entity.PersonalTheme;
import com.geeke.sys.entity.SysParamConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * Hệ thốngcấu hìnhDAOGiao diện
 * @author lc
 */
@Mapper
public interface SysParamConfigDao extends CrudDao<SysParamConfig> {
}