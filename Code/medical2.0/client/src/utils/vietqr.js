// VietQR payload builder (G4 research loop) — thuần EMVCo TLV, không gọi
// API ngân hàng nào. Chuỗi này là dữ liệu hiển thị trên mã QR để bệnh
// nhân quét và chuyển khoản; nó không xác nhận thanh toán tự động.
//
// Cấu trúc (chuẩn VietQR/NAPAS, trường 38 lồng 2 cấp):
//   00 "01"                       — payload format
//   01 "12"                       — mã động (có kèm số tiền)
//   38: 00 "A000000727"           — GUID VietQR
//       01: 00 <BIN 6 số>         — mã ngân hàng
//           01 <số tài khoản>    — tài khoản nhận tiền
//       02 "QRIBFTTA"             — dịch vụ chuyển khoản liên ngân hàng
//   53 "704"                      — VND
//   54 <số tiền nguyên VNĐ>       — chỉ khi số tiền > 0
//   58 "VN"
//   59 <tên chủ TK, tối đa 25 ký tự, bỏ dấu, viết hoa>
//   62: 08 <nội dung>             — nội dung chuyển khoản (vd mã đăng ký)
//   63 <CRC16-CCITT-FALSE, 4 ký tự hex viết hoa> trên toàn bộ chuỗi trước
//      nó cộng thêm "6304" (lỗi phổ biến nhất là tính CRC sai đoạn này).
//
// Độ dài TLV tính theo số ký tự: vì vậy tên và nội dung được chuẩn hóa
// về ASCII không dấu trước khi cắt ngắn, tránh trường hợp ký tự có dấu
// làm lệch độ dài khai báo so với byte thực tế.

function tlv(tag, value) {
  const text = String(value);
  if (text.length > 99) {
    throw new Error("Giá trị VietQR vượt quá 99 ký tự:" + tag);
  }
  return tag + String(text.length).padStart(2, "0") + text;
}

export function crc16CcittFalse(text) {
  let crc = 0xffff;
  for (let i = 0; i < text.length; i++) {
    crc ^= text.charCodeAt(i) << 8;
    for (let j = 0; j < 8; j++) {
      crc = (crc & 0x8000) !== 0 ? ((crc << 1) ^ 0x1021) & 0xffff : (crc << 1) & 0xffff;
    }
  }
  return crc.toString(16).toUpperCase().padStart(4, "0");
}

// Bỏ dấu tiếng Việt + ký tự đặc biệt, chỉ giữ chữ/số/khoảng trắng, viết hoa.
export function normalizeVietqrText(text) {
  return String(text || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/\u0111/g, "d")
    .replace(/\u0110/g, "D")
    .replace(/[^A-Za-z0-9 ]/g, " ")
    .replace(/\s+/g, " ")
    .trim()
    .toUpperCase();
}

export function buildVietqrPayload(config, amount, content) {
  if (!config || !/^\d{6}$/.test(String(config.bankBin || ""))) {
    throw new Error("Mã BIN ngân hàng phải đúng 6 chữ số.");
  }
  if (!/^\d{6,20}$/.test(String(config.accountNo || ""))) {
    throw new Error("Số tài khoản phải là 6–20 chữ số.");
  }
  const intAmount = Math.floor(Number(amount));
  if (!intAmount || intAmount <= 0) {
    throw new Error("Số tiền VietQR phải lớn hơn 0.");
  }
  const bankInfo = tlv("00", config.bankBin) + tlv("01", config.accountNo);
  const merchantAccount =
    tlv("00", "A000000727") + tlv("01", bankInfo) + tlv("02", "QRIBFTTA");
  let payload = "";
  payload += tlv("00", "01");
  payload += tlv("01", "12");
  payload += tlv("38", merchantAccount);
  payload += tlv("53", "704");
  payload += tlv("54", String(intAmount));
  payload += tlv("58", "VN");
  const merchantName = normalizeVietqrText(config.accountName).slice(0, 25);
  if (merchantName) {
    payload += tlv("59", merchantName);
  }
  const purpose = normalizeVietqrText(content).slice(0, 25);
  if (purpose) {
    payload += tlv("62", tlv("08", purpose));
  }
  payload += "6304";
  return payload + crc16CcittFalse(payload);
}
