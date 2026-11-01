import request from "@/utils/request";

// Nhật ký truy cập hồ sơ bệnh nhân (G8 research loop).
// GET trả danh sách dòng audit của tenant đang đăng nhập, mới nhất
// trước; patientId/action trống nghĩa là không lọc; limit kẹp [1, 200]
// ở backend. Chỉ đọc — không có endpoint tạo/xóa/sửa audit.
export const listRecordAudits = (params) =>
  request({
    url: "/record-audit/list",
    method: "get",
    params,
  });
