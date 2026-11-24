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
		// DS189: body {} (khong co key "params") truoc day tra null ->
		// CrudService.ensureCompanyFilter(null) return som, khong chen
		// company_id cua session tenant, PageRequest doi null ve rong
		// nen SQL chay KHONG tenant filter (tai hien live: getWorkload /
		// getDrugSales / getDrugSalesStat / patient/list tra du lieu
		// tenant khac). "params": [] thi loc dung vi list khong null.
		// Chuan hoa null ve list rong ngay tai day (choke point duy
		// nhat: moi body deu deserialize qua SearchParams) de moi caller
		// deu duoc ep tenant filter nhu "params": []. Gan vao field de
		// getNewParams() / lan goi sau thay cung list (ensureCompanyFilter
		// mutate in-place, idempotent nho extractAndRemoveCompanyId).
		// Khong doi hanh vi khi session khong co tenant (ensureCompanyFilter
		// van bo qua) hay entity khong co field company.
		if (params == null) {
			params = new java.util.ArrayList<>();
		}
		if (params.size() > 0) {
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
				// DS631: 'in'/'not in' doi value la List, 'between'/
				// 'not between' doi value la List dung 2 phan tu.
				// JSON String/number/list thieu phan tu truoc day roi
				// xuong Common.whereParams: OGNL item.value.size() /
				// item.value[0]/[1] nem exception -> 500. Fail-closed
				// 400 ngay tai day; UI va SearchParamsBuilder chi gui
				// List dung dang. value null giu nguyen (XML bo qua
				// dieu kien nhu truoc).
				checkParamValueShape(params.get(i), queryType);

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
		// DS189: nhu getParams() — null ve rong de moi caller deu thay
		// cung list (RecipelInfoReviewController.forceSessionParent truoc
		// day return som khi params null, bo qua ep parent scope;
		// CheckUtils.isTenantQuery NPE tren body {}).
		if (params == null) {
			params = new java.util.ArrayList<>();
		}
		for (Parameter p : params) {
			if (p == null) {
				throw new ServiceException("Điều kiện truy vấn không được để trống");
			}
			// DS631: nhu getParams() — fail-closed 400 cho 'in'/
			// 'between' value sai dang truoc khi xuong whereParams.
			String qt = p.getQueryType();
			if (qt != null) {
				checkParamValueShape(p, qt.replaceAll(" +", " ").trim());
			}
		}
		return this.params;
	}

	/**
	 * DS631: kiem tra hinh dang value theo queryType. 'in'/'not in'
	 * can List; 'between'/'not between' can List dung 2 phan tu.
	 * value null cho qua (Common.whereParams bo qua dieu kien).
	 * DS632: mo rong — toan tu vo huong (=, <>, >, >=, <, <=, <=>,
	 * like/left_like/right_like va dang phu dinh) dung #{item.value}
	 * truc tiep trong Common.whereParams; JSON object (Map) hoac list
	 * lam value thi MyBatis khong co type handler -> 500. Cung vay
	 * voi phan tu trong list cua 'in'/'between' (#{name} /
	 * #{item.value[0]}). Fail-closed 400 ngay tai day; chi chap
	 * nhan gia tri vo huong (String/Number/Boolean/Date...).
	 * '('/ ')'/'is null'/'is not null' khong dung value nen bo qua.
	 */
	private static void checkParamValueShape(Parameter p, String queryType) {
		Object value = p.getValue();
		if (value == null) {
			return;
		}
		String qt = queryType.toLowerCase(Locale.ROOT);
		if ("in".equals(qt) || "not in".equals(qt)) {
			if (!(value instanceof List)) {
				throw new ServiceException("Giá trị của toán tử IN phải là danh sách");
			}
			for (Object e : (List<?>) value) {
				if (!isScalarValue(e)) {
					throw new ServiceException("Phần tử của toán tử IN phải là giá trị đơn (chuỗi/số)");
				}
			}
		} else if ("between".equals(qt) || "not between".equals(qt)) {
			if (!(value instanceof List) || ((List<?>) value).size() != 2) {
				throw new ServiceException("Giá trị của toán tử BETWEEN phải là danh sách có 2 phần tử");
			}
			for (Object e : (List<?>) value) {
				if (!isScalarValue(e)) {
					throw new ServiceException("Phần tử của toán tử BETWEEN phải là giá trị đơn (chuỗi/số)");
				}
			}
		} else if ("(".equals(qt) || ")".equals(qt)
				|| "is null".equals(qt) || "is not null".equals(qt)) {
			// khong dung value — bo qua
		} else {
			// toan tu vo huong: chi chap nhan gia tri don
			if (!isScalarValue(value)) {
				throw new ServiceException("Giá trị của toán tử " + queryType.toUpperCase(Locale.ROOT) + " phải là giá trị đơn (chuỗi/số)");
			}
		}
	}

	/** DS632: vo huong = khong phai List/Map (JSON object/list). */
	private static boolean isScalarValue(Object v) {
		return v != null && !(v instanceof List) && !(v instanceof java.util.Map);
	}
	public void setParams(List<Parameter> params) {
		this.params = params;
	}
}
