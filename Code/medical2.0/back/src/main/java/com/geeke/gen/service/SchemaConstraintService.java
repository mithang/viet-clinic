package com.geeke.gen.service;

import com.geeke.common.service.BaseService;
import com.geeke.gen.dao.SchemaConstraintDao;
import com.geeke.gen.entity.Constraint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Hệ thốngbảngràng buộcService
 * @author lys
 * @version 2019-08-31
 */
 
@Service("schemaConstraintService")
@Transactional(readOnly = true)
public class SchemaConstraintService extends BaseService {
	/**
	 * tầng persistenceĐối tượng
	 */
	@Autowired
	protected SchemaConstraintDao dao;
	
	public List<Constraint> listAll(String tableName) {
		return dao.listAll(tableName);
	}

}