package com.geeke.sys.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geeke.common.constants.ActionConstants;
import com.geeke.sys.dao.RecycleDao;
import com.geeke.sys.entity.Action;
import com.geeke.sys.entity.ActionRecycle;
import com.geeke.sys.entity.DictItem;

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
		entity = this.get(entity.getId());
		int rows = recycleDao.updateActionStaus(entity.getId());
		if(rows > 0) {
			for(ActionRecycle busObject: entity.getActionRecycleList()) {
				recycleDao.restore(busObject.getTableName(), busObject.getObjectId());
			}
			
			this.saveAction(this.createAction(ActionConstants.ACTION_RESTORED, entity));
		}
		return rows;
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
		return action;
	}
}