package com.geeke.stock.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import com.alibaba.fastjson.JSONObject;
import com.geeke.outpatient.controller.RegistrationController;
import com.geeke.outpatient.entity.Registration;
import com.geeke.outpatient.service.RegistrationService;
import com.geeke.utils.SessionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geeke.common.data.Parameter;
import com.geeke.common.data.SearchParamsBuilder;
import com.geeke.common.constants.ActionConstants;
import com.geeke.common.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.CollectionUtils;

import com.geeke.common.service.ServiceException;
import com.geeke.outpatient.entity.RecipelDetail;
import com.geeke.outpatient.entity.RecipelInfo;
import com.geeke.outpatient.service.RecipelDetailService;
import com.geeke.stock.dao.MedicinalStorageControlDao;
import com.geeke.stock.entity.*;
import com.geeke.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thuốc/Kiểm soát chi tiết nhập kho vật tưService
 * @author hl
 * @version 2022-09-26
 */

@Service("medicinalStorageControlService")
@Transactional(readOnly = true)
public class MedicinalStorageControlService extends CrudService<MedicinalStorageControlDao, MedicinalStorageControl>{

    private static final Logger logger = LoggerFactory.getLogger(MedicinalStorageControlService.class);
    @Autowired
    private DrugService drugService;
    @Autowired
    private StuffService stuffService;
    @Autowired
    private SupplierStockService supplierStockService;
    @Autowired
    private MedicinalStockControlService medicinalStockControlService;
    @Autowired
    private MedicinalStockRecordService medicinalStockRecordService;
    @Autowired
    private RecipelDetailService recipelDetailService;

    @Autowired
    private DispensingService dispensingService;

    @Lazy
    @Autowired
    private RegistrationService registrationService;

    /**
     * DS69: generic single-entity save() is fail-closed. CrudController
     * exposes POST /stock/medicinalStorageControl/save, but the mapper
     * <update> writes storage_stock / used_stock / occupy_stock /
     * freeze_stock / surplus_stock verbatim by id, so any caller with the
     * permission could set absolute stock numbers with no freeze, no
     * delta and no stock record (probe-proven: occupy/freeze 5000 while
     * the aggregate still held 0, detail SUM 10777 vs aggregate 10000).
     * Detail rows are written only through the guarded flows
     * (inStorage/addStorageStock, the atomic adjust/occupy/freeze
     * bumps, inventory-verification accomplish), never by a direct save
     * -- the frontend has no UI path through this endpoint either.
     * Internal callers use saveInternal() below, which keeps the old
     * CrudService.save behaviour (tenant check + audit action).
     */
    @Override
    @Transactional(readOnly = false)
    public MedicinalStorageControl save(MedicinalStorageControl entity) {
        throw new ServiceException("Tồn kho chi tiết chỉ thay đổi qua nhập kho / kê đơn / xuất kho / kiểm kê, không hỗ trợ lưu trực tiếp!");
    }

