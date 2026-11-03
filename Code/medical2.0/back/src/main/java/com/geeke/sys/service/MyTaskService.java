package com.geeke.sys.service;

import com.geeke.common.data.Page;

import com.geeke.common.data.Parameter;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;

import com.geeke.sys.dao.MyTaskDao;
import com.geeke.sys.entity.MyTask;
import com.geeke.sys.entity.SysFile;
import com.geeke.sys.utils.SessionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service("myTaskService")
public class MyTaskService extends CrudService<MyTaskDao,MyTask> {
    @Autowired
    private MyTaskDao myTaskDaol;

    @Autowired
    private SysFileService sysFileService;

    @Transactional
    public void save(MyTask myTask, MultipartFile[] fileIdUploads, String[] deleteIds) throws IOException {
        // Q4 (iteration 40): save/update ghi de MyTaskService thay vi
        // CrudService.save nen truoc day ne hoan toan guard tenant
        // (chi con san hasAnySessionPermission o controller), trong khi
        // mapper UPDATE feedbacktable ... WHERE id tran, khong co
        // company_id — token la gui id la + company own la cuop dong
        // phan hoi sang tenant minh (tai hien live iter40-q4-repro).
        // Chi ep session tenant thi KHONG du o day (khac Q1/Q3, noi
        // WHERE co company_id): phai chan theo dong da luu, kieu
        // Q2/M1-M2, TRUOC moi ghi ke ca xu ly file ben duoi.
        // Session rong/"null" giu hanh vi cu nhu helper goc.
        // Khong dung verifyTenantOwnershipAgainstStoredRow thua ke:
        // no goi dao.get() va get() loi LONG -> Timestamp tren dong
        // khong co taskmanagement (xem getCompanyIdById) nen guard se
        // 500 thay vi 400 va chan ca dong own mo coi — dung lookup
        // gon ben duoi, cung ngu nghia (MyTask khong institution-shared
        // nen khong co nhanh institution nhu helper goc).
        verifyTenantOwnership(myTask);
        verifyStoredRowTenant(myTask);
        // L4 (iteration 59): guard Q4 chi bao ve DONG task, con
        // deleteIds ben duoi truoc day tran — token own luu task own
        // kem deleteIds [file la] la xoa duoc file tenant la (tai hien
        // live iter59-l4 pre-fix). Upload dung object_id = myTask id
        // nen tap cho phep dung la {myTask id}, mau K7; dat TRUOC
        // upload/delete/update de 400 xay ra truoc moi ghi (cung
        // transaction @Transactional nen rollback neu guard nem loi).
        // Ham nay chi dao.update (khong insert) nen khong co nhanh
        // tao moi; id rong -> tap {""} (hoac {null}) -> file ton tai
        // nao cung bi chan (fail-closed, mau K7 nhanh update id
        // rong); file khong ton tai giu hanh vi cu (guard bo qua).
        if (null != deleteIds && deleteIds.length > 0) {
            sysFileService.verifyDeleteIdsObjectIds(Arrays.asList(deleteIds),
                    Collections.singleton(myTask.getId()));
        }
        //Lưu tệp đã tải lên
        List<SysFile> sysFiles = sysFileService.changeAndSaveSysFileList(fileIdUploads, myTask.getId());
        if(sysFiles!=null&&sysFiles.size()>0){
            myTask.setFileId(sysFiles.get(0).getId());
        }
        // Theo tệp đính kèmIDXóa thông tin tệp đính kèm
        if (null != deleteIds && deleteIds.length > 0) {
            sysFileService.delete(deleteIds);
        }
        myTask.setUpdateBy(SessionUtils.getUserJson().getString("name"));
        myTask.setUpdateDate(new Date());
        this.dao.update(myTask);


    }

    // Q4 (iteration 40): kiem tra tenant cua dong da luu bang lookup
    // gon getCompanyIdById thay vi dao.get() — xem ghi chu o dao.
    // Dong khong ton tai / company rong / session rong/"null" giu
    // hanh vi cu (ghi 0 dong); dong la -> ServiceException -> 400.
    private void verifyStoredRowTenant(MyTask myTask) {
        if (myTask == null || myTask.getId() == null || myTask.getId().isEmpty()) {
            return;
        }
        String storedCompanyId = myTaskDaol.getCompanyIdById(myTask.getId());
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

    @Transactional
    public MyTask update(MyTask myTask) {
        // Q4 (iteration 40): nhu save — chan dong la theo dong da luu
        // truoc khi dao.update; dong la -> ServiceException -> 400,
        // dong own + company payload la cung 400 (verifyTenantOwnership).
        // UI that gui dong own (list loc company_id own; taskdetailsForm
        // gui lai dong vua xem) nen khong doi. Luu y: form myTask.vue
        // goi saveClinicOffice, khong phai API nay, nen picker
        // company_List cua no khong phai can cu cho phep company la o day.
        verifyTenantOwnership(myTask);
        verifyStoredRowTenant(myTask);
        int i =this.dao.update(myTask);
        if(i>0){
            return myTask;
        }
        return null;
    }
}
