package com.geeke.common.service;

import com.geeke.common.IActionSaver;
import com.geeke.common.constants.ActionConstants;
import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.persistence.CrudDao;
import com.geeke.common.persistence.DataEntity;
import com.geeke.gen.entity.GenTable;
import com.geeke.org.service.CompanyService;
import com.geeke.sys.entity.Action;
import com.geeke.sys.entity.ActionRecycle;
import com.geeke.sys.entity.DictItem;
import com.geeke.utils.Reflections;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * ServiceLớp cơ sở
 * @author lys
 * @version 2014-05-16
 */
@Transactional
public abstract class CrudService<D extends CrudDao<T>, T extends DataEntity<T>> extends BaseService {

	/**
	 * tầng persistenceĐối tượng
	 */
	@Autowired
	protected D dao;

	@Autowired
	IActionSaver actionSaver;
	@Autowired
	private CompanyService companyService;

	/**
	 * LấyđơnmụcDữ liệu
	 * @param id
	 * @return
	 */
	public T get(String id) {
		T entity = dao.get(id);
		if (entity != null) {
			verifyTenantOwnership(entity);
		}
		return entity;
	}

	/**
	 * Truy vấn phúttrangDữ liệu
	 * @param page  phúttrangĐối tượng
	 * @param entity
	 * @return
	 */
	public Page<T> listPage(List<Parameter> parameters, int offset, int limit, String orderby) {
		ensureCompanyFilter(parameters);
		PageRequest pageRequest = new PageRequest(offset, limit, parameters, orderby);
		int total = dao.count(pageRequest);
		List<T> list = total > 0 ? dao.listPage(pageRequest) : java.util.Collections.emptyList();
        return new Page<T>(total, list);
	}


	/**
	 * Truy vấnDanh sáchDữ liệu
	 * @param entity
	 * @return
	 */
	public List<T> listAll(List<Parameter> parameters, String orderby) {
		ensureCompanyFilter(parameters);
		PageRequest pageRequest = new PageRequest(parameters, orderby);
		return dao.listAll(pageRequest);
	}


	/**
	 * LưuDữ liệu(Chèn hoặc cập nhật)
	 * @param entity
	 */
	public T save(T entity) {
		// ITER64 item 1: verify payload company on EVERY save, not only on
		// update. Previously the check ran only when id was non-blank, so
		// the blank-id (insert) branch went straight to doInsert with the
		// payload company untouched, letting any guarded endpoint plant a
		// row into a foreign tenant (probe-proven on costItem, supplier,
		// manufactureFactory, noticeSend single save). Insert has no stored
		// row, so the payload check alone is sufficient for that branch;
		// the update branch keeps its stored-row cross-check below.
		// Parity (from verifyTenantOwnership itself, unchanged): null/blank
		// company passes, blank/'null' session passes, and the
		// institution-shared exception still applies. This BLOCKS foreign
		// companies; it never overwrites the payload with the session
		// company (no force-session at the generic layer).
		verifyTenantOwnership(entity);
		// Cập nhậtthao tácCầnKiểm tratenantthuộc về
		if (StringUtils.isNotBlank(entity.getId())) {
			// ITER35 M2: same stored-row cross-check as bulkUpdate (M1).
			// Payload-only verification let a foreign row id + own-tenant
			// payload company pass, and doUpdate then overwrote + re-homed
			// the foreign row (reproduced live on /org/clinicOffice/save).
			verifyTenantOwnershipAgainstStoredRow(entity);
		}
		if (StringUtils.isBlank(entity.getId())){
			entity.preInsert();
			doInsert(entity);
			this.saveAction(this.createAction(ActionConstants.ACTION_CREATED, entity));
		}else{
			entity.preUpdate();
			doUpdate(entity);
			this.saveAction(this.createAction(ActionConstants.ACTION_UPDATED, entity));
		}

		return entity;
	}

