package com.geeke.stock.service;

import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.data.SearchParamsBuilder;
import com.geeke.common.persistence.BaseEntity;
import com.geeke.common.persistence.DataEntity;
import com.geeke.common.constants.ActionConstants;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;

import com.geeke.outpatient.entity.PresDrug;
import com.geeke.outpatient.service.PresDrugService;
import com.geeke.stock.dao.MedicinalStockControlDao;
import com.geeke.org.entity.Company;
import com.geeke.stock.entity.Drug;
import com.geeke.stock.entity.MedicinalStockControl;
import com.geeke.stock.entity.Stuff;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Thuốc/Kiểm soát tổng tồn kho vật tưService
 * @author hl
 * @version 2022-09-26
 */

@Service("medicinalStockControlService")
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MedicinalStockControlService extends CrudService<MedicinalStockControlDao, MedicinalStockControl>{
    private final MedicinalStockControlDao dao;

    /** ThuốcTồn khokiểm soát là Cơ sởTổng hưởngDữ liệu: Cho phép phòng khám thuộc cùng cơ sở truy cập dữ liệu phòng khám của cơ sở(Với danh sách #{institution} SQL Nhất quán) */
    @Override
    protected boolean isInstitutionShared() {
        return true;
    }

    private final DrugService drugService;

    private final StuffService stuffService;

    private final PresDrugService presDrugService;


    public static final Boolean enable = Boolean.TRUE;

    /**
     * DS69: generic single-entity save() is fail-closed. CrudController
     * exposes POST /stock/medicinalStockControl/save, but the mapper
     * <update> writes storage_stock / used_stock / occupy_stock /
     * surplus_stock verbatim by id, so any caller with the permission
     * could set absolute aggregate totals with no matching detail move
     * (probe-proven: occupy 3000 / surplus 7000 while the detail row
     * still held 0, and a fresh aggregate row created for a drug with
     * no detail rows at all). Aggregate rows are maintained only by
     * the guarded flows (ensureAggregateRow, the atomic add/adjust
     * bumps, inventory-verification accomplish), never by a direct save
     * -- the frontend has no UI path through this endpoint either.
     * Internal callers use saveInternal() below, which keeps the old
     * CrudService.save behaviour (tenant check + audit action).
     */
    @Override
    @Transactional(readOnly = false)
    public MedicinalStockControl save(MedicinalStockControl entity) {
        throw new ServiceException("Tồn kho tổng chỉ thay đổi qua nhập kho / kê đơn / xuất kho / kiểm kê, không hỗ trợ lưu trực tiếp!");
    }

    /**
     * DS69: same fail-closed guard for the bulk writes -- CrudService
     * bulkInsert/bulkUpdate call dao.bulk* directly, NOT save(), so the
     * save() override above does not cover them, and the bulk mapper
     * writes the same absolute stock columns (probe-proven: bulkUpdate
     * set occupy 2000, bulkInsert created a new aggregate row, both
     * 200). No internal caller uses bulk on this service.
     */
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<MedicinalStockControl> entitys) {
        throw new ServiceException("Tồn kho tổng chỉ thay đổi qua nhập kho / kê đơn / xuất kho / kiểm kê, không hỗ trợ thao tác hàng loạt!");
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<MedicinalStockControl> entitys) {
        throw new ServiceException("Tồn kho tổng chỉ thay đổi qua nhập kho / kê đơn / xuất kho / kiểm kê, không hỗ trợ thao tác hàng loạt!");
    }

    /**
     * DS132 B: chan xoa truc tiep dong tong ton kho, cung mau DS126 A
     * (MedicinalStorageControl/SysSeting) va DS69 save() o tren. Truoc
     * day khong co delete() override nen generic CrudController delete
     * toi thang mapper soft-delete (UPDATE del_flag=1 WHERE id, KHONG
     * loc del_flag): ghost/blank -> 200 data 0 im lang, xoa lap ->
     * 200 rows 1, va xoa dong LIVE cung 200 rows 1 -- dong tong duy
     * nhat hien co (storage 10000 / surplus 10000) van con 1 dong chi
     * tiet LIVE trong medicinal_storage_control, nen xoa no se mo coi
     * ton kho that va pha vo bat bien "tong == tong chi tiet" ma
     * DS30/DS31 bao ve (probe pre-fix tai hien live). Dong tong la du
     * lieu dan xuat, chi doi qua cac flow co guard
     * (ensureAggregateRow, add/adjust bumps, inventory accomplish);
     * khong co duong UI xoa (client api chi co listAll), khong caller
     * noi bo nao goi delete()/bulkDelete() tren service nay (verified
     * by grep). bulkDelete cung chan han (khong giu parity 200 cua
     * DS126 B -- giong DS126 A, bulk bi chan toan bo).
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(MedicinalStockControl entity) {
        throw new ServiceException("Tồn kho tổng chỉ thay đổi qua nhập kho / kê đơn / xuất kho / kiểm kê, không hỗ trợ xóa trực tiếp!");
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<MedicinalStockControl> entitys) {
        throw new ServiceException("Tồn kho tổng chỉ thay đổi qua nhập kho / kê đơn / xuất kho / kiểm kê, không hỗ trợ xóa trực tiếp!");
    }

    /**
     * DS69: internal single-entity save for the guarded flows
     * (InventoryVerificationService.adjustStock and
     * MedicinalStorageControlService.autoInvalidStorageStock). NOT
     * reachable from the controller -- CrudController routes only to
     * save()/bulkInsert()/bulkUpdate(). Delegates to CrudService.save,
     * so the tenant check and the audit action row are kept exactly as
     * before.
     */
    @Transactional(readOnly = false)
    public MedicinalStockControl saveInternal(MedicinalStockControl entity) {
        return super.save(entity);
    }

    /**
     * DS31: ensure exactly one live aggregate row exists for
     * (drugStuffId, companyId), creating or resurrecting it as needed.
     * Previously initStock/initStockTo did a read-then-plain-insert, so two
     * concurrent imports of the same new drug both passed the empty check
     * and both inserted (8 parallel imports created 8 live rows,
     * probe-proven with tools/ds30-race.py), and a re-import after a
     * soft-delete inserted a second row instead of reviving the old one,
     * stranding its totals (DS30 caveat: the live total lost 10000 while
     * the soft-deleted row kept it).
     * - Live row: return it unchanged (callers that need a name sync do
     *   their own save, as initStock does below).
     * - Soft-deleted row: resurrect it (del_flag 0) with the stock totals
     *   recomputed from the live detail rows, so the aggregate again equals
     *   the detail sum. Callers must invoke this BEFORE inserting any new
     *   detail row for the pair (addStorageStock ensures the aggregate
     *   first, then writes the detail), so the recompute never counts the
     *   row being imported right now.
     * - No row: atomic upsert (INSERT ... ON DUPLICATE KEY UPDATE on
     *   uk_msc_drug_company). A concurrent winner's uncommitted insert
     *   makes this statement wait on the index lock and then take the
     *   UPDATE branch, which only flips del_flag and never zeroes the
     *   totals; the row is then this transaction's own modification, so
     *   later reads in the same transaction see it (a plain insert plus
     *   catch of DuplicateKeyException would leave the loser
     *   snapshot-blind under REPEATABLE READ).
     * The tenant check runs on every path, exactly as CrudService.save
     * would run it for an insert: a foreign company's row is refused
     * rather than silently revived or created.
     */
    @Transactional(readOnly = false)
    public MedicinalStockControl ensureAggregateRow(String drugStuffId, String companyId,
                                                   String drugStuffName, Integer type) {
        List<MedicinalStockControl> rows = this.dao.getByDrugOrStuffId(drugStuffId, companyId);
        if (!CollectionUtils.isEmpty(rows)) {
            MedicinalStockControl row = rows.get(0);
            verifyTenantOwnership(row);
            if (BaseEntity.DEL_FLAG_NORMAL.equals(row.getDelFlag())) {
                return row;
            }
            MedicinalStockControl sums = this.dao.sumDetailStocks(drugStuffId, companyId);
            row.setDelFlag(BaseEntity.DEL_FLAG_NORMAL);
            row.setDrugStuffName(drugStuffName);
            row.setStorageStock(sums.getStorageStock());
            row.setUsedStock(sums.getUsedStock());
            row.setOccupyStock(sums.getOccupyStock());
            row.setSurplusStock(sums.getSurplusStock());
            row.setReimburseStock(sums.getReimburseStock());
            row.preUpdate();
            this.dao.resurrect(row);
            this.saveAction(this.createAction(ActionConstants.ACTION_UPDATED, row));
            return row;
        }
        MedicinalStockControl row = new MedicinalStockControl();
        row.setCompany(new Company(companyId));
        row.setDrugStuffId(drugStuffId);
        row.setDrugStuffName(drugStuffName);
        row.setType(type);
        row.setStorageStock(BigDecimal.ZERO);
        row.setUsedStock(BigDecimal.ZERO);
        row.setOccupyStock(BigDecimal.ZERO);
        row.setSurplusStock(BigDecimal.ZERO);
        row.setReimburseStock(BigDecimal.ZERO);
        row.preInsert();
        verifyTenantOwnership(row);
        int affected = this.dao.insertOrResurrect(row);
        if (affected == 1) {
            this.saveAction(this.createAction(ActionConstants.ACTION_CREATED, row));
            return row;
        }
        // 0 = a live row already existed (concurrent winner, no change);
        // 2 = a soft-deleted row was flipped live by the UPDATE branch.
        // Either way the persisted row is ours to see now: re-read it
        // instead of trusting the local object (its id is not the row's).
        List<MedicinalStockControl> again = this.dao.getByDrugOrStuffId(drugStuffId, companyId);
        return CollectionUtils.isEmpty(again) ? row : again.get(0);
    }

    /**
     * DS31: atomically add to the aggregate storage/surplus totals. The old
     * addStorageStock bump read the row, added in Java and saved it back,
     * so N concurrent imports each read the same total and overwrote each
     * other (8 parallel imports left the total at 1.0 instead of 8.0 even
     * after the duplicate-row fix). A single UPDATE ... SET col = col +
     * delta serializes on the row lock instead. Keeps the UPDATED action
     * row that CrudService.save used to write for the bump.
     */
    @Transactional(readOnly = false)
    public void addStorageStocks(MedicinalStockControl row, BigDecimal storageDelta, BigDecimal surplusDelta) {
        verifyTenantOwnership(row);
        row.preUpdate();
        this.dao.addStorageStocks(row.getId(), storageDelta, surplusDelta, row.getUpdateBy(), row.getUpdateDate());
        this.saveAction(this.createAction(ActionConstants.ACTION_UPDATED, row));
    }

    /**
     * DS32: atomically add deltas to all six aggregate stock columns. Same
     * lost-update shape as addStorageStocks (which only covers storage /
     * surplus): the void, occupy, dispense and refund paths move occupy,
     * used, reimburse and return-todo-fee as well, and they used to read
     * the row, add in Java and save it back, so N concurrent operations on
     * the same drug overwrote each other (probe-proven: 8 parallel voids
     * left the aggregate at 10035 instead of 10000; 8 parallel outbound
     * examines left aggregate used at 5 instead of 40). A single UPDATE
     * ... SET col = col + delta serializes on the row lock instead. Keeps
     * the UPDATED action row that CrudService.save used to write.
     */
    @Transactional(readOnly = false)
    public void adjustStocks(MedicinalStockControl row, BigDecimal storageDelta, BigDecimal surplusDelta,
                             BigDecimal occupyDelta, BigDecimal usedDelta,
                             BigDecimal reimburseDelta, BigDecimal returnTodoFeeDelta) {
        verifyTenantOwnership(row);
        row.preUpdate();
        this.dao.adjustStocks(row.getId(), storageDelta, surplusDelta, occupyDelta, usedDelta,
                reimburseDelta, returnTodoFeeDelta, row.getUpdateBy(), row.getUpdateDate());
        this.saveAction(this.createAction(ActionConstants.ACTION_UPDATED, row));
    }

    /**
     * Thêm mớiThuốc hoặc Vật tưcơ bảnchínhDữ liệu giờ, Khởi tạo dữ liệu bản ghi kiểm soát tổng tồn kho
     * @param dataEntity
     */
    @Transactional(readOnly = false)
    public void initStock(DataEntity<?> dataEntity)
    {
        if (!MedicinalStockControlService.enable)
        {
            return;
        }
        // DS31: resolve the master data first (unchanged), then delegate
        // the row handling to ensureAggregateRow: live row -> sync the name
        // as before; soft-deleted row -> resurrect with recomputed totals;
        // no row -> atomic upsert. The old code read via tenant-scoped
        // listAll (del_flag=0 only) and inserted a fresh row whenever the
        // list was empty, which both raced and stranded soft-deleted
        // totals. The row is still keyed by the DRUG's company, as before
        // (for an institution drug that is the institution row; the clinic
        // row is created by initStockTo / the DS30 D2 fallback in
        // addStorageStock).
        String drugStuffId;
        String drugStuffName;
        Integer type;
        String companyId;
        if (dataEntity instanceof Drug)
        {
            Drug drug = drugService.get(dataEntity.getId());
            if (Objects.isNull(drug) || StringUtils.isBlank(drug.getId()))
            {
                throw new ServiceException("Khởi tạo[ThuốcTồn kho]Khi kiểm soát tổng,Không lấy được thông tin bảo trì cơ bản của thuốc!");
            }
            drugStuffId = drug.getId();
            drugStuffName = drug.getGoodsName();
            type = 1;
            companyId = drug.getCompany() == null ? null : drug.getCompany().getId();
        }
        else if (dataEntity instanceof Stuff)
        {
            Stuff stuff = stuffService.get(dataEntity.getId());
            if (Objects.isNull(stuff) || StringUtils.isBlank(stuff.getId()))
            {
                throw new ServiceException("Khởi tạo[Vật tưTồn kho]Khi kiểm soát tổng,Không lấy được thông tin bảo trì cơ bản của vật tư!");
            }
            drugStuffId = stuff.getId();
            drugStuffName = stuff.getName();
            type = 2;
            companyId = stuff.getCompany() == null ? null : stuff.getCompany().getId();
        }
        else
        {
            return;
        }
        MedicinalStockControl row = ensureAggregateRow(drugStuffId, companyId, drugStuffName, type);
        if (!drugStuffName.equals(row.getDrugStuffName()))
        {
            row.setDrugStuffName(drugStuffName);
            super.save(row);
        }
    }

    /**
     * Thêm mớiThuốc hoặc Vật tưTồn kho giờ, Khởi tạo dữ liệu bản ghi kiểm soát tổng tồn kho
     * @param dataEntity
     */
    @Transactional(readOnly = false)
    public void initStockTo(DataEntity<?> dataEntity)
    {
        if (!MedicinalStockControlService.enable)
        {
            return;
        }
        // DS31: same delegation as initStock, but the row is keyed by the
        // IMPORTING tenant (dataEntity.getCompany(), set by saves() to the
        // slip line's company), not by the drug's company -- unchanged
        // semantics. The old code always inserted a NEW zero row (it nulled
        // the id even when a row already existed), which is exactly the
        // duplicate the race probe caught; now a live row is left alone, a
        // soft-deleted row is resurrected with recomputed totals, and only
        // a truly absent row is upserted atomically.
        String drugStuffId;
        String drugStuffName;
        Integer type;
        if (dataEntity instanceof Drug)
        {
            Drug drug = drugService.get(dataEntity.getId());
            if (Objects.isNull(drug) || StringUtils.isBlank(drug.getId()))
            {
                throw new ServiceException("Khởi tạo[ThuốcTồn kho]Khi kiểm soát tổng,Không lấy được thông tin bảo trì cơ bản của thuốc!");
            }
            drugStuffId = drug.getId();
            drugStuffName = drug.getGoodsName();
            type = 1;
        }
        else if (dataEntity instanceof Stuff)
        {
            Stuff stuff = stuffService.get(dataEntity.getId());
            if (Objects.isNull(stuff) || StringUtils.isBlank(stuff.getId()))
            {
                throw new ServiceException("Khởi tạo[Vật tưTồn kho]Khi kiểm soát tổng,Không lấy được thông tin bảo trì cơ bản của vật tư!");
            }
            drugStuffId = stuff.getId();
            drugStuffName = stuff.getName();
            type = 2;
        }
        else
        {
            return;
        }
        String companyId = dataEntity.getCompany() == null ? null : dataEntity.getCompany().getId();
        ensureAggregateRow(drugStuffId, companyId, drugStuffName, type);
    }

    public void delAllStock(DataEntity<?> dataEntity)
    {
        if (!MedicinalStockControlService.enable)
        {
            return;
        }



        List<Parameter> parameters = SearchParamsBuilder.create()
                .eq("drug_stuff_id", dataEntity.getId())
                .build();
        List<MedicinalStockControl> list = super.listAll(parameters, "");


        if (CollectionUtils.isEmpty(list))
        {
            //TODO: tạiBậtđộngTồn khokiểm soátchức năng giờ, củatrước của lịch sửsảnsinhDữ liệuNhư như thế nàoXử lý???   khôngquản lý, còn có taycông việcbùghiDữ liệuĐếnDữ liệukho
            return;
        }else {
            throw new ServiceException("đóThuốcTồn tạiTồn khoKhông thểXóa!");
        }

//        MedicinalStockControl medicinalStockControl = list.get(0);
//        BigDecimal storageStock = medicinalStockControl.getStorageStock();    //Tổng lượng nhập kho
//        BigDecimal surplusStock = medicinalStockControl.getSurplusStock();    //Số lượng tồn kho còn khả dụng
//        if (storageStock.compareTo(surplusStock) != 0)
//        {
//            throw new RuntimeException("độngTồn khokiểm soátĐãphátthay đổi, Không thểhủyXóacơ bảncấu hìnhDữ liệu!");
//        }
//
//        //tổngTồn khokiểm soátDữ liệuđánhtrênhủyXóađánh dấu
//        super.delete(medicinalStockControl);
//
//        //TODO:Có cần xóa chi tiết thao tác tồn kho động không
    }

    @Transactional(readOnly = true)
    public List<MedicinalStockControl> getByDrugOrStuffId(String drugOrStuffId, String companyId) {
        return this.dao.getByDrugOrStuffId(drugOrStuffId,companyId);
    }

    //Truy vấntenantdướiTồn kho
    public List<MedicinalStockControl> listAlls(List<Parameter> parameters, String orderby) {
        PageRequest pageRequest = buildTenantPageRequest(parameters, orderby);

        return dao.listAlls(pageRequest);
    }

    /**
     * chỉTruy vấnnàyPhòng khámdướiTồn kho 2025.4.2
     * @param parameters
     * @param orderby
     * @return
     */
    public List<MedicinalStockControl> listAllByCompany(List<Parameter> parameters, String orderby) {

        PageRequest pageRequest = new PageRequest(parameters, orderby, SessionUtils.getLoginTenantId());

        return dao.listAlls(pageRequest);
    }



    public List<MedicinalStockControl>  listPreAll(List<Parameter> parameters,String orderby) {
        PageRequest pageRequest = buildTenantPageRequest(parameters, orderby);
        return dao.listPreAll(pageRequest);
    }


}