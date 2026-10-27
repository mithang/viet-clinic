package com.geeke.outpatient.service;

import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.service.CrudService;
import com.geeke.outpatient.dao.PatientDao;
import com.geeke.outpatient.dao.RemoteDiagnosisTreatmentDao;
import com.geeke.outpatient.entity.*;
import com.geeke.toll.entity.TollEvt;
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
        List<RemoteDiagnosisTreatment> list = null;
        if(total > 0) {
            list = remoteDiagnosisTreatmentDao.diagnosisPage(pageRequest);
        }
        return new Page<RemoteDiagnosisTreatment>(total, list);
    }

    /** SửaLưu */
    @Transactional(readOnly = false)
    public RemoteDiagnosisTreatment diagnosisSave(RemoteDiagnosisTreatment entity) {
        if (StringUtils.isBlank(entity.getId())) {
            entity.preInsert();
            remoteDiagnosisTreatmentDao.diagnosisInsert(entity);
        } else {
            entity.preUpdate();
            remoteDiagnosisTreatmentDao.diagnosisUpdate(entity);
        }
        return entity;
    }

    /** Sửa trạng thái
     * @return*/
    @Transactional(readOnly = false)
    public int modifiedState(RemoteDiagnosisTreatment entity) {
        return remoteDiagnosisTreatmentDao.modifiedState(entity);
    }

    /** khám chữa bệnh từ xaThu phí
     * @return*/
    @Transactional(readOnly = false)
    public int chargeState(TollEvt tollEvt) {
        List<RecipelInfoEvt> recipelInfos = tollEvt.getRecipelInfos();
        for(RecipelInfoEvt e:recipelInfos){
            remoteDiagnosisTreatmentDao.chargeState(e.getRecipelInfo().getId());
        }
        String id = tollEvt.getTollInfo().getMedical().getRegistration().getId();
        List<RecipelInfo> aCase = remoteDiagnosisTreatmentDao.getCase(id);
        int i = 0;
        for (RecipelInfo a:aCase){
            i+=a.getChargeStatus();
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

}