	/**
	 * thực thểbảngcóXóaTrường giờ, lấydùnglogicXóa; Nếu khônglấydùngvật lýXóaDữ liệu
	 * @param entity
	 */
	public int delete(T entity) {
		verifyTenantOwnership(entity);
		// ITER35 M2: stored-row cross-check (same as M1 bulkUpdate).
		// Payload-only verification let a foreign row id + own-tenant
		// payload company pass, and doDelete then soft-deleted the
		// foreign row (reproduced live on /org/clinicOffice/delete).
		verifyTenantOwnershipAgainstStoredRow(entity);
		// DS2 (DS1 completeness): delete payloads usually carry only
		// {id}, so createAction would record NULL company and the
		// delete event would be invisible to the object's tenant in
		// lists/drawer (and would trip DS1's strict get check for
		// restores). Resolve the audit tenant from the STORED row
		// BEFORE doDelete (after it the row is flagged and dao.get
		// may not return it). Only fills a blank payload company;
		// never overrides a payload company, and never affects the
		// verifies above (already run) or the delete itself.
		if ((entity.getCompany() == null || StringUtils.isBlank(entity.getCompany().getId()))
				&& StringUtils.isNotBlank(entity.getId())) {
			T stored = dao.get(entity.getId());
			if (stored != null && stored.getCompany() != null) {
				entity.setCompany(stored.getCompany());
			}
		}
		entity.preUpdate();
		int rows = doDelete(entity);
		// DS41: only write the audit row when the delete names a single
		// object. Some mapper <delete> statements have an <otherwise>
		// branch that deletes by a PARENT key when id is blank (e.g.
		// supplier_stock by supplier_storage_id, sys_dict_item by
		// dict_type_id -- 18 mappers), so rows > 0 with a blank id is a
		// real multi-row delete; createAction would then set
		// object_id = null and the sys_action insert throws
		// "Column 'object_id' cannot be null", rolling the whole delete
		// back as HTTP 500 (probe-proven on DictItem delete-by-type and
		// on SupplierStock no-id delete of a voided slip). A no-id
		// delete names no single object, so there is nothing to audit;
		// the framework's own blessed cascade paths (DictTypeService
		// delete) already delete child rows DAO-direct
		// with no audit row. (DS43: the org-dictionary service, the
		// second path named here before, was removed with the whole
		// dead org-dictionary feature -- its tables never existed in
		// the seed or the test DB, no router/menu or UI called it,
		// and every endpoint 500d.) save() can never hit this (preInsert
		// generates the id first) and bulkDelete never calls saveAction,
		// so this guard only affects the no-id delete path. DbActionSaver
		// stays strict: a null objectId from any other path still fails
		// loudly instead of silently dropping audit.
		if(rows > 0 && StringUtils.isNotBlank(entity.getId())) {
			this.saveAction(this.createAction(ActionConstants.ACTION_DELETED, entity));
		}
		return rows;
	}

	/**
	 * hàng loạtThêm
	 * @param entitys
	 * @return
	 */
	public List<String> bulkInsert(List<T> entitys) {
		List<String> ids = Lists.newArrayList();
		for(T entity: entitys) {
			// ITER30: bulkInsert truoc day khong verify tenant (bulkUpdate/
			// bulkDelete thi co) nen POST bulkInsert voi company cua tenant
			// khac ghi thang dong vao tenant do (tai hien live o
			// /org/clinicOffice/bulkInsert). Verify tung dong nhu bulkUpdate:
			// company rong thi verify bo qua (giong save-create), company
			// lech tenant thi fail-closed ca list nho @Transactional cap class.
			// Cac service ghi de bulkInsert rieng (User, His*) khong bi anh huong.
			verifyTenantOwnership(entity);
			entity.preInsert();
			ids.add(entity.getId());
		}
		dao.bulkInsert(entitys);
		return ids;
	}

	/**
	 * hàng loạtSửa
	 * @param entitys
	 * @return
	 */
	public List<String> bulkUpdate(List<T> entitys) {
		List<String> ids = Lists.newArrayList();
		for(T entity: entitys) {
			verifyTenantOwnership(entity);
			// ITER35 M1: also verify against the STORED row, not only the
			// payload company. Payload-only verification let a foreign row
			// id + own-tenant payload company pass, and the bulkUpdate SQL
			// (WHERE id IN payload ids) then overwrote + re-homed the
			// foreign row (reproduced live on /org/clinicOffice/bulkUpdate).
			// The loop runs before dao.bulkUpdate, so a throw here writes
			// nothing (class-level @Transactional + RuntimeException).
			verifyTenantOwnershipAgainstStoredRow(entity);
			entity.preUpdate();
			ids.add(entity.getId());
		}
		dao.bulkUpdate(entitys);
		return ids;
	}

