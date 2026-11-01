package com.geeke.druginteraction.dao;

import com.geeke.common.persistence.CrudDao;
import com.geeke.druginteraction.entity.DrugInteractionRule;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * Quy tắc tương tác thuốc DAO.
 *
 * Chỉ có một thao tác liệt kê được dùng: các quy tắc đang hiệu lực
 * (del_flag = '0') của một tenant. Controller không bao giờ trả quy tắc
 * của tenant khác.
 */
@Mapper
public interface DrugInteractionRuleDao extends CrudDao<DrugInteractionRule> {

    /**
     * Các quy tắc đang hiệu lực của một tenant, mới nhất trước.
     */
    List<DrugInteractionRule> findByTenant(Map<String, Object> params);
}
