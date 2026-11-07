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
            entity.preInsert();
            remoteDiagnosisTreatmentDao.diagnosisInsert(entity);
        } else {
            if (remoteDiagnosisTreatmentDao.diagnosisById(entity.getId()) == null) {
                throw new ServiceException("Chẩn đoán từ xa không tồn tại: "
                        + entity.getId());
            }
            entity.preUpdate();
            remoteDiagnosisTreatmentDao.diagnosisUpdate(entity);
        }
        return entity;
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

    /** XóaBản ghi */
    @Transactional(readOnly = false)
    public int diagnosisDelete(String id) {
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