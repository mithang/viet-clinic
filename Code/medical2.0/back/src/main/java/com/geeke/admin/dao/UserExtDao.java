package com.geeke.admin.dao;

import com.geeke.admin.entity.UserExt;
import com.geeke.common.persistence.CrudDao;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Người dùngmở rộngDAOGiao diện
 * @author ch
 * @version 2022-06-10
 */
@Mapper
public interface UserExtDao extends CrudDao<UserExt> {

    /**
     * Theo người dùngidtramở rộngThông tin
     * @param userId
     * @return
     */
    UserExt getUserExtByUserId(@Param("userId")String userId,@Param("companyId")String companyId);

    /**
     * Truy vấntất cả của mở rộngDữ liệu
     * @param userId
     * @return
     */
    List<UserExt> getAllUserExtByUserId(@Param("userId")String userId);
}