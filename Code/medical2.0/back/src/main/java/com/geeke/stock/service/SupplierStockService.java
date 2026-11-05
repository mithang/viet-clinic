package com.geeke.stock.service;
import com.geeke.common.constants.BizConstants;

import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.data.SearchParamsBuilder;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.org.entity.Company;
import com.geeke.org.service.CompanyService;
import com.geeke.outpatient.entity.RecipelDetail;
import com.geeke.outpatient.entity.RecipelInfo;
import com.geeke.outpatient.entity.Registration;
import com.geeke.outpatient.service.RecipelDetailService;
import com.geeke.outpatient.service.RecipelInfoService;
import com.geeke.outpatient.service.RegistrationService;
import com.geeke.stock.dao.MedicinalStockControlDao;
import com.geeke.stock.dao.SupplierStockDao;
import com.geeke.stock.entity.*;
import com.geeke.sys.entity.DictItem;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * Tồn kho nhà cung cấpService
 *
 * @author txl
 * @version 2022-06-09
 */

@Service("supplierStockService")
@Transactional(readOnly = true)
public class SupplierStockService extends CrudService<SupplierStockDao, SupplierStock> {

    /** Tồn kho nhà cung cấp là Cơ sởTổng hưởngDữ liệu: Cho phép phòng khám thuộc cùng cơ sở truy cập dữ liệu phòng khám của cơ sở(Với danh sách #{institution} SQL Nhất quán) */
    @Override
    protected boolean isInstitutionShared() {
        return true;
    }


    @Autowired
    private DispensingService dispensingService;

    @Autowired
    private SupplierStockService supplierStockService;

    @Lazy
    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private DrugService drugService;

    @Autowired
    private SupplierStorageService supplierStorageService;

    @Lazy
    @Autowired
    private RecipelInfoService recipelInfoService;

    @Autowired
    private StuffService stuffService;

    @Lazy
    @Autowired
    private RecipelDetailService recipelDetailService;

    @Autowired
    private MedicinalStorageControlService medicinalStorageControlService;

    @Lazy
    @Autowired
    private InventoryVerificationService inventoryVerificationService;

    @Autowired
    private MedicinalStockControlDao medicinalStockControlDao;

