-- DS36 (deep-search round 36): separate freeze column on the stock DETAIL
-- table, step 1 (dual-write only -- no reader/guard change this round).
--
-- Why a separate column: the outbound freeze and the prescription occupy
-- share one column, medicinal_storage_control.occupy_stock. A freeze is
-- occupy-only (surplus moves only at examine, DS32/DS33 invariant), while
-- a prescription occupy moves occupy AND surplus together, so the shared
-- column cannot tell how much of occupy is frozen stock waiting for an
-- outbound examine and how much is prescribed stock waiting to be
-- dispensed. That forces the DS33 freeze guard to use
-- surplus - occupy (conservative: it double-counts prescription occupies,
-- which already reduced surplus) and it let DS35's void path miss a frozen
-- row until a fail-closed occupy check was added. The fix is a dedicated
-- freeze_stock column that tracks ONLY the outbound-freeze portion of
-- occupy, written with the same delta at every freeze touch point
-- (freezeDetailStocks, outbound line delete, outbound cancel, examine).
-- DS37 will switch the freeze guard (and only the guard) to read it.
--
-- Why ONLY the detail table: verified 2026-11-05 by grep that the outbound
-- freeze never touches the aggregate medicinal_stock_control -- create /
-- edit / delete-line / cancel / examine all write occupy on the DETAIL row
-- only (SupplierOutboundDetailService :118/:141/:159/:188,
-- SupplierOutboundService :180). The aggregate's occupy_stock is written
-- only by prescription flows (preOccupyStock / cancelOccupy /
-- okOccupyStock and the refund paths), and the single aggregate
-- adjustStocks call in the outbound flow (examine, :194) passes
-- occupyDelta ZERO. So the aggregate needs no freeze column.
--
-- Backfill: a detail row's frozen quantity is exactly the sum of the
-- numbers on its still-pending outbound slips. Status 1 is the only live
-- frozen state this codebase creates (save() sets 1 = cho xet duyet,
-- examine sets 2, cancel sets 3); examined slips already converted their
-- freeze to used, cancelled slips already released it. Note the IFNULL on
-- o.del_flag: supplier_outbound.del_flag is NULL (not '0') on live rows
-- in this database -- the insert never sets it -- so a plain = '0'
-- predicate would backfill 0 for every live slip (caught by the DS36
-- probe: backfill stayed 0 until the predicate was fixed). The test DB holds
-- no live outbound slips at migration time, so the backfill touches 0
-- rows there; the statement is still correct for a DB that does.
ALTER TABLE medicinal_storage_control
    ADD COLUMN freeze_stock decimal(24,4) NOT NULL DEFAULT 0.0000 AFTER occupy_stock;

UPDATE medicinal_storage_control m
SET m.freeze_stock = IFNULL((
    SELECT SUM(d.number)
    FROM supplier_outbound_detail d
    JOIN supplier_outbound o ON o.id = d.supplier_outbound_id
    WHERE d.medicinal_storage_id = m.id
      AND IFNULL(o.del_flag, '0') = '0'
      AND o.status = 1
), 0)
WHERE m.del_flag = '0';
