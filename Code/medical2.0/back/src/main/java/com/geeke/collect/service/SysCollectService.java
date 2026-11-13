package com.geeke.collect.service;

import java.util.*;

import com.geeke.admin.common.dao.PermissionDAO;
import com.geeke.admin.dao.RouterDao;
import com.geeke.admin.entity.Router;
import com.geeke.common.service.ServiceException;
import com.geeke.utils.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geeke.common.service.CrudService;
import com.geeke.collect.dao.SysCollectDao;
import com.geeke.collect.entity.SysCollect;


/**
 * Mục yêu thíchService
 *
 * @author szy
 * @version 2021-07-28
 */

@Service("sysCollectService")
public class SysCollectService extends CrudService<SysCollectDao, SysCollect> {

    @Autowired
    private SysCollectDao sysCollectDao;

    @Autowired
    private RouterDao routerDao;

    @Autowired
    private PermissionDAO permissionDAO;

    /**
     * DS121 B: save fail-closed 400. Ba lo probe-proven (chi mo khi payload
     * vuot qua @Valid, tuc co company -- xem ds121b-verify.py):
     * (1) save voi id khong ton tai -> 200 silent no-op (mapper UPDATE chi
     * set sort); bat buoc dong phai ton tai.
     * (2) save-create voi routerId ma -> 200 + dong mo coi (sys_collect
     * khong co FK tren router_id); bat buoc router phai ton tai.
     * (3) save voi id dong cua user khac + userId cua minh trong payload
     * lot qua guard chi-doi-chieu-payload cua controller va ghi de sort
     * cua dong do (cung lop DS118 B / iter62 updateBatch, nhung tren
     * nhanh save); doi chieu dong da luu voi payload userId (controller
     * da dam bao payload userId == session).
     */
    @Override
    @Transactional(readOnly = false)
    public SysCollect save(SysCollect entity) {
        if (entity == null) {
            throw new ServiceException("Dữ liệu yêu thích không được để trống");
        }
        if (StringUtils.isNotBlank(entity.getId())) {
            SysCollect stored = sysCollectDao.get(entity.getId());
            if (stored == null) {
                throw new ServiceException("Mục yêu thích không tồn tại");
            }
            String storedUserId = stored.getUserId() != null
                    ? stored.getUserId().getId() : null;
            String payloadUserId = entity.getUserId() != null
                    ? entity.getUserId().getId() : null;
            if (StringUtils.isBlank(storedUserId)
                    || !storedUserId.equals(payloadUserId)) {
                throw new ServiceException("Không có quyền truy cập dữ liệu của người dùng khác");
            }
        } else {
            if (entity.getRouterId() == null
                    || StringUtils.isBlank(entity.getRouterId().getId())
                    || routerDao.get(entity.getRouterId().getId()) == null) {
                throw new ServiceException("Định tuyến không tồn tại");
            }
        }
        return super.save(entity);
    }


    /**
     * di độngsắp xếphàng loạtSửasố thứ tự, Trả vềNgười dùngnhấtmớiThông tin
     *
     * @param sysCollectList
     * @param userId
     * @return
     */
    @Transactional
    public List<SysCollect> updateBatch(List<SysCollect> sysCollectList, String userId) {

        if (CollectionUtils.isNotEmpty(sysCollectList)) {

            // DS248: phan tu null trong list truoc day qua vong pre-check
            // tenant ben duoi (bo qua null) roi NPE 500 o preUpdate(),
            // truoc moi ghi. Fail-closed 400 ca list truoc moi vong
            // (mau N1/DS247); message tai dung save().
            for (SysCollect item : sysCollectList) {
                if (item == null) {
                    throw new ServiceException("Dữ liệu yêu thích không được để trống");
                }
            }

            // N4-P3 (iteration 62): updateBatch mapper WHERE chi theo id,
            // khong loc user_id, nen nhan id dong cua user khac vao list la
            // ghi de duoc favorite cua user do (controller chi check path
            // userId == self, khong check tung item). Verify stored user_id
            // cua moi item truoc khi ghi, fail-closed ca list: stored row
            // thuoc user khac -> 400, khong ghi item nao. Id rong / khong
            // ton tai / stored user rong giu duong cu (mapper khop 0 dong).
            if (StringUtils.isNotBlank(userId)) {
                for (SysCollect item : sysCollectList) {
                    if (item == null || StringUtils.isBlank(item.getId())) {
                        continue;
                    }
                    SysCollect stored = sysCollectDao.get(item.getId());
                    if (stored != null && stored.getUserId() != null
                            && StringUtils.isNotBlank(stored.getUserId().getId())
                            && !userId.equals(stored.getUserId().getId())) {
                        throw new ServiceException("Không có quyền truy cập dữ liệu của người dùng khác");
                    }
                }
            }
            for (int i = 0; i < sysCollectList.size(); i++) {
                SysCollect sysCollect = sysCollectList.get(i);
                sysCollect.preUpdate();
                sysCollect.setSort((long)(i+1));
            }
            //hàng loạtCập nhật
            sysCollectDao.updateBatch(sysCollectList);
        }
        List<SysCollect> sysCollects = sysCollectDao.listAllByUserId(userId);
        //Trả vềnhấtmới của yêu thíchDữ liệu
        return sysCollects != null ? sysCollects : new ArrayList<>();
    }

    /**
     * Theo người dùngTruy vấnyêu thíchDanh sách
     *
     * @param sysCollect
     * @return
     */
    public List<SysCollect> listAllByUserId(String  userId) {
        if (StringUtils.isBlank(userId)) {
            throw new ServiceException("Tham số Người dùng id Không được để trống.");
        }

        List<SysCollect> sysCollectList = sysCollectDao.listAllByUserId(userId);
        return sysCollectList != null ? sysCollectList : new ArrayList<>();
    }

    /**
     * DS126 B: bulkDelete fail-closed 400. SysCollectDao.xml has NO
     * bulkDelete statement (DS121 noted "bulk* untouched, 500 with no
     * write"), so the generic CrudController endpoint always died with
     * a 500 BindingException for ANY list -- probe-proven in
     * tools/ds126b-verify.py ([], [blank-id], [ghost] all 500). There
     * is no UI bulk-delete path for favourites (the UI removes one
     * favourite at a time via the single delete, DS121), and no
     * internal caller uses bulkDelete on this service, so fail closed
     * with a clear 400 instead of a 500 (SysSeting /
     * MedicinalStockRecord precedent). The base CrudService.bulkDelete
     * guard (DS126 B) would only fix the null/empty cases; the throw
     * here covers every list.
     */
    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<SysCollect> entitys) {
        throw new ServiceException("Mục yêu thích không hỗ trợ xóa hàng loạt!");
    }
}