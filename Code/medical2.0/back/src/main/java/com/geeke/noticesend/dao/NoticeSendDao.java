package com.geeke.noticesend.dao;

import com.geeke.common.persistence.CrudDao;
import com.geeke.noticesend.entity.NoticeSend;
import org.apache.ibatis.annotations.Mapper;

/**
 * Gửi thông báo DAO.
 */
@Mapper
public interface NoticeSendDao extends CrudDao<NoticeSend> {
    /**
     * company_id thô của một dòng, dùng cho checkNoticeOwnership.
     * Không dùng dao.get(id).getCompany() vì MyBatis không map cột lồng
     * "company.id" vào field company của entity này (đã chứng minh live:
     * getById dòng lạ trả company = tenant session do getter tự tổng
     * hợp khi field null), nên check qua entity sẽ lọt dòng lạ.
     */
    String getCompanyIdById(String id);
}
