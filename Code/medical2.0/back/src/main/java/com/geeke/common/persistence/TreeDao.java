package com.geeke.common.persistence;

import java.util.List;

/**
 * DAOLớp hỗ trợ triển khai
 * @author lys
 * @version 2014-05-16
 * @param <T>
 */
public interface TreeDao<T extends TreeEntity<T>> extends CrudDao<T> {

	
}