    @Autowired
    private MedicinalStockControlService medicinalStockControlService;
    @Autowired
    private CompanyService companyService;
    @Transactional(readOnly = false)
    public void updateStock(DispensingEvt dispensingEvt) {

        Company company = SessionUtils.getUser().getCompany();
        //Đang tiến hành kiểm kê giờKhông thểtiến hành dòngphát thuốcthao tác
        List<InventoryVerification> inventoryVerifications =
                inventoryVerificationService.getByCompanyId(company.getId());
        if ("0".equals(dispensingEvt.getDispensingType())) {
            if (!CollectionUtils.isEmpty(inventoryVerifications)) {
                throw new ServiceException("Đang tiến hành kiểm kê, không thể tiến hành dòngphát thuốcthao tác!");

            }
        } else {
            if (!CollectionUtils.isEmpty(inventoryVerifications)) {
                throw new ServiceException("Đang tiến hành kiểm kê, không thể tiến hành dòngTrả thuốcthao tác!");

            }
        }
        String registrationId = dispensingEvt.getRegistrationId();
        Registration registration = registrationService.get(registrationId);
        if ("0".equals(dispensingEvt.getDispensingType())) {

            //Phát thuốc
            // DS52: claim tung don truoc moi dong ton kho, nguyen tu
            // (UPDATE ... WHERE is_dispension='0', chi 1 luong song song
            // thang claim). Code cu chi kiem tra isDispension SAU khi
            // okOccupyStock, nen cac lan phat song song deu tru kho truoc
            // roi moi bi chan, lam used tang gap N va occupy am.
            for (String recipelInfoId : dispensingEvt.getRecipelInfoIdList()) {
                RecipelInfo recipelInfo = this.recipelInfoService.get(recipelInfoId);
                if (recipelInfo == null || !"0".equals(recipelInfo.getIsDispension())) {
                    throw new ServiceException("Đơn thuốcThông tinKiểm traThất bại, Không tồn tại hoặc Đãphát thuốcqua");
                }
                if (this.recipelInfoService.claimDispensed(recipelInfoId) == 0) {
                    throw new ServiceException("Đơn thuốcThông tinKiểm traThất bại, Không tồn tại hoặc Đãphát thuốcqua");
                }
            }
            for (String recipelInfoId : dispensingEvt.getRecipelInfoIdList()) {
                //tiến hành dòngđộngTồn khochiếm dụng thực tế
                medicinalStorageControlService.okOccupyStock(registrationId, recipelInfoId);
            }


            //Cập nhật trạng thái đơn thuốc
            List<String> recipelInfoIdList = dispensingEvt.getRecipelInfoIdList();
            if (CollectionUtils.isEmpty(recipelInfoIdList)) {
                throw new ServiceException("phát thuốc của Đơn thuốcThông tinKhông thể là trống");
            }

            for (String recipelInfoId : recipelInfoIdList) {
                RecipelInfo recipelInfo = this.recipelInfoService.get(recipelInfoId);
                // DS52: the isDispension check already happened in the claim
                // loop above (which flipped the flag to '1'), so only the
                // missing-row guard stays here; the old !"0" test would
                // always throw now because this same call claimed the row.
                if (recipelInfo == null) {
                    throw new ServiceException("Đơn thuốcThông tinKiểm traThất bại, Không tồn tại hoặc Đãphát thuốcqua");
                }
                recipelInfo.setIsDispension("1");
                recipelInfo.setDispensionDate(new Date());
                recipelInfo.setDispensionStatus(1);
                this.recipelInfoService.save(recipelInfo);
            }


            registration.setDispensingDate(new Date());
        } else {
            //Trả thuốc
            List<Parameter> parameters = SearchParamsBuilder.create()
                    .eq("registration_id", registrationId)
                    .in("recipel_info_id", dispensingEvt.getRecipelInfoIdList())
                    .build();
            List<Dispensing> dispensingList = dispensingService.listAll(parameters, "");
            if (null != dispensingList && !dispensingList.isEmpty()) {
            }
            //Cập nhật trạng thái đơn thuốc
            List<String> recipelInfoIdList = dispensingEvt.getRecipelInfoIdList();
            if (CollectionUtils.isEmpty(recipelInfoIdList)) {
                throw new ServiceException("phát thuốc của Đơn thuốcThông tinKhông thể là trống");
            }
            for (String recipelInfoId : recipelInfoIdList) {
                RecipelInfo recipelInfo = this.recipelInfoService.get(recipelInfoId);
                if (recipelInfo == null || !"1".equals(recipelInfo.getIsDispension())) {
                    throw new ServiceException("Đơn thuốcThông tinKiểm traThất bại, Không tồn tại hoặc ĐãTrả thuốcqua");
                }
                if (BizConstants.RECIPEL_TYPE_INFUSION.equals(recipelInfo.getRecipelType().getValue())) {
                    List<RecipelDetail> recipelDetails = recipelDetailService.getByRecipelInfoId(recipelInfo.getId());
                    for (RecipelDetail recipelDetail :
                            recipelDetails) {
                        if (recipelDetail.getExecutions() != null) {
                            throw new ServiceException("Truyền dịchĐơn thuốcĐãhành nghề dòng, Không thể hoàn phí!");
                        }
                    }
                }
                // DS57: claim tung don truoc drugRepercussion, nguyen tu
                // (UPDATE ... WHERE is_dispension='1', chi 1 luong song
                // song thang claim). Code cu chi kiem tra isDispension
                // SELECT-then-act, nen cac lan tra song song deu doc co
                // "1" truoc khi commit roi moi ap adjustment DS32, lam
                // used am va return_todo_fee tang gap N.
                if (this.recipelInfoService.claimReturned(recipelInfoId) == 0) {
                    throw new ServiceException("Đơn thuốcThông tinKiểm traThất bại, Không tồn tại hoặc ĐãTrả thuốcqua");
                }
                //Trả thuốctạmkhônghoàn phí
                medicinalStorageControlService.drugRepercussion(recipelInfo);

                recipelInfo.setIsDispension("2");
                recipelInfo.setDispensionStatus(-1);
                recipelInfo.setRetreatChargeDate(new Date());
                this.recipelInfoService.save(recipelInfo);
                //Sửaphát thuốcThông tinbảng
                dispensingService.updateDelFlag(recipelInfoId);
            }
            registration.setReturnDate(new Date());
        }
        //Sửaphát thuốcTrạng thái
        registration.setDispensingStatus(dispensingEvt.getDispensingStatus());
        //registration.setDispensingDate(new Date());
        registrationService.save(registration);
    }

