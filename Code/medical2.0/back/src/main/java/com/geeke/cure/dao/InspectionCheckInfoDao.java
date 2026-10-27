package com.geeke.cure.dao;

import com.geeke.cure.entity.InspectionCheckInfoCostItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.cure.entity.InspectionCheckInfo;

/**
 * Chi tiết xét nghiệm và kiểm traDAOGiao diện
 * @author rys
 * @version 2022-10-19
 */
@Mapper
public interface InspectionCheckInfoDao extends CrudDao<InspectionCheckInfo> {
    int insert(InspectionCheckInfo inspectionCheckInfo);

    InspectionCheckInfo getByInspecId(String inspecId);

    InspectionCheckInfoCostItem getByInspecId2(String inspecId);

    void deleteBy(String id);

}