import request from "@/utils/request";

// Chẩn đoán mã ICD-10 tại khám (H3 research loop).
// Tìm kiếm phía server, phân trang — client không tải toàn bộ catalog
// (danh mục BYT đầy đủ ~15.800 mã). Catalog dùng chung mọi tenant;
// ghim gắn theo chính bác sĩ đang đăng nhập (backend lấy doctor_id
// từ phiên, client không gửi doctor_id).
// Catalog hiện TRỐNG (migration v11 không seed — NEED MORE INFO về
// file danh mục chính thức của BYT): tìm kiếm trả rỗng là trạng thái
// trung thực, không phải lỗi; bác sĩ vẫn lưu chẩn đoán tự do.
export const searchDiagnoses = (q, page, pageSize) =>
  request({
    url: "/diagnosis/search",
    method: "get",
    params: { q, page, pageSize },
  });

export const getPinnedDiagnoses = () =>
  request({
    url: "/diagnosis/pinned",
    method: "get",
  });

export const pinDiagnosis = (diagnosisId) =>
  request({
    url: "/diagnosis/pin",
    method: "post",
    data: { diagnosisId },
  });

export const unpinDiagnosis = (diagnosisId) =>
  request({
    url: "/diagnosis/pin/" + diagnosisId,
    method: "delete",
  });
