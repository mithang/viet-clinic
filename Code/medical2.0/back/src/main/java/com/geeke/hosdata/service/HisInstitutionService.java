package com.geeke.hosdata.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.constants.BizConstants;
import com.geeke.common.service.CrudService;
import com.geeke.hosdata.dao.HisDrugDao;
import com.geeke.hosdata.dao.HisInstitutionDao;
import com.geeke.hosdata.entity.DistDto;
import com.geeke.hosdata.entity.HisDrugs;
import com.geeke.hosdata.entity.HisInstitutions;
import com.geeke.hosdata.entity.HisMaterials;
import com.geeke.sys.utils.SessionUtils;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HisInstitutionService  extends CrudService<HisInstitutionDao, HisInstitutions>{
    private  final HisDrugDao hisDrugDao;

    private final HisInstitutionDao hisInstitutionDao;
    @Override
    public List<String> bulkInsert(List<HisInstitutions> entitys) {
        //Quan hệ tương ứng giữa bản viện và phòng khám/
        Map<String, String> typeMap= getTypeMap(BizConstants.DICT_TYPE_INSTITUTION_TYPE);
        Map<String, String> unitMap = getTypeMap(BizConstants.DICT_TYPE_INSTITUTION_UNIT);
        List<String> ids = Lists.newArrayList();
        JSONObject userObj = SessionUtils.getUserJson();
        // ITER32 J2: truoc day khong co dedup API — gui lai cung danh muc
        // HIS se chen trung cost_item/stuff. Skip theo dung ngu nghia UI
        // danh dau da dong bo: getInstitutionCode = item_code cua cac
        // dong sync_id=1 trong tenant session. Them code vua chap nhan
        // vao tap de trung trong cung mot payload cung bi skip; code
        // blank khong dedup (khong co khoa de so).
        Set<String> synced = new HashSet<>(getcCodeList(userObj.getString("companyId")));
        List<HisInstitutions> toInsert = Lists.newArrayList();
        for(HisInstitutions entity : entitys) {
            if (entity.getSfxmCode() != null && !entity.getSfxmCode().trim().isEmpty()
                    && synced.contains(entity.getSfxmCode())) {
                continue;
            }
            entity.setCompanyId(userObj.getString("companyId"));
            if("挂号项目".equals(entity.getDllx()) || "Hạng mục khám chữa bệnh".equals(entity.getDllx())){
                           entity.setDllx("Khác");
            }
            entity.setDllx(typeMap.getOrDefault(entity.getDllx(), entity.getDllx()));
            entity.setDw(unitMap.getOrDefault(entity.getDw(),unitMap.get(" lần")));
            entity.preInsert();
            ids.add(entity.getId());
            toInsert.add(entity);
            if (entity.getSfxmCode() != null && !entity.getSfxmCode().trim().isEmpty()) {
                synced.add(entity.getSfxmCode());
            }
        }
        // List rong ma goi bulkInsert se sinh SQL INSERT khong co VALUES
        // (tat ca deu da dong bo) nen chi chen khi co dong moi that su.
        if (!toInsert.isEmpty()) {
            dao.bulkInsert(toInsert);
        }
        return ids;
    }

    private Map<String, String> getTypeMap(String type) {
        List<DistDto> dist = hisDrugDao.getDist(type);
        return  dist.stream().collect(Collectors.toMap(DistDto::getDistName,DistDto::getDistValue,(value1, value2) -> value1));
    }
    public List<String> getcCodeList(String companyId) {
        return hisInstitutionDao.getInstitutionCode(companyId);
    }



}
