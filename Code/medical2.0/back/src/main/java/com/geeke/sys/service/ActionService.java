package com.geeke.sys.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geeke.common.constants.ActionConstants;
import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.data.SearchParamsBuilder;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.sys.dao.ActionDao;
import com.geeke.sys.dao.ActionRecycleDao;
import com.geeke.sys.entity.Action;
import com.geeke.sys.entity.ActionRecycle;
import com.geeke.utils.Reflections;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Lists;

/**
 * Nhật ký thao tácService
 * @author lys
 * @version 2020-06-29
 */

@Service("actionService")
@Transactional(readOnly = true)
public class ActionService extends CrudService<ActionDao, Action> {

    @Autowired
	protected ActionDao actionDao;

	@Autowired
	private ActionRecycleDao actionRecycleDao;

	@Override
	public Action get(String id) {
		Action action = super.get(id);
		// DS1: kiem tra tenant cho doc 1 dong. dao.get dung resultType
		// auto-mapping nen khong map company_id ve entity — vi vay
		// verifyTenantOwnership trong super.get khong nhin thay
		// company (luon pass) — lookup rieng 1 cot (mau N1) roi chan
		// o day. Admin giu action:read duoc bypass (xem listPage/
		// listAll ben duoi); session rong/"null" pass nhu verify
		// goc; dong NULL company (legacy/backfill-miss, dong toan
		// cuc) cung bi chan voi session clinic — fail-closed, vi
		// khong luong clinic hop le nao get() truc tiep dong audit
		// (drawer dung listAll, trang admin dung quyen admin).
		// DS42: dong khong ton tai tra null nhu hop dong cua CrudService.get
		// (truoc day NPE o action.getId() ben duoi — DS1 ghi nhan la ngoai
		// pham vi item do; nay dong ho get() NPE: DictType/User/GenTable/
		// Action deu NPE 500 voi id la, probe-chung-minh). Controller tra
		// 200 data null nhu cac get() khac.
		if (action == null) {
			return null;
		}
		if (!hasActionRead()) {
			String tenantId = SessionUtils.getLoginTenantId();
			if (StringUtils.isNotBlank(tenantId) && !"null".equals(tenantId)) {
				String companyId = actionDao.getCompanyIdById(id);
				if (StringUtils.isBlank(companyId) || !tenantId.equals(companyId)) {
					throw new ServiceException("Không có quyền truy cập tenant khácDữ liệu");
				}
			}
		}

    	/*Lấy danh sách bảng con   Thùng rác*/
		List<Parameter> params = SearchParamsBuilder.create()
				.eq("action_id", action.getId())
				.build();
		PageRequest pageRequest = new PageRequest(params);
        action.setActionRecycleList(actionRecycleDao.listAll(pageRequest));
		return action;
	}

	// DS1: doc audit loc theo tenant, tru admin.
	// - Session giu action:list (chi 2000/2001/4000) thay TOAN BO
	//   audit (trang audit admin, export) — bypass ensureCompanyFilter
	//   bang cach goi dao truc tiep (khong qua super). Dieu nay BAT
	//   BUOC vi tenantID cua system la "0" va cua full la OWN: neu
	//   de filter chay, system thay 0 dong, full chi thay dong OWN.
	// - Session con lai di qua super.listPage/listAll: ensureCompanyFilter
	//   them company_id = session tenant (chi ap duoc nho Action khai
	//   company rieng + cot sys_action.company_id, migration v15).
	//   Dong NULL company bi loc mat (company_id = tenant sai voi
	//   NULL) — fail-closed. Drawer (listAll theo object_id) cua
	//   clinic nho vay van hien lich su cua doi tuong thuoc tenant
	//   minh (gom dong cua dong nghiep cung tenant), va tra rong
	//   voi object_id la.
	@Override
	public Page<Action> listPage(List<Parameter> parameters, int offset, int limit, String orderby) {
		if (hasActionList()) {
			PageRequest pageRequest = new PageRequest(offset, limit, parameters, orderby);
			int total = dao.count(pageRequest);
			List<Action> list = total > 0 ? dao.listPage(pageRequest) : java.util.Collections.emptyList();
			return new Page<Action>(total, list);
		}
		return super.listPage(parameters, offset, limit, orderby);
	}

