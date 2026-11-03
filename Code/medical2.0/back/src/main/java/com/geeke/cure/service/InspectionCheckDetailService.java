package com.geeke.cure.service;

import java.util.List;
import java.util.Map;

import com.geeke.cure.entity.InspectionCheckDetailCostItem;
import com.geeke.treatment.entity.CostItem;
import com.geeke.treatment.entity.CostItemPackage;
import com.geeke.treatment.service.CostItemService;
import com.geeke.treatment.service.impl.CostItemPackageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import com.geeke.cure.dao.InspectionCheckDetailDao;
import com.geeke.cure.entity.InspectionCheckDetail;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Maps;

/**
 * Chi tiết xét nghiệm và kiểm traService
 * @author rys
 * @version 2022-10-19
 */
 
@Service("inspectionCheckDetailService")
@Transactional(readOnly = true)
public class InspectionCheckDetailService extends CrudService<InspectionCheckDetailDao, InspectionCheckDetail>{

    @Autowired
    private CostItemService costItemService;

    @Autowired
    private CostItemPackageService costItemPackageService;
    @Transactional
    public void allSave(InspectionCheckDetail inspectionCheckDetail) {
        this.dao.insert(inspectionCheckDetail);
    }

    public List<InspectionCheckDetail> getByInfoId(String infoId) {
        List<InspectionCheckDetail> inspectionCheckDetails = this.dao.getByInfoId(infoId);
        for (InspectionCheckDetail inspectionCheckDetail : inspectionCheckDetails) {
            CostItem costItem = costItemService.get(inspectionCheckDetail.getCostItem().getId());
            List<CostItemPackage> all = costItemPackageService.getAll(costItem.getId());
            costItem.setCostItemPackages(all);
            inspectionCheckDetail.setCostItem(costItem);
        }
        return inspectionCheckDetails;
    }

    public List<InspectionCheckDetailCostItem> getByInfoId2(String infoId) {
        List<InspectionCheckDetailCostItem> byInfoId2 = this.dao.getByInfoId2(infoId);
//        for (InspectionCheckDetailCostItem inspectionCheckDetail : byInfoId2) {
//            CostItem costItem = costItemService.get(inspectionCheckDetail.getCostItem().getId());
//            List<CostItemPackage> all = costItemPackageService.getAll(costItem.getId());
//            costItem.setCostItemPackages(all);
//            inspectionCheckDetail.setCostItem(costItem);
//        }

        return byInfoId2;
    }

    @Transactional(readOnly = false)
    public void update(InspectionCheckDetail inspectionCheckDetail) {
        this.dao.update(inspectionCheckDetail);
    }

    // R2 (iteration 41): kiem tra tenant cua dong detail da luu bang
    // lookup gon getCompanyIdById, kieu Q4 — InspectionCheckInfoService
    // goi truoc moi ghi trong saveWithFile. Dong khong ton tai /
    // company rong / session rong/"null" giu hanh vi cu (ghi 0 dong);
    // dong la -> ServiceException -> 400. Detail khong
    // institution-shared nen khong co nhanh institution nhu helper goc.
    public void verifyStoredRowTenant(InspectionCheckDetail inspectionCheckDetail) {
        if (inspectionCheckDetail == null || inspectionCheckDetail.getId() == null
                || inspectionCheckDetail.getId().isEmpty()) {
            return;
        }
        String storedCompanyId = this.dao.getCompanyIdById(inspectionCheckDetail.getId());
        if (storedCompanyId == null || storedCompanyId.isEmpty()) {
            return;
        }
        String tenantId = com.geeke.utils.SessionUtils.getLoginTenantId();
        if (tenantId == null || tenantId.isEmpty() || "null".equals(tenantId)) {
            return;
        }
        if (tenantId.equals(storedCompanyId)) {
            return;
        }
        throw new ServiceException("Không có quyền truy cập tenant khácDữ liệu");
    }

    @Transactional(readOnly = false)
    public void deleteBy(InspectionCheckDetail inspectionCheckDetail) {
        this.dao.deleteBy(inspectionCheckDetail.getId());
    }
}