package com.geeke.hosdata.dao;

import com.geeke.common.persistence.CrudDao;
import com.geeke.hosdata.entity.HisMaterials;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HisMaterialDao extends CrudDao<HisMaterials> {

    List<String> getMaterialCode(String companyId);

    // ITER33 K2: id dong stuff con hieu luc (del_flag=0) cung ma,
    // chua dong bo (sync_id NULL hoac != 1) — de nhan thay vi chen trung.
    String findUnsyncedIdByCode(@Param("companyId") String companyId, @Param("code") String code);

    // ITER33 K2: gan co dong bo cho dong da co, khong doi ten/gia.
    int markSynced(@Param("id") String id);

}
