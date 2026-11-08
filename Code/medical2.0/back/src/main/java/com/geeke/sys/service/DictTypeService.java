package com.geeke.sys.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geeke.common.constants.ActionConstants;
import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.data.SearchParamsBuilder;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.sys.dao.DictItemDao;
import com.geeke.sys.dao.DictTypeDao;
import com.geeke.sys.entity.Action;
import com.geeke.sys.entity.ActionRecycle;
import com.geeke.sys.entity.DictItem;
import com.geeke.sys.entity.DictType;
import com.geeke.utils.Reflections;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Lists;

/**
 * Loại từ điểnService
 * @author lys
 * @version 2021-12-07
 */
 
@Service("dictTypeService")
@Transactional(readOnly = true)
public class DictTypeService extends CrudService<DictTypeDao, DictType>{

    @Autowired
    private DictItemDao dictItemDao;

    /**
     * viết lạiTruy vấn phân trang: Hệ thốngTừ điển cấp này hiển thị cho tất cả tenant, Từ điển cấp nghiệp vụ cách ly theo tenant
     */
    @Override
    public Page<DictType> listPage(List<Parameter> parameters, int offset, int limit, String orderby) {
        // Từ điển đã được tối ưu thành dùng chung toàn cục, Không còn cách ly theo tenant
        PageRequest pageRequest = new PageRequest(offset, limit, parameters, orderby);
        int total = dao.count(pageRequest);
        List<DictType> list = total > 0 ? dao.listPage(pageRequest) : java.util.Collections.emptyList();
        return new Page<>(total, list);
    }

    /**
     * viết lạiDanh sáchTruy vấn: Từ điển dùng chung toàn cục, Tất cả tenant đều thấy được
     */
    @Override
    public List<DictType> listAll(List<Parameter> parameters, String orderby) {
        PageRequest pageRequest = new PageRequest(parameters, orderby);
        return dao.listAll(pageRequest);
    }

    @Override
    @Cacheable(value = "dict:type", key = "#id", unless = "#result == null")
    public DictType get(String id) {
        DictType dictType = super.get(id);
        // DS42: super.get() tra null khi dong khong ton tai (hop dong cua
        // CrudService.get); truoc day dong tiep theo deref dictType.getId()
        // -> NPE 500 o GET /sys/dictType/{id} voi id la, va o importJson
        // (nhanh create `get(id)==null` khong bao gio dung duoc, ca id
        // trong lan id khong ton tai deu 500 — cung mau NPE ma ITER67 (8c)
        // da sua o GenTableService.importJson bang super.get()). Tra null
        // nhu get() goc; controller tra 200 data null nhu cac get() khac.
        // `unless` de null khong bi cache lai neu sau nay bat cache
        // (hien tai spring.cache.type=none nen annotation la no-op).
        if (dictType == null) {
            return null;
        }

        /*Lấy danh sách bảng con   Mục từ điển*/
        SearchParamsBuilder builder = SearchParamsBuilder.create()
                .eq("dict_type_id", dictType.getId());
        PageRequest pageRequest = new PageRequest(builder.build());
        dictType.setDictItemList(dictItemDao.listAll(pageRequest));
        return dictType;
    }

    /** Theo từ điểnnameLấy từ điểnvalue */
    // DS151: tra null khi khong tim thay, KHONG nem exception. getValue
    // chay sau proxy @Transactional(readOnly = true) cua service nay:
    // bat ky exception nao thoat khoi day (truoc day: NPE o
    // value.getValue() khi ten la, TooManyResultsException khi ten
    // trung) deu danh dau transaction cua excel() rollback-only -> ca
    // file upload rollback voi warning 50001 mo, khong con dong nao
    // luu (probe pre A1/A2). Tra null de ExcelImportBuilder nem
    // IllegalArgumentException ngay trong builder (khong vuot proxy)
    // va bao loi tung dong ro ten muc. Ten trung (vd "Viên" x3):
    // DAO da LIMIT 1 + del_flag='0' + ORDER BY create_date, id nen
    // lay dong som nhat mot cach xac dinh (medicalPreparationUnit_0).
    public String getValue(String name, String id) {
        DictItem value = dictItemDao.getValue(name, id);
        if (value == null) {
            return null;
        }
        return value.getValue();
    }

