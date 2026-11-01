import request from "@/utils/request";

// Tổng kết thu phí cuối ngày (G6 research loop).
// GET trả {date, timezone, windowStart, windowEnd, rows, totals, refunds};
// chỉ đọc, không ghi DB. Ngày là ngày phòng khám Asia/Ho_Chi_Minh.
export const getDayChargeSummary = (date) =>
  request({
    url: "/charge-summary/day",
    method: "get",
    params: { date },
  });
