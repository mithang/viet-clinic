package com.geeke.followup.dao;

import com.geeke.common.persistence.CrudDao;
import com.geeke.followup.entity.FollowupReminder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Nhắc lịch tái khám DAO.
 */
@Mapper
public interface FollowupReminderDao extends CrudDao<FollowupReminder> {

    /**
     * Danh sách đến hạn: trạng thái chờ xử lý, có ngày hẹn, ngày hẹn
     * trước thời điểm dueBefore (cuối ngày hôm nay theo giờ phòng khám).
     * companyId null = mọi tenant (chỉ job hệ thống dùng).
     */
    List<FollowupReminder> listDue(Map<String, Object> params);

    int countDue(Map<String, Object> params);

    /**
     * Xác nhận một nhắc lịch. Chỉ thành công khi bản ghi còn chờ xử lý
     * và thuộc đúng tenant (trả về số dòng cập nhật, 0 = không tìm thấy
     * hoặc đã được xử lý / sai tenant).
     */
    int acknowledge(@Param("id") String id, @Param("companyId") String companyId,
                    @Param("updateBy") String updateBy, @Param("updateDate") Date updateDate);

    /**
     * Giành quyền tạo thông báo trong ngày (claim nguyên tử). Chỉ thành
     * công khi chưa có thông báo trong ngày today (chống trùng khi job
     * chạy lại, kể cả chạy đồng thời). today là chuỗi ngày theo giờ
     * phòng khám (yyyy-MM-dd) — KHÔNG dùng java.sql.Date vì driver JDBC
     * chuyển nó theo múi giờ JVM/server làm lệch ngày (đã gặp: lưu
     * 2026-10-31 cho ngày phòng khám 2026-11-01). Caller chỉ insert
     * thông báo khi claim này trả về 1.
     */
    int markNoticed(@Param("id") String id, @Param("companyId") String companyId,
                    @Param("today") String today, @Param("updateBy") String updateBy,
                    @Param("updateDate") Date updateDate);
}
