package com.geeke.toll.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.toll.entity.TollDetail;

/**
 * Quản lý thu phíDAOGiao diện
 * @author lc
 * @version 2022-06-15
 */
@Mapper
public interface TollDetailDao extends CrudDao<TollDetail> {

    /** ITER63: toll_id da luu cua mot dong chi tiet, tra truc tiep mot cot
     * (khong qua get() — get() map company null nen khong dung lam nguon
     * truth cho guard; va lookup mot cot tranh moi JOIN thua). */
    String getTollDetailTollId(@Param("id") String id);
}