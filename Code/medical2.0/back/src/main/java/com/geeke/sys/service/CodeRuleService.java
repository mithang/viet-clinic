package com.geeke.sys.service;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.sys.dao.CodeRuleDao;
import com.geeke.sys.entity.CodeRule;
import com.geeke.utils.StringUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;


import com.google.common.collect.Maps;

/**
 * Quy tắc mã hệ thốngService
 * @author lys
 * @version 2021-05-17
 */
 
@Service("codeRuleService")
@Transactional(readOnly = true)
public class CodeRuleService extends CrudService<CodeRuleDao, CodeRule>{

    // DS257: ruleDef la mot FreeMarker template duoc render moi lan sinh
    // ma (SequenceService.generate -> FreeMarkers.renderString); template
    // sai cu phap truoc day luu 200 roi moi lan tao CostItem/Drug/Stuff/
    // TollInfo/InventoryVerification moi deu 500 (ParseException o
    // FreeMarkers.renderString, probe pre). Chan truoc moi ghi: chi PARSE
    // (khong danh gia) de cac tham chieu ${biz...} hop le van qua, chi
    // cu phap hong moi 400. Blank giu nguyen (do @NotNull/@Valid xu ly
    // o duong save don, bulkUpdate cu khong chan blank).
    private static void checkRuleDefSyntax(CodeRule codeRule) {
        if (codeRule == null || StringUtils.isBlank(codeRule.getRuleDef())) {
            return;
        }
        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);
            new Template("ruleDef", new StringReader(codeRule.getRuleDef()), cfg);
        } catch (Exception e) {
            throw new ServiceException("Quy tắc mã không hợp lệ: " + codeRule.getRuleDef());
        }
    }

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
        checkRuleDefSyntax(codeRule);
        Map<String, String> colMaps = Maps.newHashMap();
        // code
        colMaps.clear();
        colMaps.put("code", "code");
        
        if(exists(dao, codeRule, colMaps)) {
            throw new ServiceException("Đã tồn tại giống hệt Mã");
        }

        CodeRule codeRuleTemp = super.save(codeRule);
        return codeRuleTemp;
    }

    // DS129 A: single delete fail-closed 400 -- mau DrugService.delete.
    // Truoc day delete ghost/blank/da xoa mem deu 200 rows 0/1 silent
    // no-op (mapper UPDATE sys_code_rule SET del_flag='1' WHERE id, KHONG
    // loc del_flag; probe DS129 A pre-fix 7/10). dao.get KHONG loc del_flag
    // nen tra ca dong da xoa mem, delFlag phan biet (mau DrugService).
    // bulkDelete ghost GIU NGUYEN 200 rows 0 idempotent (DS99/DS106/DS126 B).
    @Override
    @Transactional(readOnly = false)
    public int delete(CodeRule codeRule) {
        if (codeRule == null || StringUtils.isBlank(codeRule.getId())) {
            throw new ServiceException("Không xác định được quy tắc mã cần xóa.");
        }
        CodeRule stored = dao.get(codeRule.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Quy tắc mã không tồn tại: " + codeRule.getId());
        }
        verifyTenantOwnershipAgainstStoredRow(codeRule);
        return super.delete(codeRule);
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
            checkRuleDefSyntax(codeRule);
            if (!seenCodes.add(codeRule.getCode())
                    || exists(dao, codeRule, colMaps)) {
                throw new ServiceException("Đã tồn tại giống hệt Mã");
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
            checkRuleDefSyntax(codeRule);
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