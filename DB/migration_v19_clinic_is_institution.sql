-- Migration v19 — DS86 deep-search round 86 (2 cot clinic thieu:
-- is_institution + fixmedins_code).
--
-- BOI CANH: ClinicDao.xml (insert / update / bulkInsert / get / listPage)
-- va entity Clinic (isInstitution, Integer; fixmedinsCode, String) deu
-- doc/ghi 2 cot nay, nhung bang `clinic` khong co cot nao — ca trong DDL
-- seed DB/md_yzs.sql (CREATE TABLE clinic) lan trong DB test md_yzs
-- dang chay (SHOW CREATE TABLE clinic, 2026-11-06). Ca 2 cot deu TON TAI
-- o bang anh em org_company (is_institution int(2) NULL; fixmedins_code
-- varchar(12) NULL, cung comment), nen day la cot bi sot khi tach bang
-- clinic, khong phai truong thua.
-- HAU QUA (da probe live truoc khi sua, tools/ds86-verify.py):
-- C1 POST /org/clinic/bulkInsert -> HTTP 500 "Unknown column
-- 'is_institution' in 'field list'", khong tao duoc dong nao.
-- C2 POST /org/clinic/bulkUpdate -> HTTP 500 "Unknown column
-- 'a.fixmedins_code' in 'field list'": CrudService.bulkUpdate goi
-- verifyTenantOwnershipAgainstStoredRow -> dao.get(id) -> select
-- clinicColumns co a.fixmedins_code. Audit cot ds86-column-audit.json
-- chi bat duoc is_institution (chi quet INSERT/UPDATE); fixmedins_code
-- chi xuat hien o SELECT nen lot luoi audit — probe live C2 moi lo ra.
-- DDL duoi day lay dung dinh nghia cua org_company (is_institution
-- int(2) NULL DEFAULT NULL, dat sau version_id; fixmedins_code
-- varchar(12) NULL DEFAULT NULL, dat sau lessee_id theo thu tu select
-- trong ClinicDao). Guard INFORMATION_SCHEMA (prepared statement) de
-- chay lai an toan — restore_all.sh ap lai migration sau moi lan
-- re-import DB; MySQL 8.0 khong co ADD COLUMN IF NOT EXISTS.
-- An toan khi ap dung: cot moi NULL, khong co du lieu cu nao bi doi;
-- khong doi bang nao khac.
SET @ds86_col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'clinic'
  AND COLUMN_NAME = 'is_institution');
SET @ds86_ddl = IF(@ds86_col_exists = 0,
  'ALTER TABLE `clinic` ADD COLUMN `is_institution` int(2) NULL DEFAULT NULL COMMENT ''Có phải cơ sở không (1: Có; 0: Không)'' AFTER `version_id`',
  'SELECT 1');
PREPARE ds86_stmt FROM @ds86_ddl;
EXECUTE ds86_stmt;
DEALLOCATE PREPARE ds86_stmt;
-- DS86 (bo sung): cot fixmedins_code — cung mau thieu nhu is_institution,
-- chi lo ra khi probe live C2 (bulkUpdate -> dao.get -> select co
-- a.fixmedins_code). Dinh nghia y hệt org_company.fixmedins_code
-- (varchar(12) NULL DEFAULT NULL).
SET @ds86_fix_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'clinic'
  AND COLUMN_NAME = 'fixmedins_code');
SET @ds86_fix_ddl = IF(@ds86_fix_exists = 0,
  'ALTER TABLE `clinic` ADD COLUMN `fixmedins_code` varchar(12) NULL DEFAULT NULL COMMENT ''Mã cơ sở y tế chỉ định'' AFTER `lessee_id`',
  'SELECT 1');
PREPARE ds86_fix_stmt FROM @ds86_fix_ddl;
EXECUTE ds86_fix_stmt;
DEALLOCATE PREPARE ds86_fix_stmt;
