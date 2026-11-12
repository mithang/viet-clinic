package com.geeke.treatment.service.impl;

import com.geeke.common.sequence.service.SequenceService;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.org.service.CompanyService;
import com.geeke.treatment.dao.CostItemDao;
import com.geeke.treatment.dao.CostItemPackageDao;
import com.geeke.treatment.entity.CostItem;
import com.geeke.treatment.entity.CostItemPackage;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CostItemPackageService extends CrudService<CostItemPackageDao, CostItemPackage> {
    @Autowired
    SequenceService sequenceService;

    @Autowired
    private CostItemPackageDao costItemPackageDao;

    @Autowired
    private CostItemDao costItemDao;

    @Autowired
    private CompanyService companyService;

    /**
     * ITER64 item 2: tien kiem TOAN BO DTO saveDto TRUOC BAT KY ghi nao.
     * Controller phai goi ham nay TRUOC parent save: cac method package hien
     * la transaction rieng le va parent save chay truoc delete, nen guard dat
     * muon se de lai ghi rac (parent OWN moi) du DTO bi chan. Mot package
     * item la -> toan bo DTO 400, khong ghi gi, khong xoa gi.
     * - Guard xoa (E2): payload id trung dong package da luu -> parent
     *   cost_item cua dong do (qua costItemPkgId) phai thuoc tenant phien
     *   hoac institution cua phien (CostItem la institution-shared); neu
     *   la thi deleteById sau do se xoa ca goi cua tenant khac.
     * - Guard link (E1): item duoc link (payload costItemId, va muc tieu
     *   hieu dung sau vong match cua controller: id payload khi khong
     *   match) phai thuoc tenant phien hoac institution cua phien.
     * Parity N3c: id khong ton tai / parent khong ton tai (lookup null) ->
     * cho qua nhu cu; phien khong session (tenant rong/"null", luong noi
     * bo) -> cho qua. Bang cost_item_package KHONG co cot company nen
     * khong the dung verifyTenantOwnership tren chinh dong package
     * (company luon null -> pass); guard BAT BUOC di qua parent cost_item
     * bang single-column lookup, khong tin payload company.
     * Khong doi ngu nghia xoa: deleteById van xoa ca goi (thay the package),
     * guard chi chan goi la.
     */
    // DS237: so luong hang muc con am truoc day ghi thang xuong DB qua
    // POST /treatment/costItem/saveDto (cot quantity int signed, khong
    // guard o bat ky dau; DS215 chi chan gia cua PARENT CostItem, khong
    // chan quantity cua dong package). Frontend chan :min="0"
    // (costItemForm.vue:296) va tinh tong goi = SUM(quantity *
    // salePrice/costPrice) (:521-524, :576-578) nen quantity am luu
    // that lam giam tong tien goi khi mo lai / tinh phi (money leak,
    // cung ho "so am" DS209-DS215/DS223/DS236, be mat moi). Chan
    // fail-closed 400 o service: 0 van cho phep (quy uoc DS219).
    // Goi o validateSaveDtoPackages() TRUOC parent save (nguyen tac
    // ITER64: mot dong la -> 400, khong ghi/xoa gi) VA o save()
    // (phong thu sau cho moi caller khac).
    private static void checkCostItemPackageQuantity(
            List<CostItemPackage> packages) {
        if (packages == null) {
            return;
        }
        for (CostItemPackage pkg : packages) {
            if (pkg != null && pkg.getQuantity() < 0) {
                throw new ServiceException(
                        "Số lượng hạng mục con không được âm");
            }
        }
    }

    public void validateSaveDtoPackages(List<CostItemPackage> packages) {
        if (packages == null || packages.isEmpty()) {
            return;
        }
        // DS237: chan quantity am truoc moi ghi (ke ca parent save).
        checkCostItemPackageQuantity(packages);
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return;
        }
        String institution = companyService.getInstitution(tenantId);
        for (CostItemPackage pkg : packages) {
            if (pkg == null) {
                continue;
            }
            CostItemPackage found = null;
            if (StringUtils.isNotBlank(pkg.getId())) {
                found = costItemPackageDao.findById(pkg.getId());
            }
            if (found != null) {
                checkCostItemCompany(found.getCostItemPkgId(), tenantId, institution);
            }
            if (StringUtils.isNotBlank(pkg.getCostItemId())) {
                checkCostItemCompany(pkg.getCostItemId(), tenantId, institution);
            }
            String effectiveTarget = pkg.getId();
            if (found != null && StringUtils.isNotBlank(pkg.getCostItemId())
                    && pkg.getCostItemId().equals(found.getCostItemId())) {
                effectiveTarget = found.getCostItemId();
            }
            if (StringUtils.isNotBlank(effectiveTarget)) {
                checkCostItemCompany(effectiveTarget, tenantId, institution);
            }
        }
    }

    /** So company cua mot cost_item voi tenant phien (kem ngoai le
     * institution-shared cua CostItem); la -> 400, khong ton tai -> qua. */
    private void checkCostItemCompany(String costItemId, String tenantId, String institution) {
        if (StringUtils.isBlank(costItemId)) {
            return;
        }
        String ownerCompanyId = costItemDao.getCostItemCompanyId(costItemId);
        if (StringUtils.isBlank(ownerCompanyId)) {
            return;
        }
        if (tenantId.equals(ownerCompanyId)) {
            return;
        }
        if (StringUtils.isNotBlank(institution) && institution.equals(ownerCompanyId)) {
            return;
        }
        throw new ServiceException("Không có quyền truy cập tenant khác");
    }

    @Transactional(readOnly = false)
    public void save(List<CostItemPackage> costItemPackages, String costId) {
        // DS237: chan quantity am (phong thu sau; duong /saveDto da duoc
        // validateSaveDtoPackages() chan truoc parent save).
        checkCostItemPackageQuantity(costItemPackages);
        for (CostItemPackage costItemPackage : costItemPackages) {
            String id = costItemPackage.getId();
            costItemPackage.setCostItemId(id);
            costItemPackage.setCostItemPkgId(costId);
            costItemPackage.setId(null);
            CostItemPackage CostItemPackageTemp = super.save(costItemPackage);
        }

    }

    @Transactional(readOnly = false)
    public List<CostItemPackage> getAll(String costItemPckId){
        List<CostItemPackage> all = costItemPackageDao.getAll(costItemPckId);
        return all;
    }

    @Transactional(readOnly = false)
    public void deleteById(List<CostItemPackage> costItemPackages){
        if(costItemPackages!=null){
            for (CostItemPackage costItemPackage : costItemPackages) {
                costItemPackageDao.deleteById(costItemPackage.getCostItemPkgId());
            }
        }
    }

    @Transactional(readOnly = false)
    public List<CostItemPackage> findById(List<CostItemPackage> costItemPackages){
        List<CostItemPackage> costItemPackages1 = new ArrayList<>();
        for (CostItemPackage costItemPackage : costItemPackages) {
            CostItemPackage costItemPackageDaoById = costItemPackageDao.findById(costItemPackage.getId());
            if(costItemPackageDaoById!=null){
                costItemPackages1.add(costItemPackageDaoById);
            }
        }
        return costItemPackages1;
    }

}
