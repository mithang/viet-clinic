package com.geeke.daydashboard.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * Tổng quan ngày DAO (H5 research loop).
 *
 * Cố tình KHÔNG có entity và KHÔNG kế thừa CrudDao: H5 chỉ đọc
 * (read-only) các bảng sẵn có, không tạo bảng mới, không
 * INSERT/UPDATE/DELETE. Chỉ hai đếm tự viết ở đây (lượt khám và
 * lượt chờ thu phí trên bảng registration, cộng đếm sắp hết hạn
 * của vật tư có giới hạn tenant); thu phí/hoàn phí tái sử dụng
 * nguyên vẹn ChargeSummaryService (G6) để đối chiếu 1-1, còn
 * cảnh báo tồn kho thuốc / sắp hết hạn thuốc tái sử dụng nguyên
 * vẹn các service cảnh báo sẵn có (DrugService,
 * StuffService, SupplierStockService) để tổng bằng đúng total
 * của trang cảnh báo.
 */
@Mapper
public interface DayDashboardDao {

    /**
     * Số lượt đăng ký khám của một tenant trong một cửa sổ ngày
     * (del_flag = '0', is_pre IS NULL, status khác
     * 'registrationStatus_4' — cùng các điều kiện loại trừ của
     * RegistrationDao.count — và create_date trong
     * [startDate, endDate)).
     */
    int countVisits(Map<String, Object> params);

    /**
     * Trong các lượt trên, số lượt còn chờ thu phí: charge_status
     * '0' (chờ thu phí) hoặc '1' (thu một phần).
     */
    int countPendingCharges(Map<String, Object> params);

    /**
     * Số lô vật tư sắp hết hạn của một tenant: cùng định nghĩa
     * DATEDIFF(end_date, NOW()) &lt;= stuff.indate như đếm sắp
     * hết hạn thuốc (SupplierStockDao.countWarning), nhưng CÓ
     * giới hạn tenant (company / institution) — vì đếm sắp hết
     * hạn vật tư gốc (countStuffWarning) không có điều kiện
     * company nào, nên không được dùng nguyên vẹn cho dashboard
     * tenant-scoped. Xem ghi chú trong DayDashboardDao.xml.
     */
    int countStuffIndateWarningScoped(Map<String, Object> params);
}
