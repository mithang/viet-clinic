package com.geeke.common.controller;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.geeke.common.data.Parameter;
import com.geeke.common.service.ServiceException;
import com.geeke.utils.StringUtils;


import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

/**
 * Tham số truy vấn
 * @author alec
 *
 */
@Data
public class SearchParams implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int offset = 0;
    private int limit = 10;
    private String columnName; // sắp xếpTên trường(Tên khác)
	private String order; // Sắp xếp
	private String isPre;//Đơn thuốc điện tử
	private static String[] ORDER_LIST = {"desc", "asc", ""}; // sắp xếpký tựsố
	private static String[] LOGIC_LIST = {"and", "or", ""}; // logicthao tácký tự
	private static String[] QUERY_TYPE_LIST = {"(", ")", "=", "<=>", "between", "left_like",
			"like", "right_like","<=", "!=", "<>", ">", ">=", "<", "in", "not in", "not between",
			"not left_like", "not like", "not right_like", "is null", "is not null"}; // 运tínhthao tácký tự


	private List<Parameter> params = null;

	public String getOrderby() {
		// sắp xếpchỉTồn tại三loạitình hình: “”,  ASC, DESC
		if (StringUtils.isNotBlank(this.columnName)) {
			String columnName = this.columnName.trim();
			if (columnName.indexOf("`") >= 0) {
				throw new ServiceException("sắp xếpTrườngChứa ký tự không hợp lệ");
			}
			if (StringUtils.isNotBlank(this.order)) {
				if (!Arrays.asList(ORDER_LIST).contains(this.order.toLowerCase(Locale.ROOT).trim())) {
					throw new ServiceException("Cách sắp xếp chứa ký tự không hợp lệ");
				} else {
					return "`" + columnName + "` " + this.order;
				}
			} else {
				return "`" + columnName + "`";
			}
		}
		return null;
	}

	public List<Parameter> getParams() {
		if (null != params && params.size() > 0) {
			for (int i = 0; i < params.size(); i++) {
				if (!Arrays.asList(LOGIC_LIST).contains(params.get(i).getLogic().toLowerCase().trim())) {
					throw new ServiceException("Toán tử logic chứa ký tự không hợp lệ");
				}
				String queryType = params.get(i).getQueryType().replaceAll(" +"," ").trim();
				if (!Arrays.asList(QUERY_TYPE_LIST).contains(queryType.toLowerCase())) {
					throw new ServiceException("Toán tử tính toán chứa ký tự không hợp lệ");
				}

				String columnName = params.get(i).getColumnName();
				if (StringUtils.isNotBlank(columnName) && StringUtils.isNotBlank(columnName.trim())) {
					columnName = columnName.trim();
					if (columnName.indexOf("`") >= 0) {
						throw new ServiceException("Điều kiệnTên trườngChứa ký tự không hợp lệ");
					} else {
						int index = columnName.lastIndexOf(".");
						if (index >= 0) {
							String before = columnName.substring(0, index + 1);
							String after = columnName.substring(index + 1);
							columnName = before + "`" + after + "`";
						} else {
							columnName = "`" + columnName + "`";
						}
					}
				}
				params.get(i).setColumnName(columnName);

			}
		}
		return params;
	}

	public List<Parameter> getNewParams() {
		return this.params;
	}
	public void setParams(List<Parameter> params) {
		this.params = params;
	}
}
