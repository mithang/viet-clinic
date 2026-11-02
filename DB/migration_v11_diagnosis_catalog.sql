-- Migration v11 — H3 research loop iteration 2 (chẩn đoán mã ICD-10 tại khám).
--
-- BỐI CẢNH: trang khám (medicalOutpatientRecord) đã có picker chẩn đoán
-- qua bảng XtZd (dữ liệu tham chiếu tiếng Trung) nhưng picker đó chỉ
-- điền TÊN (westernDiagnose/chinaDiagnose + diseaseId), bỏ rơi mã ICD-10
-- mà chính nó hiển thị ở cột icd10; còn entity MedicalRecord đã có
-- trường diagnosisCode ("Mã chẩn đoán") nhưng bảng medical_record KHÔNG
-- có cột diagnosis_code và mapper KHÔNG persist trường này, nên mã
-- chẩn đoán không bao giờ được lưu. H3 nối mạch đó:
--   (1) thêm cột diagnosis_code nullable trên medical_record để đường
--       lưu sẵn có (MedicalRecordDao insert/update, dùng bởi allSave,
--       v2/allSave, saveWithFile) mang được mã theo tên;
--   (2) bảng danh mục chẩn đoán diagnosis_catalog (mã + tên tiếng Việt)
--       để picker tìm kiếm phía server, phân trang, thay vì tải toàn bộ
--       catalog về client hay dịch hàng loạt bảng XtZd;
--   (3) bảng ghim diagnosis_pin theo từng bác sĩ (doctor_id = id user
--       đang đăng nhập) để dùng lại 1 click.
--
-- QUY TẮC DATASET (QUAN TRỌNG — ĐỪNG BỊA DỮ LIỆU Y KHOA):
-- - KHÔNG dịch hàng loạt bảng XtZd: đó là dữ liệu tham chiếu tiếng
--   Trung, quyết định của chủ sở hữu (giống quyết định đã ghi cho
--   migration v4 và cho G5). Picker XtZd cũ giữ nguyên cho luồng
--   đông y; picker ICD-10 mới là luồng riêng, không xóa luồng cũ.
-- - Migration này KHÔNG SEED bất kỳ cặp mã+tên nào. Lý do: chưa kiểm
--   chứng được danh mục ICD-10 chính thức của BYT dưới dạng file có
--   thể import trực tiếp. Đã xác minh được QĐ 4469/QĐ-BYT ngày
--   28/10/2020 ban hành "Bảng phân loại quốc tế mã hóa bệnh tật ICD-10"
--   (dẫn lại trong báo cáo của Trung tâm Y tế Di Linh, Sở Y tế Lâm
--   Đồng), nhưng bản thân bảng mã của BYT chưa lấy được trong lượt
--   này nên KHÔNG seed theo nguồn thứ cấp. Đây là NEED MORE INFO:
--   cần file danh mục ICD-10 chính thức của BYT (hoặc quyết định của
--   chủ sở hữu về nguồn danh mục) rồi mới seed bằng một migration
--   tiếp theo. Bảng trống = picker trả rỗng + free-text vẫn lưu được;
--   đó là trạng thái trung thực, không phải lỗi.
-- - Mọi dòng kiểm chứng trong lượt này chỉ nằm trong DB test (INSERT
--   trực tiếp qua SQL, dữ liệu disposable, mất khi re-import) và
--   KHÔNG thuộc migration này — xem h3-impl-report.md.
--
-- QUY TẮC KHÁC:
-- - diagnosis_code nullable: bác sĩ KHÔNG BAO GIỜ bị chặn lưu chẩn đoán
--   chỉ vì catalog thiếu mã đó; gõ tự do tên và/hoặc mã vẫn lưu.
-- - diagnosis_catalog là dữ liệu tham chiếu dùng chung (không gắn
--   tenant): mã ICD-10 là chuẩn chung, không phải dữ liệu của một
--   phòng khám. Ghim thì gắn theo bác sĩ (doctor_id), không gắn
--   tenant, vì cùng một bác sĩ chỉ có một danh sách hay dùng.
-- - Catalog chỉ đọc qua API (không có endpoint ghi): danh mục chỉ lớn
--   lên bằng migration có kiểm chứng, không cho phòng khám tự bịa mã
--   qua API rồi tưởng đó là mã chuẩn.
-- - Ghim là idempotent: INSERT IGNORE theo khóa (doctor_id,
--   diagnosis_id); bỏ ghim là DELETE, vắng cũng không lỗi.
--
-- Áp dụng: mysql md_yzs < DB/migration_v11_diagnosis_catalog.sql
-- (đã nối vào restore_all.sh sau v10 để sống qua re-import DB).
-- Chạy lại an toàn (idempotent): CREATE TABLE IF NOT EXISTS, procedure
-- kiểm tra information_schema.COLUMNS trước khi ALTER (theo đúng
-- pattern add_column_if_missing của migration v10), tự xóa sau khi
-- chạy xong. Không INSERT dữ liệu, không chạm cột logic hay quyền
-- nào khác.

DROP PROCEDURE IF EXISTS add_column_if_missing_v11;
DELIMITER $$
CREATE PROCEDURE add_column_if_missing_v11(
  IN p_table VARCHAR(64), IN p_column VARCHAR(64), IN p_definition TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table AND COLUMN_NAME = p_column
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_definition);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

CALL add_column_if_missing_v11('medical_record', 'diagnosis_code', 'varchar(50) NULL DEFAULT NULL COMMENT ''Mã chẩn đoán ICD-10 (H3)''');

DROP PROCEDURE IF EXISTS add_column_if_missing_v11;

CREATE TABLE IF NOT EXISTS `diagnosis_catalog` (
  `id` bigint(20) NOT NULL,
  `code` varchar(20) NOT NULL COMMENT 'Mã ICD-10, ví dụ I10, J02.9 (duy nhất toàn bảng)',
  `name_vi` varchar(255) NOT NULL COMMENT 'Tên bệnh tiếng Việt theo danh mục BYT (khi có file chính thức)',
  `chapter` varchar(20) NULL DEFAULT NULL COMMENT 'Chương ICD-10, ví dụ J00-J99 (tùy chọn, phục vụ nhóm hiển thị sau này)',
  `create_by` varchar(100) NULL DEFAULT NULL,
  `create_date` datetime NULL DEFAULT NULL,
  `update_by` varchar(100) NULL DEFAULT NULL,
  `update_date` datetime NULL DEFAULT NULL,
  `del_flag` varchar(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_diagnosis_catalog_code` (`code`) USING BTREE,
  KEY `idx_diagnosis_catalog_name` (`name_vi`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci;

CREATE TABLE IF NOT EXISTS `diagnosis_pin` (
  `doctor_id` varchar(64) NOT NULL COMMENT 'Id user bác sĩ đang đăng nhập (SessionUtils.getUser().id)',
  `diagnosis_id` bigint(20) NOT NULL COMMENT 'Id dòng diagnosis_catalog được ghim',
  `create_by` varchar(100) NULL DEFAULT NULL,
  `create_date` datetime NULL DEFAULT NULL,
  `update_by` varchar(100) NULL DEFAULT NULL,
  `update_date` datetime NULL DEFAULT NULL,
  `del_flag` varchar(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`doctor_id`, `diagnosis_id`) USING BTREE,
  KEY `idx_diagnosis_pin_diagnosis` (`diagnosis_id`) USING BTREE,
  CONSTRAINT `fk_diagnosis_pin_catalog` FOREIGN KEY (`diagnosis_id`)
    REFERENCES `diagnosis_catalog` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci;
