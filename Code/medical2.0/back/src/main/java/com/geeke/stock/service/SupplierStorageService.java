package com.geeke.stock.service;

import cn.hutool.core.collection.ListUtil;
import com.geeke.common.constants.BizConstants;
import com.geeke.common.data.Page;
import com.geeke.common.service.ServiceException;
import com.geeke.common.sequence.service.SequenceService;
import com.geeke.common.service.CrudService;
import com.geeke.org.entity.Company;
import com.geeke.stock.dao.SupplierStorageDao;
import com.geeke.stock.entity.SupplierStock;
import com.geeke.stock.entity.SupplierStorage;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;

/**
 * Phiếu nhập khoService
 * @author txl
 * @version 2022-06-02
 */
 
@Service("supplierStorageService")
@Transactional(readOnly = true)
public class SupplierStorageService extends CrudService<SupplierStorageDao, SupplierStorage>{
    @Autowired
    SequenceService sequenceService;

    @Autowired
    private SupplierStockService supplierStockService;

    @Autowired
    private DrugService drugService;

    @Autowired
    private StuffService stuffService;

    @Autowired
    private SupplierStorageDao supplierStorageDao;

    @Autowired
    private MedicinalStorageControlService medicinalStorageControlService;

    @Override
    @Transactional(readOnly = false)
    public SupplierStorage save(SupplierStorage supplierStorage) {
        // DS230: ep company theo session khi trong truoc moi ghi (mau
        // DS229 TollInfo.save). bulkInsert da goi
        // forceSessionCompanyWhenBlank nhung save() khong: company
        // {"id": ""} roi toi INSERT thi DB nem 500 (company_id BIGINT
        // khong nhan ''), company {"id": null} thi ghi company_id NULL
        // roi an khoi moi doc loc tenant (listByCode/listAll). Chi dien
        // khi trong, verifyTenantOwnership o super.save van chan
        // company la; dat truoc getCode vi getCode deref company id.
        forceSessionCompanyWhenBlank(supplierStorage);
        // DS122 B: id khong ton tai truoc day roi qua super.save -> doUpdate
        // 0 dong roi van 200 silent no-op (lop DS94/DS99); chan truoc moi ghi.
        if (StringUtils.isNotBlank(supplierStorage.getId())
                && this.get(supplierStorage.getId()) == null) {
            throw new ServiceException("Phiếu nhập kho không tồn tại: "
                    + supplierStorage.getId());
        }
        // Khi thêm mới, Xử lý trường tự động đánh số
        supplierStorage.setCreateDate(new Date());
        if (StringUtils.isBlank(supplierStorage.getId())){
            String oldCode = supplierStorageDao.getCode(supplierStorage.getCompany().getId());
            supplierStorage.setCode(generateDatePrefixCode(oldCode));
        }
        // DS149 B: chan do dai truoc moi ghi (mau DS148 A). bulk* bypass
        // @Valid nen truong co @Length van 500 o mapper khi qua bulk
        // (code/withGoodsNo/invoiceNumber 64); truong KHONG co @Length:
        // examine .value -> varchar(64), type -> varchar(1), remarks ->
        // varchar(255). Gioi han theo cot DB; null-safe.
        checkSupplierStorageLengths(supplierStorage);
        // DS211: chan so am truoc moi ghi (xem checkSupplierStorageNonNegative).
        checkSupplierStorageNonNegative(supplierStorage);
        SupplierStorage supplierStorageTemp = super.save(supplierStorage);
        return supplierStorageTemp;
    }

