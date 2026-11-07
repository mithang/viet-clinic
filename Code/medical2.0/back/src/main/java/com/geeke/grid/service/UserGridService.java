package com.geeke.grid.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.admin.dao.UserDao;
import com.geeke.admin.entity.User;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.grid.dao.UserGridDao;
import com.geeke.grid.entity.UserGrid;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Bố cục tùy chỉnhService
 * @author ycy
 * @version 2021-10-12
 */
 
@Service("userGridService")
@Transactional(readOnly = true)
public class UserGridService extends CrudService<UserGridDao, UserGrid>{

    @Autowired
    private UserDao userDao;

    @Override
    @Transactional(readOnly = false)
    public UserGrid save(UserGrid userGrid) {
        // DS117: fail-closed truoc khi vao guard N5-P4 / upsert.
        // - null entity: truoc day exists() NPE 500 (chi framework chan
        //   JSON-null o tang controller; service tu ve).
        // - routerId null: khong co @NotNull, insert router_id NULL 500
        //   (userId da co @NotNull o @Valid nhung service van kiem tra
        //   phong thu cho caller truc tiep).
        // - id co nhung khong ton tai: truoc day super.save -> doUpdate 0
        //   dong -> 200 silent no-op.
        if (userGrid == null) {
            throw new ServiceException("Bố cục tùy chỉnh không được để trống");
        }
        if (userGrid.getUserId() == null) {
            throw new ServiceException("Người dùngidKhông được để trống");
        }
        if (userGrid.getRouterId() == null) {
            throw new ServiceException("Định tuyếnidKhông được để trống");
        }
        if (StringUtils.isNotBlank(userGrid.getId())
                && dao.get(userGrid.getId()) == null) {
            throw new ServiceException("Bố cục tùy chỉnh không tồn tại");
        }
        // N5-P4 (iteration 62): save ghi de toan bo CrudService.save nen
        // khong chay guard nao; user_grid khong co cot company va mapper
        // get() khong select company nen guard stored-company luon pass.
        // Mapper update WHERE id + userId deu tu payload, nen save voi
        // userId cua user tenant khac + company OWN ghi de duoc layout cua
        // user do (probe live N5). Verify user dich thuoc session tenant
        // truoc ca hai nhanh (exists -> doUpdate va super.save): tu-doi
        // (userId == session user) khong check; nguoc lai, neu session co
        // tenant thi user dich phai thuoc tenant do — theo user_tenant
        // active (userDao.getClinicId, tien le P1), neu khong co dong
        // user_tenant thi fallback ve sys_user.company_id da luu
        // (userDao.get); id khong ton tai / dong mo coi khong company giu
        // hanh vi cu (mapper khop 0 dong -> 200, nhu tien le orphan N3c).
        // ServiceException -> 400 qua GlobalExceptionHandler.
        if (userGrid != null && userGrid.getUserId() != null) {
            User sessionUser = SessionUtils.getUser();
            String targetUserId = String.valueOf(userGrid.getUserId());
            boolean self = sessionUser != null && targetUserId.equals(sessionUser.getId());
            if (!self) {
                String loginTenant = SessionUtils.getLoginTenantId();
                if (StringUtils.isNotBlank(loginTenant) && !"null".equals(loginTenant)) {
                    List<String> clinics = userDao.getClinicId(targetUserId);
                    if (clinics != null && !clinics.isEmpty()) {
                        if (!clinics.contains(loginTenant)) {
                            throw new ServiceException("Không có quyền truy cập tenant khácDữ liệu");
                        }
                    } else {
                        User stored = userDao.get(targetUserId);
                        if (stored != null && stored.getCompany() != null) {
                            String storedCompanyId = stored.getCompany().getId();
                            if (StringUtils.isNotBlank(storedCompanyId) && !loginTenant.equals(storedCompanyId)) {
                                throw new ServiceException("Không có quyền truy cập tenant khácDữ liệu");
                            }
                        }
                    }
                }
            }
        }
        Map<String, String> colMaps = Maps.newHashMap();
        // Người dùngphát hànhcụcduy nhất
        colMaps.clear();
        colMaps.put("user_id", "userId");
        colMaps.put("router_id", "routerId");
        
        if(exists(dao, userGrid, colMaps)) { // mộtCáiNgười dùngtạimộtCái tuyến đườngdưới chỉcó thểcómộtCáiphát hànhcục
            // DS117: upsert theo cap (user_id, router_id) — truoc day
            // doUpdate() voi id RONG khop 0 dong -> 200 silent no-op,
            // thay doi layout cua client cu bi mat ma UI van bao thanh
            // cong. Tim dong da co cua cap va gan id cua no truoc khi
            // update; khong tim thay (khong the xay ra voi cung tham so
            // nhu exists()) thi 400 thay vi im lang.
            if (StringUtils.isBlank(userGrid.getId())) {
                List<UserGrid> found = dao.listAll(new PageRequest(Lists.newArrayList(
                        new Parameter("user_id", "=", userGrid.getUserId()),
                        new Parameter("router_id", "=", userGrid.getRouterId()))));
                if (found == null || found.isEmpty()) {
                    throw new ServiceException("Bố cục tùy chỉnh không tồn tại");
                }
                userGrid.setId(found.get(0).getId());
            }
            // DS117: nhanh upsert cu goi thang doUpdate() ma khong goi
            // preUpdate() nen update_by/update_date NULL -> 500 NOT NULL
            // ngay khi update that su ghi dong (truoc day id rong khop 0
            // dong nen loi nay bi che boi silent no-op). CrudService.save
            // nhanh update luon preUpdate() truoc doUpdate().
            userGrid.preUpdate();
            this.doUpdate(userGrid);
            return userGrid;
        }

        UserGrid userGridTemp = super.save(userGrid);
        return userGridTemp;
    }

