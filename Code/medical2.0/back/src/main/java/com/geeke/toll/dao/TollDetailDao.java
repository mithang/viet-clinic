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

    /** DS127 A: so dong chi tiet ton tai theo id (SELECT truc tiep, khong
     * qua get() — bang khong co del_flag, hard delete, nen COUNT(*) la
     * chan doan ton tai chinh xac; dung cho guard ghost-id o save /
     * bulkUpdate, noi getTollDetailTollId tra null ca khi dong khong ton
     * tai lan khi dong ton tai nhung toll_id NULL). */
    int countById(@Param("id") String id);
}