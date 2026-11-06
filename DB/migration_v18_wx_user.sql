-- Migration v18 — DS84 deep-search round 84 (bang wx_user thieu).
--
-- BOI CANH: khong co CREATE TABLE wx_user o bat ky dau trong repo
-- (DB/*.sql, Doc/, Code/); md_yzs.wx_user khong ton tai (ERROR 1146).
-- Ca ba duong WeChat thanh cong (getCode / initLogin / getPhone) deu
-- goi wxUserDao.getByOpenId truoc khi luu, nen ngay ca voi WeChat
-- that (code hop le, app-id/secret da cau hinh) chung cung 500 o
-- SELECT from bang khong ton tai, truoc ca khi cham den save.
-- Bang chi duoc dung qua WxUserDao.xml, va mapper chi doc/ghi dung
-- 4 cot (id, company_id, open_id, phone); cac truong con lai cua
-- WxUser (code, sessionKey, unionId, token) la transient, khong
-- persist. DDL duoi day lay dung 4 cot do, kieu suy tu entity va
-- cac bang tuong du (sys_user.phone varchar(20), qy_open_id
-- varchar(100)); id la chuoi so do IdGen.uuid() tra ve UID long.
-- Index tren open_id vi getByOpenId/update deu truy van theo no.
-- An toan khi ap dung: bang moi, khong co du lieu cu, khong doi
-- bang nao khac. Duong thanh cong WeChat that van chua kiem chung
-- duoc end-to-end khi thieu credential WeChat that (chi chu so huu
-- moi co) — chi chung minh duoc bang ton tai va cau INSERT/SELECT
-- cua mapper chay dung (tools/ds84-verify.py P9).
CREATE TABLE IF NOT EXISTS `wx_user` (
  `id` varchar(64) NOT NULL COMMENT 'Dinh danh (IdGen.uuid)',
  `company_id` bigint(20) UNSIGNED NULL DEFAULT NULL COMMENT 'Phong kham',
  `open_id` varchar(100) NOT NULL COMMENT 'OpenId WeChat',
  `phone` varchar(20) NULL DEFAULT NULL COMMENT 'So dien thoai di dong',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_wx_user_open_id` (`open_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'Nguoi dung WeChat' ROW_FORMAT = DYNAMIC;
