package com.geeke.common.data;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Vui lòngyêu cầuDữ liệulọcĐiều kiện
 *
 * @author lys
 * @date 2020/06/27
 */
public final class Parameter {

	/**
	 * Cộttêndanh sách trắng: chỉcho phépchữ cái, Số, Gạch dưới, dấu chấm, dấu backtick
	 * Ví dụ: company_id, a.name, `company_id`
	 */
	private static final Pattern COLUMN_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_`.]+$");

	/**
	 * Truy vấnLoạidanh sách trắng. 
	 * Giải thích:  và  SearchParams.QUERY_TYPE_LIST Giữ nhất quán, số tiềnngoài纳入 "<=>", "not left_like", "not right_like", 
	 * Đảm bảo mọi loại truy vấn hợp lệ đều qua được kiểm tra, Tránhhợp lệVui lòngyêu cầubịsai判 là định dạngLỗi. 
	 */
	private static final java.util.Set<String> ALLOWED_QUERY_TYPES = new java.util.HashSet<>(java.util.Arrays.asList(
			"=", "!=", "<>", "<=>", ">", "<", ">=", "<=",
			"like", "not like", "left_like", "right_like",
			"not left_like", "not right_like",
			"in", "not in",
			"between", "not between",
			"is null", "is not null",
			"(", ")"
	));

	/**
	 * Danh sách trắng ký tự nối logic. 
	 * chú ý: Phảibao gồmtrống串 "" —— trướcđầunhómhợpĐiều kiện giờ(baseUI.compositeCondition / pushDataPermissions)
	 * sẽĐưaDòng mộtCáiĐiều kiệnbằngvà括sốLớpĐiều kiện(queryType  là  '('  hoặc  ')') của  logic đặt là  '', 
	 * nếukhôngchứa '', hợp lệVui lòngyêu cầusẽtại反thứ tựCộthóagiai đoạnbịchặn, kích hoạt 400「Định dạng thân yêu cầu sai, Vui lòng kiểm traJSONĐịnh dạng」. 
	 */
	private static final java.util.Set<String> ALLOWED_LOGIC = new java.util.HashSet<>(java.util.Arrays.asList(
			"AND", "OR", "and", "or", ""
	));

	private String logic = "AND";
	private String columnName;
	private String queryType;
	private Object value;

	public Parameter() {

	}

	
	public Parameter(String columnName, String queryType, Object value) {
		this(columnName, queryType, value, "AND");
	}

	public Parameter(String columnName, String queryType, Object value, String logic) {
		setColumnName(columnName);
		setQueryType(queryType);
		this.value = value;
		setLogic(logic);
	}


	public String getLogic() {
		return logic;
	}


	public void setLogic(String logic) {
		// Giá trị trống(null hoặc chuỗi rỗng)Cho qua trực tiếp,  và  ALLOWED_LOGIC  trong giữ lại của  '' 语义Giữ nhất quán
		if (logic != null && !logic.trim().isEmpty() && !ALLOWED_LOGIC.contains(logic)) {
			throw new IllegalArgumentException("Invalid logic value: " + logic);
		}
		this.logic = logic;
	}


	public String getColumnName() {
		return columnName;
	}


	public void setColumnName(String columnName) {
		// Giá trị trống(null hoặc chuỗi rỗng)Cho qua trực tiếp, tính hợp lệKiểm tragiaodo SearchParams.getParams() thống nhấtXử lý
		if (columnName != null && !columnName.trim().isEmpty() && !COLUMN_NAME_PATTERN.matcher(columnName).matches()) {
			throw new IllegalArgumentException("Invalid column name: " + columnName);
		}
		this.columnName = columnName;
	}


	public String getQueryType() {
		return queryType;
	}


	public void setQueryType(String queryType) {
		// Giá trị trống(null hoặc chuỗi rỗng)Cho qua trực tiếp; SearchParams 侧Đãđối với queryType làm trim Kiểm tra
		if (queryType != null && !queryType.trim().isEmpty() && !ALLOWED_QUERY_TYPES.contains(queryType.trim().toLowerCase())) {
			throw new IllegalArgumentException("Invalid query type: " + queryType);
		}
		this.queryType = queryType;
	}


	public Object getValue() {
		return value;
	}


	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * Từ Tham sốDanh sách trong đề xuấtlấyvàloại bỏ company_id Tham số, Trả vềcủa nóGiá trị. 
	 * 兼容 company_id  và  `company_id` hailoạiviếtphương pháp. 
	 * @param parameters Danh sách tham số truy vấn
	 * @return company_id  của Giá trị, NếuKhông tồn tạiThì trả về null
	 */
	public static String extractAndRemoveCompanyId(List<Parameter> parameters) {
		if (parameters == null) {
			return null;
		}
		Optional<Parameter> optional = parameters.stream()
				.filter(item -> "company_id".equals(item.getColumnName()) || "`company_id`".equals(item.getColumnName()))
				.findFirst();
		if (optional.isPresent()) {
			String id = (String) optional.get().getValue();
			parameters.removeIf(item -> "company_id".equals(item.getColumnName()) || "`company_id`".equals(item.getColumnName()));
			return id;
		}
		return null;
	}

}
