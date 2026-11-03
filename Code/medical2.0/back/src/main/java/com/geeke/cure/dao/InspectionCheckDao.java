package com.geeke.cure.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.cure.entity.InspectionCheck;

import java.util.List;

/**
 * Xét nghiệm và kiểm traDAOGiao diện
 * @author rys
 * @version 2022-10-18
 */
@Mapper
public interface InspectionCheckDao extends CrudDao<InspectionCheck> {
    void allSave(InspectionCheck inspectionCheck);

    // R3 (iteration 41): lookup gon company cua dong cha da luu, kieu
    // Q4/R2 — tranh dao.get() nang neu chi can kiem tra tenant cua cha
    // truoc khi ghi info trong saveWithFile (controller doi cha sang
    // status "1" SAU khi save() tra ve, nen guard o buoc cha la qua muon).
    String getCompanyIdById(String id);

    List<InspectionCheck> getByRecipelInfoId(String registrationId);

    void deleteByRecipelInfoId(String recipelInfoId);

    List<InspectionCheck> getByRecipelDetail(String recipelDetailId);

}