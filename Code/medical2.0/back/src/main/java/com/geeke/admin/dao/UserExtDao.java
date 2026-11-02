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

    /**
     * E1 (iteration 25): danh sach dong ext ACTIVE cua (user, tenant),
     * moi nhat truoc — dung de tai dung id khi payload update thieu
     * userExt.id, thay vi de CrudService.save insert dong moi. Khac
     * getUserExtByUserId (selectOne, nem TooManyResults khi da trung)
     * va getAllUserExtByUserId (khong loc tenant/del_flag).
     */
    List<UserExt> listActiveUserExtByUserId(@Param("userId")String userId,@Param("companyId")String companyId);
}