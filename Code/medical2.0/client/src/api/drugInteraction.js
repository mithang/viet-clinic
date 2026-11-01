import request from "@/utils/request";

// Quy tắc tương tác thuốc do phòng khám tự cấu hình (G5 research loop).
// Cảnh báo chỉ bao phủ các quy tắc đã cấu hình; bảng được tạo trống và
// không có cơ sở dữ liệu tương tác lâm sàng đầy đủ nào đi kèm.
export const getDrugInteractionRules = () =>
  request({
    url: "/drug-interaction/rules",
    method: "get",
  });

export const createDrugInteractionRule = (rule) =>
  request({
    url: "/drug-interaction/rules",
    method: "post",
    data: rule,
  });

export const checkDrugInteractions = (drugNames) =>
  request({
    url: "/drug-interaction/check",
    method: "post",
    data: { drugNames },
  });
