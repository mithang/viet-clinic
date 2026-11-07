-- Seed cho lần khởi tạo DB đầu tiên — chuyển từ tools/restore_db3b.sh
-- (bảng notice_send + role full 4000 + toàn bộ quyền router/resource).
-- Toàn bộ câu lệnh đều idempotent (IF NOT EXISTS / ON DUPLICATE KEY UPDATE /
-- INSERT IGNORE) nên chạy lại cũng an toàn.
-- Tài khoản test (testfull...) KHÔNG nằm ở đây: setup.sh tạo sau bằng hash
-- Shiro của backend, mật khẩu test được sinh ngẫu nhiên mỗi lần setup mới.

CREATE TABLE IF NOT EXISTS `notice_send` (
  `id` bigint(20) NOT NULL,
  `company_id` bigint(20) NOT NULL,
  `sender_id` varchar(100) NULL DEFAULT NULL,
  `title` varchar(50) NULL DEFAULT NULL,
  `is_site` varchar(1) NULL DEFAULT NULL,
  `is_wechat` varchar(1) NULL DEFAULT NULL,
  `is_email` varchar(1) NULL DEFAULT NULL,
  `is_sms` varchar(1) NULL DEFAULT NULL,
  `site_content` text NULL,
  `wechat_content` text NULL,
  `email_content` text NULL,
  `sms_content` text NULL,
  `received_by` varchar(255) NULL DEFAULT NULL,
  `remarks` varchar(255) NULL DEFAULT NULL,
  `create_by` varchar(100) NULL DEFAULT NULL,
  `create_date` datetime NULL DEFAULT NULL,
  `update_by` varchar(100) NULL DEFAULT NULL,
  `update_date` datetime NULL DEFAULT NULL,
  `del_flag` varchar(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci;

INSERT INTO sys_role (id, parent_id, company_id, code, name, is_locked, remarks, create_by, create_date, update_by, update_date, del_flag)
VALUES (4000, '0', 2077468568630583309, 'full', 'Toan quyen he thong', '0', '', 'system', NOW(), 'system', NOW(), '0')
ON DUPLICATE KEY UPDATE del_flag='0', is_locked='0';

INSERT IGNORE INTO sys_router_permission (id, role_id, router_id)
SELECT 600000000000000000 + ROW_NUMBER() OVER (ORDER BY id), 4000, id FROM sys_router;

INSERT IGNORE INTO sys_resource_permission (id, role_id, resource_id)
SELECT 700000000000000000 + ROW_NUMBER() OVER (ORDER BY id), 4000, id FROM sys_resource WHERE del_flag='0' AND is_locked='0';
