package com.geeke.sys.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Nhật ký thao tácDAOGiao diện
 * @author lys	Viết thủ công
 * @version 2019-12-12
 */
@Mapper
public interface RecycleDao {
	
	/**
	 * SửaNhật ký thao tácstatus là Đãkhôi phụcTrạng thái
	 * @param id  Nhật ký thao tácId
	 * @return
	 */
	int updateActionStaus(@Param("id")String id);
	
	
	/**
	 * khôi phụcĐối tượng nghiệp vụ, Đưadel_flagTrườngSửa là 0
	 * @param tableName  nghiệp vụbảngtên
	 * @param id		 Đối tượng nghiệp vụId
	 * @return
	 */
	int restore(@Param("tableName")String tableName, @Param("id")String id);
}