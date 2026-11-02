-- Migration v12 — H4 research loop iteration 2 (số thứ tự chờ khám theo ngày/bác sĩ).
--
-- BỐI CẢNH: phiếu đăng ký khám (DataBean.registration) trước đây tự đếm lại
-- số thứ tự bằng vị trí của bệnh nhân trong danh sách đăng ký của bác sĩ
-- trong ngày — sai khi cùng bệnh nhân đăng ký 2 lần/ngày với cùng bác sĩ
-- (trùng patientId) và số đó không lưu ở đâu nên danh sách chờ, trang thu
-- phí và phiếu in mỗi nơi tính một kiểu. H4 lưu số ngay lúc đăng ký.
--
-- QUY TẮC:
-- - queue_number trên registration: NULL được — lượt đăng ký tạo trước H4
--   hoặc không có bác sĩ thì không có số, không bịa; cập nhật sau đó không
--   đánh lại số (mapper update ghi nguyên giá trị entity mang theo).
-- - queue_counter: một dòng cho mỗi (phòng khám, bác sĩ, ngày phòng khám),
--   khóa chính đúng 3 cột đó nên ngày mới tự bắt đầu lại từ 1; số tăng bằng
--   INSERT ... ON DUPLICATE KEY UPDATE last_number = LAST_INSERT_ID(
--   last_number + 1) để 2 lượt đăng ký đồng thời không nhận cùng số.
-- - Ngày phòng khám = ngày Asia/Ho_Chi_Minh, khớp cửa sổ [00:00, 00:00 hôm
--   sau) theo giờ tường GMT+7 của G6/H5/H1 (create_date lưu GMT+7).
-- - KHÔNG INSERT dữ liệu, không chạm cột logic hay quyền nào khác.
-- - Không thêm router/menu/Shiro: số hiện trên các trang sẵn có (danh sách
--   chờ khám, trang thu phí) và trên phiếu in sẵn có (DataBean).
--
-- Áp dụng: mysql md_yzs < DB/migration_v12_queue_number.sql
-- (đã nối vào restore_all.sh sau v11 để sống qua re-import DB).
-- Chạy lại an toàn (idempotent): CREATE TABLE IF NOT EXISTS + procedure
-- kiểm tra information_schema.COLUMNS trước khi ALTER, tự xóa sau khi chạy
-- xong (theo đúng pattern của migration v10).

DROP PROCEDURE IF EXISTS add_column_if_missing_v12;
DELIMITER $$
CREATE PROCEDURE add_column_if_missing_v12(
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

CALL add_column_if_missing_v12('registration', 'queue_number', 'int(11) NULL DEFAULT NULL COMMENT ''Số thứ tự chờ khám trong ngày của bác sĩ (H4)''');

DROP PROCEDURE IF EXISTS add_column_if_missing_v12;

CREATE TABLE IF NOT EXISTS queue_counter (
  company_id varchar(64) NOT NULL COMMENT 'Phòng khám',
  doctor_id varchar(64) NOT NULL COMMENT 'Bác sĩ',
  queue_date date NOT NULL COMMENT 'Ngày phòng khám (Asia/Ho_Chi_Minh)',
  last_number int(11) NOT NULL COMMENT 'Số thứ tự cuối đã cấp trong ngày',
  PRIMARY KEY (company_id, doctor_id, queue_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
