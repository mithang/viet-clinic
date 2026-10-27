package com.geeke.config.action;

import org.springframework.beans.factory.annotation.Autowired;

import com.geeke.common.IActionSaver;
import com.geeke.sys.dao.ActionDao;
import com.geeke.sys.dao.ActionRecycleDao;
import com.geeke.sys.entity.Action;
import com.geeke.sys.entity.ActionRecycle;

/**
 * Ngàynhật kýtrực tiếpghiDữ liệukhoLớp
 * @author lys
 * version: 2021/11/16
 */
public class DbActionSaver  implements IActionSaver {

	/**
	 * Nhật ký thao tácbền vữngĐối tượng
	 */
	@Autowired
	protected ActionDao actionDao;
	
	/**
	 * thao tácThùng rácbền vữngĐối tượng
	 */
	@Autowired
	protected ActionRecycleDao actionRecycleDao;
	
	@Override
	public void saveAction(Action action) {
		if(action == null) {
			return;
		}
		action.preInsert();
		int rows = actionDao.insert(action);
		if(rows > 0) {
			for (ActionRecycle actionRecycle : action.getActionRecycleList()){
	            actionRecycle.setAction(action);
	            actionRecycle.preInsert();
	            actionRecycleDao.insert(actionRecycle);
	        }
		}
		
	}

}
