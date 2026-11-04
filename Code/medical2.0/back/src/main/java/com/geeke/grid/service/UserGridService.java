package com.geeke.grid.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.admin.dao.UserDao;
import com.geeke.admin.entity.User;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.grid.dao.UserGridDao;
import com.geeke.grid.entity.UserGrid;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
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
        
        if(exists(dao, userGrid, colMaps)) { // mộtCáiNgười dùngtạimộtCáituyến đườngdưới chỉcó thểcómộtCáiphát hànhcục
            this.doUpdate(userGrid);
            return userGrid;
        }

        UserGrid userGridTemp = super.save(userGrid);
        return userGridTemp;
    }

}