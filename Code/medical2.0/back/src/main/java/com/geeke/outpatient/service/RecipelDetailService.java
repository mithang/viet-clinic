package com.geeke.outpatient.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.service.CrudService;
import com.geeke.cure.entity.InspectionCheck;
import com.geeke.cure.service.InspectionCheckService;
import com.geeke.outpatient.dao.RecipelDetailDao;
import com.geeke.outpatient.dao.RecipelInfoDao;
import com.geeke.outpatient.entity.*;
import com.geeke.utils.SessionUtils;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Chi tiết đơn thuốc Service
 * @author txl
 * @version 2022-06-07
 */
 
@Service("recipelDetailService")
@Transactional(readOnly = true)
public class RecipelDetailService extends CrudService<RecipelDetailDao, RecipelDetail>{
    @Autowired
    private RecipelDetailDao recipelDetailDao;

    @Autowired
    private RecipelInfoDao recipelInfoDao;

    @Lazy
    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private InspectionCheckService inspectionCheckService;
    @Transactional
    public void addRetailrecipelDetails(List<RecipelDetail> recipelDetails, RecipelInfo recipelInfo) {
        for (RecipelDetail recipelDetail : recipelDetails) {
            recipelDetail.setRecipelInfo(recipelInfo);
            if (null != recipelInfo.getCompany()) {
                recipelDetail.setCompany(recipelInfo.getCompany());
            }
            DrugStuffEvt drugStuffId = recipelDetail.getDrugStuffId();
            super.save(recipelDetail);
        }

    }

    // Đơn thuốcidLấy chi tiết đơn thuốc
    @Transactional(readOnly = true)
    public List<RecipelDetail> getRecipelDetail(String id) {
        List<RecipelDetail> recipelDetail = recipelDetailDao.getRecipelDetail(id);
        return recipelDetail;
    }

    // Đơn thuốcidLấyhạng mụcTiến độ thực hiện
    @Transactional
    public int schedule(String id) {
        // NếuTrả vềGiá trị là âmsốthìChưahoàn thành, Nếu là 0thìĐãhoàn thành
        int recipelDetail = recipelDetailDao.schedule(id);
        return recipelDetail;
    }

    //thống kêChưahết hạn của Đãbịchiếm dụng của Thuốc
    public List<RecipelStastics> getDetailStasticsForOccupy(int days)
    {
        return this.dao.getDetailStasticsForOccupy(SessionUtils.getLoginTenantId(),days);
    }

    //thống kêHiện tạiCần của Thuốc
    public List<RecipelStastics> getDetailStasticsForNowByRecordId(String medicalRecordId)
    {
        return this.dao.getDetailStasticsForNowByRecordId(medicalRecordId,SessionUtils.getLoginTenantId());
    }

    public List<RecipelDetail> getByRecipelInfoId(String recipelInfoId)
    {

        return this.dao.getByRecipelInfoId(recipelInfoId);
    }

    /**
     * hàng loạtTruy vấnnhiềuCáiĐơn thuốc của Chi tiết
     * @param recipelInfoIds Đơn thuốcIDDanh sách
     * @return theoĐơn thuốcID phútnhóm của Chi tiếtMap
     */
    public Map<String, List<RecipelDetail>> getByRecipelInfoIds(List<String> recipelInfoIds) {
        if (CollectionUtils.isEmpty(recipelInfoIds)) {
            return Collections.emptyMap();
        }
        List<RecipelDetail> details = this.dao.getByRecipelInfoIds(recipelInfoIds);
        return details.stream().collect(Collectors.groupingBy(
                d -> d.getRecipelInfo().getId()
        ));
    }

    @Transactional(readOnly = false)
    public int batchDelete(List<RecipelDetail> var1){
        return this.dao.batchDelete(var1);
    }

