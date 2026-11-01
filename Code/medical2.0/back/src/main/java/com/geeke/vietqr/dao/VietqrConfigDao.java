package com.geeke.vietqr.dao;

import com.geeke.common.persistence.CrudDao;
import com.geeke.vietqr.entity.VietqrConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * Cấu hình VietQR DAO.
 *
 * Chỉ có 2 thao tác được dùng: tìm cấu hình đang hiệu lực của một tenant
 * và INSERT/UPDATE qua CrudDao. Cố tình KHÔNG có listPage/listAll: cấu
 * hình tài khoản của phòng khám này không bao giờ được liệt kê chung,
 * kể cả cho tài khoản của tenant khác — controller chỉ trả đúng dòng
 * của tenant đang đăng nhập.
 */
@Mapper
public interface VietqrConfigDao extends CrudDao<VietqrConfig> {

    /**
     * Cấu hình đang hiệu lực (del_flag = '0', mới nhất) của một tenant,
     * hoặc null khi phòng khám chưa cấu hình VietQR.
     */
    VietqrConfig findActive(Map<String, Object> params);
}
