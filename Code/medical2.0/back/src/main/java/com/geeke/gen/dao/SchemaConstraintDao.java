package com.geeke.gen.dao;

import com.geeke.gen.entity.Constraint;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Hệ thốngbảngTrường ràng buộcDAOGiao diện
 * @author lys
 * @version 2019-08-31
 */
@Mapper
public interface SchemaConstraintDao {
	
	/**
	 * chènDữ liệu
	 * @param entity
	 * @return
	 */
	public List<Constraint> listAll(String tableName);
}