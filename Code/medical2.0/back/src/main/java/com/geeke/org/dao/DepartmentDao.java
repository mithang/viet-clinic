package com.geeke.org.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.TreeDao;
import com.geeke.org.entity.Department;

/**
 * Quản lý phòng banDAOGiao diện
 * @author lys
 * @version 2021-07-13
 */
@Mapper
public interface DepartmentDao extends TreeDao<Department> {
	
}