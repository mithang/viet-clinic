package com.geeke.hosdata.service;


import com.alibaba.fastjson.JSONObject;
import com.geeke.common.constants.BizConstants;
import com.geeke.common.service.CrudService;
import com.geeke.hosdata.dao.HisDrugDao;
import com.geeke.hosdata.entity.DistDto;
import com.geeke.hosdata.entity.HisDrugs;
import com.geeke.stock.entity.Drug;
import com.geeke.stock.service.DrugService;
import com.geeke.stock.service.MedicinalStockControlService;
import com.geeke.sys.utils.SessionUtils;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HisDrugService  extends CrudService<HisDrugDao, HisDrugs> {

    private  final  HisDrugDao hisDrugDao;

    private  final  MedicinalStockControlService medicinalStockControlService;

    private final DrugService drugService;

    @Override
    public List<String> bulkInsert(List<HisDrugs> entitys) {
        //Quan hệ tương ứng giữa bản viện và phòng khám/
        Map<String, String> typeMap = getTypeMap(BizConstants.DICT_TYPE_DRUG_TYPE);
        Map<String, String> dosMap = getTypeMap(BizConstants.DICT_TYPE_DOSAGE_UNIT);
        Map<String, String> preMap = getTypeMap(BizConstants.DICT_TYPE_PREPARATION_UNIT);
        Map<String, String> packMap = getTypeMap(BizConstants.DICT_TYPE_PACK_SPEC);
        JSONObject userObj = SessionUtils.getUserJson();
        List<String> ids = Lists.newArrayList();
        // ITER31: truoc day vong lap `continue` khi thuoc da ton tai nhung
        // dao.bulkInsert(entitys) van chen TOAN BO list goc — dong bi skip
        // (chua preInsert, chua ep company theo session) van bi ghi, voi
        // id/companyId do client gui (tai hien: dong dup ghi thang vao
        // tenant khac voi 200). Chi chen cac dong khong bi skip.
        List<HisDrugs> toInsert = Lists.newArrayList();
        for(HisDrugs entity: entitys) {
            // ITER32: dedup cu so nham he ma — getByCode(standardCode)
            // so standardCode gui len voi cot code (ma thuoc cua vien),
            // nen import lai cung so dang ky nhung khac ma vien, hay cung
            // ma vien nhung dang ky lai, deu lot qua va chen trung dong.
            // Skip khi EITHER trung: code trung (dung getByCode voi
            // chinh code, dung ngu nghia ypdm; cung la ngu nghia UI
            // danh dau da dong bo qua getDrugCode) HOAC standard_code
            // trung (query moi). Moi nhanh chi xet khi gia tri non-blank.
            // Khong doi getByCode cu: HosDepartmentStorageController
            // dung no dung ngu nghia ma thuoc.
            String companyId = userObj.getString("companyId");
            boolean dupByCode = entity.getCode() != null && !entity.getCode().trim().isEmpty()
                    && drugService.getByCode(entity.getCode(), companyId) != null;
            boolean dupByStandard = entity.getStandardCode() != null && !entity.getStandardCode().trim().isEmpty()
                    && drugService.getByStandardCode(entity.getStandardCode(), companyId) != null;
            if(dupByCode || dupByStandard){
                continue;
            }
            entity.setCompanyId(userObj.getString("companyId"));
            entity.setType(typeMap.getOrDefault(entity.getType(),entity.getType()));
            entity.setDosisUnit(dosMap.getOrDefault(entity.getDosisUnit(),entity.getDosisUnit()));
            entity.setPreparationUnit(preMap.getOrDefault(entity.getPreparationUnit(),entity.getPreparationUnit()));
            entity.setPack(packMap.getOrDefault(entity.getPack(),entity.getPack()));
           entity.preInsert();
           ids.add(entity.getId());
           toInsert.add(entity);
        }
        // List rong ma goi bulkInsert se sinh SQL INSERT khong co VALUES
        // (tat ca deu da dong bo) nen chi chen khi co dong moi that su.
        if(!toInsert.isEmpty()) {
            dao.bulkInsert(toInsert);
        }
        ids.forEach(id->{
            Drug drug = drugService.get(id);
            medicinalStockControlService.initStock(drug);
        });
        return ids;
    }

    private Map<String, String> getTypeMap(String type) {
        List<DistDto> dist = hisDrugDao.getDist(type);
        return  dist.stream().collect(Collectors.toMap(DistDto::getDistName,DistDto::getDistValue,(value1, value2) -> value1));
    }

    public List<String> getDrugList(String companyId) {
        return hisDrugDao.getDrugCode(companyId);
    }
}
