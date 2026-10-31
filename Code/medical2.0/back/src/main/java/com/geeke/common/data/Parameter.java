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
	 * Giải thích:  và  SearchParams.QUERY_TYPE_LIST Giữ nhất quán, số tiềnngoàiđưavào "<=>", "not left_like", "not right_like", 
	 * Đảm bảo mọi loại truy vấn hợp lệ đều qua được kiểm tra, Tránhhợp lệVui lòngyêu cầubịsaiphán là định dạngLỗi. 
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
	 * chú ý: Phảibao gồmtrốngchuỗi "" —— trướcđầunhómhợpĐiều kiện giờ(baseUI.compositeCondition / pushDataPermissions)
	 * sẽĐưaDòng mộtCáiĐiều kiệnbằngvàngoặcsốLớpĐiều kiện(queryType  là  '('  hoặc  ')') của  logic đặt là  '', 
	 * nếukhôngchứa '', hợp lệVui lòngyêu cầusẽtạingượcthứ tựCộthóagiai đoạnbịchặn, kích hoạt 400「Định dạng thân yêu cầu sai, Vui lòng kiểm traJSONĐịnh dạng」. 
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
		// Giá trị trống(null hoặc chuỗi rỗng)Cho qua trực tiếp,  và  ALLOWED_LOGIC  trong giữ lại của  '' ngữ nghĩaGiữ nhất quán
		if (logic != null && !logic.trim().isEmpty() && !ALLOWED_LOGIC.contains(logic)) {
			throw new IllegalArgumentException("Invalid logic value: " + logic);
		}
		this.logic = logic;
	}


	/**
	 * Các biểu thức tồn kho còn lại duy nhất được phép vượt qua danh sách trắng
	 * tên cột: trang Tồn kho hiện tại lọc "số lượng còn lại" bằng hiệu
	 * (nhập - đã dùng - hoàn trả) của bảng tồn kho. Chỉ đúng 2 chuỗi hằng này
	 * được chấp nhận, và chúng là chuỗi cố định do frontend gửi theo đúng mẫu
	 * đã có trong views/stock/currentInventory/index.vue — không phải chuỗi
	 * tùy ý do người gọi tự đặt, nên không mở ra lỗ hổng chèn SQL: mọi tên cột
	 * khác vẫn phải khớp COLUMN_NAME_PATTERN.
	 */
	private static final java.util.Set<String> ALLOWED_REMAINING_STOCK_EXPRESSIONS =
			new java.util.HashSet<>(java.util.Arrays.asList(
					"stock.storage_stock-stock.used_stock-stock.reimburse_stock",
					"a.storage_stock-a.used_stock-a.reimburse_stock"
			));

	/** Chỉ đúng 2 biểu thức tồn kho trên được coi là hợp lệ (xem ghi chú trên). */
	public static boolean isAllowedRemainingStockExpression(String columnName) {
		return columnName != null && ALLOWED_REMAINING_STOCK_EXPRESSIONS.contains(columnName.trim());
	}

	public String getColumnName() {
		return columnName;
	}


	public void setColumnName(String columnName) {
		// Giá trị trống(null hoặc chuỗi rỗng)Cho qua trực tiếp, tính hợp lệKiểm tragiaodo SearchParams.getParams() thống nhấtXử lý
		if (columnName != null && !columnName.trim().isEmpty()
				&& !COLUMN_NAME_PATTERN.matcher(columnName).matches()
				&& !isAllowedRemainingStockExpression(columnName)) {
			throw new IllegalArgumentException("Invalid column name: " + columnName);
		}
		this.columnName = columnName;
	}


	public String getQueryType() {
		return queryType;
	}


	public void setQueryType(String queryType) {
		// Giá trị trống(null hoặc chuỗi rỗng)Cho qua trực tiếp; SearchParams phíaĐãđối với queryType làm trim Kiểm tra
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
	 * tương thích company_id  và  `company_id` hailoạiviếtphương pháp. 
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
