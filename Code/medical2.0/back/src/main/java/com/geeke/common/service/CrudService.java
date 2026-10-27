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
		// Cập nhậtthao tácCầnKiểm tratenantthuộc về
		if (StringUtils.isNotBlank(entity.getId())) {
			verifyTenantOwnership(entity);
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
		entity.preUpdate();
		int rows = doDelete(entity);
		if(rows > 0) {
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
			entity.preUpdate();
			ids.add(entity.getId());
		}
		dao.bulkUpdate(entitys);
		return ids;
	}

	/**
	 * Xóa hàng loạt
	 * @param entitys
	 * @return
	 */
	public int bulkDelete(List<T> entitys) {
		for(T entity: entitys) {
			verifyTenantOwnership(entity);
			entity.preUpdate();
		}
		return dao.bulkDelete(entitys);
	}

	/**
	 * tenantqualọc兜底: NếuTham số trong Không có company_id, thìtự độnginjectHiện tạiĐăng nhậptenant của  company_id
	 * chỉđối vớithực thểLớpbản thânkhai báorồi company Trường của nghiệp vụthực thểsinh效(排除chỉTừ  DataEntity kế thừa của Hệ thốngthực thể)
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
		// Loại bỏ ở frontendtruyền vào của  company_id, Tránh越权Truy vấn/thao táccủa nónótenantDữ liệu(IDOR)
		Parameter.extractAndRemoveCompanyId(parameters);
		// thống nhấtbằng[phiênĐăng nhậptenant]làm là cách ly边界,  và  verifyTenantOwnership Giữ nhất quán
		String tenantId = SessionUtils.getLoginTenantId();
		if (StringUtils.isNotBlank(tenantId) && !"null".equals(tenantId)) {
			parameters.add(new Parameter("company_id", "=", tenantId));
		}
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
		// vượtquản lý / 哨兵(tenantId  là trống hoặc  "null")khônggiới hạn
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
			// cho phéptruy cậpthuộc về"Cơ sởPhòng khám"(cùng parent dưới is_institution=1) Dữ liệu của, Với danh sáchcó thể见范围Giữ nhất quán
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
	 * Mặc định false(nghiệp vụDữ liệu严ôPhòng khámcấpcách ly); chỉtừ điểnLớp Service(Drug/Stuff/CostItem  v.v. )viết lại là  true, 
	 *  và của nóDanh sách SQL(company_id = #{id} or company_id = #{institution}) của có thể见范围Giữ nhất quán. 
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
	 * @param offset 偏移lượng
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
	 * conbảngđồng bộ: so với较cũDanh sách và mớiDanh sách, hành nghề dòngXóa, Cập nhật, Thêm mớithao tác
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

		// dùng Set Bộ nhớ đệm newList  của  ID, Sẽ  O(n*m) 降 là  O(n+m)
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