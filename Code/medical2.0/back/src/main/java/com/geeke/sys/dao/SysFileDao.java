package com.geeke.sys.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.sys.entity.SysFile;

/**
 * Tệp đính kèm hệ thốngDAOGiao diện
 * @author szy
 * @version 2021-09-22
 */
@Mapper
public interface SysFileDao extends CrudDao<SysFile> {
    // TheoIDmảngXóa
    int deleteByIds(@Param("ids") String[] ids);
}