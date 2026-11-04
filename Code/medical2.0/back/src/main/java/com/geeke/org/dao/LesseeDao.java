package com.geeke.org.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.org.entity.Lessee;

/**
 * Quản lý tenantDAOGiao diện
 * @author txl
 * @version 2022-05-23
 */
@Mapper
public interface LesseeDao extends CrudDao<Lessee> {

    /**
     * N3c: tên `company` (chuỗi) của dòng lessee theo id — SELECT trực
     * tiếp một cột, không qua mapper get() (bài học N1/N3d).
     */
    String getCompanyNameById(@Param("id") String id);

    /**
     * N3c: id của org_company có tên đăng ký trùng chuỗi đã cho, hoặc
     * null khi tên không gắn với công ty nào (dòng không xác định được
     * chủ sở hữu — giữ hành vi cũ, xem LesseeService.checkLesseeTenant).
     */
    String getCompanyIdByName(@Param("name") String name);
}