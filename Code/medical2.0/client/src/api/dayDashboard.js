import request from "@/utils/request";

// Tổng quan ngày cho chủ phòng khám (H5 research loop).
// GET trả {date, timezone, windowStart, windowEnd, visits, charges,
// stock, definitions}; chỉ đọc, không ghi DB. Ngày là ngày phòng khám
// Asia/Ho_Chi_Minh; tồn kho / sắp hết hạn là trạng thái hiện tại,
// không phải số phát sinh trong ngày chọn (xem definitions trả về).
export const getDayDashboard = (date) =>
  request({
    url: "/day-dashboard/day",
    method: "get",
    params: { date },
  });
