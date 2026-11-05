-- migration v14: PropertySet option (i) — narrow propertySet write grants (iter68 item 2-code-7).
-- Background: sys_property_set is GLOBAL system config with no company_id column,
-- so the generic CrudService tenant guard is blind to it. A bare per-operation
-- proxy floor is ineffective on its own because the clinic block-case roles
-- themselves hold propertySet:create/update/delete, so the grants must be
-- narrowed first (same shape as migration v13 for TollDetail).
-- This DELETE is idempotent (re-runs cleanly) and is applied by restore_all.sh
-- after the md_yzs.sql import on every restore, so it survives DB re-imports.
-- KEPT: role 4000 (Toan quyen he thong) and ALL admin roles (OWN admin
-- 2077468568630583311 + the 3 other clinics' admins) — the property-set
-- management page (views/sys/propertySet/index.vue) is a legitimate admin
-- flow and the ONLY writer of this table.
-- REMOVED from: YS 2298416718726168646 (Bac si), pharmacist 2521363969936572416
-- (Duoc si), clinic-version template role 2077468568630583319 (company 0).
-- Decided 2026-11-05 by the assistant under the owner's blanket self-decide
-- grant ("already has ALL rights"); see iter68-propertyset-impl-report.md
-- (decision gate) and iter68-propertyset-fix-report.md.
DELETE FROM sys_resource_permission
WHERE role_id IN ('2298416718726168646', '2521363969936572416', '2077468568630583319')
  AND resource_id IN (SELECT id FROM sys_resource WHERE permission IN ('propertySet:create', 'propertySet:update', 'propertySet:delete'));
