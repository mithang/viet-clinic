package com.geeke.cure.service;

import java.util.List;
import java.util.Map;

import com.geeke.cure.entity.InspectionCheckDetail;
import com.geeke.cure.entity.InspectionCheckInfo;
import com.geeke.utils.IdGen;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import com.geeke.cure.dao.InspectionCheckDao;
import com.geeke.cure.entity.InspectionCheck;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Maps;
import org.springframework.util.CollectionUtils;

/**
 * Xét nghiệm và kiểm traService
 * @author rys
 * @version 2022-10-18
 */
 
@Service("inspectionCheckService")
@Transactional(readOnly = true)
public class InspectionCheckService extends CrudService<InspectionCheckDao, InspectionCheck>{

    @Autowired
    private InspectionCheckDao inspectionCheckDao;

    @Autowired
    private InspectionCheckInfoService inspectionCheckInfoService;

    @Autowired
    private InspectionCheckDetailService inspectionCheckDetailService;

    // DS125 B: single delete truoc day de lo: ghost id / dong da xoa mem
    // roi van 200 silent no-op (verifyTenantOwnershipAgainstStoredRow bo
    // qua khi stored==null, cung mau DS123/DS124/DS125A; mapper delete
    // khong loc del_flag). Chan o service de 400 ro nghia truoc khi xoa.
    // Khong caller noi bo nao goi delete() nay; UI xoa hang live van 200
    // nhu cu.
    @Override
    @Transactional(readOnly = false)
    public int delete(InspectionCheck entity) {
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được phiếu kiểm tra cần xóa.");
        }
        InspectionCheck stored = dao.get(entity.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Phiếu kiểm tra không tồn tại: " + entity.getId());
        }
        verifyTenantOwnershipAgainstStoredRow(entity);
        return super.delete(entity);
    }

    // DS108 A1/A10-A12: save/bulkInsert/bulkUpdate truoc day de lo:
    // save voi id khong ton tai roi qua CrudService.save -> doUpdate 0
    // dong roi van 200 silent no-op (verifyTenantOwnershipAgainstStoredRow
    // bo qua khi stored==null, cung mau DS94/DS99); bulkInsert/bulkUpdate
    // bypass @Valid nen patient/registration/costItem/type thieu van ghi
    // duoc (cac cot NOT NULL trong DB) -> 500. Kiem tra ton tai + truong
    // bat buoc truoc moi ghi. @Transactional (readOnly = false) tuong minh
    // vi class-level la readOnly = true (mau DS105/DS106/DS107). Luu y:
    // registration table hien rong (0 dong) nhung khong co FK, id stub
    // van insert duoc; cac caller noi bo (RecipelDetailService.updateById,
    // InspectionCheckInfoController) deu save entity day du nap tu DB.
    @Override
    @Transactional(readOnly = false)
    public InspectionCheck save(InspectionCheck entity) {
        if (entity != null && StringUtils.isNotBlank(entity.getId())
                && dao.get(entity.getId()) == null) {
            throw new ServiceException("Phiếu kiểm tra không tồn tại: "
                    + entity.getId());
        }
        checkInspectionCheckRequired(entity);
        // DS149 C: chan do dai truoc moi ghi (mau DS148 A). bulk* bypass
        // @Valid nen truong co @Length van 500 o mapper khi qua bulk
        // (name 100, patientName/sex/phone/diagnose/completeBy 100,
        // type/status 1); remarks -> varchar(255). Gioi han theo cot DB;
        // null-safe.
        checkInspectionCheckLengths(entity);
        return super.save(entity);
    }

    // DS108 A3-A6/A10-A11: bulkInsert truoc day de lo: null/rong roi xuong
    // MyBatis <foreach> rong -> 500 (hien tai global handler anh xa mot
    // so loi thanh 400, nhung van chan ro o service de hanh vi khong phu
    // thuoc vao exception mapping); thieu patient/type -> 500 NOT NULL.
    // Chan het o service de 400 ro nghia.
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<InspectionCheck> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách phiếu kiểm tra không được để trống");
        }
        for (InspectionCheck entity : entitys) {
            checkInspectionCheckRequired(entity);
            checkInspectionCheckLengths(entity);
        }
        return super.bulkInsert(entitys);
    }

    // DS108 A2/A3/A5/A9/A12: bulkUpdate truoc day de lo: id khong ton tai
    // -> 200 silent no-op; batch hon hop [valid, nonexistent] doi ca dong
    // hop le truoc khi cham id ma; patient bi drop -> CASE set
    // patient_id=NULL -> 500 NOT NULL. Tien kiem TOAN BO list truoc moi ghi
    // (fail-closed ca list, mau N1).
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<InspectionCheck> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách phiếu kiểm tra không được để trống");
        }
        for (InspectionCheck entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Phiếu kiểm tra không được để trống");
            }
            if (StringUtils.isNotBlank(entity.getId())
                    && dao.get(entity.getId()) == null) {
                throw new ServiceException("Phiếu kiểm tra không tồn tại: "
                        + entity.getId());
            }
            checkInspectionCheckRequired(entity);
            checkInspectionCheckLengths(entity);
        }
        return super.bulkUpdate(entitys);
    }

    // DS108 A7-A8: bulkDelete null/rong -> 400 ro nghia thay vi roi xuong
    // MyBatis <foreach> rong. bulkDelete id khong ton tai GIU NGUYEN
    // 200 rows 0 idempotent (DS99 G11) -- khong chan o day.
    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<InspectionCheck> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách phiếu kiểm tra không được để trống");
        }
        return super.bulkDelete(entitys);
    }

    // DS149 C: xem ghi chu o save().
    private static void checkInspectionCheckLengths(InspectionCheck entity) {
        if (entity == null) {
            return;
        }
        checkLength(entity.getName(), 100, "Tên Độ dài phải nằm trong khoảng 1 đến 100");
        checkLength(entity.getType(), 1, "Loại(0: Xét nghiệm; 1: Kiểm tra)Độ dài phải từ 1  và  1 Giữa");
        checkLength(entity.getStatus(), 1, "điền Trạng thái (0: Chưa điền; 1: Đã điền) Độ dài phải từ 1 và 1 Giữa");
        checkLength(entity.getPatientName(), 100, "Bệnh nhân Tên Độ dài phải nằm trong khoảng 0 đến 100");
        checkLength(entity.getSex(), 100, "Giới tính Độ dài phải nằm trong khoảng 0 đến 100");
        checkLength(entity.getPhone(), 100, "số điện thoại di động Độ dài phải nằm trong khoảng 0 đến 100");
        checkLength(entity.getDiagnose(), 100, "Chẩn đoán Độ dài phải nằm trong khoảng 0 đến 100");
        checkLength(entity.getCompleteBy(), 100, "lập đơn Bác sĩ Độ dài phải nằm trong khoảng 0 đến 100");
        checkLength(entity.getRemarks(), 255, "Ghi chú Độ dài phải nằm trong khoảng 0 đến 255");
    }

    private static void checkLength(String value, int max, String message) {
        if (value != null && value.length() > max) {
            throw new ServiceException(message);
        }
    }

    // Tat ca cac truong nay deu NOT NULL trong inspection_check (company/
    // type/status them @NotNull tren single save; patient/registration/
    // costItem khong co @NotNull nen chi bulk + save o service moi chan
    // duoc). Company khong co trong bulkInsert cua CrudService (khong ep
    // theo session) nen thieu company cung 500 NOT NULL.
    private void checkInspectionCheckRequired(InspectionCheck entity) {
        if (entity == null) {
            throw new ServiceException("Phiếu kiểm tra không được để trống");
        }
        if (entity.getCompany() == null
                || StringUtils.isBlank(entity.getCompany().getId())) {
            throw new ServiceException("Phòng khám không được để trống");
        }
        if (entity.getPatient() == null
                || StringUtils.isBlank(entity.getPatient().getId())) {
            throw new ServiceException("Bệnh nhân không được để trống");
        }
        if (entity.getRegistration() == null
                || StringUtils.isBlank(entity.getRegistration().getId())) {
            throw new ServiceException("Thông tin đăng ký không được để trống");
        }
        if (entity.getCostItem() == null
                || StringUtils.isBlank(entity.getCostItem().getId())) {
            throw new ServiceException("Hạng mục khám chữa bệnh không được để trống");
        }
        if (StringUtils.isBlank(entity.getType())) {
            throw new ServiceException("Loại phiếu kiểm tra không được để trống");
        }
        if (StringUtils.isBlank(entity.getStatus())) {
            throw new ServiceException("Trạng thái phiếu kiểm tra không được để trống");
        }
    }

    @Transactional(readOnly = false)
    public void allSave(InspectionCheck inspectionCheck) {
        // DS176 A: allSave ghi thang qua DAO, vong qua
        // checkInspectionCheckLengths — completeBy/sex lay tu ten bac si /
        // gia tri dict (varchar(128)) co the vuot cot varchar(100).
        checkInspectionCheckLengths(inspectionCheck);
        inspectionCheckDao.allSave(inspectionCheck);
    }

    public List<InspectionCheck> getByRecipelInfoId(String registrationId) {
        List<InspectionCheck> inspectionChecks = this.dao.getByRecipelInfoId(registrationId);
        return inspectionChecks;
    }

    public void deleteByRecipelInfoId(String recipelInfoId) {
        //QuarecipelInfoIdLấyid
        List<InspectionCheck> byRecipelInfoId = this.dao.getByRecipelInfoId(recipelInfoId);
        if(!CollectionUtils.isEmpty(byRecipelInfoId)) {
            for (InspectionCheck inspectionCheck : byRecipelInfoId) {
                InspectionCheckInfo byInspecId = inspectionCheckInfoService.getByInspecId(inspectionCheck.getId());
                // W1 (iter44): getByInspecId tra null dung thiet ke Z1a khi
                // phieu chua co dong info (check status "0" chua nhap ket
                // qua) — truoc day deref ngay byInspecId.getId() -> NPE,
                // hoan phi don tri lieu du dieu kien fail 500 thay vi
                // thanh cong. Khong co info thi khong co gi de xoa o
                // tang info/detail; dong check van bi xoa cung o cuoi
                // method (dao.deleteByRecipelInfoId), dung ngu nghia don
                // phieu khi hoan phi.
                if (byInspecId == null) {
                    continue;
                }
                List<InspectionCheckDetail> byInfoId = inspectionCheckDetailService.getByInfoId(byInspecId.getId());
                if (!CollectionUtils.isEmpty(byInfoId)) {
                    for (InspectionCheckDetail inspectionCheckDetail : byInfoId) {
                        inspectionCheckDetailService.deleteBy(inspectionCheckDetail);
                    }
                }
                inspectionCheckInfoService.deleteBy(byInspecId);
            }
        }
        this.dao.deleteByRecipelInfoId(recipelInfoId);
    }

    public List<InspectionCheck> getByRecipelDetail(String recipelDetailId) {
        List<InspectionCheck> inspectionChecks = this.dao.getByRecipelDetail(recipelDetailId);
        return inspectionChecks;
    }
}