    @Override
    @Transactional(readOnly = false)
    @Caching(evict = {
        @CacheEvict(value = "dict:type", key = "#dictType.id"),
        @CacheEvict(value = "dict:itemsByCode", allEntries = true)
    })
    public DictType save(DictType dictType) {
        // Hệ thốngcấptừ điển của  company_id Nên là  NULL
        if ("1".equals(dictType.getIsSystem())) {
            dictType.setCompany(null);
        }
        // DS104 A1: save voi id khong ton tai truoc day roi qua
        // CrudService.save -> doUpdate 0 dong roi van 200 silent no-op
        // (verifyTenantOwnershipAgainstStoredRow bo qua khi stored==null,
        // cung mau DS94/DS99). Kiem tra ton tai truoc moi ghi.
        if (StringUtils.isNotBlank(dictType.getId())
                && dao.get(dictType.getId()) == null) {
            throw new ServiceException("Loại từ điển không tồn tại: "
                    + dictType.getId());
        }
        DictType dictTypeTemp = super.save(dictType);
        if (StringUtils.isNoneBlank(dictTypeTemp.getId())) {

            /* LưuconbảngDữ liệu     Mục từ điển */
            saveDictItemList(dictTypeTemp);
        }
        return dictTypeTemp;
    }

    // DS104 A3-A8/A10-A12: bulkInsert/bulkUpdate/bulkDelete generic truoc
    // day de lo: null/rong roi xuong MyBatis <foreach> rong -> 500 SQL
    // syntax (chi vo tinh ve 400 nho handler DS94, sai message), thieu
    // truong NOT NULL (name/code/is_system) -> 500 DataIntegrityViolation,
    // bulkUpdate id khong ton tai -> 200 silent no-op. Chan het o service
    // de 400 ro nghia, kiem tra truoc moi ghi de batch hon hop khong doi
    // dong hop le (A9). @Transactional(readOnly = false) tuong minh la bat
    // buoc vi class-level la readOnly = true (mau DepartmentService DS103).
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<DictType> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách loại từ điển không được để trống");
        }
        for (DictType dictType : entitys) {
            if (StringUtils.isBlank(dictType.getName())) {
                throw new ServiceException("Tên loại từ điển không được để trống");
            }
            if (StringUtils.isBlank(dictType.getCode())) {
                throw new ServiceException("Mã loại từ điển không được để trống");
            }
            if (StringUtils.isBlank(dictType.getIsSystem())) {
                throw new ServiceException("Cấp hệ thống không được để trống");
            }
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<DictType> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách loại từ điển không được để trống");
        }
        for (DictType dictType : entitys) {
            if (StringUtils.isNotBlank(dictType.getId())
                    && dao.get(dictType.getId()) == null) {
                throw new ServiceException("Loại từ điển không tồn tại: "
                        + dictType.getId());
            }
        }
        return super.bulkUpdate(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<DictType> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách loại từ điển không được để trống");
        }
        return super.bulkDelete(entitys);
    }

    @Transactional(readOnly = false)
    public DictType importJson(DictType dictType) {
        // Không cóBản ghi của tình hìnhdưới, trướcThêm, nhưngsaulạiQuaCập nhật
        if(get(dictType.getId()) == null ){
        	// Thêm
        	dictType.preInsert();
        	doInsert(dictType);
        	this.saveAction(this.createAction(ActionConstants.ACTION_CREATED, dictType));
        }
        
        // Cập nhật
        DictType dictTypeTemp = this.save(dictType);
        return dictTypeTemp;
    }

    /**
     * Xóa
     * @param entity
     */
    @Override
    @Transactional(readOnly = false)
    @Caching(evict = {
        @CacheEvict(value = "dict:type", key = "#dictType.id"),
        @CacheEvict(value = "dict:itemsByCode", allEntries = true)
    })
    public int delete(DictType dictType) {
        // DS134 B: blank/ghost/da xoa mem (del_flag '1') -> 400 fail-closed
        // (truoc day: blank/ghost -> 200 data 0 im lang, xoa lan 2 -> 200
        // rows 1 vi mapper soft-delete khong loc del_flag; sys_dict_type
        // la bang GLOBAL khong co company_id nen khong co tenant guard
        // nao -- lop DS123, tien le Theme/DS131 B). Check DAT TRUOC
        // cascade xoa muc con: ghost khong duoc cham vao muc con.
        // checkExists (DS103) KHONG tai dung: cho qua blank id, khong
        // check delFlag. bulkDelete ghost CO TINH GIU 200 rows 0
        // (DS99/DS106/DS126 B idempotency).
        if (dictType == null || StringUtils.isBlank(dictType.getId())) {
            throw new ServiceException("Không xác định được loại từ điển cần xóa.");
        }
        DictType stored = dao.get(dictType.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Loại từ điển không tồn tại: " + dictType.getId());
        }
        /* Xử lý bảng con     Mục từ điển */
        List<Parameter> params = SearchParamsBuilder.create()
                .eq("dict_type_id", dictType.getId())
                .build();
        PageRequest pageRequest = new PageRequest(params);
        dictType.setDictItemList(dictItemDao.listAll(pageRequest));        

        if(dictType.getDictItemList() != null && dictType.getDictItemList().size() > 0) {
            dictItemDao.bulkDelete(dictType.getDictItemList());
        }

        int rows = super.delete(dictType);
        return rows;
    }



    /**
     * Tạo nhật ký thao tác
     * @param actionTypeId  Loại thao tácId
     * @param entity        Đối tượng thực thể được thao tác
     * @return
     */
    @Override
    protected Action createAction(String actionTypeId, DictType entity) {
        Action action = super.createAction(actionTypeId, entity);
        if(action == null) {
            return null;
        }
        // Khi xóa, ghi lại và lưu dữ liệu đã lưu vào thùng rác
        if(ActionConstants.ACTION_DELETED.equals(actionTypeId)) {   
            for(DictItem child: entity.getDictItemList()) {
                ActionRecycle recycle = new ActionRecycle();
                recycle.setTableName(child.getBusTableName());
                recycle.setObjectId(child.getId());
                recycle.setObjectName((String)Reflections.invokeGetter(child, "name"));
                action.getActionRecycleList().add(recycle);            
            }
        }
        return action;
    }
    
    /* LưuconbảngDữ liệu     Mục từ điển */
    private void saveDictItemList(DictType dictType) {
        SearchParamsBuilder builder = SearchParamsBuilder.create()
                .eq("dict_type_id", dictType.getId());
        PageRequest pageRequest = new PageRequest(builder.build());
        List<DictItem> list_DictItem = dictItemDao.listAll(pageRequest);
        List<DictItem> deletes = Lists.newArrayList(); // Danh sách xóa
        List<DictItem> inserts = Lists.newArrayList(); // Danh sách thêm
        List<DictItem> updates = Lists.newArrayList(); // Danh sách cập nhật
        // K1: bulkUpdate theo id trần, không có dict_type_id, nên chỉ cho cập
        // nhật id thuộc đúng loại này; id lạ (của loại khác) -> 400, rollback
        // cả save, và kiểm tra trước mọi ghi để không xóa nhầm trước khi ném.
        java.util.Set<String> ownedIds = new java.util.HashSet<>();
        for(DictItem dictItemSaved: list_DictItem) {
            ownedIds.add(dictItemSaved.getId());
        }
        for (DictItem dictItem : dictType.getDictItemList()){
            if (StringUtils.isBlank(dictItem.getId())) {
                dictItem.setDictType(dictType);
                dictItem.preInsert();
                inserts.add(dictItem);
            } else {
                if (!ownedIds.contains(dictItem.getId())) {
                    throw new ServiceException("Mục từ điển không thuộc loại từ điển này");
                }
                dictItem.preUpdate();
                updates.add(dictItem);
            }
        }
        for(DictItem dictItemSaved: list_DictItem) {
            boolean found = false;
            for (DictItem dictItem : dictType.getDictItemList()){
               if(dictItemSaved.getId().equals(dictItem.getId())){
                   found = true;
                   break;
               }
            }
            if(!found) {
               deletes.add(dictItemSaved);
            }
        }
        if(deletes.size() > 0) {
            dictItemDao.bulkDelete(deletes);
        }
        if(updates.size() > 0) {
            dictItemDao.bulkUpdate(updates);
        }
        if(inserts.size() > 0) {
            dictItemDao.bulkInsert(inserts);
        }
    }
    
}