    // DS117: bulk* generic cua CrudService khong kiem tra null/empty/
    // [null], khong kiem tra ton tai, khong validate NOT NULL (bulk
    // bypass @Valid) — [null] NPE 500, bulkUpdate id la 200 silent,
    // bulkInsert thieu userId/routerId 500 NOT NULL. Controller da chan
    // cheo user (iter68/DS48, 403) nen day chi them fail-closed 400 o
    // tang service; bulkDelete id la giu 200 rows 0 idempotent (DS99 G11).
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<UserGrid> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách bố cục không được để trống");
        }
        for (UserGrid entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Bố cục tùy chỉnh không được để trống");
            }
            if (entity.getUserId() == null) {
                throw new ServiceException("Người dùngidKhông được để trống");
            }
            if (entity.getRouterId() == null) {
                throw new ServiceException("Định tuyếnidKhông được để trống");
            }
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<UserGrid> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách bố cục không được để trống");
        }
        for (UserGrid entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Bố cục tùy chỉnh không được để trống");
            }
            if (StringUtils.isBlank(entity.getId())) {
                throw new ServiceException("Bố cục tùy chỉnh không được để trống");
            }
            if (dao.get(entity.getId()) == null) {
                throw new ServiceException("Bố cục tùy chỉnh không tồn tại");
            }
            if (entity.getRouterId() == null) {
                throw new ServiceException("Định tuyếnidKhông được để trống");
            }
        }
        return super.bulkUpdate(entitys);
    }

    /**
     * DS124 B: single delete fail-closed 400. CrudService.delete chi chan
     * qua stored-row tenant check ma check do bo qua stored==null, nen
     * delete id ma tra 200 data=0 im lang; mapper delete la UPDATE
     * del_flag khong loc del_flag nen delete dong da xoa mem tra 200
     * rows=1 + audit DELETED trung. Kiem tra ton tai len dau qua dao.get
     * (mapper get khong loc del_flag nen phai kiem tra del_flag='0').
     * San 403 theo user giu o controller.
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(UserGrid entity) {
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được bố cục tùy chỉnh cần xóa.");
        }
        UserGrid stored = dao.get(entity.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Bố cục tùy chỉnh không tồn tại: " + entity.getId());
        }
        verifyTenantOwnershipAgainstStoredRow(entity);
        return super.delete(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<UserGrid> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách bố cục không được để trống");
        }
        for (UserGrid entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Bố cục tùy chỉnh không được để trống");
            }
        }
        return super.bulkDelete(entitys);
    }

}