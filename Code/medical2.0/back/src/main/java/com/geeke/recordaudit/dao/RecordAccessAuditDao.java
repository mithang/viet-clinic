package com.geeke.recordaudit.dao;

import com.geeke.common.persistence.CrudDao;
import com.geeke.recordaudit.entity.RecordAccessAudit;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * Nhật ký truy cập hồ sơ DAO.
 *
 * Chỉ 2 thao tác được dùng: insert (ghi 1 dòng khi xem/sửa) và
 * findByTenant (liệt kê của 1 tenant, mới nhất trước). Không có
 * update/delete nào được gọi từ service — nhật ký là append-only.
 */
@Mapper
public interface RecordAccessAuditDao extends CrudDao<RecordAccessAudit> {

    /**
     * Các dòng audit đang hiệu lực của một tenant, mới nhất trước.
     * Lọc tùy chọn theo patientId và action; limit kẹp ở service.
     */
    List<RecordAccessAudit> findByTenant(Map<String, Object> params);
}
