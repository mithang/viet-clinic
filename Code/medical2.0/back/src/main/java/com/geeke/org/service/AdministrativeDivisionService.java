package com.geeke.org.service;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import com.geeke.org.dao.AdministrativeDivisionDao;
import com.geeke.org.entity.AdministrativeDivision;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Maps;

/**
 * Phân chia khu vực hành chínhService
 * @author txl
 * @version 2022-06-21
 */
 
@Service("administrativeDivisionService")
@Transactional(readOnly = true)
public class AdministrativeDivisionService extends CrudService<AdministrativeDivisionDao, AdministrativeDivision>{

    // DS106 A1/A10-A12: save/bulkInsert/bulkUpdate truoc day de lo:
    // save voi id khong ton tai roi qua CrudService.save -> doUpdate 0
    // dong roi van 200 silent no-op (verifyTenantOwnershipAgainstStoredRow
    // bo qua khi stored==null, cung mau DS94/DS99); bulkInsert/bulkUpdate
    // bypass @Valid nen name rong/thieu van ghi duoc (cot name nullable
    // trong DB, @Length(min=1) tren DataEntity.getName chi co tac dung
    // qua @Valid o save don). Kiem tra ton tai + name truoc moi ghi.
    // @Transactional (readOnly = false) tuong minh vi class-level la
    // readOnly = true (mau ManufactureFactoryService DS105).
    @Override
    @Transactional(readOnly = false)
    public AdministrativeDivision save(AdministrativeDivision entity) {
        if (entity != null && StringUtils.isNotBlank(entity.getId())
                && dao.get(entity.getId()) == null) {
            throw new ServiceException("Khu vực hành chính không tồn tại: "
                    + entity.getId());
        }
        if (entity != null && StringUtils.isBlank(entity.getName())) {
            throw new ServiceException("Tên khu vực không được để trống");
        }
        return super.save(entity);
    }

    // DS106 A3-A6/A10-A11: bulkInsert truoc day de lo: null/rong roi xuong
    // MyBatis <foreach> rong -> 500 SQL syntax (hien tai global handler
    // anh xa mot so loi thanh 400, nhung van chan ro o service de hanh vi
    // khong phu thuoc vao exception mapping); thieu/rong name -> ghi dong
    // name NULL/rong. Chan het o service de 400 ro nghia.
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<AdministrativeDivision> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách khu vực hành chính không được để trống");
        }
        for (AdministrativeDivision entity : entitys) {
            if (entity == null || StringUtils.isBlank(entity.getName())) {
                throw new ServiceException("Tên khu vực không được để trống");
            }
        }
        return super.bulkInsert(entitys);
    }

    // DS106 A2/A3/A5/A9/A12: bulkUpdate truoc day de lo: id khong ton tai
    // -> 200 silent no-op; batch hon hop [valid, nonexistent] doi ca dong
    // hop le truoc khi cham id ma; name bi drop -> CASE set name=NULL.
    // Tien kiem TOAN BO list truoc moi ghi (fail-closed ca list, mau N1).
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<AdministrativeDivision> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách khu vực hành chính không được để trống");
        }
        for (AdministrativeDivision entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Khu vực hành chính không được để trống");
            }
            if (StringUtils.isNotBlank(entity.getId())
                    && dao.get(entity.getId()) == null) {
                throw new ServiceException("Khu vực hành chính không tồn tại: "
                        + entity.getId());
            }
            if (StringUtils.isBlank(entity.getName())) {
                throw new ServiceException("Tên khu vực không được để trống");
            }
        }
        return super.bulkUpdate(entitys);
    }

    // DS106 A7-A8: bulkDelete null/rong -> 400 ro nghia thay vi roi xuong
    // MyBatis <foreach> rong. bulkDelete id khong ton tai GIU NGUYEN
    // 200 rows 0 idempotent (DS99 G11) -- khong chan o day.
    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<AdministrativeDivision> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách khu vực hành chính không được để trống");
        }
        return super.bulkDelete(entitys);
    }

}
