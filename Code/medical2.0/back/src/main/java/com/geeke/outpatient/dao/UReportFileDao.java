package com.geeke.outpatient.dao;

import com.geeke.common.persistence.CrudDao;
import com.geeke.outpatient.entity.UReportFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * báo cáoTệpDAOGiao diện
 * @author txl
 * @version 2022-06-23
 */
@Mapper
public interface UReportFileDao extends CrudDao<UReportFile> {

    UReportFile getReportFileByName(@Param("name") String name, @Param("companyId") String companyId);

    List<UReportFile> getListAll(@Param("companyId") String companyId);
}