	@Override
	public List<Action> listAll(List<Parameter> parameters, String orderby) {
		if (hasActionList()) {
			PageRequest pageRequest = new PageRequest(parameters, orderby);
			return dao.listAll(pageRequest);
		}
		return super.listAll(parameters, orderby);
	}

	protected boolean hasActionList() {
		try {
			java.util.List<String> permissions = SessionUtils.getUserPermission();
			return permissions != null && permissions.contains("action:list");
		} catch (Exception e) {
			return false;
		}
	}

	protected boolean hasActionRead() {
		try {
			java.util.List<String> permissions = SessionUtils.getUserPermission();
			return permissions != null && permissions.contains("action:read");
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	@Transactional(readOnly = false)
	public Action save(Action action) {

		String id = super.save(action).getId();
		if (StringUtils.isNoneBlank(id)) {

            /* Xử lý bảng con     Thùng rác */
            List<Parameter> params = SearchParamsBuilder.create()
                    .eq("action_id", action.getId())
                    .build();
            PageRequest pageRequest = new PageRequest(params);
            List<ActionRecycle> list_ActionRecycle = actionRecycleDao.listAll(pageRequest);            
            for(ActionRecycle actionRecycleSaved: list_ActionRecycle) {
                boolean found = false;   
                for (ActionRecycle actionRecycle : action.getActionRecycleList()){
                   if(actionRecycleSaved.getId().equals(actionRecycle.getId())){
                       found = true;
                       break;
                   }
                }
                if(!found) {
                   actionRecycleDao.delete(actionRecycleSaved);
                }
            }
			for (ActionRecycle actionRecycle : action.getActionRecycleList()){
				int childRow = 0;
                if (StringUtils.isBlank(actionRecycle.getId())) {
                    actionRecycle.setAction(action);
                    actionRecycle.preInsert();
                    childRow = actionRecycleDao.insert(actionRecycle);
                } else {
                    actionRecycle.preUpdate();
                    childRow = actionRecycleDao.update(actionRecycle);
                }

            }

	    }
        return action;
	}


    /**
     * Xóa
     * @param entity
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(Action action) {
        /* Xử lý bảng con     Thùng rác */
        List<Parameter> params = SearchParamsBuilder.create()
                .eq("action_id", action.getId())
                .build();
        PageRequest pageRequest = new PageRequest(params);
        action.setActionRecycleList(actionRecycleDao.listAll(pageRequest));

        for(ActionRecycle actionRecycleSaved: action.getActionRecycleList()) {
            actionRecycleDao.delete(actionRecycleSaved);
        }

        int rows = super.delete(action);
        return rows;
    }



    /**
     * Tạo nhật ký thao tác
     * @param actionTypeId  Loại thao tácId
     * @param entity        Đối tượng thực thể được thao tác
     * @return
     */
    @Override
    protected Action createAction(String actionTypeId, Action entity) {
        Action action = super.createAction(actionTypeId, entity);
        if(action == null) {
            return null;
        }
        // Khi xóa, ghi lại và lưu dữ liệu đã lưu vào thùng rác
        if(ActionConstants.ACTION_DELETED.equals(actionTypeId)) {
            for(ActionRecycle child: entity.getActionRecycleList()) {
                ActionRecycle recycle = new ActionRecycle();
                recycle.setTableName(child.getBusTableName());
                recycle.setObjectId(child.getId());
                recycle.setObjectName((String)Reflections.invokeGetter(child, "name"));
                action.getActionRecycleList().add(recycle);
            }
        }
        return action;
    }

    @Transactional(readOnly = false)
    public void saveMQtoDB(Action action) {

        int rows = super.dao.insert(action);
        if(rows > 0) {
            for (ActionRecycle actionRecycle : action.getActionRecycleList()){
                actionRecycle.setAction(action);
                actionRecycleDao.insert(actionRecycle);
            }
        }
    }
}