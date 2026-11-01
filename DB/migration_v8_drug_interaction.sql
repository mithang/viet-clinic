-- Migration v8 — G5 research loop (quy tắc tương tác thuốc do phòng khám tự cấu hình).
--
-- BỐI CẢNH: bác sĩ kê đơn tây y cần được cảnh báo khi hai thuốc trong đơn
-- có tương tác mà chính phòng khám đã ghi nhận, nhưng repo này KHÔNG có
-- cơ sở dữ liệu tương tác lâm sàng nào để kế thừa, và việc bịa một bảng
-- tương tác "đầy đủ" sẽ là quảng cáo sai về độ bao phủ lâm sàng. Vì vậy
-- G5 chỉ xây cơ chế: bảng quy tắc do phòng khám tự nhập qua API, backend
-- so khớp cặp thuốc trong đơn với đúng các quy tắc đó, frontend chỉ cảnh
-- báo (không chặn lưu đơn — bác sĩ quyết định).
--
-- Mặc định: bảng trống = KHÔNG có quy tắc nào = KHÔNG có cảnh báo nào.
-- Migration này KHÔNG seed bất kỳ quy tắc lâm sàng nào; mọi quy tắc kiểm
-- chứng đều được tạo qua API trong lượt kiểm chứng và sẽ mất khi re-import
-- DB (như mọi dữ liệu test). Frontend phải ghi rõ cảnh báo chỉ bao phủ
-- các quy tắc phòng khám đã tự cấu hình.
--
-- Áp dụng: mysql md_yzs < DB/migration_v8_drug_interaction.sql
-- (đã nối vào restore_all.sh sau v7 để sống qua re-import DB).
-- Chạy lại an toàn (idempotent): CREATE TABLE IF NOT EXISTS, không
-- INSERT dữ liệu, không chạm cột logic hay quyền nào khác.

CREATE TABLE IF NOT EXISTS `drug_interaction_rule` (
  `id` bigint(20) NOT NULL,
  `company_id` bigint(20) NOT NULL COMMENT 'Phòng khám (tenant)',
  `drug_a_name` varchar(100) NOT NULL COMMENT 'Tên thuốc A (so khớp không phân biệt hoa/thường, khoảng trắng)',
  `drug_b_name` varchar(100) NOT NULL COMMENT 'Tên thuốc B',
  `severity` varchar(20) NOT NULL COMMENT 'Mức độ: CANH_BAO (thận trọng) hoặc TRANH (tránh phối hợp)',
  `message` varchar(500) NOT NULL COMMENT 'Nội dung cảnh báo hiển thị cho bác sĩ',
  `remarks` varchar(255) NULL DEFAULT NULL,
  `create_by` varchar(100) NULL DEFAULT NULL,
  `create_date` datetime NULL DEFAULT NULL,
  `update_by` varchar(100) NULL DEFAULT NULL,
  `update_date` datetime NULL DEFAULT NULL,
  `del_flag` varchar(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_drug_interaction_company` (`company_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci;
