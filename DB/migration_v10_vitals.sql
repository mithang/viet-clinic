-- Migration v10 — H2 research loop iteration 2 (dấu hiệu sinh tồn có cấu trúc).
--
-- BỐI CẢNH: trang khám đã có một hàng sinh hiệu rời rạc gắn với
-- registration/patient (nhiệt độ, mạch, nhịp thở, huyết áp dạng chuỗi
-- tự do trên BasicInfoModel), nhưng bảng medical_record — hồ sơ của
-- lượt khám — không có trường sinh hiệu có cấu trúc nào, nên không
-- lưu được SpO2, cân nặng, chiều cao, và huyết áp không tách được
-- tâm thu/tâm trương. H2 bổ sung 8 cột nullable trên medical_record,
-- đi theo đúng đường lưu medical-record sẵn có (allSave/allNewSave
-- qua MedicalRecordDao insert/update), không thêm bảng, không thêm
-- endpoint, không thêm router/menu, không đổi quyền.
--
-- QUY TẮC:
-- - Tất cả 8 cột đều NULL được: lượt khám không bao giờ bị chặn vì
--   thiếu sinh hiệu; thiếu trường nào thì trường đó NULL, không bịa.
-- - Huyết áp là cặp tâm thu/tâm trương (vital_bp_sys/vital_bp_dia),
--   không dùng chuỗi tự do; hàng BasicInfoModel cũ giữ nguyên để
--   không phá luồng đăng ký/khám hiện tại.
-- - BMI KHÔNG có cột: chỉ tính hiển thị ở frontend
--   (cân nặng kg / (chiều cao m)^2, chiều cao nhập theo cm).
-- - KHÔNG threshold, KHÔNG alert trong mục này (chỉ ghi nhận giá trị).
-- - Tái khám trên cùng một dòng medical_record: mapper update ghi đè
--   toàn bộ cột (không selective) nên bộ sinh hiệu mới REPLACE bộ cũ;
--   xóa trống một trường khi lưu lại sẽ xóa giá trị đó trong DB, chứ
--   không giữ lặng giá trị cũ. Không có bảng lịch sử sinh hiệu riêng.
--
-- Áp dụng: mysql md_yzs < DB/migration_v10_vitals.sql
-- (đã nối vào restore_all.sh sau v9 để sống qua re-import DB).
-- Chạy lại an toàn (idempotent): procedure kiểm tra
-- information_schema.COLUMNS trước khi ALTER, tự xóa sau khi chạy
-- xong (theo đúng pattern add_index_if_missing của migration v1).
-- Không INSERT dữ liệu, không chạm cột logic hay quyền nào khác.

DROP PROCEDURE IF EXISTS add_column_if_missing_v10;
DELIMITER $$
CREATE PROCEDURE add_column_if_missing_v10(
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

CALL add_column_if_missing_v10('medical_record', 'vital_pulse', 'int(11) NULL DEFAULT NULL COMMENT ''Mạch (lần/phút)''');
CALL add_column_if_missing_v10('medical_record', 'vital_bp_sys', 'int(11) NULL DEFAULT NULL COMMENT ''Huyết áp tâm thu (mmHg)''');
CALL add_column_if_missing_v10('medical_record', 'vital_bp_dia', 'int(11) NULL DEFAULT NULL COMMENT ''Huyết áp tâm trương (mmHg)''');
CALL add_column_if_missing_v10('medical_record', 'vital_temperature', 'decimal(4,1) NULL DEFAULT NULL COMMENT ''Nhiệt độ (°C)''');
CALL add_column_if_missing_v10('medical_record', 'vital_resp_rate', 'int(11) NULL DEFAULT NULL COMMENT ''Nhịp thở (lần/phút)''');
CALL add_column_if_missing_v10('medical_record', 'vital_spo2', 'int(11) NULL DEFAULT NULL COMMENT ''SpO2 (%)''');
CALL add_column_if_missing_v10('medical_record', 'vital_weight', 'decimal(5,2) NULL DEFAULT NULL COMMENT ''Cân nặng (kg)''');
CALL add_column_if_missing_v10('medical_record', 'vital_height', 'decimal(5,2) NULL DEFAULT NULL COMMENT ''Chiều cao (cm)''');

DROP PROCEDURE IF EXISTS add_column_if_missing_v10;
