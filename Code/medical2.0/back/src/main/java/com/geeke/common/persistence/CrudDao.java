
package com.geeke.common.persistence;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.geeke.common.data.PageRequest;

/**
 * DAOLớp hỗ trợ triển khai
 * @author lys
 * @version 2014-05-16
 * @param <T>
 */
public interface CrudDao<T> extends BaseDao {

	/**
	 * LấyđơnmụcDữ liệu
	 * @param id
	 * @return
	 */
	public T get(String id);
	
	
	/**
	 * Truy vấnDữ liệusố dòng(dùngtạiTruy vấn phân trang)
	 * @param pageRequest
	 * @return
	 */
	public int count(PageRequest pageRequest);
	
	/**
	 * Truy vấn phân trangDữ liệuDanh sách
	 * @param pageRequest
	 * @return
	 */
	public List<T> listPage(PageRequest pageRequest);
	
	/**
	 * Truy vấntất cảDữ liệuDanh sách
	 * @param parameters
	 * @return
	 */
	public List<T> listAll(PageRequest pageRequest);
	
	
	/**
	 * chènDữ liệu
	 * @param entity
	 * @return
	 */
	public int insert(T entity);
	
	/**
	 * Cập nhậtDữ liệu
	 * @param entity
	 * @return
	 */
	public int update(T entity);
	
	/**
	 * cóXóaTrường giờ, mộtthông thường là logicXóa, Cập nhậtdel_flagTrường là 1; Nếu khôngvật lýXóa
	 * @param entity
	 * @see public int delete(T entity)
	 * @return
	 */
	
	public int delete(T entity);
	
	/**
	 * hàng loạttăng
	 * @param entitys
	 * @return
	 */
	public int bulkInsert(@Param("entitys")List<T> entitys);
	
	/**
	 * hàng loạtSửa
	 * @param entitys
	 * @return
	 */
	public int bulkUpdate(@Param("entitys")List<T> entitys);
	
	/**
	 * Xóa hàng loạt
	 * @param entitys
	 * @return
	 */
	public int bulkDelete(@Param("entitys")List<T> entitys);
}