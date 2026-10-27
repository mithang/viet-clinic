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

import java.util.List;
import java.util.Map;
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
        for(HisInstitutions entity : entitys) {
            entity.setCompanyId(userObj.getString("companyId"));
            if("挂号项目".equals(entity.getDllx()) || "Hạng mục khám chữa bệnh".equals(entity.getDllx())){
                           entity.setDllx("Khác");
            }
            entity.setDllx(typeMap.getOrDefault(entity.getDllx(), entity.getDllx()));
            entity.setDw(unitMap.getOrDefault(entity.getDw(),unitMap.get(" lần")));
            entity.preInsert();
            ids.add(entity.getId());
        }
        dao.bulkInsert(entitys);
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
