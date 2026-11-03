package com.geeke.cure.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.cure.entity.InspectionCheckDetail;

import java.util.List;

/**
 * Chi tiết xét nghiệm và kiểm traDAOGiao diện
 * @author rys
 * @version 2022-10-19
 */
@Mapper
public interface InspectionCheckDetailDao extends CrudDao<InspectionCheckDetail> {
    int insert(InspectionCheckDetail inspectionCheckDetail);

    List<InspectionCheckDetail> getByInfoId(String infoId);

    // R2 (iteration 41): lookup gon company cua dong detail da luu, kieu
    // Q4 (MyTaskDao.getCompanyIdById) — tranh dao.get() nang neu chi can
    // kiem tra tenant truoc khi ghi detail trong saveWithFile.
    String getCompanyIdById(String id);

    void deleteBy(String id);

}