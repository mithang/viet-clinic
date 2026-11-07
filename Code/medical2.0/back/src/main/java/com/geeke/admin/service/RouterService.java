package com.geeke.admin.service;

import com.geeke.admin.dao.RouterDao;
import com.geeke.admin.entity.Router;
import com.geeke.common.service.ServiceException;
import com.geeke.common.service.TreeService;
import com.geeke.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Quản lý tuyến đườngService
 * @author lys
 * @version 2021-11-18
 */

@Service("routerService")
@Transactional(readOnly = true)
public class RouterService extends TreeService<RouterDao, Router> {


 public List<Router> getAllRoutersByVersion(String roleId)
 {
     return this.dao.listAllByVersion(roleId);
 }

    public List<String> getUserIndateWarning(String userId) {
        List<String> userIndateWarning = this.dao.getUserIndateWarning(userId);
        return userIndateWarning;
    }

    // DS117: TreeService/CrudService generic khong kiem tra ton tai,
    // khong validate NOT NULL (bulk bypass @Valid) — save id la 200
    // silent, save parent la 500 (parent_id NOT NULL), save thieu
    // sort/isLocked 500 NOT NULL, bulk [null] NPE 500, bulkUpdate id la
    // 200 silent, bulkInsert thieu name/properties/sort 500 NOT NULL.
    // Controller da san quyen theo thao tac (iter68: router:create/
    // update/delete, chi role 2000 + 4000) nen day chi them fail-closed
    // 400 o tang service; bulkDelete id la giu 200 rows 0 (DS99 G11).
    // bulk* con sua mot wart cay: TreeEntity.preInsert/preUpdate tinh
    // lai ids tu parent object cua payload — GET tra parent khong co
    // ids nen bulkUpdate am tham ghi de ids thanh "<id>." (mat tien to
    // "0."), bulkInsert cung vay. Giai quyet parent tu DB (nhu
    // TreeService.save lam cho single save) truoc khi goi super, de
    // ids luon tinh dung.
    @Override
    @Transactional(readOnly = false)
    public Router save(Router entity) {
        if (entity == null) {
            throw new ServiceException("Tuyến đường không được để trống");
        }
        validateRequired(entity);
        if (StringUtils.isNotBlank(entity.getId())
                && dao.get(entity.getId()) == null) {
            throw new ServiceException("Tuyến đường không tồn tại");
        }
        validateParentExists(entity);
        return super.save(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<Router> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách tuyến đường không được để trống");
        }
        for (Router entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Tuyến đường không được để trống");
            }
            validateRequired(entity);
            // bulk bypass TreeService.save nen khong co root-defaulting:
            // parent null -> insert parent_id NULL 500.
            if (entity.getParent() == null
                    || StringUtils.isBlank(entity.getParent().getId())) {
                throw new ServiceException("Tuyến đường cha không được để trống");
            }
            resolveParent(entity);
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<Router> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách tuyến đường không được để trống");
        }
        for (Router entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Tuyến đường không được để trống");
            }
            if (StringUtils.isBlank(entity.getId())) {
                throw new ServiceException("Tuyến đường không được để trống");
            }
            if (dao.get(entity.getId()) == null) {
                throw new ServiceException("Tuyến đường không tồn tại");
            }
            validateRequired(entity);
            if (entity.getParent() == null
                    || StringUtils.isBlank(entity.getParent().getId())) {
                throw new ServiceException("Tuyến đường cha không được để trống");
            }
            resolveParent(entity);
        }
        return super.bulkUpdate(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<Router> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách tuyến đường không được để trống");
        }
        for (Router entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Tuyến đường không được để trống");
            }
        }
        return super.bulkDelete(entitys);
    }

    // name/canPermission/properties da co @NotNull (@Valid o single
    // save) nhung bulk bypass @Valid; sort/isLocked khong co @NotNull
    // o ca hai duong — tat ca deu NOT NULL trong sys_router.
    private void validateRequired(Router entity) {
        if (StringUtils.isBlank(entity.getName())) {
            throw new ServiceException("Tên router không được để trống");
        }
        if (StringUtils.isBlank(entity.getCanPermission())) {
            throw new ServiceException("Có thể phân phối không được để trống");
        }
        if (StringUtils.isBlank(entity.getProperties())) {
            throw new ServiceException("Thuộc tính mở rộng không được để trống");
        }
        if (entity.getSort() == null) {
            throw new ServiceException("Sắp xếp không được để trống");
        }
        if (entity.getIsLocked() == null) {
            throw new ServiceException("Vô hiệu hóa không được để trống");
        }
    }

    // Single save: parent null/blank/"0" la nut goc (TreeService.save
    // tu tao parent ids "0."); parent id khac phai ton tai, neu khong
    // TreeService dat parent null -> insert parent_id NULL 500.
    private void validateParentExists(Router entity) {
        if (entity.getParent() == null
                || StringUtils.isBlank(entity.getParent().getId())
                || "0".equals(entity.getParent().getId())) {
            return;
        }
        if (dao.get(entity.getParent().getId()) == null) {
            throw new ServiceException("Tuyến đường cha không tồn tại");
        }
    }

    // Giai quyet parent tu DB de TreeEntity.preInsert/preUpdate tinh
    // ids dung ("0." cho nut goc, ids cua parent that cho nut con).
    // Goi sau khi da validate parent co id; parent la phai ton tai.
    private void resolveParent(Router entity) {
        String parentId = entity.getParent().getId();
        if ("0".equals(parentId)) {
            Router root = new Router();
            root.setId("0");
            root.setIds("0.");
            entity.setParent(root);
            return;
        }
        Router stored = dao.get(parentId);
        if (stored == null) {
            throw new ServiceException("Tuyến đường cha không tồn tại");
        }
        entity.setParent(stored);
    }
}