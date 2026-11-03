package com.geeke.cure.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.geeke.cure.entity.InspectionCheckDetail;
import com.geeke.cure.entity.InspectionCheckDetailCostItem;
import com.geeke.cure.entity.InspectionCheckInfoCostItem;
import com.geeke.sys.entity.SysFile;
import com.geeke.sys.service.SysFileService;
import com.geeke.treatment.entity.CostItem;
import com.geeke.treatment.entity.CostItems;
import com.geeke.utils.IdGen;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import com.geeke.cure.dao.InspectionCheckInfoDao;
import com.geeke.cure.entity.InspectionCheckInfo;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Maps;
import org.springframework.web.multipart.MultipartFile;

/**
 * Chi tiết xét nghiệm và kiểm traService
 * @author rys
 * @version 2022-10-19
 */
 
@Service("inspectionCheckInfoService")
@Transactional(readOnly = true)
public class InspectionCheckInfoService extends CrudService<InspectionCheckInfoDao, InspectionCheckInfo>{

    @Autowired
    private SysFileService sysFileService;

    @Autowired
    private InspectionCheckDetailService inspectionCheckDetailService;

    @Transactional(readOnly = false)
    public void save(InspectionCheckInfo inspectionCheckInfo, MultipartFile[] fileIdUploads, String[] deleteIds) throws IOException {

        // R1 (iteration 41): guard tenant TRUOC moi thao tac file/ghi.
        // Truoc day save() goi thang dao.update (mapper UPDATE WHERE id
        // tran, khong company_id) nen ne ca hai guard cua CrudService.save:
        // payload id la + company own ghi de VA doi chu dong la ve tenant
        // minh (tai hien live o saveWithFile). verifyTenantOwnership chan
        // company payload la; verifyTenantOwnershipAgainstStoredRow chan
        // dong da luu la du payload co mang company own (mau Q2/M1-M2).
        // dao.get() o day chi LEFT JOIN company/inspection_check/patient
        // don gian, khong co mapping LONG->Timestamp nhu Q4 nen dung duoc
        // helper thua ke. Session rong/"null", dong khong ton tai, company
        // rong giu hanh vi cu nhu helper goc. Chi R1 (dong info); detail
        // (R2) va cha InspectionCheck + deleteIds (R3) o iteration sau.
        verifyTenantOwnership(inspectionCheckInfo);
        verifyTenantOwnershipAgainstStoredRow(inspectionCheckInfo);
        //Lưu tệp đã tải lên
        List<SysFile> sysFiles = sysFileService.changeAndSaveSysFileList(fileIdUploads, inspectionCheckInfo.getId());
        if(sysFiles!=null&&sysFiles.size()>0){
            inspectionCheckInfo.setFileId(sysFiles.get(0).getId());
        }
        // Theo tệp đính kèmIDXóa thông tin tệp đính kèm
        if (null != deleteIds && deleteIds.length > 0) {
            sysFileService.delete(deleteIds);
        }
        this.dao.update(inspectionCheckInfo);

        //LưuxongsauLưuChi tiết
        List<CostItems> costItemsList = inspectionCheckInfo.getCostItemsList();
        List<InspectionCheckDetail> inspectionCheckDetails = inspectionCheckInfo.getInspectionCheckDetails();
        for (InspectionCheckDetail inspectionCheckDetail : inspectionCheckDetails) {
            inspectionCheckDetailService.update(inspectionCheckDetail);
        }

    }

    @Transactional
    public void allSave(InspectionCheckInfo inspectionCheckInfo) {
        this.dao.insert(inspectionCheckInfo);
    }

    @Transactional
    public InspectionCheckInfo getByInspecId(String inspecId) {
        InspectionCheckInfo inspectionCheckInfo = this.dao.getByInspecId(inspecId);
        // Z1a: ket qua xet nghiem theo id tran — id khong ton tai tra null
        // (truoc day NPE o getId() -> 500); ban ghi tenant la tra null truoc
        // khi nap chi tiet, khong de lot qua CostItem dung chung. Session
        // rong/"null" giu hanh vi cu; company null giu nhu verifyTenantOwnership.
        if (inspectionCheckInfo == null) {
            return null;
        }
        String tenant = com.geeke.utils.SessionUtils.getLoginTenantId();
        if (tenant != null && !tenant.isEmpty() && !"null".equals(tenant)
                && inspectionCheckInfo.getCompany() != null
                && inspectionCheckInfo.getCompany().getId() != null
                && !inspectionCheckInfo.getCompany().getId().isEmpty()
                && !tenant.equals(inspectionCheckInfo.getCompany().getId())) {
            return null;
        }

        List<InspectionCheckDetail> inspectionCheckDetails = inspectionCheckDetailService.getByInfoId(inspectionCheckInfo.getId());

        inspectionCheckInfo.setInspectionCheckDetails(inspectionCheckDetails);
        return inspectionCheckInfo;
    }

    @Transactional
    public InspectionCheckInfoCostItem getByInspecId2(String inspecId) {
        InspectionCheckInfoCostItem inspectionCheckInfoCostItem = this.dao.getByInspecId2(inspecId);

        List<InspectionCheckDetailCostItem> byInfoId2 = inspectionCheckDetailService.getByInfoId2(inspectionCheckInfoCostItem.getId());

        inspectionCheckInfoCostItem.setInspectionCheckDetails(byInfoId2);
        return inspectionCheckInfoCostItem;
    }


    @Transactional(readOnly = false)
    public void deleteBy(InspectionCheckInfo byInspecId) {
        this.dao.deleteBy(byInspecId.getId());
    }
}