    // DS211: phieu nhap kho khong the nhap -N mat hang / -N tien.
    // number/breed/allBid/allRetailPrice khong co @Min/@Positive va
    // khong co guard o service: save/bulkInsert/bulkUpdate deu chap
    // nhan so am va ghi thang vao DB (probe: number=-5, breed=-1,
    // allRetailPrice=-200 qua save + bulkInsert + bulkUpdate deu 100
    // va persist; allBid am thi cot `bigint unsigned` chan o DB nen
    // van 400, nhung guard o day cho message ro rang thay vi loi DB).
    // UI that chi gui so >= 0 (breed = do dai bang chi tiet, number /
    // tong tien cong tu chi tiet). 0 van cho phep, null van cho phep
    // (cac cot nullable, UI tinh mot so truong sau).
    // DS258: initial (ton kho ban dau, int) cung khong co guard --
    // mapper ghi thang ca 4 duong (insert/update/bulkInsert/bulkUpdate),
    // cot int co dau nen DB chap nhan initial=-5 (probe 8/8). Luong
    // that luon dat initial = number >= 0 (SupplierStockService.savesTo
    // :351/:644), khong luong hop le nao viet initial am; initial am
    // con pha guard huy phieu o client (initial > initialNum moi chan
    // huy khi ton kho da dung -- am luon lot). Chan < 0 o day, 0/absent
    // van qua (int primitive mac dinh 0).
    private static void checkSupplierStorageNonNegative(SupplierStorage entity) {
        if (entity == null) {
            return;
        }
        if (entity.getInitial() < 0) {
            throw new ServiceException("Số lượng tồn kho ban đầu không được âm");
        }
        if (entity.getNumber() != null && entity.getNumber() < 0) {
            throw new ServiceException("Số lượng nhập kho không được âm");
        }
        if (entity.getBreed() != null && entity.getBreed() < 0) {
            throw new ServiceException("Số chủng loại nhập kho không được âm");
        }
        if (entity.getAllBid() != null
                && entity.getAllBid().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new ServiceException("Tổng giá nhập không được âm");
        }
        if (entity.getAllRetailPrice() != null
                && entity.getAllRetailPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new ServiceException("Tổng giá bán lẻ không được âm");
        }
    }

    // DS149 B: xem ghi chu o save().
    private static void checkSupplierStorageLengths(SupplierStorage entity) {
        if (entity == null) {
            return;
        }
        checkLength(entity.getCode(), 64, "Phiếu nhập kho Độ dài phải nằm trong khoảng 0 đến 64");
        checkLength(entity.getWithGoodsNo(), 64, "theo hàng đơn Độ dài phải nằm trong khoảng 0 đến 64");
        checkLength(entity.getInvoiceNumber(), 64, "Số hóa đơn Độ dài phải nằm trong khoảng 0 đến 64");
        checkLength(entity.getType(), 1, "nhập kho Loại Độ dài phải nằm trong khoảng 0 đến 1");
        checkLength(entity.getRemarks(), 255, "Ghi chú Độ dài phải nằm trong khoảng 0 đến 255");
        checkDictLength(entity.getExamine(), 64, "Trạng thái xét duyệt Độ dài phải nằm trong khoảng 0 đến 64");
    }

    private static void checkDictLength(com.geeke.sys.entity.DictItem item, int max, String message) {
        if (item != null) {
            checkLength(item.getValue(), max, message);
        }
    }

    private static void checkLength(String value, int max, String message) {
        if (value != null && value.length() > max) {
            throw new ServiceException(message);
        }
    }

    /**
     * DS122 B: bulk* fail-closed 400. bulkInsert/bulkUpdate khong qua
     * save() nen khong co @Valid (CrudController bulk* khong @Valid):
     * dong thieu supplier -> 500 NOT NULL (supplier_id NOT NULL),
     * id ma -> 200 silent no-op, [null] -> NPE 500. Validate toan bo list
     * truoc moi ghi: null/empty/[null] -> 400; supplier (bat buoc tren
     * entity) -> 400; bulkUpdate/bulkDelete id khong ton tai -> 400
     * (kiem tra stored-row tenant). Company rong -> ep theo session
     * (mau BookingRequest); company la de guard generic cua
     * super.bulk* chan 400.
     */
    private void checkSupplierStorageRequired(SupplierStorage entity) {
        if (entity == null) {
            throw new ServiceException("Phiếu nhập kho không được để trống");
        }
        if (entity.getSupplier() == null
                || StringUtils.isBlank(entity.getSupplier().getId())) {
            throw new ServiceException("Nhà cung cấp không được để trống");
        }
    }

