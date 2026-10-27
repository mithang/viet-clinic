package com.geeke.org.dao;

import com.geeke.common.data.PageRequest;
import com.geeke.common.persistence.TreeDao;
import com.geeke.org.entity.Company;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Phòng khám/Đơn vị thuêDAOGiao diện
 * @author lys
 * @version 2022-05-25
 */
@Mapper
public interface CompanyDao extends TreeDao<Company> {

    /**Truy vấn phân trangtenant*/
    List<Company> listTenantPage(PageRequest var1);

    int countTenant(PageRequest var1);

    /**dướikhung kéoTruy vấntenant*/
    List<Company> listAllTenant(PageRequest var1);

    /**Truy vấn phân trangPhòng khám*/
    List<Company> listClinicPage(PageRequest var1);

    int countClinic(PageRequest var1);

    /**dướikhung kéoTruy vấnPhòng khám*/
    List<Company> listAllClinic(PageRequest var1);

    /**TheoĐăng nhậptênTruy vấnPhòng khám*/
    List<Company> selectClinicsByLoginName(@Param("loginName") String loginName);

    List<Company> getCompanys(String id);

    String getInstitution(@Param("companyId") String id);
}