package com.geeke.stock.service;

import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.stock.dao.SupplierOutboundDao;
import com.geeke.stock.dao.SupplierOutboundDetailDao;
import com.geeke.stock.entity.*;
import com.geeke.sys.entity.DictItem;
import com.geeke.toll.utils.BigdecimalConvert;
import com.geeke.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Xuất khođơnChi tiếtService
 *
 * @author txl
 * @version 2022-06-02/
 */


@Service("supplierOutboundDetailService")
@Transactional(readOnly = true)
public class SupplierOutboundDetailService extends CrudService<SupplierOutboundDetailDao, SupplierOutboundDetail> {
    @Autowired
    private SupplierStockService supplierStockService;

    @Autowired
    private SupplierOutboundService supplierOutboundService;

    @Autowired
    private DrugService drugService;

    @Autowired
    private StuffService stuffService;

    @Autowired
    private SupplierOutboundDetailDao supplierOutboundDetailDao;

    @Autowired
    private MedicinalStorageControlService medicinalStorageControlService;

    /**
     * LưuXuất khođơnChi tiết
     *
     * @param supplierOutboundDetails
     * @return
     */
    @Transactional(readOnly = false)
    public void save(String outboundId, List<SupplierOutboundDetail> supplierOutboundDetails) {
        List<SupplierOutboundDetail> exist = supplierOutboundDetailDao.getByOutboundId(outboundId);

        SupplierOutbound outbound = supplierOutboundService.get(outboundId);
        if (outbound == null) throw new ServiceException("Phiếu xuất kho không tồn tại");

        //Cập nhậtĐãcóChi tiết, tìm raCầnXóa của Chi tiết
        List<SupplierOutboundDetail> willDeleteDetail = new ArrayList<>();
        for (SupplierOutboundDetail d : exist) {
            // DS33: match by id, NOT List.indexOf. BaseEntity /
            // DataEntity / SupplierOutboundDetail define no equals /
            // hashCode, so indexOf is identity comparison between the
            // DB-loaded rows and the request-deserialized rows and is
            // ALWAYS -1: every existing line was treated as "to delete",
            // updateSupplierOutboundDetail was never called, and the add
            // loop below skips request lines that carry an id — so editing
            // a slip silently deleted all its lines while returning 200
            // (probe-proven: edit 5->8 left 0 lines, occupy 0).
            SupplierOutboundDetail updated = null;
            for (SupplierOutboundDetail n : supplierOutboundDetails) {
                if (n.getId() != null && n.getId().equals(d.getId())) {
                    updated = n;
                    break;
                }
            }
            if (updated == null) {
                willDeleteDetail.add(d);//tức làSẽ Xóa
            } else {
                //Cập nhật: pass the REQUEST line (new quantity), not the
                //old DB row — passing the old row made addNumber always 0.
                updateSupplierOutboundDetail(updated);
            }
        }

        //XóaCầnXóa của 
        for (SupplierOutboundDetail d : willDeleteDetail) {
            deleteSupplierOutboundDetail(d.getId());
        }

        //Thêm
        for (SupplierOutboundDetail d : supplierOutboundDetails) {
            d.setSupplierOutbound(outbound);
            if (!StringUtils.isNullOrEmpty(d.getId())) continue;

            //khóachỉ địnhTồn kho
            MedicinalStorageControl medicinalStorageControl = d.getMedicinalStorage();
            MedicinalStorageControl queryStorageControl = medicinalStorageControlService.get(d.getMedicinalStorage().getId());
            if (queryStorageControl == null) {
                throw new ServiceException("độngTồn khoBản ghiKhông tồn tại");
            }

            // DS33: freeze with the atomic guarded bump (occupy +N, surplus
            // untouched — surplus moves only at examine, DS32 invariant).
            // The old Java check (surplus >= N, then an unguarded bump)
            // let two parallel creates of 6000 against surplus 10000 both
            // pass and freeze 12000 total (probe-proven). A refused freeze
            // throws and the whole saveEvt rolls back (ServiceException is
            // a RuntimeException), so no outbound header is left behind.
            int frozen = medicinalStorageControlService.freezeDetailStocks(queryStorageControl, d.getNumber());
            if (frozen != 1) {
                throw new ServiceException("khả dụngTồn khokhôngđủ");
            }

            save(d);
        }
    }

