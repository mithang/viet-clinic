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
			"not left_like", "not like", "not right_like", "is null", "is not null"}; // vậntínhthao tácký tự


	private List<Parameter> params = null;

	public String getOrderby() {
		// sắp xếpchỉTồn tạibaloạitình hình: “”,  ASC, DESC
		if (StringUtils.isNotBlank(this.columnName)) {
			String columnName = this.columnName.trim();
			if (columnName.indexOf("`") >= 0) {
				throw new ServiceException("sắp xếp Trường Chứa ký tự không hợp lệ");
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
				// DS185: phan tu null trong params (JSON {"params":[null]})
				// truoc day NPE 500 o getLogic() ben duoi — fail-closed
				// 400; UI khong bao gio gui phan tu null.
				if (params.get(i) == null) {
					throw new ServiceException("Điều kiện truy vấn không được để trống");
				}
				// V2 (iteration 16): JSON khong gui logic/queryType thi Jackson de
				// null (queryType khong co gia tri mac dinh nhu logic) -> NPE o
				// dong duoi. Mac dinh ve gia tri hop le trong whitelist, van
				// chan gia tri khong null ma khong hop le nhu cu.
				String logic = params.get(i).getLogic();
				if (logic == null) { logic = "AND"; params.get(i).setLogic("AND"); }
				if (!Arrays.asList(LOGIC_LIST).contains(logic.toLowerCase().trim())) {
					throw new ServiceException("Toán tử logic chứa ký tự không hợp lệ");
				}
				String queryType = params.get(i).getQueryType();
				if (queryType == null) { queryType = "="; params.get(i).setQueryType("="); }
				queryType = queryType.replaceAll(" +"," ").trim();
				if (!Arrays.asList(QUERY_TYPE_LIST).contains(queryType.toLowerCase())) {
					throw new ServiceException("Toán tử tính toán chứa ký tự không hợp lệ");
				}

				String columnName = params.get(i).getColumnName();
				if (StringUtils.isNotBlank(columnName) && StringUtils.isNotBlank(columnName.trim())) {
					columnName = columnName.trim();
					if (columnName.indexOf("`") >= 0) {
						throw new ServiceException("Điều kiện Tên trường Chứa ký tự không hợp lệ");
					} else if (Parameter.isAllowedRemainingStockExpression(columnName)) {
						// Biểu thức hiệu tồn kho của trang Tồn kho hiện tại phải giữ
						// nguyên, không được bọc backtick (bọc sẽ biến cả biểu thức
						// thành một tên cột duy nhất và câu SQL sẽ lỗi).
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
		// DS185: nhu getParams() — cac caller (RecipelInfoReview
		// forceReviewListScope/removeIf, HosDepartmentStorageController,
		// HttpUtil.getHosData) deu deref phan tu khong guard: phan tu
		// null truoc day NPE 500. Fail-closed 400 ngay tai day.
		if (null != params) {
			for (Parameter p : params) {
				if (p == null) {
					throw new ServiceException("Điều kiện truy vấn không được để trống");
				}
			}
		}
		return this.params;
	}
	public void setParams(List<Parameter> params) {
		this.params = params;
	}
}