    private void forceSessionCompanyWhenBlank(SupplierStorage entity) {
        if (entity.getCompany() != null
                && StringUtils.isNotBlank(entity.getCompany().getId())) {
            return;
        }
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return;
        }
        Company company = new Company();
        company.setId(tenantId);
        entity.setCompany(company);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<SupplierStorage> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách phiếu nhập kho không được để trống");
        }
        for (SupplierStorage entity : entitys) {
            checkSupplierStorageRequired(entity);
            checkSupplierStorageLengths(entity);
            checkSupplierStorageNonNegative(entity); // DS211
            forceSessionCompanyWhenBlank(entity);
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<SupplierStorage> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách phiếu nhập kho không được để trống");
        }
        for (SupplierStorage entity : entitys) {
            if (entity == null || StringUtils.isBlank(entity.getId())) {
                throw new ServiceException("Phiếu nhập kho không được để trống");
            }
            if (this.get(entity.getId()) == null) {
                throw new ServiceException("Phiếu nhập kho không tồn tại: "
                        + entity.getId());
            }
            verifyTenantOwnershipAgainstStoredRow(entity);
            checkSupplierStorageRequired(entity);
            checkSupplierStorageLengths(entity);
            checkSupplierStorageNonNegative(entity); // DS211
        }
        return super.bulkUpdate(entitys);
    }

    /**
     * DS39: generic delete must not bypass the blessed cancel path. The
     * generic CrudController delete only flips del_flag, so deleting a
     * passed/pending import slip left its detail storage/surplus and the
     * aggregate counting it (probe-proven: slip del_flag=1, stock still
     * 5/5, agg 10005) while the slip vanished from every list -- phantom
     * inventory no UI flow can reach again (only an API cancel with the
     * known id could still repair it). Fail-closed: refuse unless the
     * slip is already voided (examine _2, stock already zeroed by
     * cancel()); a null examine is refused too (stock state unknown).
     * DS123 A: id rong/id ma/dong da xoa mem -> 400 truoc guard DS39
     * (truoc day id ma roi qua super.delete -> 0 dong roi van 200).
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(SupplierStorage entity) {
        // DS123 A: id rong/id ma/dong da xoa mem -> 400 truoc guard DS39
        // (truoc day id ma roi qua super.delete -> 0 dong roi van 200).
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được phiếu nhập kho cần xóa.");
        }
        SupplierStorage stored = this.get(entity.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Phiếu nhập kho không tồn tại: "
                    + entity.getId());
        }
        if (stored.getExamine() == null
                || !BizConstants.SUPPLIER_STORAGE_EXAMINE_VOIDED.equals(stored.getExamine().getValue())) {
            throw new ServiceException("Phiếu nhập kho chưa hủy, không thể xóa trực tiếp. Hãy hủy phiếu nhập kho trước.");
        }
        return super.delete(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<SupplierStorage> entitys) {
        // DS122 B: null/empty/[null]/id rong/id ma -> 400 truoc moi ghi
        // (truoc day id ma roi qua super.bulkDelete -> 0 dong roi van 200).
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách phiếu nhập kho không được để trống");
        }
        for (SupplierStorage entity : entitys) {
            if (entity == null || StringUtils.isBlank(entity.getId())) {
                throw new ServiceException("Phiếu nhập kho không được để trống");
            }
            SupplierStorage stored = this.get(entity.getId());
            if (stored == null) {
                throw new ServiceException("Phiếu nhập kho không tồn tại: "
                        + entity.getId());
            }
            verifyTenantOwnershipAgainstStoredRow(entity);
            if (stored.getExamine() == null
                    || !BizConstants.SUPPLIER_STORAGE_EXAMINE_VOIDED.equals(stored.getExamine().getValue())) {
                throw new ServiceException("Phiếu nhập kho chưa hủy, không thể xóa trực tiếp. Hãy hủy phiếu nhập kho trước.");
            }
        }
        return super.bulkDelete(entitys);
    }

    /**
     * DS55: atomically claim a slip for approval (mirror of the DS34 void
     * claim). Returns true only when the slip exists and was still PENDING;
     * the conditional UPDATE flips it to the passed value in the same
     * statement, so exactly one of N parallel audits wins the slip.
     */
    @Transactional(readOnly = false)
    public boolean claimForAudit(String id, String passedValue, Date examineDate) {
        SupplierStorage stored = this.get(id);
        if (stored == null) {
            return false;
        }
        stored.preUpdate();
        int claimed = this.dao.claimAuditExamine(id, passedValue,
                BizConstants.SUPPLIER_STORAGE_EXAMINE_PENDING, examineDate,
                stored.getUpdateBy(), stored.getUpdateDate());
        return claimed == 1;
    }

    @Transactional(readOnly = false)
    public void cancel(String id) {
        //Trước khi hủy, kiểm tra trước thuốc hoặc vật tư đó có đang được sử dụng không
        SupplierStorage supplierStorage = this.get(id);
        // DS14: get() tra null khi id khong ton tai (Outbound cancel
        // co kiem tra tuong tu); khong co kiem tra nay thi setExamine
        // duoi day NPE -> 500. Fail-closed 400, khong ghi gi.
        if (supplierStorage == null) {
            throw new ServiceException("Phiếu nhập kho không tồn tại");
        }
        // DS34: a voided slip must not be voided again. The old code had
        // no status guard at all (unlike the outbound cancel, which throws
        // on status 3/2): a sequential second cancel was accepted and wrote
        // a duplicate zero op=-1 stock record (probe-proven).
        if (supplierStorage.getExamine() != null
                && BizConstants.SUPPLIER_STORAGE_EXAMINE_VOIDED.equals(supplierStorage.getExamine().getValue())) {
            throw new ServiceException("Phiếu nhập kho đã hủy, không thể hủy lặp lại");
        }
        // DS34: atomically claim the slip for voiding. The sequential guard
        // above is read-then-act, so 8 parallel cancels all read PASS and
        // all ran invalidStorageStock, each decrementing the aggregate by
        // the slip quantity (probe-proven: aggregate 9965 vs detail 10000
        // for one slip of 5). The conditional UPDATE lets exactly one
        // caller win; losers get 0 rows and throw, and the transaction
        // rolls back before any stock moves. Replaces the old unconditional
        // setExamine + this.save(supplierStorage) (same columns written).
        supplierStorage.preUpdate();
        int claimed = this.dao.voidExamine(id, BizConstants.SUPPLIER_STORAGE_EXAMINE_VOIDED,
                new Date(), supplierStorage.getUpdateBy(), supplierStorage.getUpdateDate());
        if (claimed != 1) {
            throw new ServiceException("Phiếu nhập kho đã hủy, không thể hủy lặp lại");
        }
        //hủysauCầnSẽ tương ứng của Chi tiếtbảnghủyrơi
        List<SupplierStock> byStorageId = supplierStockService.getByStorageId(id);

        supplierStockService.cancellation(byStorageId);

        //gọiđộngTồn khokiểm soát
        this.medicinalStorageControlService.invalidStorageStock(supplierStorage);

        // hủytrừTồn kho - Đãchuyển đổiđếnđộngTồn khokiểm soátmáychế
    }


    public Page<SupplierStorage> listByCode(String pinyinCode, String status, String startTime, String endTime, int limit, int offset) {

        String companyId = SessionUtils.getUserDto().getCompanyId();
        // DS201: escape LIKE wildcards in pinyinCode (mirror DS200) —
        // "%" must match literally, not every stocked drug/stuff.
        pinyinCode = StringUtils.escapeLike(pinyinCode);
        List<SupplierStorage> supplierStorages = supplierStorageDao.listByCode(companyId, pinyinCode, status, startTime, endTime);
        int total = supplierStorages.size();
        List<SupplierStorage> list = ListUtil.page(offset, limit, supplierStorages);

        return new Page<>(total, list);
    }
}