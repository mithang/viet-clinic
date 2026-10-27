package com.geeke.member.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.member.entity.MemberManagement;

import java.util.List;

/**
 * Quản lý thẻ hội viênDAOGiao diện
 * @author rys
 * @version 2022-10-26
 */
@Mapper
public interface MemberManagementDao extends CrudDao<MemberManagement> {
    List<MemberManagement> getByMemberSetId(String memberSetId);

    List<MemberManagement> getByPatientAndMember(@Param("patientId") String patientId, @Param("memberSetId") String memberSetId);

    List<MemberManagement> getByPatientId(String patientId);

    void updateStatus(MemberManagement memberManagement);

}