	/**
	 * ITER35 M1: cross-check a write payload against the row already stored
	 * in the DB, instead of trusting the company the payload carries.
	 * Rules mirror verifyTenantOwnership, only the company source changes:
	 * blank id, missing stored row, or blank stored company pass (same as
	 * today's semantics: a nonexistent id writes 0 rows, and orphan rows
	 * keep the legacy blank behaviour); a stored company that is neither
	 * the login tenant nor (for institution-shared services) the tenant's
	 * institution throws, so a foreign row can no longer be overwritten
	 * or re-homed by carrying an own-tenant company in the payload.
	 */
	protected void verifyTenantOwnershipAgainstStoredRow(T entity) {
		if (entity == null || StringUtils.isBlank(entity.getId())) {
			return;
		}
		T stored = dao.get(entity.getId());
		if (stored == null || stored.getCompany() == null) {
			return;
		}
		String storedCompanyId = stored.getCompany().getId();
		if (StringUtils.isBlank(storedCompanyId)) {
			return;
		}
		String tenantId = SessionUtils.getLoginTenantId();
		if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
			return;
		}
		if (tenantId.equals(storedCompanyId)) {
			return;
		}
		if (isInstitutionShared()) {
			String institution = companyService.getInstitution(tenantId);
			if (StringUtils.isNotBlank(institution) && institution.equals(storedCompanyId)) {
				return;
			}
		}
		throw new ServiceException("Không có quyền truy cập tenant khácDữ liệu");
	}

	/**
	 * Xóa hàng loạt
	 * @param entitys
	 * @return
	 */
	public int bulkDelete(List<T> entitys) {
		// DS126 B: fail-closed 400 for malformed lists (DS106/DS107
		// pattern, now at the base so the 41 services with no bulkDelete
		// override -- and every overrider that calls super -- are covered):
		// - null/empty list used to reach dao.bulkDelete, where an empty
		//   MyBatis <foreach> produced BadSqlGrammarException (mapped to
		//   400 only by the DS94 handler, with a misleading sort/filter
		//   message) or, where no bulkDelete mapper exists, a 500
		//   BindingException (probe-proven on SysCollect).
		// - a null element used to NPE at entity.preUpdate() below -> 500
		//   (probe-proven on Clinic/CostItem/Patient).
		// - a blank-id element used to return 200 rows 0, a silent no-op
		//   (probe-proven; same class as DS123 single-delete blank -> 400).
		// Deliberately UNCHANGED: a ghost id still returns 200 rows 0
		// (idempotent by design, DS99 G11 / DS106 -- the tenant checks
		// below skip a null stored row on purpose). The loop still runs
		// before dao.bulkDelete, so a throw writes nothing. No internal
		// service-level caller passes null/blank-id entities: the only
		// one, ClinicVersionService.bulkDeleteClinic, already rejects
		// null/blank ids first; UserService/DepartmentService have custom
		// bulkDelete implementations that do not call super; DAO-level
		// bulkDelete calls are unaffected.
		if (entitys == null || entitys.isEmpty()) {
			throw new ServiceException("Danh sách xóa không được để trống");
		}
		for(T entity: entitys) {
			if (entity == null) {
				throw new ServiceException("Dòng xóa không được để trống");
			}
			if (StringUtils.isBlank(entity.getId())) {
				throw new ServiceException("Dòng xóa không xác định được id cần xóa");
			}
			verifyTenantOwnership(entity);
			// ITER35 M2: stored-row cross-check (same as M1 bulkUpdate).
			// A mixed list [own, foreign] previously returned 200 and
			// soft-deleted the foreign row too (reproduced live on
			// /org/clinicOffice/bulkDelete); the loop runs before
			// dao.bulkDelete, so a throw writes nothing.
			verifyTenantOwnershipAgainstStoredRow(entity);
			entity.preUpdate();
		}
		return dao.bulkDelete(entitys);
	}

	/**
	 * tenantqualọcdự phòng: NếuTham số trong Không có company_id, thìtự độnginjectHiện tạiĐăng nhậptenant của  company_id
	 * chỉđối vớithực thểLớpbản thânkhai báorồi company Trường của nghiệp vụthực thểsinhhiệu lực(xếptrừchỉTừ  DataEntity kế thừa của Hệ thốngthực thể)
	 * @param parameters Danh sách tham số truy vấn
	 */
	protected void ensureCompanyFilter(List<Parameter> parameters) {
		if (parameters == null) {
			return;
		}
		// Kiểm trathực thểLớpbản thânCó hay khôngcó company Trường(khôngchứachaLớp), Không cóthìBỏ qua
		try {
			Class<?> entityClass = Reflections.getClassGenricType(getClass(), 1);
			if (entityClass == null || entityClass == Object.class || !Reflections.hasOwnField(entityClass, "company")) {
				return;
			}
		} catch (Exception e) {
			logger.debug("ensureCompanyFilter check failed: {}", e.getMessage());
			return;
		}
		// Loại bỏ ở frontendtruyền vào của  company_id, Tránhvượt quyềnTruy vấn/thao táccủa nónótenantDữ liệu(IDOR)
		Parameter.extractAndRemoveCompanyId(parameters);
		// thống nhấtbằng[phiênĐăng nhậptenant]làm là cách lybênranh giới,  và  verifyTenantOwnership Giữ nhất quán
		String tenantId = SessionUtils.getLoginTenantId();
		if (StringUtils.isNotBlank(tenantId) && !"null".equals(tenantId)) {
			wrapBodyParamsForTenantFilter(parameters);
			parameters.add(new Parameter("company_id", "=", tenantId));
		}
	}

	/**
	 * AA1: bọc toàn bộ tham số body trong cặp ngoặc trước khi AND
	 * tenant filter. Trước đây SQL có dạng
	 * "body... OR phone LIKE Y AND company_id = tenant" — do AND kết
	 * chặt hơn OR nên disjunct OR đứng trước thoát khỏi tenant filter
	 * và trả về dòng của mọi tenant (đã tái hiện live ở patient/list).
	 * Sau sửa: "(body...) AND company_id = tenant" nên mọi disjunct
	 * đều nằm trong tenant. Chỉ bọc khi có ít nhất một tham số thực
	 * sự render ra SQL dùng logic OR; các trường hợp còn lại giữ
	 * nguyên hành vi cũ (không bọc khi body rỗng hoặc toàn AND).
	 * Tham số không render (giá trị rỗng — whereParams bỏ qua) chỉ
	 * bị loại khi bọc, vì "( )" rỗng sẽ lỗi SQL; loại chúng là
	 * trung hòa với output SQL. Logic của tham số đầu tiên trong
	 * nhóm được đặt thành "" vì ở đầu nhóm không có điều kiện nào
	 * đứng trước nó. Nhóm người dùng mất cân bằng ngoặc vẫn lỗi SQL
	 * fail-closed như trước, không rò rỉ.
	 */
	private void wrapBodyParamsForTenantFilter(List<Parameter> parameters) {
		List<Parameter> rendering = Lists.newArrayList();
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
		List<Parameter> wrapped = Lists.newArrayList();
		wrapped.add(new Parameter(null, "(", null, "AND"));
		wrapped.addAll(rendering);
		wrapped.add(new Parameter(null, ")", null, ""));
		parameters.clear();
		parameters.addAll(wrapped);
	}

	/**
	 * AA1: tham số có render ra SQL trong Common.whereParams hay không.
	 * "(" / ")" / "is null" / "is not null" luôn render; các nhánh còn
	 * lại đều bỏ qua khi value null hoặc chuỗi rỗng; "in" bỏ qua khi
	 * danh sách rỗng. Các dạng value khác giữ nguyên (bảo thủ).
	 */
	private boolean isRenderingParam(Parameter p) {
		String queryType = p.getQueryType();
		if (queryType == null) {
			return true;
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

	/**
	 * Kiểm trathực thểCó hay khôngthuộctạiHiện tạitenant
	 * dùng để get/save/delete  v.v. thao tác của an toànKiểm tra
	 * @param entity chờKiểm tra của thực thể
	 * @throws ServiceException Nếuthực thểkhôngthuộctạiHiện tạitenant
	 */
	protected void verifyTenantOwnership(DataEntity<?> entity) {
		if (entity == null || entity.getCompany() == null) {
			return;
		}
		String tenantId = SessionUtils.getLoginTenantId();
		// vượtquản lý / sentinel(tenantId  là trống hoặc  "null")khônggiới hạn
		if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
			return;
		}
		String entityCompanyId = entity.getCompany().getId();
		if (StringUtils.isBlank(entityCompanyId)) {
			return;
		}
		// Phòng khámcấpDữ liệucách ly: nghiệp vụDữ liệuchỉthuộctạiĐăng nhậpPhòng khámbản thân(Với danh sách ensureCompanyFilter  của chính xác company_id Nhất quán). 
		if (!tenantId.equals(entityCompanyId)) {
			// Cơ sởtừ điển dùng chungthực thể(Thuốc/vật tư tiêu hao/Chi phíhạng mục v.v. , Danh sách SQL  là  company_id=#{id} or #{institution}): 
			// cho phéptruy cậpthuộc về"Cơ sởPhòng khám"(cùng parent dưới is_institution=1) Dữ liệu của, Với danh sáchcó thểthấyphạmviGiữ nhất quán
			if (isInstitutionShared()) {
				String institution = companyService.getInstitution(tenantId);
				if (StringUtils.isNotBlank(institution) && institution.equals(entityCompanyId)) {
					return;
				}
			}
			throw new ServiceException("Không có quyền truy cập tenant khácDữ liệu");
		}
	}

	/**
	 * thực thểCó hay không là "Cơ sởtừ điển dùng chung"(Dữ liệu cơ bản theo phòng khám của cơ sở được chia sẻ cho các phòng khám thuộc cùng cơ sở). 
	 * Mặc định false(nghiệp vụDữ liệunghiêmôPhòng khámcấpcách ly); chỉtừ điểnLớp Service(Drug/Stuff/CostItem  v.v. )viết lại là  true, 
	 *  và của nóDanh sách SQL(company_id = #{id} or company_id = #{institution}) của có thểthấyphạmviGiữ nhất quán. 
	 */
	protected boolean isInstitutionShared() {
		return false;
	}


	/**
	 * Xây dựng yêu cầu phân trang kèm thông tin tenant
	 * Trích xuất từ tham sốcompany_id, LấyCơ sởThông tin, xây dựngPageRequest
	 * @param parameters Tham số truy vấn
	 * @param orderby Sắp xếp
	 * @return PageRequest
	 */
	protected PageRequest buildTenantPageRequest(List<Parameter> parameters, String orderby) {
		// Loại bỏ ở frontend company_id(Ngăn vượt quyền), Thống nhất lấy tenant đăng nhập trong phiên làm ranh giới cách ly
		Parameter.extractAndRemoveCompanyId(parameters);
		String id = SessionUtils.getLoginTenantId();
		if ("null".equals(id)) {
			id = null;
		}
		String institution = companyService.getInstitution(id);
		return new PageRequest(parameters, orderby, id, institution);
	}

	/**
	 * Xây dựng yêu cầu phân trang kèm thông tin tenant(kèm phúttrangTham số)
	 * @param parameters Tham số truy vấn
	 * @param offset độ lệchlượng
	 * @param limit mỗitrangSố lượng
	 * @param orderby Sắp xếp
	 * @return PageRequest
	 */
	protected PageRequest buildTenantPageRequest(List<Parameter> parameters, int offset, int limit, String orderby) {
		// Loại bỏ ở frontend company_id(Ngăn vượt quyền), Thống nhất lấy tenant đăng nhập trong phiên làm ranh giới cách ly
		Parameter.extractAndRemoveCompanyId(parameters);
		String id = SessionUtils.getLoginTenantId();
		if ("null".equals(id)) {
			id = null;
		}
		String institution = companyService.getInstitution(id);
		return new PageRequest(offset, limit, parameters, orderby, id, institution);
	}

	/**
	 * Kiểm traCó hay khôngcótrùng lặp của Dữ liệu
	 * @param curdDao   dao
	 * @param entity	nghiệp vụDữ liệu
	 * @param colMaps	Kiểm traTrường
	 * @return
	 */
	protected boolean exists(CrudDao<?> curdDao, DataEntity<?> entity, Map<String, String> colMaps) {
		if(colMaps == null || colMaps.size() <= 0) {
			return false;
		}

		List<Parameter> params = Lists.newArrayList();

		if(!StringUtils.isBlank(entity.getId())) {
			params.add(new Parameter("id", "<>", entity.getId()));
		}


		for(String column: colMaps.keySet()) {
			params.add(new Parameter(StringUtils.toUnderScoreCase(column), "=", Reflections.invokeGetter(entity, colMaps.get(column))));
		}

		PageRequest pageRequest = new PageRequest(params);
		int row = curdDao.count(pageRequest);
		return row > 0;
	}

	/**
	 * chènthao tác
	 * @param entity
	 * @return
	 */
	protected int doInsert(T entity) {
		return dao.insert(entity);
	}


	/**
	 * Cập nhậtthao tác
	 * @param entity
	 * @return
	 */
	protected int doUpdate(T entity) {
		return dao.update(entity);
	}

	/**
	 * Xóathao tác
	 * @param entity
	 * @return
	 */
	protected int doDelete(T entity) {
		return dao.delete(entity);
	}

	/**
	 * Tạo nhật ký thao tác
	 * @param actionTypeId  Loại thao tácId
	 * @param entity		Đối tượng thực thể được thao tác
	 * @return
	 */
	protected Action createAction(String actionTypeId, T entity) {
		// Không códel_flagTrường
		if(entity == null || !entity.getBusTableHasDelFlag()) {
			return null;
		}
		Action action = new Action();

		DictItem di = new DictItem();
		di.setValue(actionTypeId);
		action.setActionType(di);
		action.setObjectType(new GenTable(entity.getBusTableId()));
		action.setObjectId(entity.getId());
		action.setObjectName((String)Reflections.invokeGetter(entity, "name"));
		action.setStatus("0");
		// DS1: dong audit mang tenant cua DOI TUONG bi thao tac
		// (sys_action.company_id, migration v15) de doc audit loc
		// duoc theo tenant. Entity khong co company (bang toan cuc,
		// Company...) de NULL: fail-closed. Dat o day vi save()/
		// delete() goi createAction SAU doInsert/doUpdate/doDelete
		// nen company luc nay la company cuoi cung cua entity.
		if (entity.getCompany() != null && StringUtils.isNotBlank(entity.getCompany().getId())) {
			action.setCompany(entity.getCompany());
		}


		List<ActionRecycle> recycles = Lists.newArrayList();
		// Khi xóa, ghi lại và lưu dữ liệu đã lưu vào thùng rác
		if(ActionConstants.ACTION_DELETED.equals(actionTypeId)) {
			ActionRecycle recycle = new ActionRecycle();
			recycle.setTableName(entity.getBusTableName());
			recycle.setObjectId(entity.getId());
			recycle.setObjectName((String)Reflections.invokeGetter(entity, "name"));
			recycles.add(recycle);
		}
		action.setActionRecycleList(recycles);
		return action;
	}

	/**
	 * hàng loạtPhương thức thực thi trước khi cập nhật, Cần gọi thủ công
	 * @param entities
	 * @return
	 */
	protected List<DataEntity> preUpdateList(List<DataEntity> entities) {
		for(DataEntity entity: entities) {
			entity.preUpdate();
		}
		return entities;
	}

	/**
	 * LưuNhật ký thao tác
	 */
	protected void saveAction(Action action) {
		actionSaver.saveAction(action);
	}

	/**
	 * Truy vấn phân trangmẫuPhương thức
	 * @param countFn Truy vấntổng số của Hàm
	 * @param listFn Truy vấnDanh sách của Hàm
	 * @return  phúttrangKết quả
	 */
	protected <R> Page<R> paginate(java.util.function.Supplier<Integer> countFn, java.util.function.Supplier<List<R>> listFn) {
		int total = countFn.get();
		List<R> list = total > 0 ? listFn.get() : java.util.Collections.emptyList();
		return new Page<>((long) total, list);
	}

	/**
	 * xây dựngđơnĐiều kiệnTham số truy vấn
	 * @param column Cộttên
	 * @param operator thao tácký tự
	 * @param value Giá trị
	 * @return PageRequest
	 */
	protected PageRequest buildPageRequest(String column, String operator, Object value) {
		return new PageRequest(Lists.newArrayList(new Parameter(column, operator, value)));
	}

	/**
	 * xây dựngnhiềuĐiều kiệnchính xáckhớpTham số truy vấn
	 * @param exactFilters chính xáckhớpĐiều kiệnMap
	 * @return PageRequest
	 */
	protected PageRequest buildPageRequest(Map<String, Object> exactFilters) {
		List<Parameter> params = Lists.newArrayList();
		for (Map.Entry<String, Object> entry : exactFilters.entrySet()) {
			params.add(new Parameter(entry.getKey(), "=", entry.getValue()));
		}
		return new PageRequest(params);
	}

	/**
	 * conbảngđồng bộ: so vớisocũDanh sách và mớiDanh sách, hành nghề dòngXóa, Cập nhật, Thêm mớithao tác
	 * @param oldList cũDữ liệuDanh sách
	 * @param newList mớiDữ liệuDanh sách
	 * @param childDao Bảng conDAO
	 * @param idMatcher IDkhớpthiết bị
	 */
	@SuppressWarnings("unchecked")
	protected <E> void syncChildList(List<E> oldList, List<E> newList,
									 CrudDao<E> childDao, java.util.function.BiPredicate<E, E> idMatcher) {
		List<E> toDelete = new java.util.ArrayList<>();
		List<E> toInsert = new java.util.ArrayList<>();
		List<E> toUpdate = new java.util.ArrayList<>();

		// dùng Set Bộ nhớ đệm newList  của  ID, Sẽ  O(n*m) giảm là  O(n+m)
		java.util.Set<String> newIds = new java.util.HashSet<>();
		for (E newItem : newList) {
			if (newItem instanceof DataEntity) {
				String id = ((DataEntity<?>) newItem).getId();
				if (StringUtils.isNotBlank(id)) {
					newIds.add(id);
				}
			}
		}

		// tìm raCầnXóa của Bản ghi(oldList  trong không ở newList Trong)
		for (E oldItem : oldList) {
			if (oldItem instanceof DataEntity) {
				String oldId = ((DataEntity<?>) oldItem).getId();
				if (!newIds.contains(oldId)) {
					toDelete.add(oldItem);
				}
			} else {
				// không phải DataEntity Loạitrả lạitrảĐến BiPredicate khớp
				boolean found = newList.stream().anyMatch(newItem -> idMatcher.test(oldItem, newItem));
				if (!found) {
					toDelete.add(oldItem);
				}
			}
		}

		// tìm raCầnThêm mới và Cập nhật của Bản ghi
		for (E newItem : newList) {
			if (newItem instanceof DataEntity) {
				DataEntity<?> entity = (DataEntity<?>) newItem;
				if (StringUtils.isBlank(entity.getId())) {
					entity.preInsert();
					toInsert.add(newItem);
				} else {
					entity.preUpdate();
					toUpdate.add(newItem);
				}
			}
		}

		// hành nghề dònghàng loạtthao tác
		if (!toDelete.isEmpty()) {
			childDao.bulkDelete(toDelete);
		}
		if (!toUpdate.isEmpty()) {
			childDao.bulkUpdate(toUpdate);
		}
		if (!toInsert.isEmpty()) {
			childDao.bulkInsert(toInsert);
		}
	}

}