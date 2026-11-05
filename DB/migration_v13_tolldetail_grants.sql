-- migration v13: TollDetail option (i) — narrow tollDetail write grants (iter68 item 2-code-12).
-- Background: toll_detail has no company_id and no del_flag (hard DELETE); the iter63
-- parent-toll guard already blocks foreign-linked writes (400) but passes the orphan
-- branch (no tollId / nonexistent parent). A bare per-operation proxy floor is
-- ineffective on its own because the clinic block-case roles themselves hold
-- tollDetail:create/update/delete, so the grants must be narrowed first.
-- This DELETE is idempotent (re-runs cleanly) and is applied by restore_all.sh
-- after the md_yzs.sql import on every restore, so it survives DB re-imports.
-- KEPT: role 4000 (Toan quyen he thong) and 2077468568630583311 (admin) — the
-- admin form flow (tollDetailForm.vue -> /toll/tollDetail/save) stays usable.
-- REMOVED from: YS 2298416718726168646 (Bac si), pharmacist 2521363969936572416
-- (Duoc si), clinic-version role 2077468568630583319, inspection-clinic role
-- 2314654134646620573. Decided 2026-11-05 by the owner under the blanket
-- self-decide grant ("already has ALL rights"); see iter68-tolldetail-impl-report.md.
DELETE FROM sys_resource_permission
WHERE role_id IN ('2298416718726168646', '2521363969936572416', '2077468568630583319', '2314654134646620573')
  AND resource_id IN (SELECT id FROM sys_resource WHERE permission IN ('tollDetail:create', 'tollDetail:update', 'tollDetail:delete'));
