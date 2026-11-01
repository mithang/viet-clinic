-- Migration v6 — G2 research loop (hàng chờ yêu cầu đặt lịch online, lễ tân xác nhận).
--
-- BỐI CẢNH: hệ thống chưa có intake đặt lịch cho bệnh nhân. Module lịch
-- hẹn hiện có là lịch trực bác sĩ (bảng scheduling_data) và trường hẹn
-- trên phiếu đăng ký (registration.subscribe_date / subscribe_project);
-- KHÔNG có bảng lịch hẹn bệnh nhân riêng. Vì vậy G2 thêm bảng hàng chờ
-- tối thiểu của riêng nó, và khi lễ tân xác nhận thì tạo một dòng
-- registration mang subscribe_date/subscribe_project qua RegistrationService
-- (đường đăng ký hiện có) — không tạo hồ sơ khám, không chẩn đoán, không
-- đơn thuốc. Yêu cầu ở trạng thái pending KHÔNG BAO GIỜ ghi dữ liệu khám
-- chữa: endpoint intake chỉ INSERT vào bảng này (và chỉ đọc lại chính
-- bảng này để chống trùng), không đọc/ghi patient, registration hay bất
-- kỳ bảng clinical nào.
--
-- Trạng thái: '0' = chờ xử lý (còn trong hàng chờ), '1' = đã xác nhận
-- (đã tạo registration, registration_id trỏ tới lịch hẹn thật),
-- '2' = đã từ chối (giữ lại reject_reason, không xóa).
--
-- Áp dụng: mysql md_yzs < DB/migration_v6_booking_request.sql
-- (đã nối vào restore_all.sh sau v5 để sống qua re-import DB).
-- Chạy lại an toàn (idempotent): CREATE TABLE IF NOT EXISTS, không
-- UPDATE dữ liệu cũ, không chạm cột logic hay quyền nào khác.

CREATE TABLE IF NOT EXISTS `booking_request` (
  `id` bigint(20) NOT NULL,
  `company_id` bigint(20) NOT NULL COMMENT 'Phòng khám (tenant)',
  `patient_name` varchar(100) NOT NULL COMMENT 'Tên người đặt lịch (không cần tài khoản)',
  `phone` varchar(20) NOT NULL COMMENT 'SĐT đã chuẩn hóa (chỉ chữ số, +84 -> 0)',
  `wanted_date` date NOT NULL COMMENT 'Ngày muốn khám theo giờ phòng khám Asia/Ho_Chi_Minh',
  `service` varchar(100) NULL DEFAULT NULL COMMENT 'Dịch vụ/hạng mục muốn khám (không phải chẩn đoán)',
  `status` varchar(1) NOT NULL DEFAULT '0' COMMENT 'Trạng thái (0: Chờ xử lý; 1: Đã xác nhận; 2: Đã từ chối)',
  `reject_reason` varchar(255) NULL DEFAULT NULL COMMENT 'Lý do từ chối (chỉ khi status = 2)',
  `registration_id` bigint(20) NULL DEFAULT NULL COMMENT 'Lịch hẹn thật (registration) tạo khi xác nhận, NULL khi chưa xác nhận',
  `remarks` varchar(255) NULL DEFAULT NULL COMMENT 'Ghi chú',
  `create_by` varchar(100) NULL DEFAULT NULL,
  `create_date` datetime NULL DEFAULT NULL,
  `update_by` varchar(100) NULL DEFAULT NULL,
  `update_date` datetime NULL DEFAULT NULL,
  `del_flag` varchar(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_booking_request_company` (`company_id`) USING BTREE,
  KEY `idx_booking_request_pending` (`company_id`, `status`, `wanted_date`) USING BTREE,
  KEY `idx_booking_request_phone` (`company_id`, `phone`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci;