    /**
     * DS69: same fail-closed guard for the bulk writes -- CrudService
     * bulkInsert/bulkUpdate call dao.bulk* directly, NOT save(), so the
     * save() override above does not cover them, and the bulk mapper
     * writes the same absolute stock columns (probe-proven: bulkUpdate
     * set occupy/freeze 4000, bulkInsert added a 333 row, both 200).
     * No internal caller uses bulk on this service.
     */
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<MedicinalStorageControl> entitys) {
        throw new ServiceException("Tồn kho chi tiết chỉ thay đổi qua nhập kho / kê đơn / xuất kho / kiểm kê, không hỗ trợ thao tác hàng loạt!");
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<MedicinalStorageControl> entitys) {
        throw new ServiceException("Tồn kho chi tiết chỉ thay đổi qua nhập kho / kê đơn / xuất kho / kiểm kê, không hỗ trợ thao tác hàng loạt!");
    }

    /**
     * DS69: internal single-entity save for the guarded flows
     * (InventoryVerificationService.adjustStock). NOT reachable from the
     * controller -- CrudController routes only to save()/bulkInsert()/
     * bulkUpdate(). Delegates to CrudService.save, so the tenant check
     * and the audit action row are kept exactly as before.
     */
    @Transactional(readOnly = false)
    public MedicinalStorageControl saveInternal(MedicinalStorageControl entity) {
        return super.save(entity);
    }

    /**
     * Thêm mớinhập khoThông tin giờ, kiểm soátThuốc hoặc Vật tư của độngTồn khoChi tiết
     * @param supplierStorage
     */
    @Transactional(readOnly = false)
    public void addStorageStock(SupplierStorage supplierStorage)
    {
        if (!MedicinalStockControlService.enable)
        {
            return;
        }
        List<SupplierStock> list = this.supplierStockService.listAll(
                SearchParamsBuilder.create().eq("supplier_storage_id", supplierStorage.getId()).build(), "");
        if (CollectionUtils.isEmpty(list))
        {
            return;
        }

        for (SupplierStock supplierStock:list)
        {
            MedicinalStorageControl medicinalStorageControl = new MedicinalStorageControl();
            medicinalStorageControl.setCompany(supplierStock.getCompany());
            medicinalStorageControl.setStorageId(supplierStock.getId());
            if (Objects.nonNull(supplierStock.getDrug()) && StringUtils.isNotBlank(supplierStock.getDrug().getId()))
            {
                Drug drug = this.drugService.get(supplierStock.getDrug().getId());
                if (Objects.isNull(drug) || StringUtils.isBlank(drug.getId()))
                {
                    throw new ServiceException("Nhập kho thất bại, lấy thông tin cơ bản của thuốc nhập kho thất bại!");
                }
                this.medicinalStockControlService.initStock(drug);

                medicinalStorageControl.setDrugStuffId(drug.getId());
                medicinalStorageControl.setDrugStuffName(drug.getGoodsName());
                medicinalStorageControl.setType(1);
            }
            else if (Objects.nonNull(supplierStock.getStuff()) && StringUtils.isNotBlank(supplierStock.getStuff().getId()))
            {
                Stuff stuff = this.stuffService.get(supplierStock.getStuff().getId());
                if (Objects.isNull(stuff) || StringUtils.isBlank(stuff.getId()))
                {
                    throw new ServiceException("Nhập kho thất bại, lấy thông tin cơ bản của vật tư nhập kho thất bại!");
                }
                this.medicinalStockControlService.initStock(stuff);

                medicinalStorageControl.setDrugStuffId(stuff.getId());
                medicinalStorageControl.setDrugStuffName(stuff.getName());
                medicinalStorageControl.setType(2);
            }
            else
            {
                throw new ServiceException("Nhập kho thất bại, tạmchỉhỗ trợThuốc hoặc Vật tưnhập kho!");
            }

            medicinalStorageControl.setStorageStock(new BigDecimal(supplierStock.getInitial()));
            medicinalStorageControl.setUsedStock(BigDecimal.ZERO);
            medicinalStorageControl.setOccupyStock(BigDecimal.ZERO);
            medicinalStorageControl.setFreezeStock(BigDecimal.ZERO);
            medicinalStorageControl.setSurplusStock(new BigDecimal(supplierStock.getInitial()));
            medicinalStorageControl.setReimburseStock(BigDecimal.ZERO);

            // DS31: ensure the (drug, clinic) aggregate row BEFORE writing
            // this line's detail row. The DS30 fix created the row here
            // when the tenant-scoped lookup came back empty (initStock
            // above keys by the DRUG's company, so for an institution drug
            // it creates (drug, INSTITUTION) while this lookup is scoped
            // to the clinic); a plain insert of a fresh zero row 500s
            // under the uk_msc_drug_company unique index when the row
            // only looks missing because it was soft-deleted.
            // ensureAggregateRow resurrects a soft-deleted row instead
            // (recomputing its totals from the live detail rows -- safe
            // here precisely because this line's detail row is not
            // inserted yet, so it cannot be double-counted), creates the
            // row atomically when absent, and leaves a live row alone.
            MedicinalStockControl medicinalStockControl = this.medicinalStockControlService.ensureAggregateRow(
                    medicinalStorageControl.getDrugStuffId(),
                    medicinalStorageControl.getCompany().getId(),
                    medicinalStorageControl.getDrugStuffName(),
                    medicinalStorageControl.getType());

            //ThêmThuốc/Kiểm soát chi tiết nhập kho vật tưKhởi tạoDữ liệu
            super.save(medicinalStorageControl);

            // DS31: atomic bump (see addStorageStocks): the old code added
            // in Java and saved the row back, so concurrent imports of the
            // same drug overwrote each other and the total came out short.
            this.medicinalStockControlService.addStorageStocks(medicinalStockControl,
                    medicinalStorageControl.getStorageStock(), medicinalStorageControl.getSurplusStock());

            //tăngTồn khothao tácChi tiếtBản ghi
            MedicinalStockRecord medicinalStockRecord = new MedicinalStockRecord();
            medicinalStockRecord.setCompany(supplierStock.getCompany());
            medicinalStockRecord.setDrugStuffId(medicinalStorageControl.getDrugStuffId());
            medicinalStockRecord.setDrugStuffName(medicinalStorageControl.getDrugStuffName());
            medicinalStockRecord.setType(medicinalStorageControl.getType());
            medicinalStockRecord.setOperationType(1);    //Tồn khonhập khothao tácđịnh danh
            medicinalStockRecord.setOperationStock(new BigDecimal(supplierStock.getInitial()));
            medicinalStockRecord.setStorageId(supplierStock.getSupplierStorage().getId());

            this.medicinalStockRecordService.saveInternal(medicinalStockRecord);
        }
    }

    /**
     * DS32: atomically add deltas to all six detail stock columns. Same
     * lost-update shape as the aggregate bump: the occupy / dispense /
     * refund / outbound paths used to read a detail row, add in Java and
     * save it back, so N concurrent operations on the same detail row
     * overwrote each other (probe-proven: 8 parallel outbound examines
     * left the shared detail row at used 5 instead of 40). A single
     * UPDATE ... SET col = col + delta serializes on the row lock
     * instead. Keeps the UPDATED action row that CrudService.save used
     * to write. Returns the rows updated (1).
     */
    @Transactional(readOnly = false)
    public int adjustDetailStocks(MedicinalStorageControl row, BigDecimal storageDelta, BigDecimal surplusDelta,
                                  BigDecimal occupyDelta, BigDecimal usedDelta,
                                  BigDecimal reimburseDelta, BigDecimal returnTodoFeeDelta) {
        // DS36: non-freeze callers (prescription occupy / confirm / refund
        // paths) pass freezeDelta ZERO, so they never move freeze_stock.
        return this.adjustDetailStocks(row, storageDelta, surplusDelta, occupyDelta, usedDelta,
                reimburseDelta, returnTodoFeeDelta, BigDecimal.ZERO);
    }

    /**
     * DS36: same atomic bump as the 7-argument overload, plus a freezeDelta
     * dual-written to the freeze_stock column. Only the outbound-freeze
     * touch points (freezeDetailStocks, outbound line delete, outbound
     * cancel, examine) pass a non-zero freezeDelta, always equal to the
     * occupyDelta of the same move, so freeze_stock always holds the
     * frozen portion of occupy_stock.
     */
    @Transactional(readOnly = false)
    public int adjustDetailStocks(MedicinalStorageControl row, BigDecimal storageDelta, BigDecimal surplusDelta,
                                  BigDecimal occupyDelta, BigDecimal usedDelta,
                                  BigDecimal reimburseDelta, BigDecimal returnTodoFeeDelta,
                                  BigDecimal freezeDelta) {
        row.preUpdate();
        int affected = this.dao.adjustStocks(row.getId(), storageDelta, surplusDelta, occupyDelta, usedDelta,
                reimburseDelta, returnTodoFeeDelta, freezeDelta, row.getUpdateBy(), row.getUpdateDate());
        this.saveAction(this.createAction(ActionConstants.ACTION_UPDATED, row));
        return affected;
    }

    /**
     * DS32: atomically occupy a detail row, but only when its surplus still
     * covers the amount. preOccupyStock reads the surplus to decide how
     * much to take from the row; without the guard two concurrent
     * prescriptions can both read surplus 10000 and both occupy 6000,
     * leaving surplus -2000. Returns 1 when applied, 0 when the row no
     * longer has enough surplus (caller throws stock-not-enough and the
     * transaction rolls back, exactly like the existing insufficient-stock
     * check at the end of the occupy loop).
     */
    @Transactional(readOnly = false)
    public int occupyDetailStocks(MedicinalStorageControl row, BigDecimal occupyDelta) {
        row.preUpdate();
        int affected = this.dao.occupyStocks(row.getId(), occupyDelta, occupyDelta.negate(),
                occupyDelta, row.getUpdateBy(), row.getUpdateDate());
        if (affected == 1) {
            this.saveAction(this.createAction(ActionConstants.ACTION_UPDATED, row));
        }
        return affected;
    }

    /**
     * DS33: atomically freeze (or release) an outbound quantity on a
     * detail row: occupy only, surplus untouched (surplus moves at
     * examine, DS32 invariant). A positive delta is guarded by
     * freezeStocks (DS37: surplus minus freeze_stock must cover it --
     * the exact headroom, since a prescription occupy already reduced
     * surplus), so parallel outbound creates cannot over-freeze; a
     * non-positive delta cannot over-freeze, so it uses the plain
     * atomic bump instead (a guarded decrease could spuriously refuse
     * on a row that is already over-frozen by pre-fix data). Returns 1
     * when applied, 0 when the guarded increase was refused; the
     * caller throws stock-not-enough and the transaction rolls back.
     */
    @Transactional(readOnly = false)
    public int freezeDetailStocks(MedicinalStorageControl row, BigDecimal occupyDelta) {
        row.preUpdate();
        int affected;
        if (occupyDelta.compareTo(BigDecimal.ZERO) <= 0) {
            // DS36: a freeze decrease dual-writes the same delta to
            // freeze_stock (the guarded branch below does it in SQL).
            affected = this.adjustDetailStocks(row,
                    BigDecimal.ZERO, BigDecimal.ZERO, occupyDelta,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, occupyDelta);
        } else {
            affected = this.dao.freezeStocks(row.getId(), occupyDelta, row.getUpdateBy(), row.getUpdateDate());
            if (affected == 1) {
                this.saveAction(this.createAction(ActionConstants.ACTION_UPDATED, row));
            }
        }
        return affected;
    }

    //hủynhập khoThông tin, Cập nhậtđộngTồn kho
    @Transactional(readOnly = false)
    public void invalidStorageStock(SupplierStorage supplierStorage)
    {
        if (!MedicinalStockControlService.enable)
        {
            return;
        }
        List<SupplierStock> list = this.supplierStockService.listAll(
                SearchParamsBuilder.create().eq("supplier_storage_id", supplierStorage.getId()).build(), "");
        if (CollectionUtils.isEmpty(list))
        {
            throw new ServiceException("nhập khoThông tinhủyThất bại, Chưatìm kiếmĐếnCầnhủy của Chi tiết nhập khoThông tin. ");
        }

        for (SupplierStock supplierStock:list)
        {
            List<MedicinalStorageControl> medicinalStorageControlList = super.listAll(
                    SearchParamsBuilder.create().eq("storage_id", supplierStock.getId()).build(), "");
            if (CollectionUtils.isEmpty(medicinalStorageControlList))
            {
                continue;
            }

            for (MedicinalStorageControl medicinalStorageControl:medicinalStorageControlList)
            {
                BigDecimal storageStock = medicinalStorageControl.getStorageStock();    //Tổng lượng nhập kho
                BigDecimal surplusStock = medicinalStorageControl.getSurplusStock();    //Số lượng tồn kho còn khả dụng
                if (storageStock.compareTo(surplusStock) != 0)
                {
                    throw new ServiceException("độngTồn khokiểm soátĐãphátthay đổi, Không thểhủyXóanhập khoDữ liệu!");
                }

                // DS35: refuse the void while the detail row is still
                // occupied. The storage==surplus check above catches a
                // prescription occupy (it lowers surplus), but an outbound
                // FREEZE is occupy-only (DS32/DS33 invariant: freeze adds
                // occupy, surplus moves only at examine), so storage ==
                // surplus still holds and the old code zeroed storage and
                // surplus while leaving occupy > 0 orphaned on the zeroed
                // row; examining the frozen outbound slip afterwards then
                // drove detail surplus negative (probe-proven: freeze 5,
                // void, examine -> surplus -5). Fail-closed: cancel the
                // outbound slip first (releasing the freeze), then void
                // the import slip.
                // DS37: the check now reads freeze_stock, not occupy.
                // The switch preserves behaviour exactly: a prescription
                // occupy is already refused by the storage != surplus
                // check above (it lowers surplus), so the only state
                // this check adds is the freeze -- and freeze is the
                // only occupy-only path (DS32/DS33 invariant; every Rx
                // occupy/release moves surplus with occupy). Probe-proven
                // in both directions: a frozen row still refuses the
                // void, and an Rx-only row (freeze 0, occupy 3) still
                // refuses it via the first check.
                BigDecimal freezeStock = medicinalStorageControl.getFreezeStock();
                if (freezeStock != null && freezeStock.compareTo(BigDecimal.ZERO) > 0)
                {
                    throw new ServiceException("Tồn kho đang bị chiếm dụng, không thể hủy nhập kho! Vui lòng hủy phiếu xuất kho đang giữ tồn kho này trước.");
                }

                //1.hủynhập khoThông tin giờ, Đưakhả dụngTồn khođặt là 0
                // DS34: conditional atomic zero (was absolute set-to-0 +
                // super.save()): parallel cancels of the same slip all
                // read storage==surplus and all zeroed the row, while EACH
                // decremented the aggregate by the read values
                // (probe-proven: aggregate 9965 vs detail 10000 for one
                // slip of 5). The UPDATE only applies when the row still
                // holds exactly the values read above, so exactly one
                // caller zeroes a line and decrements the aggregate for it.
                // Keeps the UPDATED action row that super.save() wrote.
                medicinalStorageControl.preUpdate();
                int zeroed = this.dao.zeroVoidStocks(medicinalStorageControl.getId(),
                        storageStock, surplusStock,
                        medicinalStorageControl.getUpdateBy(), medicinalStorageControl.getUpdateDate());
                if (zeroed == 1) {
                    medicinalStorageControl.setStorageStock(BigDecimal.ZERO);
                    medicinalStorageControl.setSurplusStock(BigDecimal.ZERO);
                    this.saveAction(this.createAction(ActionConstants.ACTION_UPDATED, medicinalStorageControl));
                } else {
                    // Another thread already voided this line, or its stock
                    // moved since the read above: re-read and decide
                    // fail-closed instead of decrementing the aggregate on
                    // stale values.
                    MedicinalStorageControl fresh = this.get(medicinalStorageControl.getId());
                    if (fresh != null
                            && fresh.getStorageStock() != null
                            && fresh.getStorageStock().compareTo(BigDecimal.ZERO) == 0
                            && fresh.getSurplusStock() != null
                            && fresh.getSurplusStock().compareTo(BigDecimal.ZERO) == 0) {
                        continue;
                    }
                    throw new ServiceException("độngTồn khokiểm soátĐãphátthay đổi, Không thểhủyXóanhập khoDữ liệu!");
                }

                // DS28: a missing aggregate row must not abort the void. The
                // detail row above is already zeroed, so skip only the
                // aggregate decrement and still write the stock record and
                // process the remaining lines. Previously `return` here left
                // the 2nd+ lines of a voided slip holding live stock
                // (probe-proven: cancel of a 2-line slip with the aggregate
                // row soft-deleted zeroed line 1 only, detail SUM stayed
                // 10003 instead of returning to 10000).
                List<MedicinalStockControl> medicinalStockControlList = this.medicinalStockControlService.listAll(
                        SearchParamsBuilder.create().eq("drug_stuff_id", medicinalStorageControl.getDrugStuffId()).build(), "");
                if (!CollectionUtils.isEmpty(medicinalStockControlList)) {
                    // DS32: atomic aggregate decrement (was read-modify-write
                    // via save(): 8 parallel voids left the aggregate at
                    // 10035 instead of 10000, probe-proven).
                    this.medicinalStockControlService.adjustStocks(medicinalStockControlList.get(0),
                            storageStock.negate(), surplusStock.negate(),
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
                }

                //3.chènChi tiết thao tác tồn kho động
                MedicinalStockRecord medicinalStockRecord = new MedicinalStockRecord();
                medicinalStockRecord.setCompany(medicinalStorageControl.getCompany());
                medicinalStockRecord.setDrugStuffId(medicinalStorageControl.getDrugStuffId());
                medicinalStockRecord.setDrugStuffName(medicinalStorageControl.getDrugStuffName());
                medicinalStockRecord.setType(medicinalStorageControl.getType());
                medicinalStockRecord.setOperationType(-1);    //Tồn khoĐịnh danh thao tác hủy nhập kho
                medicinalStockRecord.setOperationStock(storageStock);
                medicinalStockRecord.setStorageId(medicinalStorageControl.getStorageId());

                this.medicinalStockRecordService.saveInternal(medicinalStockRecord);
            }
        }
    }

    //Chi tiết nhập khotự độngĐếnLogic gọi chức năng theo kỳ(nàyXử lýPhần sau để giao diện frontend làm thủ công, Nếu không, chạy tác vụ định kỳ ở backend có thể gặp vấn đề)
    @Transactional(readOnly = false)
    public void autoInvalidStorageStock(SupplierStock supplierStock)
    {
        //Kiểm tra hiện tại còn tồn kho động bị chiếm dụng chưa được giải phóng không
        List<MedicinalStockRecord> medicinalStockRecords = this.medicinalStockRecordService.getStockRecordByStorageId(supplierStock.getId());
        if (!CollectionUtils.isEmpty(medicinalStockRecords))
        {
            throw new ServiceException("Hiện vẫn còn tình trạng tồn kho bị chiếm dụng hoặc trả thuốc chưa hoàn phí và tồn kho chưa được giải phóng, Vui lòngtrướcXử lýsaulạithao tác!");
        }

        List<MedicinalStorageControl> medicinalStorageControlList = super.listAll(
                SearchParamsBuilder.create().eq("storage_id", supplierStock.getId()).build(), "");
        if (CollectionUtils.isEmpty(medicinalStorageControlList))
        {
            return;
        }

        MedicinalStorageControl medicinalStorageControl = medicinalStorageControlList.get(0);
        BigDecimal storageStock = medicinalStorageControl.getStorageStock();		// Tổng lượng nhập kho
        BigDecimal usedStock = medicinalStorageControl.getUsedStock();		        // Số lượng tồn kho đã sử dụng
        BigDecimal occupyStock = medicinalStorageControl.getOccupyStock();		    // Số lượng tồn kho đang bị chiếm dụng
        BigDecimal surplusStock = medicinalStorageControl.getSurplusStock();        // Số lượng tồn kho còn khả dụng
        BigDecimal reimburseStock = medicinalStorageControl.getReimburseStock();    // Số lượng tồn kho báo hỏng
        medicinalStorageControl.setSurplusStock(BigDecimal.ZERO);
        medicinalStorageControl.setReimburseStock(reimburseStock.add(surplusStock));
        super.save(medicinalStorageControl);

        List<MedicinalStockControl> medicinalStockControlList = this.medicinalStockControlService.listAll(
                SearchParamsBuilder.create().eq("drug_stuff_id", medicinalStorageControl.getDrugStuffId()).build(), "");
        if (CollectionUtils.isEmpty(medicinalStockControlList)) {
            return;
        }
        MedicinalStockControl medicinalStockControl = medicinalStockControlList.get(0);
        medicinalStockControl.setSurplusStock(medicinalStockControl.getSurplusStock().subtract(surplusStock));
        medicinalStockControl.setReimburseStock(medicinalStockControl.getReimburseStock().add(surplusStock));
        // DS69: internal save -- the generic save() is fail-closed, so
        // the guarded auto-invalidate flow writes through saveInternal().
        this.medicinalStockControlService.saveInternal(medicinalStockControl);

        //3.chènChi tiết thao tác tồn kho động
        MedicinalStockRecord medicinalStockRecord = new MedicinalStockRecord();
        medicinalStockRecord.setCompany(medicinalStorageControl.getCompany());
        medicinalStockRecord.setDrugStuffId(medicinalStorageControl.getDrugStuffId());
        medicinalStockRecord.setDrugStuffName(medicinalStorageControl.getDrugStuffName());
        medicinalStockRecord.setType(medicinalStorageControl.getType());
        medicinalStockRecord.setOperationType(-2);    //Tồn khoĐịnh danh thao tác hủy nhập kho
        medicinalStockRecord.setOperationStock(surplusStock);
        medicinalStockRecord.setStorageId(medicinalStorageControl.getStorageId());

        this.medicinalStockRecordService.saveInternal(medicinalStockRecord);
    }

    /**
     * TheoĐơn thuốclấyhủychiếm dụng trướcTồn khosố
     */
    @Transactional(readOnly = false)
    public void cancelOccupy(RecipelInfo recipelInfo) {
        if (!MedicinalStockControlService.enable)
        {
            return;
        }
        String recipelInfoId = recipelInfo.getId();    //Đơn thuốcID
        if (StringUtils.isBlank(recipelInfoId))
        {
            return;
        }
        List<MedicinalStockRecord> list = this.medicinalStockRecordService.listAll(
                SearchParamsBuilder.create().eq("recipel_info_id", recipelInfoId).build(), "");
        if (CollectionUtils.isEmpty(list))
        {
            return;
        }

        // DS29: a missing detail or aggregate row must not abort the release.
        // Both lookups used to `return` here, which skipped the aggregate
        // release, every remaining record, AND the batchDelete(list) at the
        // end -- so a refunded prescription kept its op=2 occupy records
        // forever and the aggregate kept the refunded occupy (probe-proven:
        // refund of a 2-line retail charge after the detail row was
        // soft-deleted left 2 op=2 records and aggregate occupy 5, while
        // the refund still returned 200 code 100). Each guard now skips
        // only its own update: a deleted detail row has nothing left to
        // release, but the aggregate rollup (keyed by drug, not by the
        // detail row) is still corrected and the records are still deleted.
        for (MedicinalStockRecord medicinalStockRecord:list)
        {
            BigDecimal operationStock = medicinalStockRecord.getOperationStock();    //chiếm dụngTồn khosố
            //Cập nhậttrướcchiếm dụng của Chi tiết nhập kho
            List<MedicinalStorageControl> medicinalStorageControlList = super.listAll(
                    SearchParamsBuilder.create().eq("storage_id", medicinalStockRecord.getStorageId()).build(), "");
            if (!CollectionUtils.isEmpty(medicinalStorageControlList))
            {
                // DS32: atomic detail release (occupy -op, surplus +op).
                //TODO: Trả lại đúng lúc gặp chi tiết nhập kho hết hạn thì xử lý thế nào?    giải quyếtPhương thức: khôngXử lý, củatrướcnhư thế nàogìchiếm dụng của Bây giờ trả lại như thế nào
                this.adjustDetailStocks(medicinalStorageControlList.get(0),
                        BigDecimal.ZERO, operationStock, operationStock.negate(),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            }

            //Cập nhật lượng chiếm dụng trước của kiểm soát tổng
            List<MedicinalStockControl> medicinalStockControlList = this.medicinalStockControlService.listAll(
                    SearchParamsBuilder.create().eq("drug_stuff_id", medicinalStockRecord.getDrugStuffId()).build(), "");
            if (!CollectionUtils.isEmpty(medicinalStockControlList))
            {
                // DS32: atomic aggregate release (occupy -op, surplus +op).
                this.medicinalStockControlService.adjustStocks(medicinalStockControlList.get(0),
                        BigDecimal.ZERO, operationStock, operationStock.negate(),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            }
        }

        //XóaChi tiếtDữ liệu(mụctrướclàmvật lýXóa)
        this.medicinalStockRecordService.batchDelete(list);
    }

    /**
     * Tồn kho đặt trước
     * @param recipelInfo
     */
    @Transactional(readOnly = false)
    public void preOccupyStock(RecipelInfo recipelInfo) {
        if (!MedicinalStockControlService.enable)
        {
            return;
        }
        String recipelInfoId = recipelInfo.getId();    //Đơn thuốcID
        if (StringUtils.isBlank(recipelInfoId))
        {
            return;
        }
        //1.Giải phóng trước lượng chiếm dụng trướcTồn kho,báosổ sáchchiếm dụngTồn kho của Hiệu lựctính
        this.cancelOccupy(recipelInfo);
        //2.Lạichiếm dụng
        List<RecipelDetail> recipelDetailList = this.recipelDetailService.listAll(
                SearchParamsBuilder.create().eq("recipel_info_id", recipelInfoId).build(), "");
        if (CollectionUtils.isEmpty(recipelDetailList))
        {
            return;
        }

        for (RecipelDetail recipelDetail:recipelDetailList)
        {
            String drugStuffId = recipelDetail.getDrugStuffId().getDrugStuffId();
            BigDecimal calculationBigDecimal = BigDecimal.ZERO;                       //dùng đểTính, nhấtsau và  totalBigDecimal làmso vớisotiến hành dòngkiểm soát
            
            MedicinalStockRecord medicinalStockRecord = new MedicinalStockRecord();
            medicinalStockRecord.setCompany(recipelDetail.getCompany());
            medicinalStockRecord.setOperationType(2);    //Định danh thao tác chiếm dụng trước
            //TODO thêmmộtCáiPhòng khám Id  của Điều kiện
            Drug drug = this.drugService.get(drugStuffId);
            Stuff stuff = this.stuffService.get(drugStuffId);
            if (Objects.nonNull(drug) && StringUtils.isNotBlank(drug.getId()))
            {
                medicinalStockRecord.setDrugStuffId(drug.getId());
                medicinalStockRecord.setDrugStuffName(drug.getGoodsName());
                medicinalStockRecord.setType(1);
                if(Objects.equals(drug.getStatus(), "0"))
                {
                    throw new ServiceException("ThuốcĐãdừngdùng");
                }
            }
            else if (Objects.nonNull(stuff) && StringUtils.isNotBlank(stuff.getId()))
            {
                medicinalStockRecord.setDrugStuffId(stuff.getId());
                medicinalStockRecord.setDrugStuffName(stuff.getName());
                medicinalStockRecord.setType(2);
                if(Objects.equals(stuff.getStatus(), "0"))
                {
                    throw new ServiceException("Vật tưĐãdừngdùng");
                }
            }
            else
            {
                //mụctrướcchỉcóThuốc, Vật tưmớitiến hành dòngđộngTồn khokiểm soát, kháckhôngkiểm soát
                continue;
            }
            
            // Tồn khobảnglưu trữ của  có Đơn vị đóng gói
            // - khôngbán lẻ giờ: trực tiếpsử dụng total(Đơn vị đóng gói) và Tồn khoso vớiso, chiếm dụngcũngdùngĐơn vị đóng gói
            // - bán lẻ giờ: CầnĐưatổng lượng(Đơn vị bào chế)Chuyển đổi thànhĐơn vị đóng góiđếnchiếm dụngTồn kho
            BigDecimal totalBigDecimal;
            boolean isUnpackSell = recipelDetail.getIsUnpackSell() != null && recipelDetail.getIsUnpackSell() == 1;

            if (isUnpackSell) {
                // Bán lẻ: total  có Đơn vị bào chế, CầnChuyển đổi thànhĐơn vị đóng gói(hướng tớitrênLàm tròn)
                totalBigDecimal = new BigDecimal(recipelDetail.getTotal());
            } else {
                // Không bán lẻ: total  có Đơn vị đóng gói, trực tiếpsử dụng
                totalBigDecimal = new BigDecimal(recipelDetail.getTotal());
            }
            
            if (totalBigDecimal.compareTo(BigDecimal.ZERO) < 0)
            {
                throw new ServiceException("Đơn thuốcdùnglượngTồn tạikêâmsố của tình hình, Vui lòngđúngđiềnĐơn thuốckêThông tin!");
            }
            if (totalBigDecimal.compareTo(BigDecimal.ZERO) == 0)
            {
                continue;
            }

            String companyId = recipelInfo.getCompany().getId();
            List<MedicinalStorageControl> medicinalStorageControlList = super.dao.getSurplusStockByDrugStuffIdTo(drugStuffId, companyId);

            if (CollectionUtils.isEmpty(medicinalStorageControlList))
            {
                throw new ServiceException("[" + recipelInfo.getName() + "] trong [" + medicinalStockRecord.getDrugStuffName() + "]Tồn khokhông đủ!");
            }

            // bán lẻ giờ, CầnĐưatổng lượng(Đơn vị bào chế)Chuyển đổi thànhĐơn vị đóng gói
            BigDecimal stockToOccupy = totalBigDecimal;
            if (isUnpackSell && drug != null && drug.getPreparation() != null && Integer.valueOf(drug.getPreparation()) > 0) {
                // Đơn vị bào chếchuyển là Đơn vị đóng gói: hướng tớitrênLàm tròn(chắc chắnbảocóđủđủ của bào chếSố lượng)
                BigDecimal preparationBD = new BigDecimal(drug.getPreparation());
                stockToOccupy = totalBigDecimal.divide(preparationBD, 0, BigDecimal.ROUND_UP);
            }

            for (int i = 0; i < medicinalStorageControlList.size(); i++) {
                MedicinalStorageControl medicinalStorageControl = medicinalStorageControlList.get(i);
                if (stockToOccupy.compareTo(BigDecimal.ZERO) == 0)
                {
                    break;
                }

                BigDecimal surplusStock = medicinalStorageControl.getSurplusStock();   //Số lượng tồn kho còn khả dụng(Đơn vị đóng gói)

                // Chi tiếtTồn khođủdùng thì chiếm đúng số còn thiếu, không đủ
                // thì chiếm hết số khả dụng của dòng này (như 2 nhánh cũ).
                BigDecimal take = surplusStock.compareTo(stockToOccupy) >= 0 ? stockToOccupy : surplusStock;
                medicinalStockRecord.setId(null);
                medicinalStockRecord.setOperationStock(take);  //Số lượng tồn kho thao tác
                medicinalStockRecord.setStorageId(medicinalStorageControl.getStorageId());    //chiếm dụng của Chi tiết nhập khoID
                medicinalStockRecord.setRecipelInfoId(recipelInfoId);  //chiếm dụng của Đơn thuốcID
                medicinalStockRecord.setRefId(recipelDetail.getId());  //chiếm dụng của liên kếtBản ghiID
                this.medicinalStockRecordService.saveInternal(medicinalStockRecord);

                // DS32: atomic conditional occupy of the detail row
                // (occupy +take, surplus -take, only when the surplus still
                // covers take). The old code read the row, added in Java
                // and saved it back, so two concurrent prescriptions could
                // both read the same surplus and both occupy from it. When
                // the guard refuses (0 rows), the surplus moved under us:
                // throw stock-not-enough exactly like the check at the end
                // of this loop; the transaction rolls the record back too.
                int occupied = this.occupyDetailStocks(medicinalStorageControl, take);
                if (occupied == 0)
                {
                    throw new ServiceException("[" + recipelInfo.getName() + "] trong [" + medicinalStockRecord.getDrugStuffName() + "]Tồn khokhông đủ!");
                }

                calculationBigDecimal = calculationBigDecimal.add(take);
                stockToOccupy = stockToOccupy.subtract(take);
            }

            if (stockToOccupy.compareTo(BigDecimal.ZERO) > 0)
            {
                throw new ServiceException("[" + recipelInfo.getName() + "] trong [" + medicinalStockRecord.getDrugStuffName() + "]Tồn khokhông đủ!");
            }

            //Cập nhật lượng chiếm dụng trước của kiểm soát tổng
            // DS29: a missing aggregate row must not abort the occupy. The
            // detail rows above are already occupied and their op=2 records
            // already saved, so skip only the aggregate bump and still
            // process the remaining lines. Previously `return` here left
            // the 2nd+ lines of a prescription unoccupied while the charge
            // still returned 200 code 100 (probe-proven: retail charge of
            // 2+3 with the aggregate row soft-deleted occupied only 2 in
            // the detail row and wrote a single op=2 record, although the
            // prescription itself showed both lines). Deliberately NOT
            // fail-closed (throwing would make a prescribable drug
            // unprescribable until someone restores a deleted aggregate
            // row) and deliberately NOT recreating the aggregate row here
            // (the import path owns its creation via initStock).
            List<MedicinalStockControl> medicinalStockControlList = this.medicinalStockControlService.listAll(
                    SearchParamsBuilder.create()
                            .eq("drug_stuff_id", medicinalStockRecord.getDrugStuffId())
                            .eq("company_id", companyId)
                            .build(), "");
            if (!CollectionUtils.isEmpty(medicinalStockControlList)) {
                // DS32: atomic aggregate occupy (occupy +calc, surplus -calc).
                this.medicinalStockControlService.adjustStocks(medicinalStockControlList.get(0),
                        BigDecimal.ZERO, calculationBigDecimal.negate(), calculationBigDecimal,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            }
        }
    }

    /**
     * xác nhậnchiếm dụngTồn kho(tức là: Chuyển tồn kho chiếm dụng trước thành tồn kho chiếm dụng thực tế)
     * Thông thường khi phát thuốc tức là chuyển tồn kho chiếm dụng trước thành tồn kho chiếm dụng thực tế
     * @param
     */
    @Transactional(readOnly = false)
    public void okOccupyStock(String registrationId, String recipelInfoIds) {
        Registration registration = new Registration();
        registration.setId(registrationId);

        RecipelInfo recipelInfo = new RecipelInfo();
        recipelInfo.setId(recipelInfoIds);
        Registration entity = registrationService.get(registrationId);
        recipelInfo.setCompany(entity.getCompany());

        if (!MedicinalStockControlService.enable)
        {
            return;
        }
        String recipelInfoId = recipelInfo.getId();    //Đơn thuốcID
        if (StringUtils.isBlank(recipelInfoId))
        {
            return;
        }
        //Lạitrướcchiếm dụngTồn khomộtĐưa,  là rồihơntốt của kiểm soátTồn khoKhông cóvấn đề, NếuLạichiếm dụngcóvấn đề, mộtthông thườngGiải thíchkiểm soátcótôiđâyđề xuất
        this.preOccupyStock(recipelInfo);

        //Chuyển tồn kho chiếm dụng trước thành tồn kho chiếm dụng thực tế
        List<MedicinalStockRecord> medicinalStockRecordList = this.medicinalStockRecordService.listAll(
                SearchParamsBuilder.create().eq("recipel_info_id", recipelInfoId).build(), "");
        if (CollectionUtils.isEmpty(medicinalStockRecordList))
        {
            logger.warn("Đơn thuốc[{}]Không tìm thấy bản ghi thao tác tồn kho, Bỏ qua xử lý tồn kho", recipelInfoId);
            return;
        }

        for (MedicinalStockRecord medicinalStockRecord:medicinalStockRecordList)
        {
            BigDecimal operationStock = medicinalStockRecord.getOperationStock();		// Số lượng tồn kho thao tác
            List<MedicinalStorageControl> medicinalStorageControlList = super.listAll(
                    SearchParamsBuilder.create().eq("storage_id", medicinalStockRecord.getStorageId()).build(), "");
            if (CollectionUtils.isEmpty(medicinalStorageControlList))
            {
                throw new ServiceException("Kiểm soát tồn kho động thất bại, Không tìm thấy chi tiết nhập kho cần kiểm soát tồn kho[Chi tiếtID=" + medicinalStockRecord.getStorageId() + "]. ");
            }
            // DS32: atomic detail confirm (used +op, occupy -op).
            this.adjustDetailStocks(medicinalStorageControlList.get(0),
                    BigDecimal.ZERO, BigDecimal.ZERO, operationStock.negate(), operationStock,
                    BigDecimal.ZERO, BigDecimal.ZERO);

            //Cập nhật lượng chiếm dụng trước của kiểm soát tổng
            List<MedicinalStockControl> medicinalStockControlList = this.medicinalStockControlService.listAll(
                    SearchParamsBuilder.create().eq("drug_stuff_id", medicinalStockRecord.getDrugStuffId()).build(), "");
            if (CollectionUtils.isEmpty(medicinalStockControlList))
            {
                throw new ServiceException("Kiểm soát tồn kho động thất bại, Không tìm thấy mặt hàng[ " + medicinalStockRecord.getDrugStuffName() + " ] của bản ghi kiểm soát tổng tồn kho. ");
            }
            // DS32: atomic aggregate confirm (used +op, occupy -op).
            this.medicinalStockControlService.adjustStocks(medicinalStockControlList.get(0),
                    BigDecimal.ZERO, BigDecimal.ZERO, operationStock.negate(), operationStock,
                    BigDecimal.ZERO, BigDecimal.ZERO);

            //Cập nhật trạng thái bản ghi thao tác tồn kho động
            medicinalStockRecord.setOperationType(3);
            this.medicinalStockRecordService.saveInternal(medicinalStockRecord);

            //LưuChi tiết phát thuốc
            Dispensing dispensing = new Dispensing();
            dispensing.setRegistration(registration);
            dispensing.setRecipelInfo(recipelInfo);
            dispensing.setId("");
            SupplierStock supplierStock = new SupplierStock();
            // DS32: the two rows are only read here for the dispensing
            // record (storage id, company); the stock moves above are
            // atomic now, so the locals are re-declared here instead of
            // above where they used to be mutated and saved back.
            MedicinalStorageControl medicinalStorageControl = medicinalStorageControlList.get(0);
            MedicinalStockControl medicinalStockControl = medicinalStockControlList.get(0);
            supplierStock.setId(medicinalStorageControl.getStorageId());
            dispensing.setSupplierStock(supplierStock);
            dispensing.setCompany(medicinalStockControl.getCompany());
            dispensing.setNumber(Integer.parseInt(operationStock.stripTrailingZeros().toPlainString()));
            // DS71: DispensingService.save is fail-closed now; the real
            // dispense-confirm flow writes through saveInternal().
            dispensingService.saveInternal(dispensing);
        }
    }

    /**
     * Trả thuốctạmkhônghoàn phítình hình:nàytình hìnhdướithuốc của Đãsử dụnglượngcầntrừrơi, Lượng tồn kho khả dụng phải đợi hoàn phí xong mới được cộng thêm
     * @param recipelInfo
     */
    @Transactional(readOnly = false)
    public void drugRepercussion(RecipelInfo recipelInfo)
    {
        if (!MedicinalStockControlService.enable)
        {
            return;
        }
        String recipelInfoId = recipelInfo.getId();    //Đơn thuốcID
        if (StringUtils.isBlank(recipelInfoId))
        {
            return;
        }

        List<MedicinalStockRecord> medicinalStockRecordList = this.medicinalStockRecordService.listAll(
                SearchParamsBuilder.create()
                        .eq("recipel_info_id", recipelInfoId)
                        .eq("operation_type", 3)
                        .build(), "");
        if (CollectionUtils.isEmpty(medicinalStockRecordList))
        {
            logger.warn("Đơn thuốc[{}]Không tìm thấy bản ghi thao tác tồn kho, Bỏ qua xử lý tồn kho", recipelInfoId);
            return;
        }

        for (MedicinalStockRecord medicinalStockRecord:medicinalStockRecordList)
        {
            BigDecimal operationStock = medicinalStockRecord.getOperationStock();		// Số lượng tồn kho thao tác
            List<MedicinalStorageControl> medicinalStorageControlList = super.listAll(
                    SearchParamsBuilder.create().eq("storage_id", medicinalStockRecord.getStorageId()).build(), "");
            if (CollectionUtils.isEmpty(medicinalStorageControlList))
            {
                throw new ServiceException("Kiểm soát tồn kho động thất bại, Không tìm thấy chi tiết nhập kho cần kiểm soát tồn kho[Chi tiếtID=" + medicinalStockRecord.getStorageId() + "]. ");
            }
            // DS32: atomic detail return (used -op, return-todo-fee +op).
            // The old code null-guarded returnTodoFeeStock in Java; the
            // atomic UPDATE does the same with IFNULL in SQL (the column
            // is NULL on live rows).
            this.adjustDetailStocks(medicinalStorageControlList.get(0),
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, operationStock.negate(),
                    BigDecimal.ZERO, operationStock);

            //Cập nhật lượng chiếm dụng trước của kiểm soát tổng
            List<MedicinalStockControl> medicinalStockControlList = this.medicinalStockControlService.listAll(
                    SearchParamsBuilder.create().eq("drug_stuff_id", medicinalStockRecord.getDrugStuffId()).build(), "");
            if (CollectionUtils.isEmpty(medicinalStockControlList))
            {
                throw new ServiceException("Kiểm soát tồn kho động thất bại, Không tìm thấy mặt hàng[ " + medicinalStockRecord.getDrugStuffName() + " ] của bản ghi kiểm soát tổng tồn kho. ");
            }
            // DS32: atomic aggregate return (used -op, return-todo-fee +op).
            this.medicinalStockControlService.adjustStocks(medicinalStockControlList.get(0),
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, operationStock.negate(),
                    BigDecimal.ZERO, operationStock);

            //Cập nhật trạng thái bản ghi thao tác tồn kho động
            medicinalStockRecord.setOperationType(4);
            this.medicinalStockRecordService.saveInternal(medicinalStockRecord);
        }
    }

    /**
     * Trả thuốcsau, tiến hành dònghoàn phí của tình hình:Trong trường hợp này do trước đóTrả thuốcthao tácĐãĐưa”Số lượng tồn kho đã sử dụng“trừtrả lạiđirồi, này lầnchỉĐưakhả dụngTồn khothêmtrênđithì dòngrồi
     * @param recipelInfo
     */
    @Transactional(readOnly = false)
    public void goBackFee(RecipelInfo recipelInfo)
    {
        if (!MedicinalStockControlService.enable)
        {
            return;
        }
        String recipelInfoId = recipelInfo.getId();    //Đơn thuốcID
        if (StringUtils.isBlank(recipelInfoId))
        {
            return;
        }

        List<MedicinalStockRecord> medicinalStockRecordList = this.medicinalStockRecordService.listAll(
                SearchParamsBuilder.create()
                        .eq("recipel_info_id", recipelInfoId)
                        .eq("operation_type", 4)
                        .build(), "");
        if (CollectionUtils.isEmpty(medicinalStockRecordList))
        {
            logger.warn("Đơn thuốc[{}]Không tìm thấy bản ghi thao tác tồn kho, Bỏ qua xử lý tồn kho", recipelInfoId);
            return;
        }

        for (MedicinalStockRecord medicinalStockRecord:medicinalStockRecordList)
        {
            BigDecimal operationStock = medicinalStockRecord.getOperationStock();		// Số lượng tồn kho thao tác
            List<MedicinalStorageControl> medicinalStorageControlList = super.listAll(
                    SearchParamsBuilder.create().eq("storage_id", medicinalStockRecord.getStorageId()).build(), "");
            if (CollectionUtils.isEmpty(medicinalStorageControlList))
            {
                throw new ServiceException("Kiểm soát tồn kho động thất bại, Không tìm thấy chi tiết nhập kho cần kiểm soát tồn kho[Chi tiếtID=" + medicinalStockRecord.getStorageId() + "]. ");
            }
            // DS32: atomic detail refund (surplus +op, return-todo-fee -op).
            this.adjustDetailStocks(medicinalStorageControlList.get(0),
                    BigDecimal.ZERO, operationStock, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, operationStock.negate());

            //Cập nhật lượng chiếm dụng trước của kiểm soát tổng
            List<MedicinalStockControl> medicinalStockControlList = this.medicinalStockControlService.listAll(
                    SearchParamsBuilder.create().eq("drug_stuff_id", medicinalStockRecord.getDrugStuffId()).build(), "");
            if (CollectionUtils.isEmpty(medicinalStockControlList))
            {
                throw new ServiceException("Kiểm soát tồn kho động thất bại, Không tìm thấy mặt hàng[ " + medicinalStockRecord.getDrugStuffName() + " ] của bản ghi kiểm soát tổng tồn kho. ");
            }
            // DS32: atomic aggregate refund (surplus +op, return-todo-fee -op).
            this.medicinalStockControlService.adjustStocks(medicinalStockControlList.get(0),
                    BigDecimal.ZERO, operationStock, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, operationStock.negate());
        }

        //Xóachiếm dụngthao tácChi tiết(này lầnvật lýXóa)
        this.medicinalStockRecordService.batchDelete(medicinalStockRecordList);
    }

    //Lấytất cả của Tồn khoChi tiết
    @Transactional(readOnly = true)
    public List<MedicinalStorageControl> getAll(String compayId, String type,String variety) {
        return this.dao.getAll(compayId,type,variety);
    }

    @Transactional(readOnly = true)
    public BigDecimal getByDrugOrStuffId(String drugOrStuffId) {
        return this.dao.getByDrugOrStuffId(drugOrStuffId, SessionUtils.getLoginTenantId());
    }

    @Transactional(readOnly = true)
    public BigDecimal getByDrugOrStuffIds(List<String> drugOrStuffIds) {
        return this.dao.getByDrugOrStuffIds(drugOrStuffIds);
    }



    /**
     * Hoàn phí hạng mục khám chữa bệnhVật tưthao tác
     *
     * @param recipelInfo
     */
    @Transactional
    public void materialRefund(RecipelInfo recipelInfo) {
        if (!MedicinalStockControlService.enable)
        {
            return;
        }
        String recipelInfoId = recipelInfo.getId();    //Đơn thuốcID
        if (StringUtils.isBlank(recipelInfoId))
        {
            return;
        }

        List<MedicinalStockRecord> medicinalStockRecordList = this.medicinalStockRecordService.listAll(
                SearchParamsBuilder.create()
                        .eq("recipel_info_id", recipelInfoId)
                        .eq("operation_type", 3)
                        .build(), "");
        if (CollectionUtils.isEmpty(medicinalStockRecordList))
        {
            logger.warn("Đơn thuốc[{}]Không tìm thấy bản ghi thao tác tồn kho, Bỏ qua xử lý tồn kho", recipelInfoId);
            return;
        }

        for (MedicinalStockRecord medicinalStockRecord:medicinalStockRecordList)
        {
            BigDecimal operationStock = medicinalStockRecord.getOperationStock();		// Số lượng tồn kho thao tác
            List<MedicinalStorageControl> medicinalStorageControlList = super.listAll(
                    SearchParamsBuilder.create().eq("storage_id", medicinalStockRecord.getStorageId()).build(), "");
            if (CollectionUtils.isEmpty(medicinalStorageControlList))
            {
                throw new ServiceException("Kiểm soát tồn kho động thất bại, Không tìm thấy chi tiết nhập kho cần kiểm soát tồn kho[Chi tiếtID=" + medicinalStockRecord.getStorageId() + "]. ");
            }
            // DS32: atomic detail material refund (used -op, surplus +op).
            this.adjustDetailStocks(medicinalStorageControlList.get(0),
                    BigDecimal.ZERO, operationStock, BigDecimal.ZERO, operationStock.negate(),
                    BigDecimal.ZERO, BigDecimal.ZERO);

            //Cập nhật lượng chiếm dụng trước của kiểm soát tổng
            List<MedicinalStockControl> medicinalStockControlList = this.medicinalStockControlService.listAll(
                    SearchParamsBuilder.create().eq("drug_stuff_id", medicinalStockRecord.getDrugStuffId()).build(), "");
            if (CollectionUtils.isEmpty(medicinalStockControlList))
            {
                throw new ServiceException("Kiểm soát tồn kho động thất bại, Không tìm thấy mặt hàng[ " + medicinalStockRecord.getDrugStuffName() + " ] của bản ghi kiểm soát tổng tồn kho. ");
            }
            // DS32: atomic aggregate material refund (used -op, surplus +op).
            this.medicinalStockControlService.adjustStocks(medicinalStockControlList.get(0),
                    BigDecimal.ZERO, operationStock, BigDecimal.ZERO, operationStock.negate(),
                    BigDecimal.ZERO, BigDecimal.ZERO);
        }
        //Xóachiếm dụngthao tácChi tiết(này lầnvật lýXóa)
        this.medicinalStockRecordService.batchDelete(medicinalStockRecordList);
    }


    //chiếm dụng trước: theoĐơn thuốcđếnkiểm soát, trướcTruy vấnvàvàgiải phóng, lạiLạichiếm dụng trước

    //chiếm dụng thực tế: thì có phát thuốc giờ, theoĐơn thuốcđếnkiểm soát,  trướcLạichiếm dụng trướcmộtĐưa, lạiĐưachiếm dụng trước của thay đổi là chiếm dụng thực tế

    //Trả thuốc: trướcTồn khotrảtrả lạiđi, khả dụngTồn khotrướckhôngrõgiải phóng,  v.v. Hoàn phí xong mới thực sự giải phóng

    //----------------------------
    /**
     * khophòngthậtthựcTồn kholượng=Tổng lượng nhập kho - Số lượng tồn kho đã sử dụng - Số lượng tồn kho báo hỏng
     * mởthuốckhả dụngTồn kholượng=Số lượng tồn kho còn khả dụng
     * Số lượng tồn kho đang bị chiếm dụng=Số lượng tồn kho đang bị chiếm dụng
     * Số lượng tồn kho báo hỏng=Số lượng tồn kho báo hỏng
     * ĐãTrả thuốcchờhoàn phí của thuốcchiếm dụngTồn kholượng=(Tổng lượng nhập kho - Số lượng tồn kho đã sử dụng - Số lượng tồn kho báo hỏng) - Số lượng tồn kho đang bị chiếm dụng - Số lượng tồn kho còn khả dụng
     *                                     thực tếTồn kho(lớn)
     */
}