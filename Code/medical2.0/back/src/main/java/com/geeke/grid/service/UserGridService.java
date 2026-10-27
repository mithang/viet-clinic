package com.geeke.grid.service;

import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.service.CrudService;
import com.geeke.grid.dao.UserGridDao;
import com.geeke.grid.entity.UserGrid;
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

    @Override
    @Transactional(readOnly = false)
    public UserGrid save(UserGrid userGrid) {
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