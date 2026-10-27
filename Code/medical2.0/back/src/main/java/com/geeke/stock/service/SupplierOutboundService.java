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
        supplierOutboundDao.cancel(supplierOutbound);

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

        //TheoChi tiếtvòng lặptrừđiTồn kho
        List<SupplierOutboundDetail> details = supplierOutboundDetailService.getByOutboundId(id);
        for (SupplierOutboundDetail detail : details) {
            //Cập nhậtđộngTồn kho
            MedicinalStorageControl medicinalStorageControl = medicinalStorageControlService.get(detail.getMedicinalStorage().getId()); //detail.getMedicinalStorage();
            medicinalStorageControl.setOccupyStock(medicinalStorageControl.getOccupyStock().subtract(detail.getNumber()));
            medicinalStorageControl.setUsedStock(medicinalStorageControl.getUsedStock().add(detail.getNumber()));
            medicinalStorageControl.setSurplusStock(medicinalStorageControl.getSurplusStock().subtract(detail.getNumber()));
            medicinalStorageControlService.save(medicinalStorageControl);

            //Cập nhậttổngTồn khokiểm soát
            String drugOrStuffId = (detail.getDrug() != null && !StringUtils.isNullOrEmpty(detail.getDrug().getId()))? detail.getDrug().getId() : detail.getStuff().getId();
            String companyId = supplierOutboundService.get(id).getCompany().getId();
            List<MedicinalStockControl> stockControls = medicinalStockControlService.getByDrugOrStuffId(drugOrStuffId,companyId);
            if (stockControls.size() == 0)
                throw new ServiceException("ChưatìmĐếnThuốcVật tưTồn khoBản ghi");
            if (stockControls.size() > 1)
                throw new ServiceException("tìmĐếnnhiềumụcThuốcVật tưTồn khoBản ghi");
            MedicinalStockControl medicinalStockControl = stockControls.get(0);
            medicinalStockControl.setUsedStock(medicinalStockControl.getUsedStock().add(detail.getNumber()));
            medicinalStockControl.setSurplusStock(medicinalStockControl.getSurplusStock().subtract(detail.getNumber()));
            medicinalStockControlService.save(medicinalStockControl);

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
