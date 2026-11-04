package com.geeke.treatment.dao;

import com.geeke.common.data.PageRequest;
import com.geeke.common.persistence.CrudDao;
import com.geeke.treatment.entity.CostItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Hạng mục khám chữa bệnhDAOGiao diện
 * @author txl
 * @version 2022-05-30
 */
@Mapper
public interface CostItemDao extends CrudDao<CostItem> {
    @Select("select count(1) from cost_item where item_name = #{name} and company_id = #{tenantId}")
    int countByName(@Param("name")String name,@Param("tenantId")String tenantId);

    @Select("select id from cost_item where item_name = #{name} and company_id = #{tenantId}")
    String GetIDByName(@Param("name")String name,@Param("tenantId")String tenantId);

    /** ITER64 item 2: single-column lookup, khong tin payload company va
     * khong dung mapper get (LEFT JOIN tra '' khi thieu dong org_company).
     * cost_item_package khong co cot company nen guard saveDto phai di qua
     * parent cost_item bang lookup nay. Tra null khi id khong ton tai. */
    @Select("select company_id from cost_item where id = #{id}")
    String getCostItemCompanyId(@Param("id") String id);

    CostItem getByRecipelInfo(String recipelDetailId);


    int countByInstitution(PageRequest pageRequest);

    List<CostItem> listByInstitution(PageRequest pageRequest);
}