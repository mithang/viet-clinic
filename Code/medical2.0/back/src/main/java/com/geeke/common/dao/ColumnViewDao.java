package com.geeke.common.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.entity.ColumnView;

/**
 * bảngCộttiêu đềQuản lýdaoGiao diện
 * 
 * @author lys
 * @date 2019/2/2
 */
@Mapper
public interface ColumnViewDao {

	/**
	 * Theoxem图, bảng, Người dùng Truy vấnbảngCộttiêu đềTriển khaiTrạng thái
	 * @param userId
	 * @param routerId
	 * @param tableId
	 * @return
	 */
	List<ColumnView> list(@Param("userId")String userId, @Param("routerId")String routerId, @Param("tableId")String tableId);
	
	int insert(ColumnView cv);
	
	int update(ColumnView cv);
	
	/**
	 * LưuTất cảhiển thịCột
	 * @param userId
	 * @param routerId
	 * @param tableId
	 * @return
	 */
	List<ColumnView> saveAll(@Param("userId")String userId, @Param("routerId")String routerId, @Param("tableId")String tableId);
	
	/**
	 * XóaĐặt của hiển thịCột
	 * @param userId
	 * @param routerId
	 * @param tableId
	 */
	void deleteAll(@Param("userId")String userId, @Param("routerId")String routerId, @Param("tableId")String tableId);
	
	/**
	 * XóaĐặt của hiển thịCột
	 * @param id
	 */
	void delete(@Param("id")String id);
	
	int bulkUpdate(@Param("list")List<ColumnView> list);
	int bulkInsert(@Param("list")List<ColumnView> list);
}
