package com.geeke.booking.dao;

import com.geeke.booking.entity.BookingRequest;
import com.geeke.common.persistence.CrudDao;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Yêu cầu đặt lịch DAO.
 */
@Mapper
public interface BookingRequestDao extends CrudDao<BookingRequest> {

    /**
     * Hàng chờ pending của một tenant, ngày muốn khám tăng dần.
     */
    List<BookingRequest> listPending(Map<String, Object> params);

    int countPending(Map<String, Object> params);

    /**
     * Các yêu cầu trước đó của cùng một SĐT trong cùng tenant (mọi trạng
     * thái), mới nhất trước — lễ tân xem khi xử lý trùng SĐT. Chỉ đọc
     * bảng booking_request, không đọc dữ liệu bệnh nhân hay khám chữa.
     */
    List<BookingRequest> listPriorByPhone(Map<String, Object> params);

    int countPriorByPhone(Map<String, Object> params);

    /**
     * Yêu cầu pending trùng (cùng tenant, SĐT, ngày muốn khám) — intake
     * trả lại bản ghi này thay vì tạo trùng.
     */
    BookingRequest findPendingDuplicate(Map<String, Object> params);

    /**
     * Giành quyền xác nhận (claim nguyên tử, bài học G1: claim trước,
     * insert sau). Chỉ thành công khi bản ghi còn pending và thuộc đúng
     * tenant. Caller chỉ tạo registration khi claim này trả về 1; toàn
     * bộ confirm chạy trong một transaction nên nếu tạo registration
     * thất bại thì claim cũng rollback.
     */
    int claimConfirm(@Param("id") String id, @Param("companyId") String companyId,
                     @Param("updateBy") String updateBy, @Param("updateDate") java.util.Date updateDate);

    /**
     * Ghi registration_id sau khi lịch hẹn thật đã tạo xong (vẫn trong
     * cùng transaction với claim).
     */
    int linkRegistration(@Param("id") String id, @Param("companyId") String companyId,
                         @Param("registrationId") String registrationId,
                         @Param("updateBy") String updateBy, @Param("updateDate") java.util.Date updateDate);

    /**
     * Từ chối: chỉ thành công khi bản ghi còn pending và thuộc đúng
     * tenant; lý do được giữ lại, bản ghi không bị xóa.
     */
    int reject(@Param("id") String id, @Param("companyId") String companyId,
               @Param("rejectReason") String rejectReason,
               @Param("updateBy") String updateBy, @Param("updateDate") java.util.Date updateDate);

    /**
     * Số bác sĩ trực trong ngày (bảng scheduling_data, đường lịch trực
     * hiện có). Ngày không có lịch trực = ngày chưa mở nhận lịch hẹn:
     * confirm bị chặn để lễ tân không xác nhận vào ngày không có bác sĩ.
     * Đây là định nghĩa "ngày đã kín/chưa mở" của G2 — không dùng hằng
     * số sức chứa tự đặt, mà dựa trên lịch trực thật của phòng khám.
     */
    int countRosterForDate(@Param("companyId") String companyId, @Param("wantedDate") String wantedDate);
}
