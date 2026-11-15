package com.geeke.outpatient.service;

import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.org.entity.Company;
import com.geeke.outpatient.dao.PatientDao;
import com.geeke.outpatient.dao.RemoteDiagnosisTreatmentDao;
import com.geeke.outpatient.entity.*;
import com.geeke.toll.entity.TollEvt;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * khám chữa bệnh từ xaQuản lýService
 * @author zh
 * @version 2023-12-06
 */
 
@Service("remoteDiagnosisTreatmentService")
@Transactional(readOnly = true)
public class RemoteDiagnosisTreatmentService extends CrudService<PatientDao, Patient>{
    @Autowired
    private PatientDao patientDao;
    @Autowired
    private RemoteDiagnosisTreatmentDao remoteDiagnosisTreatmentDao;

    /** idTruy vấn */
    public RemoteDiagnosisTreatment diagnosisById(String id) {
        return remoteDiagnosisTreatmentDao.diagnosisById(id);
    }

    /** Đăng ký khámidTruy vấn */
    public RemoteDiagnosisTreatment getRegistrationId(String registrationId) {
        return remoteDiagnosisTreatmentDao.getRegistrationId(registrationId);
    }

    /**
     * Truy vấn phúttrangDữ liệu
     * @return
     */
    public Page<RemoteDiagnosisTreatment> diagnosisPage(List<Parameter> parameters, int offset, int limit, String orderby) {
        PageRequest pageRequest = new PageRequest(offset, limit, parameters, orderby);
        int total = remoteDiagnosisTreatmentDao.diagnosisCount(pageRequest);
        // Trả danh sách rỗng thay vì null khi total=0: frontend gọi
        // .length trên rows nên null sẽ văng TypeError và hiện dialog lỗi.
        List<RemoteDiagnosisTreatment> list = new java.util.ArrayList<>();
        if(total > 0) {
            list = remoteDiagnosisTreatmentDao.diagnosisPage(pageRequest);
        }
        return new Page<RemoteDiagnosisTreatment>(total, list);
    }

    /** SửaLưu */
    @Transactional(readOnly = false)
    public RemoteDiagnosisTreatment diagnosisSave(RemoteDiagnosisTreatment entity) {
        // DS115 C: (1) entity null -> 400 thay vi NPE o controller
        // (entity.getId()); (2) update id khong ton tai -> 400 thay vi
        // 200 silent no-op (diagnosisUpdate cham 0 dong, probe C4);
        // (3) tao moi chua co company -> ep company theo session tenant
        // (neu khong dong mo coi se khong bao gio gan duoc don thuoc qua
        // addRecipel vi guard DS88 doi company khac null, probe C18).
        if (entity == null) {
            throw new ServiceException("Chẩn đoán từ xa không được để trống");
        }
        if (StringUtils.isBlank(entity.getId())) {
            if (entity.getCompany() == null
                    || StringUtils.isBlank(entity.getCompany().getId())) {
                String tenantId = SessionUtils.getLoginTenantId();
                if (StringUtils.isNotBlank(tenantId) && !"null".equals(tenantId)) {
                    entity.setCompany(new Company(tenantId));
                }
            }
            // DS320: tao moi chi chap nhan status null/'0'/'1'. '1' la
            // luong gui yeu cau that (patientForm onSubmit goi
            // statusEdit("1") qua save sau khi HIS apply OK); null ep
            // ve '0' (Chua gui) de hang moi hien nut Gui/Xoa (list chi
            // hien thao tac khi status === '0'). '2'..'5' (Dang kham /
            // Da ket thuc / Da phat thuoc / Da tra lai) chi phia HIS
            // moi duoc chuyen, khong bao gio sinh ra tu UI nay; gia
            // tri rac cung khong co nhan hien thi -> 400.
            checkStatusOnCreate(entity.getStatus());
            if (entity.getStatus() == null) {
                entity.setStatus("0");
            }
            entity.preInsert();
            remoteDiagnosisTreatmentDao.diagnosisInsert(entity);
        } else {
            RemoteDiagnosisTreatment stored =
                    remoteDiagnosisTreatmentDao.diagnosisById(entity.getId());
            if (stored == null) {
                throw new ServiceException("Chẩn đoán từ xa không tồn tại: "
                        + entity.getId());
            }
            // DS320: diagnosisUpdate chi ghi status (mapper :145) nen
            // save-update thuc chat la mot endpoint doi-trang-thai.
            // Chi cho phep: giu nguyen, 0/null -> '1' (Gui, sau HIS
            // apply), '1' -> '0' (Thu hoi). Moi chuyen khac (sang
            // '2'..'5', gia tri rac, hoac null lam NULL cot) -> 400.
            checkStatusTransition(stored.getStatus(), entity.getStatus());
            entity.preUpdate();
            remoteDiagnosisTreatmentDao.diagnosisUpdate(entity);
        }
        return entity;
    }

