package com.geeke.chargesummary.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * Tổng kết thu phí cuối ngày DAO (G6 research loop).
 *
 * Cố tình KHÔNG có entity và KHÔNG kế thừa CrudDao: G6 chỉ đọc
 * (read-only) hai bảng sẵn có toll_info và toll_audit_log, không tạo
 * bảng mới, không INSERT/UPDATE/DELETE. Kết quả trả về dạng Map với
 * các key đặt tên theo alias trong XML, để service cộng tổng bằng
 * BigDecimal mà không cần lớp thực thể trung gian.
 */
@Mapper
public interface ChargeSummaryDao {

    /**
     * Các phiếu thu phí của một tenant trong một cửa sổ ngày
     * (del_flag = '0', create_date trong [startDate, endDate)).
     */
    List<Map<String, Object>> findCharges(Map<String, Object> params);

    /**
     * Các dòng audit hoàn phí (action = 'REFUND', del_flag = '0') của
     * một tenant trong cùng cửa sổ ngày.
     */
    List<Map<String, Object>> findRefunds(Map<String, Object> params);
}
