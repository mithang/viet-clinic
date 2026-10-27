package com.geeke.common.persistence;

import java.util.HashMap;

/**
 * Tham số truy vấnLớp
 * @author lys
 * @version 2013-8-23
 */
public class Parameter extends HashMap<String, Object> {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * xây dựngLớp, ví dụ: new Parameter(id, ids)
	 * @param values Tham sốGiá trị
	 */
	public Parameter(Object... values) {
		if (values != null){
			for (int i=0; i<values.length; i++){
				put("p"+(i+1), values[i]);
			}
		}
	}
	
	/**
	 * xây dựngLớp, ví dụ: new Parameter(new Object[][]{{"id", id}, {"ids", ids}})
	 * @param parameters Tham số二维mảng
	 */
	public Parameter(Object[][] parameters) {
		if (parameters != null){
			for (Object[] os : parameters){
				if (os.length == 2){
					put((String)os[0], os[1]);
				}
			}
		}
	}
	
}
