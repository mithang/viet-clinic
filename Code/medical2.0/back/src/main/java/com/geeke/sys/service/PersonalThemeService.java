package com.geeke.sys.service;

import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geeke.common.service.CrudService;
import com.geeke.sys.dao.PersonalThemeDao;
import com.geeke.sys.entity.PersonalTheme;
import com.google.common.collect.Maps;

/**
 * Chủ đề hệ thốngService
 * @author lys
 * @version 2021-07-16
 */
 
@Service("personalThemeService")
public class PersonalThemeService extends CrudService<PersonalThemeDao, PersonalTheme>{

}