package com.geeke.gen.service;

import com.geeke.common.service.CrudService;
import com.geeke.gen.dao.SchemaTableDao;
import com.geeke.gen.entity.GenTable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Điều khiển bảng hệ thốngService
 * @author lys
 * @version 2019-08-31
 */
 
@Service("schemaTableService")
@Transactional(readOnly = true)
public class SchemaTableService extends CrudService<SchemaTableDao, GenTable>{


}