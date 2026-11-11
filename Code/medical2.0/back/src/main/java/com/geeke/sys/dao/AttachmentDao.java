package com.geeke.sys.dao;

import org.apache.ibatis.annotations.Mapper;

import com.geeke.common.persistence.CrudDao;
import com.geeke.sys.entity.Attachment;

/**
 * Tệptệp đính kèmDAOGiao diện
 * @author lys
 * @version 2021-04-28
 */
@Mapper
public interface AttachmentDao extends CrudDao<Attachment> {
}