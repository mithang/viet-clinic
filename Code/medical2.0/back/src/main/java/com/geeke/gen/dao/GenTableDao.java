package com.geeke.gen.dao;

import com.geeke.common.persistence.CrudDao;
import com.geeke.gen.entity.GenTable;
import org.apache.ibatis.annotations.Mapper;

/**
 * Quản lý bảng nghiệp vụDAOGiao diện
 * @author lys
 * @version 2021-08-26
 */
@Mapper
public interface GenTableDao extends CrudDao<GenTable> {
}