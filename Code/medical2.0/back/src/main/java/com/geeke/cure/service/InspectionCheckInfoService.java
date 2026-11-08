package com.geeke.cure.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.geeke.cure.entity.InspectionCheckDetail;
import com.geeke.common.service.ServiceException;
import com.geeke.cure.dao.InspectionCheckDao;
import com.geeke.sys.dao.SysFileDao;
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

    // R3 (iteration 41): lookup truc tiep qua DAO, khong autowire
    // InspectionCheckService (service do autowire nguoc lai service nay —
    // vong tron) va khong autowire SysFileService cho lookup (chi can id).
    @Autowired
    private InspectionCheckDao inspectionCheckDao;

    @Autowired
    private SysFileDao sysFileDao;

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
        // rong giu hanh vi cu nhu helper goc. Detail (R2) guard ngay
        // duoi; cha InspectionCheck + deleteIds (R3) guard tiep theo,
        // tat ca deu TRUOC moi thao tac file/ghi.
        verifyTenantOwnership(inspectionCheckInfo);
        verifyTenantOwnershipAgainstStoredRow(inspectionCheckInfo);
        // R2 (iteration 41): guard tenant cho tung detail TRUOC moi thao
        // tac file/ghi, cung cho nhu R1 — vong detail o duoi goi thang
        // inspectionCheckDetailService.update() (mapper UPDATE WHERE id
        // tran, khong company_id) nen info own + detail la van ghi de
        // VA doi chu dong la ve tenant minh (tai hien live
        // iter41-r2-repro). verifyTenantOwnership chan company payload
        // la; verifyStoredRowTenant (lookup gon kieu Q4) chan dong da
        // luu la du payload co mang company own. UI that gui lai detail
        // own vua nap qua getByInfoId nen khong doi; detail khong id /
        // khong ton tai / company rong giu hanh vi cu nhu helper goc.
        if (inspectionCheckInfo.getInspectionCheckDetails() != null) {
            for (InspectionCheckDetail inspectionCheckDetail : inspectionCheckInfo.getInspectionCheckDetails()) {
                verifyTenantOwnership(inspectionCheckDetail);
                inspectionCheckDetailService.verifyStoredRowTenant(inspectionCheckDetail);
            }
        }
        // T2 (iteration 42): payload khong co cha -> 400 TRUOC moi thao
        // tac file/ghi. Truoc sua: save() chay toi dao.update thi DB
        // bao inspection_check_id NOT NULL -> 500 DataIntegrityViolation
        // (transaction rollback nen info/file chua doi, nhung client
        // nhan 500 cho loi nhap lieu); controller sau do cung khong the
        // chay duoc vi no deref getInspectionCheck().getId() nen
        // khong-cha khong bao gio thanh cong. Guard o day tra 400 sach,
        // cung mau R1-R3; cha co id rong coi nhu khong co cha.
        if (inspectionCheckInfo.getInspectionCheck() == null
                || inspectionCheckInfo.getInspectionCheck().getId() == null
                || inspectionCheckInfo.getInspectionCheck().getId().isEmpty()) {
            throw new ServiceException("Thiếu phiếu kiểm tra");
        }
        // T3 (iteration 42): details null -> 400 TRUOC moi thao tac
        // file/ghi, cung mau T2. Truoc sua: guard R2 bo qua null, save()
        // chay qua file + dao.update roi moi NPE o vong detail cuoi ->
        // 500 (transaction rollback nen info/file thuc te khong doi,
        // tai hien pre-fix: 500, info giu nguyen, files=0) nhung client
        // nhan 500 cho payload thieu truong. Chi null bi chan: list
        // rong [] van 200 nhu cu (control da kiem chung) vi do la
        // payload hop le tuong minh, khac null la thieu truong. Chan
        // som cung ngan controller danh dau cha xong ("1") cho mot
        // phieu khong co chi tiet nao neu null duoc chap nhan thanh 200.
        if (inspectionCheckInfo.getInspectionCheckDetails() == null) {
            throw new ServiceException("Thiếu chi tiết kiểm tra");
        }
        // R3 (iteration 41): guard cha InspectionCheck + deleteIds, cung
        // TRUOC moi thao tac file/ghi.
        // (a) Cha: controller doi cha sang status "1" SAU khi save() tra
        // ve (transaction rieng, da commit) — guard o buoc cha nem loi
        // nhung dong info/detail/file da doi (ghi mot nua, tai hien live
        // iter41-r3-repro: 400 nhung info van doi). Guard o day bang
        // lookup gon getCompanyIdById kieu R2: cha la -> 400 truoc khi
        // ghi bat cu thu gi; cha own -> di tiep, buoc cha sau do van
        // chay guard CrudService.save nhu cu. Cha khong id / company
        // rong / session rong/"null" giu hanh vi cu nhu helper goc;
        // InspectionCheck khong institution-shared (khong override
        // isInstitutionShared) nen khong co nhanh institution.
        // T1 (iteration 42): getCompanyIdById tra null ca khi cha khong
        // ton tai lan khi company_id null, nen R3 bo qua ca hai — cha
        // khong ton tai roi xuong controller get() null -> NPE 500 SAU
        // khi info commit (tai hien live iter42-t1-repro: 500 nhung info
        // van doi + file da tao). Guard them o day, cung TRUOC moi ghi:
        // lookup null + existsById == 0 -> 400, khong ghi mot nua.
        // Cha co company rong nhung ton tai giu hanh vi cu (controller
        // van set status duoc) — chi chan dung dong khong ton tai.
        // (b) deleteIds: sys_file KHONG co cot company_id nen khong
        // kiem tra tenant truc tiep duoc; file cua form nay luon co
        // object_id = id dong info (uploadFiles = info.id, file moi luu
        // cung qua changeAndSaveSysFileList(..., info.getId())) nen
        // guard bang object_id: id file co object_id khac info id (ke
        // ca file tenant la hay file cua doi tuong khac) -> 400 truoc
        // khi xoa; id khong ton tai giu hanh vi cu (xoa 0 dong).
        if (inspectionCheckInfo.getInspectionCheck() != null
                && inspectionCheckInfo.getInspectionCheck().getId() != null
                && !inspectionCheckInfo.getInspectionCheck().getId().isEmpty()) {
            String parentCompanyId = inspectionCheckDao.getCompanyIdById(
                    inspectionCheckInfo.getInspectionCheck().getId());
            if (parentCompanyId != null && !parentCompanyId.isEmpty()) {
                String tenantId = com.geeke.utils.SessionUtils.getLoginTenantId();
                if (tenantId != null && !tenantId.isEmpty() && !"null".equals(tenantId)
                        && !tenantId.equals(parentCompanyId)) {
                    throw new ServiceException("Không có quyền truy cập tenant khác Dữ liệu");
                }
            } else if (inspectionCheckDao.existsById(
                    inspectionCheckInfo.getInspectionCheck().getId()) == 0) {
                // T1: cha khong ton tai -> 400 TRUOC moi ghi (controller
                // get() null -> NPE 500 sau commit neu khong chan o day).
                throw new ServiceException("Phiếu kiểm tra không tồn tại");
            }
        }
        if (null != deleteIds && deleteIds.length > 0) {
            String infoId = inspectionCheckInfo.getId();
            for (String deleteId : deleteIds) {
                if (deleteId == null || deleteId.isEmpty()) {
                    continue;
                }
                String fileObjectId = sysFileDao.getObjectIdById(deleteId);
                if (fileObjectId == null) {
                    continue;
                }
                if (infoId == null || infoId.isEmpty() || !infoId.equals(fileObjectId)) {
                    throw new ServiceException("Không có quyền truy cập tenant khác Dữ liệu");
                }
            }
        }
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

    /**
     * DS133 A: single delete fail-closed 400. Truoc day khong co
     * delete() override; mapper <delete> la HARD DELETE (DELETE FROM
     * inspection_check_info WHERE id, khong loc del_flag -- bang cung
     * khong co cot del_flag) nen ghost/blank -> 200 data 0 im lang,
     * xoa lap (dong da bi xoa cung) -> 200 data 0 im lang. Cung lop
     * DS123/DS129/DS130/DS131/DS132. Chan truoc bang dao.get (tra
     * null khi dong khong ton tai; khong co cot del_flag nen "khong
     * ton tai" CHINH LA trang thai da xoa) theo mau CodeRule/DS129 A.
     * checkExists (DS103) KHONG tai dung (cho qua blank, khong check
     * ton tai, dung chung voi save/bulkUpdate). bulkDelete ghost CO
     * TINH GIU 200 rows 0 (DS99/DS106/DS126 B idempotency) -- khong
     * doi; bulkDelete dong live van la hard delete vi bang khong co
     * co xoa mem de giu. deleteBy (xoa theo inspection_check_id,
     * goi noi bo) khong doi.
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(InspectionCheckInfo inspectionCheckInfo) {
        if (inspectionCheckInfo == null || StringUtils.isBlank(inspectionCheckInfo.getId())) {
            throw new ServiceException("Không xác định được chi tiết kiểm tra cần xóa.");
        }
        if (dao.get(inspectionCheckInfo.getId()) == null) {
            throw new ServiceException("Chi tiết kiểm tra không tồn tại: " + inspectionCheckInfo.getId());
        }
        return super.delete(inspectionCheckInfo);
    }

    @Transactional(readOnly = false)
    public void deleteBy(InspectionCheckInfo byInspecId) {
        this.dao.deleteBy(byInspecId.getId());
    }
}