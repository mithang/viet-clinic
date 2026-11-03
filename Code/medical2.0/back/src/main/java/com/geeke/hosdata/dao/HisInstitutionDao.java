package com.geeke.hosdata.dao;

import com.geeke.common.persistence.CrudDao;
import com.geeke.hosdata.entity.HisInstitutions;
import com.geeke.hosdata.entity.HisMaterials;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HisInstitutionDao  extends CrudDao<HisInstitutions> {

    //Đãđồng bộ của duy nhấtMã
    List<String> getInstitutionCode(String companyId);

    // ITER33 K2: id dong cost_item con hieu luc (del_flag=0) cung ma,
    // chua dong bo (sync_id NULL hoac != 1) — de nhan thay vi chen trung.
    String findUnsyncedIdByCode(@Param("companyId") String companyId, @Param("itemCode") String itemCode);

    // ITER33 K2: gan co dong bo cho dong da co, khong doi ten/gia.
    int markSynced(@Param("id") String id);

}
