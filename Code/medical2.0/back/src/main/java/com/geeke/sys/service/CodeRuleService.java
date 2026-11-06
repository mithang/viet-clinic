package com.geeke.sys.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.sys.dao.CodeRuleDao;
import com.geeke.sys.entity.CodeRule;
import com.geeke.utils.StringUtils;


import com.google.common.collect.Maps;

/**
 * Quy tắc mã hệ thốngService
 * @author lys
 * @version 2021-05-17
 */
 
@Service("codeRuleService")
@Transactional(readOnly = true)
public class CodeRuleService extends CrudService<CodeRuleDao, CodeRule>{

    @Override
    @Transactional(readOnly = false)
    public CodeRule save(CodeRule codeRule) {
        // DS104 B1: save voi id khong ton tai truoc day roi qua
        // CrudService.save -> doUpdate 0 dong roi van 200 silent no-op
        // (verifyTenantOwnershipAgainstStoredRow bo qua khi stored==null,
        // cung mau DS94/DS99). Kiem tra ton tai truoc exists() trung ma.
        if (StringUtils.isNotBlank(codeRule.getId())
                && dao.get(codeRule.getId()) == null) {
            throw new ServiceException("Quy tắc mã không tồn tại: "
                    + codeRule.getId());
        }
        Map<String, String> colMaps = Maps.newHashMap();
        // code
        colMaps.clear();
        colMaps.put("code", "code");
        
        if(exists(dao, codeRule, colMaps)) {
            throw new ServiceException("Đã tồn tạigiống hệtMã");
        }

        CodeRule codeRuleTemp = super.save(codeRule);
        return codeRuleTemp;
    }

    // DS104 B3-B8/B10-B12/B16: bulkInsert/bulkUpdate/bulkDelete generic
    // truoc day de lo: null/rong roi xuong MyBatis <foreach> rong -> 500
    // SQL syntax (chi vo tinh ve 400 nho handler DS94, sai message),
    // thieu truong NOT NULL (name/code/rule_def) -> 500
    // DataIntegrityViolation, trung code UNIQUE -> 500 (bulk khong qua
    // exists() cua save), bulkUpdate id khong ton tai -> 200 silent
    // no-op. Chan het o service de 400 ro nghia, kiem tra truoc moi ghi
    // de batch hon hop khong doi dong hop le (B9). @Transactional
    // (readOnly = false) tuong minh vi class-level la readOnly = true
    // (mau DepartmentService DS103).
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<CodeRule> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách quy tắc mã không được để trống");
        }
        Map<String, String> colMaps = Maps.newHashMap();
        colMaps.put("code", "code");
        java.util.Set<String> seenCodes = new java.util.HashSet<>();
        for (CodeRule codeRule : entitys) {
            if (StringUtils.isBlank(codeRule.getName())) {
                throw new ServiceException("Tên quy tắc mã không được để trống");
            }
            if (StringUtils.isBlank(codeRule.getCode())) {
                throw new ServiceException("Mã không được để trống");
            }
            if (StringUtils.isBlank(codeRule.getRuleDef())) {
                throw new ServiceException("Quy tắc không được để trống");
            }
            if (!seenCodes.add(codeRule.getCode())
                    || exists(dao, codeRule, colMaps)) {
                throw new ServiceException("Đã tồn tạigiống hệtMã");
            }
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<CodeRule> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách quy tắc mã không được để trống");
        }
        for (CodeRule codeRule : entitys) {
            if (StringUtils.isNotBlank(codeRule.getId())
                    && dao.get(codeRule.getId()) == null) {
                throw new ServiceException("Quy tắc mã không tồn tại: "
                        + codeRule.getId());
            }
        }
        return super.bulkUpdate(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<CodeRule> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách quy tắc mã không được để trống");
        }
        return super.bulkDelete(entitys);
    }

    
    public List<CodeRule> listNotInCompany(String companyId) {
    	return dao.listNotInCompany(companyId);
    }
}