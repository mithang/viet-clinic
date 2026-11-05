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
     * XóaXuất khoChi tiết, giải phóngđóng băngTồn kho
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
        // DS32: atomic detail release (occupy -N).
        medicinalStorageControlService.adjustDetailStocks(medicinalStorageControl,
                BigDecimal.ZERO, BigDecimal.ZERO, detail.getNumber().negate(),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        supplierOutboundDetailDao.delete(detail);
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
            // DS32: atomic detail release (occupy -N).
            medicinalStorageControlService.adjustDetailStocks(medicinalStorageControl,
                    BigDecimal.ZERO, BigDecimal.ZERO, detail.getNumber().negate(),
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
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
        if (addNumber.compareTo(BigDecimal.ZERO) != 0) {
            int frozen = medicinalStorageControlService.freezeDetailStocks(medicinalStorageControl, addNumber);
            if (frozen != 1) {
                throw new ServiceException("khả dụngTồn khokhôngđủ");
            }
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
