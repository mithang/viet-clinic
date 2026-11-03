package com.geeke.hosdata.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.constants.BizConstants;
import com.geeke.common.service.CrudService;
import com.geeke.hosdata.dao.HisDrugDao;
import com.geeke.hosdata.dao.HisInstitutionDao;
import com.geeke.hosdata.dao.HisMaterialDao;
import com.geeke.hosdata.entity.DistDto;
import com.geeke.hosdata.entity.HisDrugs;
import com.geeke.hosdata.entity.HisMaterials;
import com.geeke.stock.entity.Stuff;
import com.geeke.stock.service.MedicinalStockControlService;
import com.geeke.stock.service.StuffService;
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
public class HisMaterialService  extends CrudService<HisMaterialDao, HisMaterials> {


    private  final HisDrugDao hisDrugDao;

    private  final HisMaterialDao hisMaterialDao;

    private final StuffService stuffService;

    private  final MedicinalStockControlService medicinalStockControlService;



    @Override
    public List<String> bulkInsert(List<HisMaterials> entitys) {
        //Quan hệ tương ứng giữa bản viện và phòng khám/
        Map<String, String> preMap = getTypeMap(BizConstants.DICT_TYPE_PREPARATION_UNIT);
        Map<String, String> packMap = getTypeMap(BizConstants.DICT_TYPE_PACK_SPEC);
        List<String> ids = Lists.newArrayList();
        JSONObject userObj = SessionUtils.getUserJson();
        // ITER32 J2: nhu HisInstitutionService — skip theo dung ngu nghia
        // UI danh dau da dong bo: getMaterialCode = code cua cac dong
        // stuff sync_id=1 trong tenant session; trung trong cung payload
        // cung bi skip; code blank khong dedup. ITER33 K2: id duoc nhan
        // (dong tao tay) cung qua initStock (tai dung stock control da co).
        Set<String> synced = new HashSet<>(getcCodeList(userObj.getString("companyId")));
        List<HisMaterials> toInsert = Lists.newArrayList();
        for(HisMaterials entity : entitys) {
            if (entity.getSfxmCode() != null && !entity.getSfxmCode().trim().isEmpty()
                    && synced.contains(entity.getSfxmCode())) {
                continue;
            }
            // ITER33 K2: nhu HisInstitutionService — ma da co dong tao tay
            // active thi nhan dong do (chi sync_id=1, giu ten/gia) thay vi
            // chen trung; id duoc nhan van qua initStock nhu dong moi
            // (initStock tai dung dong stock control da co neu co).
            if (entity.getSfxmCode() != null && !entity.getSfxmCode().trim().isEmpty()) {
                String adoptedId = hisMaterialDao.findUnsyncedIdByCode(
                        userObj.getString("companyId"), entity.getSfxmCode());
                if (adoptedId != null) {
                    hisMaterialDao.markSynced(adoptedId);
                    ids.add(adoptedId);
                    synced.add(entity.getSfxmCode());
                    continue;
                }
            }
            entity.setCompanyId(userObj.getString("companyId"));
            entity.setDw(preMap.getOrDefault(entity.getDw(), preMap.get("Cái")));
            entity.setGg(packMap.getOrDefault(entity.getGg(), preMap.get("Cái")));
            entity.preInsert();
            ids.add(entity.getId());
            toInsert.add(entity);
            if (entity.getSfxmCode() != null && !entity.getSfxmCode().trim().isEmpty()) {
                synced.add(entity.getSfxmCode());
            }
        }
        if (!toInsert.isEmpty()) {
            dao.bulkInsert(toInsert);
        }
        ids.forEach(id -> {
            Stuff stuff =  stuffService.get(id);
            medicinalStockControlService.initStock(stuff);
        });
        return ids;
    }

    private Map<String, String> getTypeMap(String type) {
        List<DistDto> dist = hisDrugDao.getDist(type);
        return  dist.stream().collect(Collectors.toMap(DistDto::getDistName,DistDto::getDistValue,(value1, value2) -> value1));
    }

    public List<String> getcCodeList(String companyId) {
        return hisMaterialDao.getMaterialCode(companyId);
    }

}
