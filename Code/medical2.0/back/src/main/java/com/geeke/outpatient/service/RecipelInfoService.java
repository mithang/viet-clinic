package com.geeke.outpatient.service;

import java.util.*;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.outpatient.entity.*;
import com.geeke.stock.service.MedicinalStorageControlService;
import com.geeke.sys.entity.DictItem;
import com.geeke.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import com.geeke.outpatient.dao.RecipelInfoDao;

import com.google.common.collect.Maps;

/**
 * Thông tin đơn thuốcService
 * @author txl
 * @version 2022-06-15
 */
 
@Service("recipelInfoService")
@Transactional(readOnly = true)
public class RecipelInfoService extends CrudService<RecipelInfoDao, RecipelInfo>{
    @Lazy
    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private RecipelInfoDao recipelInfoDao;

    @Lazy
    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private MedicinalStorageControlService medicinalStorageControlService;

    /**
     * DS52: atomic claim for dispensing. Sets is_dispension='1' only when it
     * is still '0'; returns the rows affected (0 = already dispensed or
     * missing). Exactly one concurrent dispense wins the claim, so a
     * parallel double dispense cannot move stock twice. The caller still
     * saves dispensionDate/dispensionStatus afterwards as before.
     */
    @Transactional(readOnly = false)
    public int claimDispensed(String id) {
        return this.recipelInfoDao.claimDispensed(id);
    }

    /**
     * DS53: atomic claim for charging. Sets is_pay='1' only when the
     * recipel is still unpaid (is_pay '0' or NULL); returns the rows
     * affected (0 = already paid/refunded or missing). Exactly one
     * concurrent charge wins the claim, so a parallel double charge cannot
     * write a second TollInfo or deduct stock twice. The caller still
     * sets isPay/payDate/chargeStatus afterwards as before.
     */
    @Transactional(readOnly = false)
    public int claimPaid(String id) {
        return this.recipelInfoDao.claimPaid(id);
    }

    /**
     * DS54: atomic claim for refunding. Sets is_pay='2' only when the
     * recipel is still PAID (is_pay '1'); returns the rows affected
     * (0 = already refunded / never paid / missing). Exactly one
     * concurrent refund wins the claim, so a parallel double refund
     * cannot write a second refund TollInfo or REFUND audit row, or
     * run materialRefund twice. The caller still sets isPay/
     * chargeStatus/retreatDate afterwards as before.
     */
    @Transactional(readOnly = false)
    public int claimRefunded(String id) {
        return this.recipelInfoDao.claimRefunded(id);
    }

    /**
     * DS57: atomic claim for returning dispensed drugs. Sets
     * is_dispension='2' only when it is still '1'; returns the rows
     * affected (0 = already returned / never dispensed / missing).
     * Exactly one concurrent return wins the claim, so a parallel
     * double return cannot run drugRepercussion twice and drift
     * used/return_todo_fee. The caller still sets
     * dispensionStatus/retreatChargeDate afterwards as before.
     */
    @Transactional(readOnly = false)
    public int claimReturned(String id) {
        return this.recipelInfoDao.claimReturned(id);
    }

    @Transactional(readOnly = false)
    public int invalid(String id) {
        RecipelInfo recipelInfo = super.get(id);
        if (Objects.nonNull(recipelInfo))
        {
            if (Objects.equals(recipelInfo.getStatus(), 1) && Objects.equals(recipelInfo.getChargeStatus(), 0))
            {
                recipelInfo.setStatus(-1);
                super.save(recipelInfo);

                //độngTồn khogiải phóngthao tác
                this.medicinalStorageControlService.cancelOccupy(recipelInfo);

                //Theoca bệnhidLấytất cả của Đơn thuốcThông tin
                List<RecipelInfo> byMedicalId = recipelInfoDao.getByMedicalId(recipelInfo.getRegistration().getId());
                int recipelInfoLength=byMedicalId.size();
                int count=0;
                for (RecipelInfo recipelInfos : byMedicalId  ) {
                    if(recipelInfos.getStatus()==-1){
                        count++;
                    }
                }

                //tất cảĐơn thuốcđềuhủysau, Sẽ đăng kýThông tin của cũngcờ
                if(count==recipelInfoLength){
                    //Theoca bệnhThông tinLấyđăng kýThông tin
                    //MedicalRecord medicalRecord = medicalRecordService.get(recipelInfo.getRegistration().getId());
                    //Theoca bệnhThông tinLấyđăng kýid, Đặtđăng ký của Đơn thuốcTrạng thái
                    registrationService.updateRecipeStatus(recipelInfo.getRegistration().getId());
                }
                return 1;
            }
        }

        return 0;
    }

    public List<RecipelInfoDTO> getHistoryRecipel(PageRequest pageRequest) {
        List<RecipelInfoDTO> recipelInfos=recipelInfoDao.getHistoryRecipel(pageRequest);
        return recipelInfos;
    }

    @Transactional(readOnly=false)
    public void updateSchedule(String id, int cureType){
        recipelInfoDao.updateSchedule(id,cureType);
    }

    /** X3: company của đơn thuốc theo id, ủy thác cho DAO (controller kiểm tra sở hữu trước khi gọi service ghi/đọc). */
    public String getRecipelInfoCompanyId(String id) {
        return recipelInfoDao.getRecipelInfoCompanyId(id);
    }

    @Transactional(readOnly = false)
    public int updateNotShowById(String id) {
        RecipelInfo recipelInfo = this.get(id);
        if (ObjectUtil.isNull(recipelInfo)) {
            return 0;
        }
        recipelInfo.setNotShow(0);
        return this.doUpdate(recipelInfo);
    }
}