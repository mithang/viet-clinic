package com.geeke.cure.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    // DS108 B1/B10-B12: save/bulkInsert/bulkUpdate truoc day de lo:
    // save voi id khong ton tai roi qua CrudService.save -> doUpdate 0
    // dong roi van 200 silent no-op (verifyTenantOwnershipAgainstStoredRow
    // bo qua khi stored==null, cung mau DS94/DS99); bulkInsert/bulkUpdate
    // bypass @Valid nen inspectionCheckInfo/company thieu van ghi duoc
    // (inspection_check_info_id/company_id NOT NULL trong DB) -> 500.
    // Kiem tra ton tai + truong bat buoc truoc moi ghi. @Transactional
    // (readOnly = false) tuong minh vi class-level la readOnly = true
    // (mau DS105/DS106/DS107). Luu y: bang KHONG CO cot del_flag nen
    // bulkDelete la HARD DELETE (B15 kiem row GONE, khong phai flag=1);
    // update() tuy chinh dung cho InspectionCheckInfoService.saveWithFile
    // khong bi anh huong (chi override save/bulk* cua CrudService).
    @Override
    @Transactional(readOnly = false)
    public InspectionCheckDetail save(InspectionCheckDetail entity) {
        if (entity != null && StringUtils.isNotBlank(entity.getId())
                && dao.get(entity.getId()) == null) {
            throw new ServiceException("Chi tiết phiếu kiểm tra không tồn tại: "
                    + entity.getId());
        }
        checkInspectionCheckDetailRequired(entity);
        return super.save(entity);
    }

    // DS108 B3-B6/B10-B11: bulkInsert truoc day de lo: null/rong roi xuong
    // MyBatis <foreach> rong -> 500 (hien tai global handler anh xa mot
    // so loi thanh 400, nhung van chan ro o service de hanh vi khong phu
    // thuoc vao exception mapping); thieu inspectionCheckInfo/company ->
    // 500 NOT NULL. Chan het o service de 400 ro nghia.
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<InspectionCheckDetail> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách chi tiết phiếu kiểm tra không được để trống");
        }
        for (InspectionCheckDetail entity : entitys) {
            checkInspectionCheckDetailRequired(entity);
        }
        return super.bulkInsert(entitys);
    }

    // DS108 B2/B3/B5/B9/B12: bulkUpdate truoc day de lo: id khong ton tai
    // -> 200 silent no-op; batch hon hop [valid, nonexistent] doi ca dong
    // hop le truoc khi cham id ma; inspectionCheckInfo bi drop -> CASE
    // set inspection_check_info_id=NULL -> 500 NOT NULL. Tien kiem TOAN
    // BO list truoc moi ghi (fail-closed ca list, mau N1).
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<InspectionCheckDetail> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách chi tiết phiếu kiểm tra không được để trống");
        }
        for (InspectionCheckDetail entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Chi tiết phiếu kiểm tra không được để trống");
            }
            if (StringUtils.isNotBlank(entity.getId())
                    && dao.get(entity.getId()) == null) {
                throw new ServiceException("Chi tiết phiếu kiểm tra không tồn tại: "
                        + entity.getId());
            }
            checkInspectionCheckDetailRequired(entity);
        }
        return super.bulkUpdate(entitys);
    }

    // DS108 B7-B8: bulkDelete null/rong -> 400 ro nghia thay vi roi xuong
    // MyBatis <foreach> rong. bulkDelete id khong ton tai GIU NGUYEN
    // 200 rows 0 idempotent (DS99 G11) -- khong chan o day.
    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<InspectionCheckDetail> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách chi tiết phiếu kiểm tra không được để trống");
        }
        return super.bulkDelete(entitys);
    }

    // inspectionCheckInfo/company la NOT NULL trong DB (company them
    // @NotNull tren single save); costItem la @NotNull tren single save
    // nen bulk cung chan cho dong nhat (cot cost_item_id nullable nhung
    // UI luon gui). Company khong duoc CrudService.bulkInsert ep theo
    // session nen thieu company cung 500 NOT NULL.
    private void checkInspectionCheckDetailRequired(InspectionCheckDetail entity) {
        if (entity == null) {
            throw new ServiceException("Chi tiết phiếu kiểm tra không được để trống");
        }
        if (entity.getCompany() == null
                || StringUtils.isBlank(entity.getCompany().getId())) {
            throw new ServiceException("Phòng khám không được để trống");
        }
        if (entity.getInspectionCheckInfo() == null
                || StringUtils.isBlank(entity.getInspectionCheckInfo().getId())) {
            throw new ServiceException("Chi tiết bảng kiểm tra không được để trống");
        }
        if (entity.getCostItem() == null
                || StringUtils.isBlank(entity.getCostItem().getId())) {
            throw new ServiceException("Hạng mục khám chữa bệnh không được để trống");
        }
    }

    @Transactional
    public void allSave(InspectionCheckDetail inspectionCheckDetail) {
        this.dao.insert(inspectionCheckDetail);
    }

    public List<InspectionCheckDetail> getByInfoId(String infoId) {
        List<InspectionCheckDetail> inspectionCheckDetails = this.dao.getByInfoId(infoId);
        for (InspectionCheckDetail inspectionCheckDetail : inspectionCheckDetails) {
            // U2 (iteration 43): detail co the khong co cost item hop le —
            // cost_item_id NULL (mapper LEFT JOIN, khong ep NOT NULL) hoac
            // tro toi cost item da xoa (service.get() tra null). Truoc day
            // ca hai nhanh deu NPE -> GET /info 500 cho ca form doc. Nay
            // giu nguyen dong detail, dung placeholder rong (packages [])
            // de form khong vo o costItem.costItemPackages.length; khong
            // tra 400 vi day la loi doc, du lieu detail van hop le.
            CostItem costItem = null;
            if (inspectionCheckDetail.getCostItem() != null
                    && inspectionCheckDetail.getCostItem().getId() != null
                    && !inspectionCheckDetail.getCostItem().getId().isEmpty()) {
                costItem = costItemService.get(inspectionCheckDetail.getCostItem().getId());
            }
            if (costItem == null) {
                costItem = new CostItem();
                costItem.setCostItemPackages(new ArrayList<CostItemPackage>());
                inspectionCheckDetail.setCostItem(costItem);
                continue;
            }
            List<CostItemPackage> all = costItemPackageService.getAll(costItem.getId());
            costItem.setCostItemPackages(all);
            inspectionCheckDetail.setCostItem(costItem);
        }
        return inspectionCheckDetails;
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