    // DS320: trang thai tao moi cua mot yeu cau kham tu xa.
    private void checkStatusOnCreate(String status) {
        if (status != null && !"0".equals(status) && !"1".equals(status)) {
            throw new ServiceException("Trạng thái chẩn đoán từ xa không hợp lệ khi tạo mới: "
                    + status);
        }
    }

    // DS320: chuyen trang thai hop le qua save / modifiedState. stored
    // null = hang nhap (truoc day insert ghi null). Luong UI that chi
    // co: giu nguyen, null/'0' -> '1' (Gui), '1' -> '0' (Thu hoi).
    private void checkStatusTransition(String stored, String next) {
        if (next == null) {
            throw new ServiceException("Trạng thái chẩn đoán từ xa không được để trống");
        }
        if (next.equals(stored)) {
            return;
        }
        if ("1".equals(next) && (stored == null || "0".equals(stored))) {
            return;
        }
        if ("0".equals(next) && ("1".equals(stored) || stored == null)) {
            return;
        }
        throw new ServiceException("Không được chuyển trạng thái chẩn đoán từ xa từ "
                + (stored == null ? "chưa gửi" : stored) + " sang " + next);
    }

    // DS320: medicationStatus chi co luong thu phi dat '1' (tollInfo
    // saveTollTollInfo + chargeEdit). '2' (Da phat thuoc) khong co
    // caller nao trong codebase nay; gia tri rac / NULL hoa cot -> 400.
    private String checkMedicationStatusTransition(String stored, String next) {
        if (next == null) {
            return stored;
        }
        if (next.equals(stored)) {
            return next;
        }
        if ("1".equals(next) && (stored == null || "0".equals(stored))) {
            return next;
        }
        throw new ServiceException("Trạng thái phát thuốc không hợp lệ: " + next);
    }

    /** Sửa trạng thái
     * @return*/
    @Transactional(readOnly = false)
    public int modifiedState(RemoteDiagnosisTreatment entity) {
        // DS115 C: entity null / id rong / id khong ton tai -> 400 thay vi
        // NPE 500 (probe C6) hay 200 rows 0 lang le (probe C7).
        if (entity == null) {
            throw new ServiceException("Chẩn đoán từ xa không được để trống");
        }
        if (StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Chẩn đoán từ xa không được để trống");
        }
        if (remoteDiagnosisTreatmentDao.diagnosisById(entity.getId()) == null) {
            throw new ServiceException("Chẩn đoán từ xa không tồn tại: "
                    + entity.getId());
        }
        // DS320: modifiedState ghi status + medicationStatus verbatim
        // (mapper :153-154) nen cung la duong gia mao trang thai: '4'
        // (Da phat thuoc) / '5' / gia tri rac, hay medicationStatus
        // '2' (Da phat thuoc) khi chua thu phi/phat thuoc. Luong UI
        // that chi co: status giu nguyen (luu thong tin nguoi nhan),
        // null/'0' -> '1' (Gui sau HIS apply), medicationStatus
        // null/'0' -> '1' (sau thu phi). Payload null giu gia tri da
        // luu thay vi NULL hoa cot.
        RemoteDiagnosisTreatment stored =
                remoteDiagnosisTreatmentDao.diagnosisById(entity.getId());
        if (entity.getStatus() == null) {
            entity.setStatus(stored.getStatus());
        } else {
            checkStatusTransition(stored.getStatus(), entity.getStatus());
        }
        entity.setMedicationStatus(checkMedicationStatusTransition(
                stored.getMedicationStatus(), entity.getMedicationStatus()));
        return remoteDiagnosisTreatmentDao.modifiedState(entity);
    }