    /**
     * DS38: generic delete khong duoc phep xoa truc tiep mot dong cua phieu
     * xuat kho chua huy. CrudController mo endpoint delete chung, nhung
     * CrudService.delete chi xoa vat ly dong chi tiet ma khong giai phong
     * dong bang ton kho (occupy/freeze_stock) -- dong bang bi ket lai tren
     * dong kho trong khi phieu van cho xet duyet nhung khong con dong nao
     * (probe: xoa dong 3000 cua phieu cho duyet tra 200, dong kho van
     * occupy 3000 / freeze 3000, huy phieu sau do cung khong giai phong
     * vi cancel chi duyet cac dong con lai). Duong xoa dong dung la sua
     * phieu qua saveEvt (goi deleteSupplierOutboundDetail, co giai phong
     * dong bang). Chi cho phep xoa dong khi phieu me da huy (status 3,
     * dong bang da duoc giai phong luc huy) hoac khong tim thay phieu me;
     * phieu cho duyet (1) / da duyet (2) thi fail-closed 400.
     * @Transactional(readOnly = false) tuong minh la bat buoc vi service
     * nay khai bao class-level readOnly = true (mau LesseeService N3d).
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(SupplierOutboundDetail entity) {
        if (entity != null && StringUtils.isNotBlank(entity.getId())) {
            SupplierOutboundDetail stored = this.get(entity.getId());
            if (stored != null && stored.getSupplierOutbound() != null
                    && StringUtils.isNotBlank(stored.getSupplierOutbound().getId())) {
                SupplierOutbound parent = supplierOutboundService.get(stored.getSupplierOutbound().getId());
                if (parent != null && parent.getStatus() != null && parent.getStatus() != 3) {
                    throw new ServiceException("Chi tiết thuộc phiếu xuất kho chưa hủy, không thể xóa trực tiếp. Hãy sửa phiếu xuất kho để xóa dòng.");
                }
            }
        }
        return super.delete(entity);
    }

    /**
     * DS40: generic bulkDelete khong duoc phep xoa truc tiep dong cua
     * phieu xuat kho chua huy. Endpoint bulkDelete chung ton tai
     * (CrudController) nhung SupplierOutboundDetailDao.xml truoc day
     * KHONG co statement bulkDelete nen moi lan goi deu 500 "Invalid
     * bound statement" (probe-proven). Statement moi them (xoa vat ly,
     * mau SysParamConfigDao, bang chi tiet khong co cot del_flag) chi
     * duoc goi qua override nay, giong gate cua delete() o DS38: chi cho
     * phep xoa dong khi phieu me da huy (status 3, dong bang da duoc
     * giai phong luc huy); phieu cho duyet (1) / da duyet (2) / status
     * null thi fail-closed 400. Gate chay truoc super.bulkDelete nen
     * mot list tron (dong cua phieu da huy + dong cua phieu chua huy)
     * bi tu choi nguyen list, khong dong nao bi xoa.
     * @Transactional(readOnly = false) tuong minh la bat buoc vi service
     * nay khai bao class-level readOnly = true (mau DS38/DS39).
     */
    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<SupplierOutboundDetail> entitys) {
        if (entitys != null) {
            for (SupplierOutboundDetail entity : entitys) {
                if (entity != null && StringUtils.isNotBlank(entity.getId())) {
                    SupplierOutboundDetail stored = this.get(entity.getId());
                    if (stored != null && stored.getSupplierOutbound() != null
                            && StringUtils.isNotBlank(stored.getSupplierOutbound().getId())) {
                        SupplierOutbound parent = supplierOutboundService.get(stored.getSupplierOutbound().getId());
                        if (parent != null && (parent.getStatus() == null || parent.getStatus() != 3)) {
                            throw new ServiceException("Chi tiết thuộc phiếu xuất kho chưa hủy, không thể xóa trực tiếp. Hãy hủy phiếu xuất kho trước.");
                        }
                    }
                }
            }
        }
        return super.bulkDelete(entitys);
    }

    /**
     * DS50: bulkInsert/bulkUpdate fail-closed 400 (DS48 SysSeting
     * pattern), NOT new mapper statements. SupplierOutboundDetailDao.
     * xml has no bulkInsert/bulkUpdate statements so every call 500d
     * "Invalid bound statement" (probe-proven), but working statements
     * would be unsafe here: detail bulkInsert bypasses the atomic
     * freeze (DS33), so a line with no freeze lets examine() drive
     * surplus negative; detail bulkUpdate bypasses
     * updateSupplierOutboundDetail's atomic delta-occupy (DS33), so it
     * could change a line quantity with no freeze adjustment. No
     * loop-through-save() exists for this signature either --
     * save(outboundId, list) treats the list as the slip's FULL new
     * line set (lines absent from it are deleted), so it cannot serve
     * a bulk row list. The generic bulk endpoints stay reachable but
     * now answer an intentional 400 instead of an accidental 500. No
     * internal caller uses bulk* on this service (only the generic
     * controller); lines are edited only via saveEvt. bulkDelete keeps
     * its DS40 statement + gate (cancelled slips only).
     * @Transactional(readOnly = false) tuong minh la bat buoc vi service
     * nay khai bao class-level readOnly = true (mau DS38/DS40).
     */
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<SupplierOutboundDetail> entitys) {
        throw new ServiceException("Chi tiết phiếu xuất kho chỉ sửa qua phiếu xuất kho, không hỗ trợ thao tác hàng loạt!");
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<SupplierOutboundDetail> entitys) {
        throw new ServiceException("Chi tiết phiếu xuất kho chỉ sửa qua phiếu xuất kho, không hỗ trợ thao tác hàng loạt!");
    }

    /**
     * XóaXuất khoChi tiết, giải phóngđóng băngTồn kho
     *
     * DS67: claim the line for deletion BEFORE releasing its freeze.
     * The old code read the stored line (plain SELECT, so under
     * REPEATABLE READ a concurrent edit's new number was invisible),
     * released occupy/freeze by that stored number, then deleted the
     * row and ignored the DELETE's affected-row count. A parallel edit
     * 5 -> 8 plus a delete therefore left NO line but occupy/freeze 3
     * (probe-proven phantom freeze no line accounts for), and two
     * parallel deletes both released -5 for one 5-unit line
     * (probe-proven occupy -5). The conditional DELETE lets exactly
     * one caller own the deletion; only that caller releases the
     * freeze. A loser re-reads with getForUpdate (a plain get would
     * return its opening snapshot, DS66 lesson): row gone means the
     * other delete already released it, so this is the sequential
     * no-op -- accept without releasing again; row moved to a
     * different number means a stale delete -- refuse 400 like the
     * DS66 stale edit, and the whole saveEvt rolls back. Claiming the
     * detail row first also puts delete on the same lock order as the
     * DS66 edit claim (detail row, then stock row), so the edit/delete
     * deadlock disappears. A row that still holds the read number
     * after a failed claim cannot happen (the conditional DELETE is a
     * current write); retry once defensively, then refuse as stale.
     *
     * @param detailId
     */
    @Transactional(readOnly = false)
    public void deleteSupplierOutboundDetail(String detailId) {
        SupplierOutboundDetail detail = supplierOutboundDetailDao.get(detailId);
        if (detail == null) {
            throw new ServiceException("Xuất khođơnChi tiếtKhông tồn tại");
        }

        MedicinalStorageControl medicinalStorageControl = medicinalStorageControlService.get(detail.getMedicinalStorage().getId()); //detail.getMedicinalStorage();
        for (int attempt = 0; attempt < 2; attempt++) {
            int deleted = supplierOutboundDetailDao.deleteIfNumber(detailId, detail.getNumber());
            if (deleted == 1) {
                // DS32: atomic detail release (occupy -N). DS36: the same delta is
                // dual-written to freeze_stock (this line's freeze is released).
                medicinalStorageControlService.adjustDetailStocks(medicinalStorageControl,
                        BigDecimal.ZERO, BigDecimal.ZERO, detail.getNumber().negate(),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, detail.getNumber().negate());
                return;
            }
            // DS66: locking re-read -- a plain get() under REPEATABLE READ
            // would return this transaction's opening snapshot (the stale
            // number), not the winning edit/delete's committed value.
            SupplierOutboundDetail fresh = supplierOutboundDetailDao.getForUpdate(detailId);
            if (fresh == null) {
                // Another delete already removed the line and released its
                // freeze: the sequential equivalent is a no-op, so accept
                // without releasing again.
                return;
            }
            if (fresh.getNumber() != null
                    && fresh.getNumber().compareTo(detail.getNumber()) == 0) {
                // Still holds the quantity we read: retry the claim once.
                continue;
            }
            // The line moved to a different quantity: this is a stale
            // delete -- refuse like the sequential guard, instead of
            // releasing a freeze computed from an old value.
            throw new ServiceException("Xuất kho đơn chi tiết đã thay đổi, vui lòng tải lại phiếu rồi sửa lại");
        }
        throw new ServiceException("Xuất kho đơn chi tiết đã thay đổi, vui lòng tải lại phiếu rồi sửa lại");
    }

    /**
     * hủy bỏtất cảXuất khođơnChi tiết, giải phóngTồn kho
     *
     * @param outboundId
     */
    @Transactional(readOnly = false)
    public void cancelSupplierOutboundDetail(String outboundId) {
        List<SupplierOutboundDetail> details = supplierOutboundDetailDao.getByOutboundId(outboundId);
        for (SupplierOutboundDetail detail : details) {
            MedicinalStorageControl medicinalStorageControl = medicinalStorageControlService.get(detail.getMedicinalStorage().getId());//detail.getMedicinalStorage();
            // DS32: atomic detail release (occupy -N). DS36: the same delta
            // is dual-written to freeze_stock (this line's freeze is released).
            medicinalStorageControlService.adjustDetailStocks(medicinalStorageControl,
                    BigDecimal.ZERO, BigDecimal.ZERO, detail.getNumber().negate(),
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, detail.getNumber().negate());
        }
    }

    /**
     * SửaXuất khohạnChi tiết, sửađóng băngSố lượng tồn kho.
     * Takes the REQUEST line (new quantity). DS33 rewrite:
     * the old code took the OLD DB row (so addNumber was always 0), never
     * persisted the new quantity, and its decrease branch was inverted
     * (occupy subtracts a negative delta, i.e. increases; surplus adds a
     * negative delta, i.e. decreases). Now the delta is applied as an
     * atomic occupy-only bump (DS32 invariant: freeze is occupy-only,
     * surplus moves only at examine, so examine cannot double-subtract
     * surplus), guarded when the quantity grows, and the new quantity is
     * saved on the stored row. A refused increase throws and the whole
     * saveEvt rolls back, leaving the line and its freeze untouched.
     *
     * @param detail request line with the new quantity
     */
    @Transactional(readOnly = false)
    public void updateSupplierOutboundDetail(SupplierOutboundDetail detail) {
        SupplierOutboundDetail old = supplierOutboundDetailDao.get(detail.getId());
        if (old == null) throw new ServiceException("Xuất khođơnChi tiếtKhông tồn tại");

        MedicinalStorageControl medicinalStorageControl = medicinalStorageControlService.get(old.getMedicinalStorage().getId());
        BigDecimal addNumber = detail.getNumber().subtract(old.getNumber());//tăngđóng băng của Số lượng
        if (addNumber.compareTo(BigDecimal.ZERO) == 0) {
            // No quantity change: the delta is 0, so there is nothing to
            // freeze; keep the old behaviour of persisting the line as-is.
            old.setNumber(detail.getNumber());
            this.save(old);
            return;
        }
        // DS66: claim the quantity transition BEFORE freezing the delta.
        // The delta above was computed from the stored number just read,
        // so two parallel saveEvt edits of the same line (both 5 -> 8)
        // both froze +3 and left occupy/freeze at 11 for a line of 8
        // (probe-proven). The conditional UPDATE lets exactly one caller
        // move the number; only that caller freezes its delta. A refused
        // freeze still throws and the whole saveEvt rolls back (as
        // before), which also rolls the claim back.
        int claimed = supplierOutboundDetailDao.claimNumber(detail.getId(),
                detail.getNumber(), old.getNumber());
        if (claimed != 1) {
            // DS66: locking re-read -- a plain get() under REPEATABLE READ
            // would return this transaction's opening snapshot (the stale
            // number), not the winning edit's committed value.
            SupplierOutboundDetail fresh = supplierOutboundDetailDao.getForUpdate(detail.getId());
            if (fresh != null && fresh.getNumber() != null
                    && fresh.getNumber().compareTo(detail.getNumber()) == 0) {
                // Another edit already moved the line to exactly the
                // quantity this caller wanted: the sequential equivalent
                // is a delta-0 no-op, so accept without freezing again.
                return;
            }
            // The line moved to a different quantity (or was deleted):
            // this is a stale edit -- refuse like the sequential guard,
            // instead of freezing a delta computed from an old value.
            throw new ServiceException("Xuất kho đơn chi tiết đã thay đổi, vui lòng tải lại phiếu rồi sửa lại");
        }
        int frozen = medicinalStorageControlService.freezeDetailStocks(medicinalStorageControl, addNumber);
        if (frozen != 1) {
            throw new ServiceException("khả dụngTồn khokhôngđủ");
        }
        old.setNumber(detail.getNumber());
        this.save(old);
    }

    /**
     * hủy bỏXuất khođơn
     *
     * @param outboundId
     */
    @Transactional(readOnly = false)
    public List<SupplierOutboundDetail> getByOutboundId(String outboundId) {
        // Z1a: chi tiet phieu xuat theo outbound_id tran — chi giu dong
        // thuoc session tenant (DAO WHERE duy nhat supplier_outbound_id).
        // Chi sua method public dung cho API + duyet phieu; cac luong ghi
        // noi bo goi thang DAO sau khi outbound da qua get() kiem so huu
        // thi khong doi. Session rong/"null" giu hanh vi cu.
        List<SupplierOutboundDetail> rows = supplierOutboundDetailDao.getByOutboundId(outboundId);
        String tenant = com.geeke.utils.SessionUtils.getLoginTenantId();
        if (tenant == null || tenant.isEmpty() || "null".equals(tenant) || rows == null) {
            return rows;
        }
        List<SupplierOutboundDetail> kept = new ArrayList<>();
        for (SupplierOutboundDetail row : rows) {
            if (row == null || row.getCompany() == null || row.getCompany().getId() == null
                    || row.getCompany().getId().isEmpty() || tenant.equals(row.getCompany().getId())) {
                kept.add(row);
            }
        }
        return kept;
    }
}
