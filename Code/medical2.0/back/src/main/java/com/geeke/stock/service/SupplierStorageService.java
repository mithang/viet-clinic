package com.geeke.stock.service;

import cn.hutool.core.collection.ListUtil;
import com.geeke.common.constants.BizConstants;
import com.geeke.common.data.Page;
import com.geeke.common.service.ServiceException;
import com.geeke.common.sequence.service.SequenceService;
import com.geeke.common.service.CrudService;
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
        // Khi thêm mới, Xử lý trường tự động đánh số
        supplierStorage.setCreateDate(new Date());
        if (StringUtils.isBlank(supplierStorage.getId())){
            String oldCode = supplierStorageDao.getCode(supplierStorage.getCompany().getId());
            supplierStorage.setCode(generateDatePrefixCode(oldCode));
        }
        SupplierStorage supplierStorageTemp = super.save(supplierStorage);
        return supplierStorageTemp;
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
     * Nonexistent ids delegate to super (0 rows, no harm).
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(SupplierStorage entity) {
        if (entity != null && StringUtils.isNotBlank(entity.getId())) {
            SupplierStorage stored = this.get(entity.getId());
            if (stored != null && (stored.getExamine() == null
                    || !BizConstants.SUPPLIER_STORAGE_EXAMINE_VOIDED.equals(stored.getExamine().getValue()))) {
                throw new ServiceException("Phiếu nhập kho chưa hủy, không thể xóa trực tiếp. Hãy hủy phiếu nhập kho trước.");
            }
        }
        return super.delete(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<SupplierStorage> entitys) {
        if (entitys != null) {
            for (SupplierStorage entity : entitys) {
                if (entity != null && StringUtils.isNotBlank(entity.getId())) {
                    SupplierStorage stored = this.get(entity.getId());
                    if (stored != null && (stored.getExamine() == null
                            || !BizConstants.SUPPLIER_STORAGE_EXAMINE_VOIDED.equals(stored.getExamine().getValue()))) {
                        throw new ServiceException("Phiếu nhập kho chưa hủy, không thể xóa trực tiếp. Hãy hủy phiếu nhập kho trước.");
                    }
                }
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
        List<SupplierStorage> supplierStorages = supplierStorageDao.listByCode(companyId, pinyinCode, status, startTime, endTime);
        int total = supplierStorages.size();
        List<SupplierStorage> list = ListUtil.page(offset, limit, supplierStorages);

        return new Page<>(total, list);
    }
}