    public List<SupplierStock> getByStorageId(String id) {
        // Z1a: ton kho theo storage_id tran — chi giu dong thuoc session
        // tenant (DAO WHERE duy nhat supplier_storage_id). Session rong/
        // "null" giu hanh vi cu; company null giu nhu verifyTenantOwnership.
        List<SupplierStock> rows = this.dao.getByStorageId(id);
        String tenant = SessionUtils.getLoginTenantId();
        if (tenant == null || tenant.isEmpty() || "null".equals(tenant) || rows == null) {
            return rows;
        }
        List<SupplierStock> kept = new ArrayList<>();
        for (SupplierStock row : rows) {
            if (row == null || row.getCompany() == null || row.getCompany().getId() == null
                    || row.getCompany().getId().isEmpty() || tenant.equals(row.getCompany().getId())) {
                kept.add(row);
            }
        }
        return kept;
    }

    @Transactional(readOnly = false)
    public void saves(StorageEvt storageEvt) {
        // Thêm mớiThuốc hoặc Vật tưTồn kho giờ, Nếu là khởi tạo tồn kho thì khởi tạo trước dữ liệu bản ghi kiểm soát tổng tồn kho
        for (SupplierStock entity : storageEvt.getSupplierStockList()) {
            String companyID = entity.getCompany().getId();
            String drugID = entity.getDrug().getId();
            // String drug_stuff_ID = drugID != null ? drugID : stuffID;

            if (null != drugID) {
                List<MedicinalStockControl> inventory = medicinalStockControlDao.inventory(companyID, drugID);
                if (inventory.isEmpty()) {
                    Drug drugTemp = drugService.get(drugID);
                    drugTemp.getCompany().setId(companyID);
                    this.medicinalStockControlService.initStockTo(drugTemp);
                }
            }
            String stuffID = entity.getStuff().getId();
            if (null != stuffID) {
                List<MedicinalStockControl> inventory = medicinalStockControlDao.inventory(companyID, stuffID);
                if (inventory.isEmpty()) {
                    Stuff drugTemp = stuffService.get(stuffID);
                    drugTemp.getCompany().setId(companyID);
                    this.medicinalStockControlService.initStockTo(drugTemp);
                }
            }
        }
    }

    //thônphiếu nhập kho và Tồn kho
    @Transactional(readOnly = false)
    public void savesTo(StorageEvt storageEvt) {
        //trướcLưuphiếu nhập kho
        SupplierStorage supplierStorage = storageEvt.getSupplierStorage();
        supplierStorage.setInitial(supplierStorage.getNumber());
        SupplierStorage save = supplierStorageService.save(supplierStorage);
        if (null != storageEvt.getSupplierStockList() && !storageEvt.getSupplierStockList().isEmpty()) {
            for (SupplierStock entity : storageEvt.getSupplierStockList()) {


                //Lưu số phiếu nhập kho vào từng bảng nhập kho
                entity.setSupplierStorage(save);
                entity.setInitial(entity.getNumber());
                entity.setCancellation("0");
                String id = supplierStockService.saveInternal(entity).getId();
                //Sau khi lưu thành công, lưu tồn kho của mặt hàng đó vào cơ sở dữ liệu tương ứng
//                if(id!=null){
//                    if(entity.getDrug().getId()!=null){
//                        //thìLấytương ứng của ThuốcTồn kho
//                        drugService.updateInventory_3502(entity.getNumber(),entity.getDrug().getId());
//                    }else if (entity.getStuff().getId()!=null){
//                        stuffService.updateInventory_3502(entity.getNumber(),entity.getStuff().getId());
//                    }
//                }
            }

            //độngTồn khokiểm soát
            this.medicinalStorageControlService.addStorageStock(save);
        }
    }


