# Ghi chú dài hạn — viet-clinic (Phòng khám đám mây)

## Kiến trúc
- `Code/medical2.0/back` — Spring Boot 2.1 / Java 8, MyBatis-Plus, Shiro + JWT, MySQL, Redis.
- `Code/medical2.0/client` — Vue 2 (Options API) + Element UI 2.x nạp qua CDN (`index.html`),
  build bằng Vue CLI 5. Không bundle Element UI.
- Routing do backend điều khiển: menu/quyền trả về sau login → `router.addRoute()`.
- `views/<module>/index.vue` (list) + `<module>Form.vue` (form). List kế thừa `MainUI`
  (`views/components/mainUI.vue`), base là `BaseUI`.

## Quy ước khi bản địa hoá (quan trọng)
- **KHÔNG dịch** chuỗi dùng để so sánh với dữ liệu DB: `dict` value (`payType_0`),
  mã trạng thái (`'0'`, `'1'`), tên cột Excel import (`药品材料名称`), enum value
  (`'药师'`, `'男'`, `'初诊'`). Chỉ dịch nhãn/message/placeholder.
- Element UI locale tiếng Việt: `src/utils/element-vi-locale.js`, áp trong `main.js`
  qua `window.ELEMENT.locale(...)`.
- Dict thanh toán VN: `DB/payment_dict_vi.sql` — `payType_0` Tiền mặt, `payType_3`
  Thẻ ngân hàng, `payType_5` Chuyển khoản, `payType_6` Ví điện tử; Alipay/WeChat/BHYT
  đã ẩn (`del_flag=1`).
- Định dạng: ngày hiển thị `dd/MM/yyyy`, số `1.234.567,89`, tiền tệ `VND`.
- Font: `Inter / Be Vietnam Pro / Segoe UI / Roboto / Noto Sans` — không dùng font
  Trung Quốc vì lệch dấu tiếng Việt. Bảng ánh xạ font luckysheet khi xuất Excel
  (`utils/export.js`) cũng đã đổi sang font Latin.
- **i18n module báo cáo**: `views/report/lang/index.js` khai báo `messages` và hàm
  `getLanguage()`. Locale tiếng Việt là khoá `vi_VN` (file `lang/vi.js`), mặc định
  trả `vi_VN`. fit2cloud-ui chưa có bản vi nên dùng `fuEn` cho các khoá của nó.
- **Excel nhập kho** (`stock/*/addstorage.vue`): đọc qua `sheet_to_json` nên key là
  header. Dùng `HEADER_ALIAS` để chấp nhận đồng thời header tiếng Việt và tiếng Trung,
  rồi truy cập bằng field chuẩn (`goodsName`, `batchNumber`, `producedDate`,
  `expireDate`, `quantity`, `inPrice`, `specification`, `price`). Đừng đổi trực tiếp
  tên cột tiếng Trung — sẽ vỡ template cũ.
- Giá trị backend trả về bằng tiếng Trung (vd `scheduleJob.status` = `执行中`/`已暂停`)
  thì giữ nguyên giá trị bound, chỉ map nhãn hiển thị qua computed.

## Công cụ có sẵn trong repo
- `.workbuddy-ai/tools/scan_cn.py` — quét CJK, phân loại TEMPLATE / SCRIPT-STR / COMMENT.
- `.workbuddy-ai/tools/apply_vi.py` — áp bản dịch kèm assert số lần khớp.
- `.workbuddy-ai/tools/fix_indent.py` — khôi phục thụt lề bằng đối chiếu git HEAD.

## Lưu ý
- `node_modules` không có trong repo → không chạy được `npm run build`.
  **Nhưng `node` v22 có sẵn**: validate bằng `node --check` trên file `.js` và trên
  khối `<script>` trích từ `.vue` (đã chạy 271/271 file, 0 lỗi). Đây là mức kiểm
  chứng mạnh nhất hiện có, mạnh hơn đếm ngoặc.
- `views/report/lang/{zh,tw,ja}.js` là i18n UReport2 cho ngôn ngữ khác — **giữ nguyên**.
  `vi.js` thì dịch đầy đủ và đã đăng ký.
- `utils/calendar.js` là thư viện âm lịch, comment tiếng Trung thuộc bản chất domain.
- **CẤM dùng regex/sed hàng loạt trên file nguồn.** Đợt trước script chuẩn hoá dấu câu
  đã phá 890 dòng thụt lề toàn dự án. Chỉ dùng thay thế chuỗi chính xác kèm assert
  số lần khớp, hoặc sửa từng file bằng Edit.

