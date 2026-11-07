package com.geeke.hosdata.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.constants.BizConstants;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.utils.StringUtils;
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
        // DS114: fail-closed 400 thay vi 500 (xem HisDrugService: cung
        // ly do). Kiem tra TOAN BO list truoc khi xu ly de lo [hop le,
        // loi] khong tao tac dung phu. List rong giu 200 no-op nhu cu.
        if (entitys == null) {
            throw new ServiceException("Danh sách hạng mục đồng bộ không được để trống");
        }
        for (HisInstitutions entity : entitys) {
            validateRequired(entity);
        }
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
            // ITER33 K2: ma da co dong tao tay active (sync_id NULL) thi NHAN
            // dong do — chi gan sync_id=1, giu nguyen ten/gia cua phong kham —
            // thay vi chen dong thu 2 cung ma (danh sach UI hien 2 dong cung
            // ma, gia khac nhau, chon nham se thu sai gia). Skip lang thi
            // trang dong bo van hien "chua dong bo" mai (syncNum theo
            // sync_id=1), nen nhan la trang thai cuoi nhat quan duy nhat.
            // Dong xoa mem khong duoc nhan (find chi loc del_flag=0).
            if (entity.getSfxmCode() != null && !entity.getSfxmCode().trim().isEmpty()) {
                String adoptedId = hisInstitutionDao.findUnsyncedIdByCode(
                        userObj.getString("companyId"), entity.getSfxmCode());
                if (adoptedId != null) {
                    hisInstitutionDao.markSynced(adoptedId);
                    ids.add(adoptedId);
                    synced.add(entity.getSfxmCode());
                    continue;
                }
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

    // DS114: cac cot NOT NULL cua bang cost_item ma bulkInsert ghi (id va
    // create/update do preInsert dien; company_id do service ep tu
    // session; is_use/sync_id la hang so trong SQL). dllx/dw nullable
    // nen khong kiem tra.
    private void validateRequired(HisInstitutions entity) {
        if (entity == null) {
            throw new ServiceException("Hạng mục đồng bộ không được để trống");
        }
        if (StringUtils.isBlank(entity.getSfxmmc())) {
            throw new ServiceException("Tên hạng mục đồng bộ không được để trống");
        }
        if (StringUtils.isBlank(entity.getSfxmCode())) {
            throw new ServiceException("Mã hạng mục đồng bộ không được để trống");
        }
        if (StringUtils.isBlank(entity.getPy())) {
            throw new ServiceException("Mã pinyin hạng mục đồng bộ không được để trống");
        }
    }

    private Map<String, String> getTypeMap(String type) {
        List<DistDto> dist = hisDrugDao.getDist(type);
        return  dist.stream().collect(Collectors.toMap(DistDto::getDistName,DistDto::getDistValue,(value1, value2) -> value1));
    }
    public List<String> getcCodeList(String companyId) {
        return hisInstitutionDao.getInstitutionCode(companyId);
    }



}