    @Transactional(readOnly = false)
    public void updateById(BigDecimal executions, String id, String stuffType, int infuseType, String cureId){
        if("3".equals(stuffType)){
            int i = recipelDetailDao.updateById(executions, id,0);
            if(i>0){
                RecipelDetail recipelDetail = this.dao.get(id);
                List<RecipelDetail> recipelDetails = recipelDetailDao.findByInfoId(recipelDetail.getRecipelInfo().getId(),recipelDetail.getStuffType());
                int count=0;
                if(recipelDetails!=null){
                    for (RecipelDetail recipelDetail1:
                            recipelDetails) {
                        if(recipelDetail1.getTotal().equals(recipelDetail1.getExecutions())){
                            count++;
                        }
                    }
                    //NếuTất cả các hạng mục đều hoàn thành của nói, thìSửaĐơn thuốc của cờ
                    if(count==recipelDetails.size()){
                        int i1 = recipelInfoDao.updateById(1,0, recipelDetail.getRecipelInfo().getId());
                        if(i1>0){
                            RecipelInfo recipelInfo = recipelInfoDao.get(recipelDetail.getRecipelInfo().getId());
                            Registration registration = registrationService.get(recipelInfo.getRegistration().getId());
                            registration.setTreatmentDate(new Date());
                            registrationService.save(registration);
                            
                            // Cập nhật trạng thái xét nghiệm - kiểm tra của đơn thuốc này thành đã điền
                            List<InspectionCheck> inspectionChecks = inspectionCheckService.getByRecipelInfoId(recipelInfo.getId());
                            if (!CollectionUtils.isEmpty(inspectionChecks)) {
                                for (InspectionCheck inspectionCheck : inspectionChecks) {
                                    inspectionCheck.setStatus("1");
                                    inspectionCheckService.save(inspectionCheck);
                                }
                            }
                        }
                    }else {
                        recipelInfoDao.updateById(0,0,recipelDetail.getRecipelInfo().getId());
                    }
                }
            }

        }else {
            RecipelDetail recipelDetail = this.dao.get(id);
            List<RecipelDetail> recipelDetails = recipelDetailDao.findByInfoId(recipelDetail.getRecipelInfo().getId(),recipelDetail.getStuffType());
            for (RecipelDetail recipelDetail1:
            recipelDetails) {
                if(recipelDetail.getInfuseGroup()==recipelDetail1.getInfuseGroup()){
                    if(null==cureId||cureId.isEmpty()){
                        recipelDetailDao.updateById(executions, recipelDetail1.getId(),1);
                    }else {
                        recipelDetailDao.updateById(executions, recipelDetail1.getId(),0);
                    }
                }
            }
            List<RecipelDetail> recipelDetails2 = recipelDetailDao.findByInfoId(recipelDetail.getRecipelInfo().getId(),recipelDetail.getStuffType());
            int count=0;
            if(recipelDetails2!=null){
                for (RecipelDetail recipelDetail1:
                        recipelDetails2) {
                    if(recipelDetail1.getDays().getName()==recipelDetail1.getExecutions().toString()){
                        count++;
                    }
                }
                //NếuTất cả các hạng mục đều hoàn thành của nói, thìSửaĐơn thuốc của cờ
                if(count==recipelDetails2.size()){
                    int i1 = recipelInfoDao.updateById(1,1, recipelDetail.getRecipelInfo().getId());
                    if(i1>0){
                        RecipelInfo recipelInfo = recipelInfoDao.get(recipelDetail.getRecipelInfo().getId());
                        Registration registration = registrationService.get(recipelInfo.getRegistration().getId());
                        registration.setTreatmentDate(new Date());
                        registrationService.save(registration);
                        
                        // Cập nhật trạng thái xét nghiệm - kiểm tra của đơn thuốc này thành đã điền
                        List<InspectionCheck> inspectionChecks = inspectionCheckService.getByRecipelInfoId(recipelInfo.getId());
                        if (!CollectionUtils.isEmpty(inspectionChecks)) {
                            for (InspectionCheck inspectionCheck : inspectionChecks) {
                                inspectionCheck.setStatus("1");
                                inspectionCheckService.save(inspectionCheck);
                            }
                        }
                    }
                }else {
                    recipelInfoDao.updateById(0,0,recipelDetail.getRecipelInfo().getId());
                }
            }
        }
    }
}