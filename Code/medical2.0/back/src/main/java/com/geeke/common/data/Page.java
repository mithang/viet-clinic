package com.geeke.common.data;

import java.util.List;

/**
 * Truy vấn phân trangKết quảLớp
 * 
 * @author Lining
 * @date 2017/11/7
 * @param <T>
 */
public final class Page<T> implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private long total;

	private List<T> rows;

	public Page(long total, List<T> rows) {
		this.total = total;
		this.rows = rows;
	}

	public long getTotal() {
		return this.total;
	}

	public List<T> getRows() {
		return this.rows;
	}
}
