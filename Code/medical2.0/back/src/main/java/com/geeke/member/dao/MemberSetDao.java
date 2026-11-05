package com.geeke.member.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.member.entity.MemberSet;

/**
 * Cài đặt thẻ hội viênDAOGiao diện
 * @author rys
 * @version 2022-10-25
 */
@Mapper
public interface MemberSetDao extends CrudDao<MemberSet> {
    int insert(MemberSet memberSet);

    int updateNumber(@Param("id") String id,@Param("number") int number);

    // DS61: tru tuyet doi number = number - 1, chi khi number > 0.
    // Tra ve so dong bi anh huong (0 = het the hoac thua cuoc dua song song).
    int decrementNumber(@Param("id") String id);

    // DS61: cong tuyet doi number = number + 1 (hoan tra khi huy the).
    int incrementNumber(@Param("id") String id);

}