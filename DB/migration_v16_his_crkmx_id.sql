-- DS27 (deep-search round 27): import idempotency for the HIS drug-storage audit.
-- auditHosDrugsStorage inserts via saves/savesTo unconditionally, and the
-- HIS row's unique id (crkmxId, "Tồn kho Thông tin duy nhất định danh") was
-- never persisted anywhere, so a second identical audit (deliberate re-sync
-- while HIS is unreachable and still lists the rows as un-audited, or an
-- accidental double submit) inserted a second storage slip + stock rows and
-- a second control row, adding the quantity to stock totals a second time.
-- This migration persists the HIS detail id on each imported stock row so
-- the audit can skip rows that were already imported for the same company.
-- NULL for every pre-existing row (manual stock-ins, older HIS imports):
-- they carry no HIS id and are never treated as duplicates. No unique
-- constraint: the application checks before insert and skips duplicates
-- with a 200 + hisAlreadyImportedIds flag, so a race can never turn into
-- a 500; the index only makes the pre-insert lookup cheap.
ALTER TABLE supplier_stock ADD COLUMN his_crkmx_id VARCHAR(64) NULL AFTER drug_trac_codg;
CREATE INDEX idx_supplier_stock_his_crkmx ON supplier_stock (company_id, his_crkmx_id);
