package com.geeke.hosdata.dao;

import com.geeke.common.persistence.CrudDao;
import com.geeke.hosdata.entity.DistDto;
import com.geeke.hosdata.entity.HisDrugs;
import com.geeke.stock.entity.Drug;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface HisDrugDao extends CrudDao<HisDrugs> {

   List<DistDto> getDist(String distTypeId);

   //Đãđồng bộ của duy nhấtMã thuốc
   List<String> getDrugCode(String companyId);


}
