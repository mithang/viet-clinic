package com.geeke.sys.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.sys.entity.Attachment;

/**
 * Tệptệp đính kèmDAOGiao diện
 * @author lys
 * @version 2021-04-28
 */
@Mapper
public interface AttachmentDao extends CrudDao<Attachment> {
	
	/**
	 * TheoĐối tượng nghiệp vụidXóatệp đính kèm
	 * @param bizId
	 * @see public int delete(T entity)
	 * @return
	 */
	
	public int deleteByBizId(String bizId);
}