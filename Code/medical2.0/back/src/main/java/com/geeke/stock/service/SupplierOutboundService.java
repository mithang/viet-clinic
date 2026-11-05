package com.geeke.stock.service;

import com.geeke.admin.entity.User;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.stock.dao.SupplierOutboundDao;
import com.geeke.stock.entity.*;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 * Phiếu xuất khoService
 *
 * @author txl
 * @version 2022-06-02/
 */


@Service("supplierOutboundService")
@Transactional(readOnly = true)
public class SupplierOutboundService extends CrudService<SupplierOutboundDao, SupplierOutbound> {
    @Autowired
    private SupplierStockService supplierStockService;

    @Autowired
    private DrugService drugService;

    @Autowired
    private StuffService stuffService;

    @Autowired
    private SupplierOutboundDao supplierOutboundDao;

    @Autowired
    private SupplierOutboundDetailService supplierOutboundDetailService;

    @Autowired
    private MedicinalStorageControlService medicinalStorageControlService;

    @Autowired
    private MedicinalStockControlService medicinalStockControlService;

    @Autowired
    private MedicinalStockRecordService medicinalStockRecordService;

    @Autowired
    private SupplierOutboundService supplierOutboundService;
    /**
     * LưuXuất khođơn
     *
     * @param supplierOutbound
     * @return
     */
    //@Override
    @Transactional(readOnly = false)
    public SupplierOutbound save(OutboundEvt outboundEvt) {
        if(ObjectUtils.isEmpty(outboundEvt.getSupplierOutbound())==false && ObjectUtils.isEmpty(outboundEvt.getSupplierOutbound().getId())==false) {
            SupplierOutbound old = this.get(outboundEvt.getSupplierOutbound().getId());
            if (old == null)
                throw new ServiceException("Phiếu xuất kho không tồn tại");
            if (old.getStatus() == 2)
                throw new ServiceException("Xuất khođơnĐãxét duyệt, Không thểSửa");
            if (old.getStatus() == 3)
                throw new ServiceException("Xuất khođơnĐãhủy, Không thểSửa");
        }

        // Khi thêm mới, Xử lý trường tự động đánh số
        SupplierOutbound supplierOutbound = outboundEvt.getSupplierOutbound();
        supplierOutbound.setCreateDate(new Date());
        supplierOutbound.setExamineBy(null);
        supplierOutbound.setStatus(1);//Chờ xét duyệt

        if (StringUtils.isBlank(supplierOutbound.getId())) {
            String oldOutbound = supplierOutboundDao.getCode(supplierOutbound.getCompany().getId());
            supplierOutbound.setCode(generateDatePrefixCode(oldOutbound));
        }
        SupplierOutbound supplierOutboundTemp = super.save(supplierOutbound);

        //LưuChi tiết
        supplierOutboundDetailService.save(supplierOutboundTemp.getId(), outboundEvt.getSupplierOutboundDetailList());

        return supplierOutboundTemp;
    }

