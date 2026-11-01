import request from "@/utils/request";

// Cấu hình VietQR của phòng khám đang đăng nhập (G4 research loop).
// GET trả {configured, config}; chưa cấu hình thì config = null.
export const getVietqrConfig = () =>
  request({
    url: "/vietqr/config",
    method: "get",
  });

export const saveVietqrConfig = (config) =>
  request({
    url: "/vietqr/config",
    method: "post",
    data: config,
  });
