package com.geeke.sys.service;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.sys.dao.PropertySetDao;
import com.geeke.sys.entity.PropertySet;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Maps;

/**
 * Quản lý tập thuộc tínhService
 * @author lys
 * @version 2021-12-26
 */
 
@Service("propertySetService")
@Transactional(readOnly = true)
public class PropertySetService extends CrudService<PropertySetDao, PropertySet>{

    /**
     * DS112: CrudService.save re nhanh update khi id khac rong, doUpdate
     * khong dong den dong nao neu id khong ton tai ma van tra 200 (probe
     * A1; ho silent no-op DS94/DS99). Chi chap nhan update khi dong da ton
     * tai; tao moi phai gui id rong. Kiem tra bang dao.get: mapper get()
     * la SELECT don bang sys_property_set, khong JOIN nen dang tin.
     * Cung validate name/propertiesDef o day (probe A11): name null lot
     * qua @Valid (DataEntity.getName chi @Length(min=1), khong @NotNull)
     * roi 500 o cot name NOT NULL; propertiesDef khong co validation nao.
     * Bang toan cuc, khong co cot company_id nen khong co guard tenant o
     * day; san quyen theo thao tac nam o PropertySetController (iter68).
     * LUU Y: class nay la @Transactional(readOnly = true) nen MOI override
     * ghi o day phai co @Transactional rieng (read-write); khong co no,
     * Spring lay readOnly cua class va moi ghi 500 "Connection is
     * read-only" (chi cac method ke thua tu CrudService moi lay
     * @Transactional read-write cua CrudService).
     */
    private void validateRequired(PropertySet entity) {
        if (entity == null) {
            throw new ServiceException("Tập thuộc tính không được để trống");
        }
        if (StringUtils.isBlank(entity.getName())) {
            throw new ServiceException("Tên tập thuộc tính không được để trống");
        }
        if (StringUtils.isBlank(entity.getPropertiesDef())) {
            throw new ServiceException("Định nghĩa thuộc tính không được để trống");
        }
    }

    @Override
    @Transactional
    public PropertySet save(PropertySet entity) {
        validateRequired(entity);
        if (StringUtils.isNotBlank(entity.getId())
                && dao.get(entity.getId()) == null) {
            throw new ServiceException("Tập thuộc tính không tồn tại: "
                    + entity.getId());
        }
        return super.save(entity);
    }

    @Override
    @Transactional
    public List<String> bulkInsert(List<PropertySet> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách tập thuộc tính thêm mới không được để trống");
        }
        for (PropertySet entity : entitys) {
            validateRequired(entity);
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional
    public List<String> bulkUpdate(List<PropertySet> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách tập thuộc tính cập nhật không được để trống");
        }
        for (PropertySet entity : entitys) {
            validateRequired(entity);
            if (StringUtils.isBlank(entity.getId())) {
                throw new ServiceException("Cập nhật hàng loạt yêu cầu id của tập thuộc tính đã tồn tại");
            }
            if (dao.get(entity.getId()) == null) {
                throw new ServiceException("Tập thuộc tính không tồn tại: "
                        + entity.getId());
            }
        }
        return super.bulkUpdate(entitys);
    }

    /**
     * DS124 A: single delete fail-closed 400. CrudService.delete chi chan
     * qua stored-row tenant check ma check do bo qua stored==null, nen
     * delete id ma tra 200 data=0 im lang; mapper delete la UPDATE
     * del_flag khong loc del_flag nen delete dong da xoa mem tra 200
     * rows=1 + audit DELETED trung. Kiem tra ton tai len dau qua dao.get
     * (mapper get khong loc del_flag nen phai kiem tra del_flag='0').
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(PropertySet entity) {
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được tập thuộc tính cần xóa.");
        }
        PropertySet stored = dao.get(entity.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Tập thuộc tính không tồn tại: " + entity.getId());
        }
        verifyTenantOwnershipAgainstStoredRow(entity);
        return super.delete(entity);
    }

    @Override
    @Transactional
    public int bulkDelete(List<PropertySet> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách tập thuộc tính xóa không được để trống");
        }
        return super.bulkDelete(entitys);
    }

}