    /*
     * Lấynhập khoThuốc của Trung bìnhgiá nhập*/
    @Transactional
    public BigDecimal getByDrugId(String drugOrStuffId) {
        BigDecimal byDrugOrStuffId = this.dao.getByDrugId(drugOrStuffId);

        return byDrugOrStuffId;
    }

    /**
     * DS27: đếm số dòng nhập kho HIS đã có cho (company, crkmxId) — dùng
     * để chặn ghi trùng khi đồng bộ lại cùng một danh sách HIS. Trả 0 khi
     * thiếu company/crkmxId (dòng HIS cũ không có id thì không dedup được
     * và vẫn nhập như trước).
     */
    public int countImportedHisDetail(String companyId, String hisCrkmxId) {
        if (companyId == null || companyId.isEmpty() || hisCrkmxId == null || hisCrkmxId.isEmpty()) {
            return 0;
        }
        return this.dao.countByHisCrkmxId(companyId, hisCrkmxId);
    }

    /**
     * DS39: a line may only be generic-deleted once its parent import slip
     * is voided. The generic delete only flips the line's del_flag, so
     * deleting a line of a passed slip left its detail storage/surplus
     * live (probe-proven: line del_flag=1, stock still 5/5, agg 10005)
     * AND broke the blessed cancel for good: invalidStorageStock lists
     * lines via listAll, which filters del_flag='0', so it finds no lines
     * and throws, leaving the stock stranded with no repair path at all.
     * Fail-closed: refuse unless the parent slip is already voided
     * (examine _2, stock already zeroed by cancel()); a null examine is
     * refused too, and a live line whose parent cannot be resolved is
     * refused too (stock state unknown). The no-id payload branch
     * (delete by supplierStorage.id) is gated the same way. Nonexistent
     * ids delegate to super (0 rows, no harm).
     */
    private void checkLineDeletable(SupplierStock entity) {
        if (entity == null) {
            return;
        }
        String slipId = null;
        boolean lineExists = false;
        if (StringUtils.isNotBlank(entity.getId())) {
            SupplierStock stored = this.get(entity.getId());
            if (stored != null) {
                lineExists = true;
                if (stored.getSupplierStorage() != null) {
                    slipId = stored.getSupplierStorage().getId();
                }
            }
        } else if (entity.getSupplierStorage() != null) {
            slipId = entity.getSupplierStorage().getId();
        }
        if (lineExists && StringUtils.isBlank(slipId)) {
            throw new ServiceException("Chi tiết nhập kho không xác định được phiếu nhập kho, không thể xóa trực tiếp.");
        }
        if (StringUtils.isNotBlank(slipId)) {
            SupplierStorage slip = supplierStorageService.get(slipId);
            if (slip != null && (slip.getExamine() == null
                    || !BizConstants.SUPPLIER_STORAGE_EXAMINE_VOIDED.equals(slip.getExamine().getValue()))) {
                throw new ServiceException("Chi tiết thuộc phiếu nhập kho chưa hủy, không thể xóa trực tiếp. Hãy hủy phiếu nhập kho trước.");
            }
        }
    }

