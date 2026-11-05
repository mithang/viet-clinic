package com.geeke.sys.service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geeke.common.constants.ActionConstants;
import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.service.ServiceException;
import com.geeke.org.entity.Company;
import com.geeke.sys.dao.RecycleDao;
import com.geeke.sys.entity.Action;
import com.geeke.sys.entity.ActionRecycle;
import com.geeke.sys.entity.DictItem;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;

/**
 * Nhật ký thao tácService
 * @author lys	Viết thủ công
 * @version 2019-12-12
 */
 
@Service("recycleService")
@Transactional(readOnly = true)
public class RecycleService extends ActionService {
	
	@Autowired
	private RecycleDao recycleDao;
	
	@Transactional(readOnly = false)
	public int restore(Action entity) {
		// DS44: get() tra null khi id trong/khong ton tai (hop dong
		// CrudService.get, DS42) — deref entity.getId() o dong duoi
		// NPE 500 (4 dong trong backend-ds43.log). Fail-closed 400
		// truoc khi deref; chi goi updateActionStaus/restore khi
		// entity ton tai (mau ServiceException cua DS42).
		if (entity == null || StringUtils.isBlank(entity.getId())) {
			throw new ServiceException("Không tìm thấy bản ghi nhật ký cần khôi phục!");
		}
		entity = this.get(entity.getId());
		if (entity == null) {
			throw new ServiceException("Không tìm thấy bản ghi nhật ký cần khôi phục!");
		}
		// DS46: chi khoi phuc duoc dong DELETED. Chi ACTION_DELETED
		// moi sinh dong sys_action_recycle (CrudService.createAction,
		// nhanh DELETED); dong CREATED/UPDATED/LOGIN/LOGOUT khong co
		// dong recycle nao nen khong co gi de khoi phuc, nhung
		// restore() truoc day van chap nhan bat ky action status='0'
		// nao: lat status 0->1 va ghi them dong audit RESTORED gia
		// cho cung object_id (probe ds46 pre: CREATED + UPDATED moi
		// dong deu 200 rows 1, restored_rows 0 -> 2). UI da gate nut
		// Khoi phuc bang status 0 + actionType deleted
		// (views/sys/action/index.vue:84); API nay moi ep cung dieu
		// kien do. Kiem tra ca 2 ve: loai thao tac phai la 'deleted'
		// VA danh sach recycle khong rong — bat bien live da chung
		// minh 0 dong DELETED thieu recycle va 0 dong khong phai
		// DELETED co recycle, nen guard khong chan khoi phuc hop le.
		// Fail-closed 400 (mau DS42/DS44/DS45) truoc claim DS45.
		if (entity.getActionType() == null
				|| !ActionConstants.ACTION_DELETED.equals(entity.getActionType().getValue())
				|| entity.getActionRecycleList() == null || entity.getActionRecycleList().isEmpty()) {
			throw new ServiceException("Chỉ bản ghi đã xóa mới có thể khôi phục!");
		}
		int rows = recycleDao.updateActionStaus(entity.getId());
		// DS45: claim co dieu kien (mapper: AND status='0') — rows==0
		// nghia la action da duoc khoi phuc truoc do (hoac bi doi
		// trang thai giua chung). Fail-closed 400, khong chay lai
		// restore() tren cac dong recycle va khong ghi them dong
		// audit RESTORED trung. Claim la atomic nen 2 restore song
		// song cung action chi 1 duoc chap nhan.
		if (rows == 0) {
			throw new ServiceException("Bản ghi này đã được khôi phục trước đó!");
		}
		if(rows > 0) {
			for(ActionRecycle busObject: entity.getActionRecycleList()) {
				recycleDao.restore(busObject.getTableName(), busObject.getObjectId());
			}

			this.saveAction(this.createAction(ActionConstants.ACTION_RESTORED, entity));
		}
		return rows;
	}

