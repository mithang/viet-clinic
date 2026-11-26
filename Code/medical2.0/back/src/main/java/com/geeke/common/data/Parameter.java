package com.geeke.common.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.geeke.common.service.ServiceException;

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
				.filter(item -> isCompanyIdColumn(item.getColumnName()))
				.findFirst();
		if (optional.isPresent()) {
			// DS250: value la Object do JSON deserialize — number/boolean/
			// object/array ve day se nem ClassCastException 500 o cast
			// (String) ben duoi. Fail-closed 400 truoc moi cast; null giu
			// nguyen (cast null hop le, tenant filter van duoc them sau).
			Object value = optional.get().getValue();
			if (value != null && !(value instanceof String)) {
				throw new ServiceException("Mã phòng khám không hợp lệ");
			}
			String id = (String) value;
			parameters.removeIf(item -> isCompanyIdColumn(item.getColumnName()));
			return id;
		}
		return null;
	}

	/**
	 * AA1: chuẩn hóa tên cột trước khi so sánh company_id.
	 * SearchParams.getParams() bọc backtick sau khi kiểm tra ký tự, nên
	 * body "a.company_id" tới đây thành "a.`company_id`" và "COMPANY_ID"
	 * thành "`COMPANY_ID`" — cả hai đều lọt bộ lọc cũ chỉ khớp đúng
	 * "company_id" / "`company_id`". Chuẩn hóa: bỏ backtick, lowercase,
	 * lấy phần sau dấu chấm cuối (bỏ prefix bảng) rồi so sánh; đồng
	 * thời chặn biến thể "company.id" (thành "company.`id`").
	 */
	private static boolean isCompanyIdColumn(String columnName) {
		if (columnName == null) {
			return false;
		}
		String normalized = columnName.replace("`", "").trim().toLowerCase(java.util.Locale.ROOT);
		if ("company_id".equals(normalized) || "company.id".equals(normalized)) {
			return true;
		}
		int dot = normalized.lastIndexOf('.');
		if (dot >= 0) {
			return "company_id".equals(normalized.substring(dot + 1));
		}
		return false;
	}

	/**
	 * DS720: boc toan bo tham so body trong cap ngoac truoc khi AND
	 * them filter pham vi (tenant / parent / ids / t.tenant_id).
	 * Cung logic AA1 cua CrudService.wrapBodyParamsForTenantFilter, tach
	 * ra day de moi duong tu ep scope (RemoteDiagnosisTreatmentController,
	 * RecipelInfoReviewController, CompanyService.scopedParams,
	 * UserService.listPageForNoAdmin, CrudService.buildTenantPageRequest,
	 * StuffService.buildStuffTenantPageRequest) deu dung chung mot cho:
	 * truoc day SQL co dang "body... OR x AND scope = ..." — do AND ket
	 * chat hon OR nen disjunct OR dung truoc thoat khoi filter pham vi
	 * va tra ve dong tenant khac (tai hien live DS720: remote diagnosis,
	 * review, company, user, stuff, dispensing report). Sau sua:
	 * "(body...) AND scope" nen moi disjunct deu nam trong pham vi.
	 * Chi boc khi co it nhat mot tham so thuc su render ra SQL dung
	 * logic OR; cac truong hop con lai giu nguyen hanh vi cu. Tham so
	 * khong render (gia tri rong — whereParams bo qua) chi bi loai khi
	 * boc, vi "( )" rong se loi SQL; loai chung la trung hoa voi output
	 * SQL. Logic cua tham so dau tien trong nhom duoc dat thanh "" vi
	 * o dau nhom khong co dieu kien nao dung truoc no. Nhom nguoi dung
	 * mat can bang ngoac van loi SQL fail-closed nhu truoc, khong ro ri.
	 * Null-safe: parameters null thi khong lam gi.
	 */
	public static void wrapBodyParamsForTenantFilter(List<Parameter> parameters) {
		if (parameters == null) {
			return;
		}
		List<Parameter> rendering = new ArrayList<>();
		boolean hasOr = false;
		for (Parameter p : parameters) {
			if (p == null || !isRenderingParam(p)) {
				continue;
			}
			rendering.add(p);
			String logic = p.getLogic();
			if (logic != null && "or".equalsIgnoreCase(logic.trim())) {
				hasOr = true;
			}
		}
		if (rendering.isEmpty() || !hasOr) {
			return;
		}
		rendering.get(0).setLogic("");
		List<Parameter> wrapped = new ArrayList<>();
		wrapped.add(new Parameter(null, "(", null, "AND"));
		wrapped.addAll(rendering);
		wrapped.add(new Parameter(null, ")", null, ""));
		parameters.clear();
		parameters.addAll(wrapped);
	}

	/**
	 * DS720: tham so co render ra SQL trong Common.whereParams hay khong.
	 * "(" / ")" / "is null" / "is not null" luon render; cac nhanh con
	 * lai deu bo qua khi value null hoac chuoi rong; "in" bo qua khi
	 * danh sach rong. Cac dang value khac giu nguyen (bao thu).
	 * queryType null KHONG render (whereParams bo qua item co
	 * queryType null) nen tra false: UserController chen new Parameter()
	 * rong vao params[0] o duong listPageForNoAdmin, neu coi la
	 * rendering thi nhom boc thanh "( OR ...)" loi SQL 400 cho ca
	 * truy van OR hop le; loai no la trung hoa vi no khong render gi.
	 */
	private static boolean isRenderingParam(Parameter p) {
		String queryType = p.getQueryType();
		if (queryType == null) {
			return false;
		}
		String q = queryType.trim();
		if ("(".equals(q) || ")".equals(q)
				|| "is null".equalsIgnoreCase(q) || "is not null".equalsIgnoreCase(q)) {
			return true;
		}
		Object value = p.getValue();
		if (value == null) {
			return false;
		}
		if (value instanceof String) {
			return !((String) value).isEmpty();
		}
		if (value instanceof List) {
			if ("in".equalsIgnoreCase(q) || "not in".equalsIgnoreCase(q)) {
				return !((List<?>) value).isEmpty();
			}
			return true;
		}
		return true;
	}

}