    /**
     * DS40: generic delete khong duoc phep xoa truc tiep mot phieu xuat
     * kho chua huy. CrudController mo endpoint delete/bulkDelete chung,
     * nhung SupplierOutboundDao.xml truoc day KHONG co statement delete /
     * bulkDelete nen moi lan goi deu 500 "Invalid bound statement"
     * (probe-proven, DS38/DS39). Statement moi them (mau
     * SupplierStorageDao, xoa mem del_flag=1) chi duoc goi qua override
     * nay: chi cho phep xoa khi phieu da huy (status 3, dong bang da
     * duoc giai phong luc huy) hoac khong tim thay phieu; phieu cho duyet
     * (1) / da duyet (2) / status null thi fail-closed 400. Duong huy
     * chuan cancel() khong doi.
     * @Transactional(readOnly = false) tuong minh la bat buoc vi service
     * nay khai bao class-level readOnly = true (mau DS38/DS39).
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(SupplierOutbound entity) {
        if (entity != null && StringUtils.isNotBlank(entity.getId())) {
            SupplierOutbound stored = this.get(entity.getId());
            if (stored != null && (stored.getStatus() == null || stored.getStatus() != 3)) {
                throw new ServiceException("Phiếu xuất kho chưa hủy, không thể xóa trực tiếp. Hãy hủy phiếu xuất kho trước.");
            }
        }
        return super.delete(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<SupplierOutbound> entitys) {
        if (entitys != null) {
            for (SupplierOutbound entity : entitys) {
                if (entity != null && StringUtils.isNotBlank(entity.getId())) {
                    SupplierOutbound stored = this.get(entity.getId());
                    if (stored != null && (stored.getStatus() == null || stored.getStatus() != 3)) {
                        throw new ServiceException("Phiếu xuất kho chưa hủy, không thể xóa trực tiếp. Hãy hủy phiếu xuất kho trước.");
                    }
                }
            }
        }
        return super.bulkDelete(entitys);
    }

    /**
     * hủy bỏXuất khođơn
     *
     * @param id
     */
    @Transactional(readOnly = false)
    public void cancel(String id) {
        //Trước khi hủy, kiểm tra trước thuốc hoặc vật tư đó có đang được sử dụng không
        SupplierOutbound supplierOutbound = this.get(id);
        if (supplierOutbound == null)
            throw new ServiceException("Phiếu xuất kho không tồn tại");
        if (supplierOutbound.getStatus() == 3)
            throw new ServiceException("Xuất khođơnĐãhủy, Không thểtrùng lặpthao tác");
        if (supplierOutbound.getStatus() == 2)
            throw new ServiceException("Xuất khođơnĐãxét duyệt, Không thểhủy");
        supplierOutbound.setStatus(3);
        //super.save(supplierOutbound);
        supplierOutbound.setCancelDate(new Date());
        // DS34: atomic cancel claim. The status checks above are
        // read-then-act: 8 parallel cancels of one slip of 5 had 5 accepted
        // and released the freeze 5 times (detail occupy -20, probe-proven).
        // The conditional UPDATE lets exactly one caller win; losers get 0
        // rows and throw before any stock is released, and the transaction
        // rolls back.
        int claimed = supplierOutboundDao.cancel(supplierOutbound);
        if (claimed != 1) {
            SupplierOutbound fresh = this.get(id);
            if (fresh != null && fresh.getStatus() == 3) {
                throw new ServiceException("Xuất khođơnĐãhủy, Không thểtrùng lặpthao tác");
            }
            if (fresh != null && fresh.getStatus() == 2) {
                throw new ServiceException("Xuất khođơnĐãxét duyệt, Không thểhủy");
            }
            throw new ServiceException("Xuất khođơnĐãhủy, Không thểtrùng lặpthao tác");
        }

        //giải phóngTồn kho
        supplierOutboundDetailService.cancelSupplierOutboundDetail(id);
    }

