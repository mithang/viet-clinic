package com.geeke.noticesend.dao;

import com.geeke.common.persistence.CrudDao;
import com.geeke.noticesend.entity.NoticeSend;
import org.apache.ibatis.annotations.Mapper;

/**
 * Gửi thông báo DAO.
 */
@Mapper
public interface NoticeSendDao extends CrudDao<NoticeSend> {
}
