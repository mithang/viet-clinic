package com.geeke.member.dao;

import com.geeke.common.persistence.CrudDao;
import com.geeke.member.entity.MemberManagement;
import com.geeke.member.entity.MemberManagementDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Quản lý thẻ hội viênDAOGiao diện
 * @author rys
 * @version 2022-10-26
 */
@Mapper
public interface MemberManagementDetailDao extends CrudDao<MemberManagementDetail> {
    List<MemberManagementDetail> getByManagementId(String managementId);

    MemberManagementDetail getByManagementIdAndMemberItemId(@Param("managementId") String managementId, @Param("memberItemId") String memberItemId);

    void updateUseNumber(MemberManagementDetail memberManagementDetail);

    /**
     * DS62: claim co dieu kien cho use-count cua chi tiet the.
     * SET use_number = #{useNumber} chi khi gia tri hien tai van dung
     * #{expectedUseNumber} (doc truoc do trong transaction), gia tri moi
     * khong giam va khong vuot allowance number. Tra so dong bi anh
     * huong: 0 = luong khac da doi the truoc -> caller tu choi nhu
     * truong hop tuan tu tuong duong (nap lai trang thai moi roi thu lai).
     */
    int claimUseNumber(@Param("id") String id, @Param("useNumber") Integer useNumber,
                       @Param("expectedUseNumber") Integer expectedUseNumber);

    MemberManagementDetail getOwnership(String id);

}