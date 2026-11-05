-- DS1 (deep-search round 1): tenant-scope audit reads.
-- sys_action had no company column, so ensureCompanyFilter had
-- nothing to filter and ANY session with >=1 resource could list
-- every tenant's audit rows, and the history drawer (listAll by
-- object_id) could read any object's history.
-- This migration adds company_id = the OBJECT's tenant, set at write
-- time in CrudService.createAction from the acted-on entity's
-- company (entities without a company — global tables, Company
-- itself — leave it NULL: fail-closed, hidden from clinic lists,
-- still visible to action:list/read holders via the ActionService
-- admin bypass). Action declares its own company field so
-- ensureCompanyFilter applies (it checks getDeclaredField).
ALTER TABLE sys_action ADD COLUMN company_id VARCHAR(45) NULL AFTER object_name;
-- Backfill from the actor: create_by is "Name(loginName)"; resolve
-- the actor's company via sys_user.login_name. Rows that cannot be
-- resolved (deleted users, unparseable names) stay NULL: fail-closed
-- for clinics, still visible to admins.
UPDATE sys_action a
  JOIN sys_user u
    ON u.login_name = SUBSTRING_INDEX(SUBSTRING_INDEX(a.create_by, '(', -1), ')', 1)
  SET a.company_id = u.company_id
  WHERE a.company_id IS NULL;
CREATE INDEX idx_sys_action_company ON sys_action (company_id);
