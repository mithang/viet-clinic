package com.geeke.sys.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.sys.entity.DictItem;

/**
 * Loại từ điểnDAOGiao diện
 * @author lys
 * @version 2021-12-07
 */
@Mapper
public interface DictItemDao extends CrudDao<DictItem> {
    /** Theo từ điểnnameLấy từ điểnvalue */
    DictItem getValue(String name, String id);

    /** Theo loại từ điểncodeLấy tất cả mục từ điển */
    List<DictItem> listByDictTypeCode(@Param("code") String code);
}