    @Override
    @Transactional(readOnly = false)
    public int delete(SupplierStock entity) {
        checkLineDeletable(entity);
        return super.delete(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<SupplierStock> entitys) {
        if (entitys != null) {
            for (SupplierStock entity : entitys) {
                checkLineDeletable(entity);
            }
        }
        return super.bulkDelete(entitys);
    }

    /**
     * DS70: generic single save fail-closed 400 (DS50/DS68/DS69 pattern).
     * CrudController exposes POST /stock/supplierStock/save, which fell
     * through to CrudService.save -> doInsert/doUpdate, whose mapper
     * &lt;update&gt; writes number, bid, least_bid, retail_price, all_bid,
     * all_retail_price, initial, cancellation absolutely by id -- quantity
     * AND money, with no freeze, no delta, no stock record and no
     * aggregate adjustment (probe-proven: an AUDITED slip's line 10000 /
     * 200.00 was rewritten to 888 / 8888.88 with the control rows still
     * 10000, and a phantom line 777 was inserted on the same audited
     * slip, both 200; parallel rounds both 200). The frontend has no live
     * UI path through this endpoint (supplierStockForm.vue is imported by
     * no view and no router). Real writes go through inStorage
     * (savesTo) / inStorageByCompany (savesToAudit), which use
     * saveInternal() below. Internal callers use saveInternal(), which
     * keeps the old CrudService.save behaviour (tenant check + audit
     * action).
     */
    @Override
    @Transactional(readOnly = false)
    public SupplierStock save(SupplierStock entity) {
        throw new ServiceException("Chi tiết nhập kho chỉ thay đổi qua phiếu nhập kho, không hỗ trợ lưu trực tiếp!");
    }

    /**
     * DS70: same fail-closed guard for the bulk writes -- CrudService
     * bulkInsert/bulkUpdate call dao.bulk* directly, NOT save(), so the
     * save() override above does not cover them. Note the bulk mapper is
     * ALSO broken today independent of this guard (bulkInsert references
     * #{cancellation} instead of #{entity.cancellation}, bulkUpdate emits
     * malformed CASE SQL -- both 500 for any payload, probe-proven), so
     * the bulk paths were not exploitable; this override makes them
     * fail-closed 400 instead of 500 and keeps them closed if the mapper
     * is ever fixed. No internal caller uses bulk on this service.
     */
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<SupplierStock> entitys) {
        throw new ServiceException("Chi tiết nhập kho chỉ thay đổi qua phiếu nhập kho, không hỗ trợ thao tác hàng loạt!");
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<SupplierStock> entitys) {
        throw new ServiceException("Chi tiết nhập kho chỉ thay đổi qua phiếu nhập kho, không hỗ trợ thao tác hàng loạt!");
    }

    /**
     * DS70: internal single-entity save for the guarded import flows
     * (savesTo / savesToAudit, both INSERTs of fresh import lines).
     * NOT reachable from the controller -- CrudController routes only to
     * save()/bulkInsert()/bulkUpdate(). Delegates to CrudService.save,
     * so the tenant check and the audit action row are kept exactly as
     * before.
     */
    @Transactional(readOnly = false)
    public SupplierStock saveInternal(SupplierStock entity) {
        return super.save(entity);
    }

    @Transactional(readOnly = false)
    public void cancellation(List<SupplierStock> supplierStockList) {
        this.dao.updateCancel(supplierStockList);
    }

    public BigDecimal getByStuffId(String stuffId) {
        BigDecimal supplierStocDecimal = this.dao.getByStuffId(stuffId);
        return supplierStocDecimal;
    }

    @Transactional
    public List<SupplierStock> getByStuffIdDetail(String drugStuffId) {
        List<SupplierStock> supplierStockList = this.dao.getByStuffIdDetail(drugStuffId);
        return supplierStockList;
    }

    public Page<SupplierStock> getDrugIndateWarning(List<Parameter> parameters, int offset, int limit, String
            orderby) {
        // Loại bỏ ở frontend company_id(Ngăn vượt quyền), Thống nhất lấy tenant đăng nhập trong phiên làm ranh giới cách ly
        Parameter.extractAndRemoveCompanyId(parameters);
        String id = SessionUtils.getLoginTenantId();
        if ("null".equals(id)) {
            id = null;
        }
        String institution = companyService.getInstitution(id);

        PageRequest pageRequest = new PageRequest(offset, limit, parameters, orderby, id, institution);
        return paginate(
            () -> this.dao.countWarning(pageRequest),
            () -> {
                List<SupplierStock> list = this.dao.getDrugIndateWarning(pageRequest);
                for (SupplierStock supplierStock : list) {
                    if (!Objects.isNull(supplierStock.getMedicinalStorageControl()) && !Objects.isNull(supplierStock.getMedicinalStorageControl().getStorageStock())) {
                        String number =
                                supplierStock.getMedicinalStorageControl().getStorageStock().subtract(supplierStock.getMedicinalStorageControl().getUsedStock().add(supplierStock.getMedicinalStorageControl().getReimburseStock())).stripTrailingZeros().toPlainString();
                        supplierStock.setNumber(Integer.parseInt(number));
                    } else {
                        supplierStock.setNumber(0);
                    }
                }
                return list;
            }
        );
    }