    /**
     * xét duyệtXuất khođơn
     *
     * @param id
     */
    @Transactional(readOnly = false)
    public void examine(String id) {
        //Trước khi hủy, kiểm tra trước thuốc hoặc vật tư đó có đang được sử dụng không
        SupplierOutbound supplierOutbound = this.get(id);
        if (supplierOutbound == null)
            throw new ServiceException("Phiếu xuất kho không tồn tại");
        if (supplierOutbound.getStatus() == 3)
            throw new ServiceException("Xuất khođơnĐãhủy bỏ, Không thểxét duyệt");
        if (supplierOutbound.getStatus() == 2)
            throw new ServiceException("Xuất khođơnKhông thểtrùng lặpxét duyệt");

        // DS34: atomically claim the slip for examining BEFORE any stock
        // move. The status checks above are read-then-act and status was
        // only set to 2 at the end (examinee), so 8 parallel examines of
        // one slip of 5 all applied the confirm move (detail used 40,
        // surplus 9960, occupy -35, probe-proven). The conditional UPDATE
        // lets exactly one caller win; losers get 0 rows and throw before
        // touching stock, and the transaction rolls the claim back if a
        // later move throws. examinee below still writes examine_by/date
        // and re-asserts status 2.
        int claimed = supplierOutboundDao.claimExamine(id, 2);
        if (claimed != 1) {
            SupplierOutbound fresh = this.get(id);
            if (fresh != null && fresh.getStatus() == 3) {
                throw new ServiceException("Xuất khođơnĐãhủy bỏ, Không thểxét duyệt");
            }
            if (fresh != null && fresh.getStatus() == 2) {
                throw new ServiceException("Xuất khođơnKhông thểtrùng lặpxét duyệt");
            }
            throw new ServiceException("Xuất khođơnKhông thểtrùng lặpxét duyệt");
        }

        //TheoChi tiếtvòng lặptrừđiTồn kho
        List<SupplierOutboundDetail> details = supplierOutboundDetailService.getByOutboundId(id);
        for (SupplierOutboundDetail detail : details) {
            //Cập nhậtđộngTồn kho
            MedicinalStorageControl medicinalStorageControl = medicinalStorageControlService.get(detail.getMedicinalStorage().getId()); //detail.getMedicinalStorage();
            // DS32: atomic detail confirm (occupy -N, used +N, surplus -N).
            // Was read-modify-write via save(): 8 parallel examines left
            // the shared detail row at used 5 instead of 40, probe-proven.
            // DS36: the same occupy delta is dual-written to freeze_stock
            // (the freeze converts to used).
            medicinalStorageControlService.adjustDetailStocks(medicinalStorageControl,
                    BigDecimal.ZERO, detail.getNumber().negate(), detail.getNumber().negate(), detail.getNumber(),
                    BigDecimal.ZERO, BigDecimal.ZERO, detail.getNumber().negate());

            //Cập nhậttổngTồn khokiểm soát
            String drugOrStuffId = (detail.getDrug() != null && !StringUtils.isNullOrEmpty(detail.getDrug().getId()))? detail.getDrug().getId() : detail.getStuff().getId();
            String companyId = supplierOutboundService.get(id).getCompany().getId();
            List<MedicinalStockControl> stockControls = medicinalStockControlService.getByDrugOrStuffId(drugOrStuffId,companyId);
            if (stockControls.size() == 0)
                throw new ServiceException("ChưatìmĐếnThuốcVật tưTồn khoBản ghi");
            if (stockControls.size() > 1)
                throw new ServiceException("tìmĐếnnhiềumụcThuốcVật tưTồn khoBản ghi");
            MedicinalStockControl medicinalStockControl = stockControls.get(0);
            // DS32: atomic aggregate confirm (used +N, surplus -N).
            medicinalStockControlService.adjustStocks(medicinalStockControl,
                    BigDecimal.ZERO, detail.getNumber().negate(), BigDecimal.ZERO, detail.getNumber(),
                    BigDecimal.ZERO, BigDecimal.ZERO);

            //LưuTồn khokiểm soátBản ghi
            MedicinalStockRecord medicinalStockRecord = new MedicinalStockRecord();
            medicinalStockRecord.setOperationType(6);
            medicinalStockRecord.setOperationStock(detail.getNumber());
            medicinalStockRecord.setCompany(medicinalStorageControl.getCompany());
            medicinalStockRecord.setDrugStuffId(medicinalStorageControl.getDrugStuffId());
            medicinalStockRecord.setStorageId(medicinalStorageControl.getStorageId());
            if (detail.getDrug() != null && Objects.equals("", detail.getDrug().getId())) {
                medicinalStockRecord.setType(1);
            } else {
                medicinalStockRecord.setType(2);
            }
            medicinalStockRecord.setDrugStuffName(medicinalStorageControl.getDrugStuffName());
            medicinalStockRecordService.save(medicinalStockRecord);
        }

        supplierOutbound.setStatus(2);
        //super.save(supplierOutbound);
        User loginUser= SessionUtils.getUser();
        supplierOutbound.setExamineBy(loginUser.getName());
        supplierOutbound.setExamineDate(new Date());
        supplierOutboundDao.examinee(supplierOutbound);
    }

}
