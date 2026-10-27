package com.geeke.stock.dao;

import com.geeke.common.data.PageRequest;
import com.geeke.stock.entity.Drug;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.stock.entity.MedicinalStockControl;

import java.util.List;

/**
 * Thuốc/Kiểm soát tổng tồn kho vật tưDAOGiao diện
 * @author hl
 * @version 2022-09-26
 */
@Mapper
public interface MedicinalStockControlDao extends CrudDao<MedicinalStockControl> {
    List<MedicinalStockControl> getByDrugOrStuffId(@Param("drugOrStuffId")String drugOrStuffId,@Param("companyId")String companyId);

    List<MedicinalStockControl> inventory(@Param("companyID") String companyID, @Param("drugID") String drugID);

    List<MedicinalStockControl> listAlls(PageRequest pageRequest);

    List<MedicinalStockControl> listPreAll(PageRequest pageRequest);



}