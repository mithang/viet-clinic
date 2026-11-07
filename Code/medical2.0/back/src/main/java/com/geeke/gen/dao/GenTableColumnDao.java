package com.geeke.gen.dao;

import com.geeke.common.persistence.CrudDao;
import com.geeke.gen.entity.GenTableColumn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Quản lý bảng nghiệp vụDAOGiao diện
 * @author lys
 * @version 2021-08-26
 */
@Mapper
public interface GenTableColumnDao extends CrudDao<GenTableColumn> {

    /**
     * DS118 C: bang gen_table co ton tai khong, phuc vu guard
     * fail-closed o save -- id la tao dong mo coi (khong co FK
     * tren gen_table_id).
     */
    int countGenTableById(@Param("id") String id);
}