    /** khám chữa bệnh từ xaThu phí
     * @return*/
    @Transactional(readOnly = false)
    public int chargeState(TollEvt tollEvt) {
        // DS115 C: tollEvt / tollInfo / medical / registration null -> 400
        // thay vi NPE 500 (probe C13/C14); luot dang ky khong ton tai ->
        // 400 thay vi 200 lang le (getCase rong, 0==0 nen chargeEdit chay
        // 0 dong roi tra 1, probe C15); tung don trong list null / thieu
        // id -> 400 thay vi NPE o e.getRecipelInfo().getId().
        if (tollEvt == null) {
            throw new ServiceException("Thông tin thu phí không được để trống");
        }
        List<RecipelInfoEvt> recipelInfos = tollEvt.getRecipelInfos();
        if (recipelInfos != null) {
            for (RecipelInfoEvt e : recipelInfos) {
                if (e == null || e.getRecipelInfo() == null
                        || StringUtils.isBlank(e.getRecipelInfo().getId())) {
                    throw new ServiceException("Đơn thuốc thu phí không được để trống");
                }
            }
        }
        if (tollEvt.getTollInfo() == null || tollEvt.getTollInfo().getMedical() == null
                || tollEvt.getTollInfo().getMedical().getRegistration() == null
                || StringUtils.isBlank(tollEvt.getTollInfo().getMedical().getRegistration().getId())) {
            throw new ServiceException("Lượt đăng ký thu phí không được để trống");
        }
        String id = tollEvt.getTollInfo().getMedical().getRegistration().getId();
        if (getRegistrationCompanyId(id) == null) {
            throw new ServiceException("Lượt đăng ký không tồn tại: " + id);
        }
        if (recipelInfos != null) {
            for (RecipelInfoEvt e : recipelInfos) {
                remoteDiagnosisTreatmentDao.chargeState(e.getRecipelInfo().getId());
            }
        }
        List<RecipelInfo> aCase = remoteDiagnosisTreatmentDao.getCase(id);
        int i = 0;
        for (RecipelInfo a : aCase) {
            // DS115 C: charge_status NULL (don chua tung thu phi) unbox
            // thanh NPE 500 o i += getChargeStatus(); NULL nghia la chua
            // thu phi nen tinh nhu 0, dung ngu nghia "tat ca da thu phi".
            Integer cs = a.getChargeStatus();
            i += (cs == null ? 0 : cs);
        }
        if (i == aCase.size()){
            remoteDiagnosisTreatmentDao.chargeEdit(id);
        }
        return 1;
    }

    /** XóaBản ghi
     * DS133 C: diagnosisDelete fail-closed 400. Truoc day goi thang
     * dao.diagnosisDelete, ma mapper UPDATE remote_diagnosis SET
     * del_flag='1' WHERE id KHONG loc del_flag nen ghost/blank ->
     * 200 data 0 im lang, xoa lap (del_flag da '1') -> 200 rows 1.
     * Cung lop DS123/DS129/DS130/DS131/DS132. Check X2 o controller
     * cho ghost qua co tinh (company null -> khong phai la) nen chan
     * o day: blank -> 400; getDelFlagById null (khong ton tai) hoac
     * '1' (da xoa) -> 400. Lookup rieng khong JOIN (diagnosisById
     * co patient joins) theo mau countById DS127; checkExists (DS103)
     * KHONG tai dung. diagnosisDelete chi duoc controller nay goi.
     */
    @Transactional(readOnly = false)
    public int diagnosisDelete(String id) {
        if (StringUtils.isBlank(id)) {
            throw new ServiceException("Không xác định được bản ghi khám từ xa cần xóa.");
        }
        String delFlag = remoteDiagnosisTreatmentDao.getDelFlagById(id);
        if (delFlag == null || "1".equals(delFlag)) {
            throw new ServiceException("Bản ghi khám từ xa không tồn tại: " + id);
        }
        return remoteDiagnosisTreatmentDao.diagnosisDelete(id);
    }

    /** X2: ủy thác tra company cho kiểm tra sở hữu ở controller */
    public String getRemoteDiagnosisCompanyId(String id) {
        return remoteDiagnosisTreatmentDao.getRemoteDiagnosisCompanyId(id);
    }

    /** X2: ủy thác tra company theo lượt khám */
    public String getRemoteDiagnosisCompanyIdByRegistrationId(String registrationId) {
        return remoteDiagnosisTreatmentDao.getRemoteDiagnosisCompanyIdByRegistrationId(registrationId);
    }

    /** X2: ủy thác tra company lượt khám cho chargeState */
    public String getRegistrationCompanyId(String id) {
        return remoteDiagnosisTreatmentDao.getRegistrationCompanyId(id);
    }

    /** X2: ủy thác tra company đơn thuốc cho chargeState */
    public String getRecipelInfoCompanyId(String id) {
        return remoteDiagnosisTreatmentDao.getRecipelInfoCompanyId(id);
    }

}