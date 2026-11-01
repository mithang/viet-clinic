-- Migration v9 — G8 research loop (nhật ký truy cập hồ sơ bệnh nhân).
--
-- BỐI CẢNH: chủ phòng khám cần trả lời "ai đã xem hồ sơ bệnh nhân này,
-- và khi nào". Repo này trước G8 không có bảng audit truy cập hồ sơ nào:
-- sys_action chỉ ghi thao tác hệ thống chung, không gắn patient_id.
-- Vì vậy G8 tạo bảng riêng, append-only: backend chỉ INSERT và SELECT,
-- KHÔNG có endpoint xóa/sửa; del_flag luôn '0' (không dùng cờ xóa để
-- giấu dòng audit — xóa mềm một dòng audit cũng là sửa lịch sử).
--
-- Mặc định: bảng trống = chưa có lượt truy cập nào được ghi = nhật ký
-- trống, không phải lỗi. Migration này KHÔNG seed bất kỳ dòng audit nào;
-- mọi dòng kiểm chứng đều do gọi API thật trong lượt kiểm chứng và sẽ
-- mất khi re-import DB (như mọi dữ liệu test).
--
-- Áp dụng: mysql md_yzs < DB/migration_v9_record_access_audit.sql
-- (đã nối vào restore_all.sh sau v8 để sống qua re-import DB).
-- Chạy lại an toàn (idempotent): CREATE TABLE IF NOT EXISTS, không
-- INSERT dữ liệu, không chạm cột logic hay quyền nào khác.
--
-- Hành động ghi: VIEW (xem chi tiết 1 bệnh nhân), LIST (mở danh sách —
-- ghi ĐÚNG 1 dòng cho cả lần mở, không ghi theo từng dòng bệnh nhân),
-- EDIT (lưu sửa hồ sơ đã tồn tại). Tạo mới bệnh nhân không ghi EDIT
-- vì chưa có hồ sơ trước đó để "truy cập".

CREATE TABLE IF NOT EXISTS `record_access_audit` (
  `id` bigint(20) NOT NULL,
  `company_id` bigint(20) NOT NULL COMMENT 'Phòng khám (tenant)',
  `user_id` varchar(64) NULL DEFAULT NULL COMMENT 'ID người truy cập',
  `user_login_name` varchar(100) NULL DEFAULT NULL COMMENT 'Tên đăng nhập người truy cập',
  `user_name` varchar(100) NULL DEFAULT NULL COMMENT 'Họ tên người truy cập',
  `patient_id` bigint(20) NULL DEFAULT NULL COMMENT 'ID bệnh nhân (NULL với LIST: xem danh sách, không gắn 1 bệnh nhân)',
  `patient_name` varchar(100) NULL DEFAULT NULL COMMENT 'Tên bệnh nhân tại thời điểm ghi (ảnh chụp, tránh join)',
  `action` varchar(20) NOT NULL COMMENT 'Hành động: VIEW, LIST hoặc EDIT',
  `remarks` varchar(255) NULL DEFAULT NULL,
  `create_by` varchar(100) NULL DEFAULT NULL,
  `create_date` datetime NULL DEFAULT NULL COMMENT 'Thời điểm truy cập (giờ lưu trữ GMT+8 như mọi create_date khác, không đổi múi giờ)',
  `update_by` varchar(100) NULL DEFAULT NULL,
  `update_date` datetime NULL DEFAULT NULL,
  `del_flag` varchar(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_record_audit_company` (`company_id`) USING BTREE,
  KEY `idx_record_audit_patient` (`company_id`, `patient_id`) USING BTREE,
  KEY `idx_record_audit_date` (`company_id`, `create_date`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci;
