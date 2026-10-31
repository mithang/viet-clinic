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
            int found = supplierOutboundDetails.indexOf(d);
            if (found < 0) {
                willDeleteDetail.add(d);//tức làSẽ Xóa
            } else {
                //Cập nhật
                updateSupplierOutboundDetail(d);
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

            if (queryStorageControl.getSurplusStock().compareTo(d.getNumber()) < 0) {
                throw new ServiceException("khả dụngTồn khokhôngđủ");
            }
            medicinalStorageControl.setOccupyStock(queryStorageControl.getOccupyStock().add(d.getNumber()));
            medicinalStorageControlService.save(queryStorageControl);

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
        medicinalStorageControl.setOccupyStock(medicinalStorageControl.getOccupyStock().subtract(detail.getNumber()));
        medicinalStorageControlService.save(medicinalStorageControl);

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
            medicinalStorageControl.setOccupyStock(medicinalStorageControl.getOccupyStock().subtract(detail.getNumber()));
            medicinalStorageControlService.save(medicinalStorageControl);
        }
    }

    /**
     * SửaXuất khohạnChi tiết, sửađóng băngSố lượng tồn kho
     *
     * @param detail
     */
    @Transactional(readOnly = false)
    public void updateSupplierOutboundDetail(SupplierOutboundDetail detail) {
        SupplierOutboundDetail old = supplierOutboundDetailDao.get(detail.getId());
        if (old == null) throw new ServiceException("Xuất khođơnChi tiếtKhông tồn tại");

        MedicinalStorageControl medicinalStorageControl = medicinalStorageControlService.get(detail.getMedicinalStorage().getId());//old.getMedicinalStorage();
        BigDecimal addNumber = detail.getNumber().subtract(old.getNumber());//tăngđóng băng của Số lượng
        if (addNumber.compareTo(BigDecimal.ZERO) > 0) {
            if (addNumber.compareTo(medicinalStorageControl.getSurplusStock()) > 0) {
                throw new ServiceException("khả dụngTồn khokhôngđủ");
            }
            medicinalStorageControl.setOccupyStock(medicinalStorageControl.getOccupyStock().add(addNumber));
            medicinalStorageControl.setSurplusStock(medicinalStorageControl.getSurplusStock().subtract(addNumber));
        } else {
            medicinalStorageControl.setOccupyStock(medicinalStorageControl.getOccupyStock().subtract(addNumber));
            medicinalStorageControl.setSurplusStock(medicinalStorageControl.getSurplusStock().add(addNumber));
        }
        medicinalStorageControlService.save(medicinalStorageControl);
    }

    /**
     * hủy bỏXuất khođơn
     *
     * @param outboundId
     */
    @Transactional(readOnly = false)
    public List<SupplierOutboundDetail> getByOutboundId(String outboundId) {
        return supplierOutboundDetailDao.getByOutboundId(outboundId);
    }
}
