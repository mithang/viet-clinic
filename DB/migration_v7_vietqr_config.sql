-- Migration v7 — G4 research loop (cấu hình tài khoản nhận VietQR của phòng khám).
--
-- BỐI CẢNH: trang /charge đã thu phí qua toll API thật, nhưng khi chọn
-- "Chuyển khoản ngân hàng" lễ tân vẫn phải đọc số tài khoản cho bệnh nhân
-- bằng miệng, dễ sai và khó đối soát. G4 chỉ thêm cấu hình HIỂN THỊ mã
-- VietQR: ngân hàng + số tài khoản + tên tài khoản do quản trị viên của
-- phòng khám tự nhập, KHÔNG lưu mật khẩu hay credential ngân hàng nào,
-- KHÔNG tự động xác nhận thanh toán (thu ngân vẫn xác nhận đã nhận tiền
-- như quy trình hiện tại), KHÔNG gọi API ngân hàng hay VietQR nào.
--
-- Mặc định: bảng trống = CHƯA cấu hình. Frontend khi đó hiện trạng thái
-- "chưa cấu hình" rõ ràng, không vẽ mã QR giả, không hard-code tài khoản
-- nào trong code hay trong migration. Tài khoản dùng để kiểm chứng UI là
-- dữ liệu test nhập qua API, sẽ mất khi re-import DB (như mọi dữ liệu test).
--
-- Một phòng khám chỉ có một cấu hình đang hiệu lực: service luôn UPDATE
-- dòng del_flag = '0' của tenant đó thay vì INSERT thêm; không dùng
-- UNIQUE key vì dòng cũ vẫn giữ lại khi bị thay thế bằng soft-delete
-- (del_flag = '1') trong tương lai — hiện tại service chỉ UPDATE tại chỗ.
--
-- Áp dụng: mysql md_yzs < DB/migration_v7_vietqr_config.sql
-- (đã nối vào restore_all.sh sau v6 để sống qua re-import DB).
-- Chạy lại an toàn (idempotent): CREATE TABLE IF NOT EXISTS, không
-- UPDATE dữ liệu cũ, không chạm cột logic hay quyền nào khác.

CREATE TABLE IF NOT EXISTS `vietqr_config` (
  `id` bigint(20) NOT NULL,
  `company_id` bigint(20) NOT NULL COMMENT 'Phòng khám (tenant)',
  `bank_bin` varchar(6) NOT NULL COMMENT 'Mã BIN ngân hàng VietQR, đúng 6 chữ số (vd 970422)',
  `bank_name` varchar(50) NULL DEFAULT NULL COMMENT 'Tên ngân hàng hiển thị (tùy chọn)',
  `account_no` varchar(20) NOT NULL COMMENT 'Số tài khoản nhận tiền, chỉ chữ số',
  `account_name` varchar(50) NOT NULL COMMENT 'Tên chủ tài khoản hiển thị trên mã QR',
  `remarks` varchar(255) NULL DEFAULT NULL,
  `create_by` varchar(100) NULL DEFAULT NULL,
  `create_date` datetime NULL DEFAULT NULL,
  `update_by` varchar(100) NULL DEFAULT NULL,
  `update_date` datetime NULL DEFAULT NULL,
  `del_flag` varchar(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_vietqr_config_company` (`company_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci;
