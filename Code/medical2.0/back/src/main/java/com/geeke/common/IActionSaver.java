package com.geeke.common;

import com.geeke.sys.entity.Action;

/**
 * ghithao tácNgàygiao diện
 * @author lys
 * 2021/11/11
 */
public interface IActionSaver {
	/**
	 * LưuNgàynhật ký
	 * @param action
	 */
	public void saveAction(Action action);
}
