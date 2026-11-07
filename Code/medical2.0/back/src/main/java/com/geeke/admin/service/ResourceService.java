package com.geeke.admin.service;

import com.geeke.admin.dao.ResourceDao;
import com.geeke.admin.entity.Resource;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Quản lý tài nguyênService
 * @author lys
 * @version 2021-11-18
 */
 
@Service("resourceService")
@Transactional(readOnly = true)
public class ResourceService extends CrudService<ResourceDao, Resource>{

    // DS107 C1/C10-C12: save/bulkInsert/bulkUpdate truoc day de lo:
    // save voi id khong ton tai roi qua CrudService.save -> doUpdate 0
    // dong roi van 200 silent no-op (verifyTenantOwnershipAgainstStoredRow
    // bo qua khi stored==null, cung mau DS94/DS99); bulkInsert/bulkUpdate
    // bypass @Valid nen code/url/router thieu van ghi duoc (NOT NULL trong
    // DB, @NotNull DS80 chi co tac dung qua @Valid o save don) -> 500.
    // Kiem tra ton tai + truong bat buoc truoc moi ghi. @Transactional
    // (readOnly = false) tuong minh vi class-level la readOnly = true
    // (mau DS105/DS106). Luu y: sys_resource KHONG CO cot company_id
    // (bang toan cuc) nen khong validate company o day; quyen ghi da gat
    // o controller (resource:create/update/delete, iter68). bulkUpdate
    // CASE khong cover router_id nhung van validate router de fail-closed
    // khi row thieu truong bat buoc.
    @Override
    @Transactional(readOnly = false)
    public Resource save(Resource entity) {
        if (entity != null && StringUtils.isNotBlank(entity.getId())
                && dao.get(entity.getId()) == null) {
            throw new ServiceException("Tài nguyên không tồn tại: "
                    + entity.getId());
        }
        return super.save(entity);
    }

    // DS107 C3-C6/C10-C11: bulkInsert truoc day de lo: null/rong roi xuong
    // MyBatis <foreach> rong -> 500; thieu/rong code/url/router/... ->
    // 500 NOT NULL. Chan het o service de 400 ro nghia.
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<Resource> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách tài nguyên không được để trống");
        }
        for (Resource entity : entitys) {
            checkResourceRequired(entity);
        }
        return super.bulkInsert(entitys);
    }

    // DS107 C2/C3/C5/C9/C12: bulkUpdate truoc day de lo: id khong ton tai
    // -> 200 silent no-op; batch hon hop [valid, nonexistent] doi ca dong
    // hop le truoc khi cham id ma; code bi drop -> CASE set code=NULL ->
    // 500 NOT NULL. Tien kiem TOAN BO list truoc moi ghi (fail-closed ca
    // list, mau N1).
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<Resource> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách tài nguyên không được để trống");
        }
        for (Resource entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Tài nguyên không được để trống");
            }
            if (StringUtils.isNotBlank(entity.getId())
                    && dao.get(entity.getId()) == null) {
                throw new ServiceException("Tài nguyên không tồn tại: "
                        + entity.getId());
            }
            checkResourceRequired(entity);
        }
        return super.bulkUpdate(entitys);
    }

    // DS107 C7-C8: bulkDelete null/rong -> 400 ro nghia thay vi roi xuong
    // MyBatis <foreach> rong. bulkDelete id khong ton tai GIU NGUYEN
    // 200 rows 0 idempotent (DS99 G11) -- khong chan o day.
    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<Resource> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách tài nguyên không được để trống");
        }
        return super.bulkDelete(entitys);
    }

    /**
     * DS119 B: mapper <update id="delete"> co nhanh <otherwise> xoa mem
     * theo router_id khi id rong -- delete khong id + router se xoa
     * sach moi resource cua router do. Chan o service: chi xoa khi co
     * id; khong caller noi bo nao goi save/delete/bulk* cua service nay
     * (chi PermissionController doc listAllByVersion/listAll), nen khoa
     * o service la du va khong vo luong noi bo.
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(Resource entity) {
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được tài nguyên cần xóa.");
        }
        // DS131 A: blank da 400 (DS119 B) nhung ghost id van lot qua
        // super.delete -> 200 rows 0 im lang, xoa lap 200 rows 1 (mapper
        // soft-delete khong loc del_flag) -- cung lop DS123/DS129/DS130.
        // Chan truoc bang dao.get (khong loc del_flag nen tra ca dong da
        // xoa mem, delFlag phan biet) theo mau CodeRuleService.delete
        // (DS129 A); KHONG tai dung checkExists (DS103): no cho qua blank
        // va khong check delFlag, lai dung chung voi save/bulkUpdate.
        // bulkDelete ghost GIU NGUYEN 200 rows 0 (DS99/DS106/DS126 B).
        Resource stored = dao.get(entity.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Tài nguyên không tồn tại: "
                    + entity.getId());
        }
        return super.delete(entity);
    }

    private void checkResourceRequired(Resource entity) {
        if (entity == null) {
            throw new ServiceException("Tài nguyên không được để trống");
        }
        if (StringUtils.isBlank(entity.getName())) {
            throw new ServiceException("Tên tài nguyên không được để trống");
        }
        if (StringUtils.isBlank(entity.getCode())) {
            throw new ServiceException("Mã tài nguyên không được để trống");
        }
        if (StringUtils.isBlank(entity.getUrl())) {
            throw new ServiceException("Url không được để trống");
        }
        if (StringUtils.isBlank(entity.getCanPermission())) {
            throw new ServiceException("Có thể ủy quyền không được để trống");
        }
        if (StringUtils.isBlank(entity.getIsDefault())) {
            throw new ServiceException("Mặc định quyền không được để trống");
        }
        if (entity.getRouter() == null
                || StringUtils.isBlank(entity.getRouter().getId())) {
            throw new ServiceException("Tuyến đường không được để trống");
        }
    }

    public List<Resource> listAllByVersion(String roleId){
        return this.dao.listAllByVersion(roleId);
    }


}