    public Page<SupplierStock> getStuffIndateWarning(List<Parameter> parameters, int offset, int limit,
                                                     String orderby) {
        // Z2b: cùng mẫu getDrugIndateWarning — strip company_id body, ép tenant session
        Parameter.extractAndRemoveCompanyId(parameters);
        String id = SessionUtils.getLoginTenantId();
        if ("null".equals(id)) {
            id = null;
        }
        String institution = companyService.getInstitution(id);

        PageRequest pageRequest = new PageRequest(offset, limit, parameters, orderby, id, institution);
        return paginate(
            () -> this.dao.countStuffWarning(pageRequest),
            () -> {
                List<SupplierStock> list = this.dao.getStuffIndateWarning(pageRequest);
                for (SupplierStock supplierStock : list) {
                    if (!Objects.isNull(supplierStock.getMedicinalStorageControl()) && !Objects.isNull(supplierStock.getMedicinalStorageControl().getStorageStock())) {
                        String number =
                                supplierStock.getMedicinalStorageControl().getStorageStock().subtract(supplierStock.getMedicinalStorageControl().getUsedStock().add(supplierStock.getMedicinalStorageControl().getReimburseStock())).stripTrailingZeros().toPlainString();
                        supplierStock.setNumber(Integer.parseInt(number));
                    } else {
                        supplierStock.setNumber(0);
                    }
                }
                return list;
            }
        );
    }

    /**
     * nhập khoChờ xét duyệt
     *
     * @param storageEvt
     */
    @Transactional
    public void savesToAudit(StorageEvt storageEvt) {
        //trướcLưuphiếu nhập kho
        SupplierStorage supplierStorage = storageEvt.getSupplierStorage();
        supplierStorage.setInitial(supplierStorage.getNumber());
        SupplierStorage save = supplierStorageService.save(supplierStorage);


        if (null != storageEvt.getSupplierStockList() && !storageEvt.getSupplierStockList().isEmpty()) {
            for (SupplierStock entity : storageEvt.getSupplierStockList()) {

                //Lưu số phiếu nhập kho vào từng bảng nhập kho
                entity.setSupplierStorage(save);
                entity.setInitial(entity.getNumber());
                entity.setCancellation("0");
                supplierStockService.saveInternal(entity);

            }
        }

    }

    /**
     * xét duyệtQuavànhập kho
     *
     * @param storageEvt
     */

    @Transactional
    public void auditStorage(StorageEvt storageEvt) {
        // DS55: claim the slip BEFORE any stock move. The old code loaded the
        // slip, checked examine == PENDING, set examine=PASS, saved, then
        // added stock -- SELECT-then-act with no claim, so N parallel audits
        // of one slip all read PENDING and each added the slip quantity
        // (probe-proven: 8 parallel audits of a slip of 5 left the aggregate
        // at 10025 with 5 op=1 records). claimForAudit flips PENDING -> the
        // passed value in one conditional UPDATE; losers get 0 rows and skip
        // the slip (idempotent approve -- the sequential 2nd audit already
        // returned 200 with no stock change, so skipping preserves the
        // existing contract). A null payload examine / null slip id is a
        // no-op instead of an NPE (the old code NPEd at
        // storage.getExamine().getValue() for an unknown slip id or a slip
        // with no examine state).
        if (storageEvt == null || storageEvt.getSupplierStorage() == null
                || storageEvt.getSupplierStorage().getExamine() == null
                || storageEvt.getSupplierStockList() == null) {
            return;
        }
        DictItem examine = storageEvt.getSupplierStorage().getExamine();
        storageEvt.getSupplierStockList().forEach(stock -> {
            if (stock == null || StringUtils.isBlank(stock.getId())) {
                return;
            }
            SupplierStorage storage = supplierStorageService.get(stock.getId());
            if (storage == null) {
                return;
            }
            if (!supplierStorageService.claimForAudit(storage.getId(), examine.getValue(), new Date())) {
                return;
            }
            //độngTồn khokiểm soát
            this.medicinalStorageControlService.addStorageStock(storage);
        });
    }
}