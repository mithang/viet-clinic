
package com.geeke.common.persistence;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.utils.IdGen;
import com.geeke.utils.StringUtils;


/**
 * Entityhỗ trợLớp
 * @author lys
 * @version 2014-05-16
 */
public abstract class BaseEntity<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * thực thểmã số(duy nhấtđịnh danh)
	 */
	protected String id;

	public BaseEntity() {
	}
	
	public BaseEntity(String id) {
		this();
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	
	/**
	 * Phương thức thực thi trước khi chèn, Cần gọi thủ công
	 */
	@JsonIgnore
	public void preInsert(){
		if (StringUtils.isBlank(getId())) {
			setId(IdGen.uuid());
		}
	}

	/**
	 * Phương thức thực thi trước khi cập nhật, Cần gọi thủ công
	 */
	@JsonIgnore
	public void preUpdate(){
	}
	
	/**
	 * Lấy tên bảng tương ứng của thực thể, Ghi đè trong lớp thực thể
	 */
	@JsonIgnore
	public String getBusTableName() {
		return "";
	}
	
	/**
	 * Lấythực thểtương ứngbảngId, Ghi đè trong lớp thực thể
	 */
	@JsonIgnore
	public String getBusTableId() {
		return "";
	}
	
	/**
	 * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường, Ghi đè trong lớp thực thể
	 */
	@JsonIgnore
	public boolean getBusTableHasDelFlag() {
		return false;
	}	
	
	/**
	 * Cờ xóa(0: Bình thường; 1: Xóa; 2: Duyệt; )
	 */
	public static final String DEL_FLAG_NORMAL = "0";
	public static final String DEL_FLAG_DELETE = "1";
	public static final String DEL_FLAG_AUDIT = "2";
	
}
