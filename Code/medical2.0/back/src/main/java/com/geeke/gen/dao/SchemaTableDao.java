package com.geeke.gen.dao;

import com.geeke.common.persistence.CrudDao;
import com.geeke.gen.entity.GenTable;
import org.apache.ibatis.annotations.Mapper;

/**
 * Điều khiển bảng hệ thốngDAOGiao diện
 * @author lys
 * @version 2019-08-31
 */
@Mapper
public interface SchemaTableDao extends CrudDao<GenTable> {
	
}