	// DS2: list rieng cho /sys/recycle — KHONG the dung
	// ensureCompanyFilter cua CrudService o day. Filter generic giai
	// entity class bang Reflections.getClassGenricType(getClass(), 1),
	// chi doc DIRECT superclass: RecycleService -> ActionService (lop
	// concrete, khong co type parameter) -> tra Object.class -> filter
	// bo qua lang le. Hau qua da chung minh live: clinic /sys/recycle/
	// list thay TOAN BO dong (158 dong, ca marker la). Khong sua util
	// generic (anh huong toan bo service) — loc tuong minh o day:
	// admin giu action:list bypass (nhu ActionService), con lai them
	// company_id = session tenant (dong NULL bi loc mat, fail-closed).
	// Luu y: khong goi wrapBodyParamsForTenantFilter (private trong
	// CrudService) — caller cua recycle list chi gui filter don
	// (drawer: 1 eq object_id; trang admin: cac truong AND), khong co
	// nhom OR kieu iter20; neu sau nay co caller gui OR thi phai wrap
	// truoc khi them company_id.
	@Override
	public Page<Action> listPage(List<Parameter> parameters, int offset, int limit, String orderby) {
		if (hasActionList()) {
			PageRequest pageRequest = new PageRequest(offset, limit, parameters, orderby);
			int total = dao.count(pageRequest);
			List<Action> list = total > 0 ? dao.listPage(pageRequest) : Collections.emptyList();
			return new Page<Action>(total, list);
		}
		return listScoped(parameters, offset, limit, orderby);
	}

	@Override
	public List<Action> listAll(List<Parameter> parameters, String orderby) {
		if (hasActionList()) {
			PageRequest pageRequest = new PageRequest(parameters, orderby);
			return dao.listAll(pageRequest);
		}
		Parameter.extractAndRemoveCompanyId(parameters);
		String tenantId = SessionUtils.getLoginTenantId();
		if (StringUtils.isNotBlank(tenantId) && !"null".equals(tenantId)) {
			parameters.add(new Parameter("company_id", "=", tenantId));
		}
		PageRequest pageRequest = new PageRequest(parameters, orderby);
		return dao.listAll(pageRequest);
	}

	private Page<Action> listScoped(List<Parameter> parameters, int offset, int limit, String orderby) {
		Parameter.extractAndRemoveCompanyId(parameters);
		String tenantId = SessionUtils.getLoginTenantId();
		if (StringUtils.isNotBlank(tenantId) && !"null".equals(tenantId)) {
			parameters.add(new Parameter("company_id", "=", tenantId));
		}
		PageRequest pageRequest = new PageRequest(offset, limit, parameters, orderby);
		int total = dao.count(pageRequest);
		List<Action> list = total > 0 ? dao.listPage(pageRequest) : Collections.emptyList();
		return new Page<Action>(total, list);
	}
	
	/**
	 * Tạo nhật ký thao tác
	 * @param actionTypeId  Loại thao tácId
	 * @param entity		Đối tượng thực thể được thao tác
	 * @return
	 */
    @Override
    protected Action createAction(String actionTypeId,  Action entity) {
		Action action = new Action();
		DictItem di = new DictItem();
		di.setValue(actionTypeId);
		action.setActionType(di);
		action.setObjectType(entity.getObjectType());
		action.setObjectId(entity.getObjectId());
		action.setObjectName(entity.getObjectName());
		action.setStatus("0");
		// DS2: dong RESTORED mang tenant cua dong goc (lookup 1 cot,
		// mau N1) de tenant cua doi tuong thay su kien khoi phuc
		// trong list/drawer. Override nay khong goi super
		// (CrudService.createAction) nen logic company cua DS1 khong
		// ap o day; dong goc NULL company -> dong restored NULL
		// (fail-closed, nhu cu).
		if (StringUtils.isNotBlank(entity.getId())) {
			String companyId = actionDao.getCompanyIdById(entity.getId());
			if (StringUtils.isNotBlank(companyId)) {
				action.setCompany(new Company(companyId));
			}
		}
		return action;
	}
}