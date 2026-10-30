package com.geeke.toll.dao;

import com.geeke.toll.entity.TollAuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * DAO audit hủy/hoàn hóa đơn (blocker 6).
 * Chỉ cần ghi log (insert): bảng audit không có luồng đọc/sửa/xóa qua
 * ứng dụng, tra cứu trực tiếp bằng SQL khi kiểm toán. Vì vậy không extends
 * CrudDao — tránh bắt buộc phải có đủ statement get/count/listPage...
 * trong mapper XML cho một bảng chỉ-append.
 */
@Mapper
public interface TollAuditLogDao {

    int insertAudit(TollAuditLog log);
}
