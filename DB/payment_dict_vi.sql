-- ============================================================================
-- payment_dict_vi.sql — Dict phương thức thanh toán Việt Nam (payType)
--
-- Bối cảnh (xác minh từ code + dump md_yzs.sql ngày 2026-10-30):
--   * Dict đang dùng thật là sys_dict_type code = 'payType'
--     (id 1008793465990693120), KHÔNG phải bảng xt_zd (xt_zd là từ điển
--     chẩn đoán Đông y) và KHÔNG phải dict type 'paymentType'
--     (id 1024846610635146222, đã del_flag = 1, chết).
--   * Frontend lấy danh sách qua listByDictTypeCode('payType'), mapper lọc
--     a.del_flag = '0' AND dictType.del_flag = '0', sắp xếp theo create_date.
--     Vì vậy: đổi del_flag là đủ để một giá trị biến mất khỏi mọi form
--     thu phí/đăng ký khám mà KHÔNG xóa dữ liệu lịch sử.
--   * Backend + báo cáo key theo giá trị payType_* (BizConstants,
--     TollInfoDao.xml, tollDetail/orgtolldetail): giá trị cũ giữ nguyên
--     nên các dòng toll_info lịch sử vẫn hiển thị và vẫn cộng vào báo cáo.
--
-- Thay đổi:
--   * GIỮ   payType_0 'Tiền mặt', payType_3 'Thẻ ngân hàng' (không đổi).
--   * THÊM  payType_5 'Chuyển khoản ngân hàng',
--          payType_6 'Ví điện tử' (MoMo/ZaloPay/VNPay — chỉ ghi nhận
--          hình thức thu, CHƯA có tích hợp cổng thanh toán hay đối soát
--          tự động; thu ngân vẫn phải kiểm tra tiền về tài khoản/ví).
--   * ẨN    payType_1 'Alipay', payType_2 'WeChat' (del_flag = 1):
--          không còn chọn được ở production Việt Nam.
--   * ẨN    payType_4 'Bảo hiểm y tế' (del_flag = 1): hệ thống KHÔNG có
--          quyết toán BHYT Việt Nam — phiếu in ghi rõ "không có chức
--          năng thanh toán bảo hiểm", cổng BHYT Trung Quốc (medicare)
--          đang check = false. Để giá trị này cho chọn sẽ khiến thu ngân
--          tưởng đã quyết toán BHYT trong khi thực tế chỉ ghi một dòng
--          thu phí thường. Khi nào có liên thông BHYT thật mới mở lại.
--
-- Cách áp dụng (KHÔNG sửa md_yzs.sql gốc — file này chạy SAU import):
--   1. Sau mỗi lần import lại md_yzs.sql (xem restore_all.sh bước 3),
--      chạy file này vào DB md_yzs, ví dụ:
--        ~/workspace/tools/mysql_test.sh md_yzs < DB/payment_dict_vi.sql
--   2. Kiểm tra bằng SELECT cuối file: chỉ còn 4 dòng del_flag = 0
--      (payType_0, payType_3, payType_5, payType_6).
--   3. Frontend không cần restart DB; nhưng dictCache frontend cache
--      trong bộ nhớ theo phiên đăng nhập nên thu ngân phải đăng nhập
--      lại mới thấy danh sách mới.
--
-- Idempotent: chạy lại nhiều lần vẫn cho cùng kết quả (UPDATE theo value,
-- INSERT ... ON DUPLICATE KEY UPDATE theo UNIQUE INDEX value).
-- ============================================================================

-- 1. Ẩn các phương thức không dùng ở production Việt Nam.
UPDATE sys_dict_item
   SET del_flag = '1',
       update_by = 'payment_dict_vi',
       update_date = NOW()
 WHERE dict_type_id = 1008793465990693120
   AND value IN ('payType_1', 'payType_2', 'payType_4')
   AND del_flag = '0';

-- 2. Thêm phương thức Việt Nam (id mới, không trùng id nào trong dump).
INSERT INTO sys_dict_item
  (id, dict_type_id, name, value, remarks, create_by, create_date, update_by, update_date, del_flag)
VALUES
  (202610300000000001, 1008793465990693120, 'Chuyển khoản ngân hàng', 'payType_5', '', 'payment_dict_vi', NOW(), 'payment_dict_vi', NOW(), '0'),
  (202610300000000002, 1008793465990693120, 'Ví điện tử', 'payType_6', '', 'payment_dict_vi', NOW(), 'payment_dict_vi', NOW(), '0')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  del_flag = VALUES(del_flag),
  update_by = VALUES(update_by),
  update_date = NOW();

-- 3. Khẳng định lại 2 giá trị giữ nguyên luôn hiển thị (phòng trường hợp
--    có lần sửa tay nào đó đã ẩn chúng).
UPDATE sys_dict_item
   SET del_flag = '0',
       update_by = 'payment_dict_vi',
       update_date = NOW()
 WHERE dict_type_id = 1008793465990693120
   AND value IN ('payType_0', 'payType_3')
   AND del_flag <> '0';

-- 4. Kiểm chứng: danh sách mà frontend sẽ thấy (del_flag = 0, theo
--    create_date — đúng thứ tự của listByDictTypeCode).
SELECT value, name, del_flag
  FROM sys_dict_item
 WHERE dict_type_id = 1008793465990693120
 ORDER BY del_flag, create_date;
