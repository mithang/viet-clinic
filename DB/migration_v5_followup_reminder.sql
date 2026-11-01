-- Migration v5 — G1 research loop (nhắc lịch tái khám nội bộ).
--
-- BỐI CẢNH (kiểm chứng bằng code ngày 2026-11-01, khác giả định của
-- research doc iteration 1): KHÔNG có hệ thống hồ sơ follow-up nội bộ nào
-- trong repo này. Các view client/src/views/followUp/* đều là iframe 16
-- dòng trỏ tới hệ thống ngoài qua process.env.FOLLOW_UP_URL; không có
-- entity, DAO hay bảng follow-up nào ở backend; bảng registration cũng
-- không có cột follow_up_date (field Registration.followUpDate là dead
-- field, lệch với schema). Vì vậy G1 không thể "lắp ráp trên hồ sơ
-- followUp sẵn có" mà phải có bảng nhắc lịch tối thiểu của riêng nó.
-- Bảng này là nguồn sự thật cho danh sách đến hạn/quá hạn + job tạo
-- thông báo nội bộ; nó KHÔNG gửi SMS/Zalo và KHÔNG chứa chi tiết sức
-- khỏe ngoài tên bệnh nhân + loại tái khám + ngày hẹn.
--
-- Áp dụng: mysql md_yzs < DB/migration_v5_followup_reminder.sql
-- (đã nối vào restore_all.sh sau v4 để sống qua re-import DB).
-- Chạy lại an toàn (idempotent): CREATE TABLE IF NOT EXISTS, không
-- UPDATE dữ liệu cũ, không chạm cột logic hay quyền nào khác.

CREATE TABLE IF NOT EXISTS `followup_reminder` (
  `id` bigint(20) NOT NULL,
  `company_id` bigint(20) NOT NULL COMMENT 'Phòng khám (tenant)',
  `patient_id` bigint(20) NOT NULL COMMENT 'Bệnh nhân',
  `patient_name` varchar(100) NOT NULL COMMENT 'Tên bệnh nhân lúc tạo nhắc (snapshot hiển thị)',
  `followup_type` varchar(50) NOT NULL COMMENT 'Loại tái khám',
  `next_date` datetime NULL DEFAULT NULL COMMENT 'Ngày hẹn tái khám (NULL = chưa có ngày, không bao giờ vào danh sách đến hạn)',
  `status` varchar(1) NOT NULL DEFAULT '0' COMMENT 'Trạng thái (0: Chờ xử lý; 1: Đã xác nhận)',
  `last_notice_date` date NULL DEFAULT NULL COMMENT 'Ngày đã tạo thông báo nội bộ gần nhất (chống trùng 1 thông báo/ngày)',
  `remarks` varchar(255) NULL DEFAULT NULL COMMENT 'Ghi chú',
  `create_by` varchar(100) NULL DEFAULT NULL,
  `create_date` datetime NULL DEFAULT NULL,
  `update_by` varchar(100) NULL DEFAULT NULL,
  `update_date` datetime NULL DEFAULT NULL,
  `del_flag` varchar(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_followup_reminder_company` (`company_id`) USING BTREE,
  KEY `idx_followup_reminder_due` (`status`, `next_date`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci;
