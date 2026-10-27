package com.geeke.outpatient.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.outpatient.entity.Recipetemplate;

import java.util.List;

/**
 * Đơn thuốc mẫuDAOGiao diện
 * @author rys
 * @version 2022-10-08
 */
@Mapper
public interface RecipetemplateDao extends CrudDao<Recipetemplate> {

    void allSave(Recipetemplate recipetemplate);

    List<Recipetemplate> listAi(String id);

    String listOne(String companyId);

    Recipetemplate getById(String id, String companyId);

    int updateById(Recipetemplate recipetemplate);

}