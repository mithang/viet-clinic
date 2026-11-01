package com.geeke.druginteraction.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.persistence.BaseEntity;
import com.geeke.common.service.CrudService;
import com.geeke.druginteraction.dao.DrugInteractionRuleDao;
import com.geeke.druginteraction.entity.DrugInteractionRule;
import com.geeke.org.entity.Company;
import com.geeke.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Quy tắc tương tác thuốc Service (G5 research loop).
 *
 * Ranh giới an toàn:
 * - Chỉ đọc/ghi bảng drug_interaction_rule, luôn giới hạn đúng tenant
 *   đang đăng nhập. Không đọc/ghi dữ liệu khám chữa khác.
 * - Validate ở service: tên thuốc 1–100 ký tự sau trim, hai tên phải
 *   khác nhau sau chuẩn hóa (trim + lowercase), mức độ chỉ CANH_BAO
 *   hoặc TRANH, nội dung 1–500 ký tự. Dữ liệu sai trả 400, không ghi DB.
 * - So khớp không phân biệt thứ tự, hoa/thường, khoảng trắng thừa;
 *   kết quả khử trùng theo ruleId. Đầu vào rỗng trả matches rỗng,
 *   không phải lỗi.
 */
@Service("drugInteractionRuleService")
@Transactional(readOnly = true)
@Slf4j
public class DrugInteractionRuleService extends CrudService<DrugInteractionRuleDao, DrugInteractionRule> {

    public static String normalize(String name) {
        return name == null ? "" : name.trim().toLowerCase();
    }

    public List<DrugInteractionRule> listRules(String companyId) {
        if (StringUtils.isBlank(companyId)) {
            return new ArrayList<>();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("companyId", companyId);
        params.put("DEL_FLAG_NORMAL", BaseEntity.DEL_FLAG_NORMAL);
        return dao.findByTenant(params);
    }

    @Transactional(readOnly = false)
    public DrugInteractionRule createRule(String companyId, DrugInteractionRule input) {
        if (StringUtils.isBlank(companyId)) {
            throw new IllegalArgumentException("Chưa xác định được phòng khám.");
        }
        if (input == null) {
            throw new IllegalArgumentException("Quy tắc tương tác không được để trống.");
        }
        String drugA = input.getDrugAName() == null ? null : input.getDrugAName().trim();
        String drugB = input.getDrugBName() == null ? null : input.getDrugBName().trim();
        String severity = input.getSeverity() == null ? null : input.getSeverity().trim();
        String message = input.getMessage() == null ? null : input.getMessage().trim();
        if (StringUtils.isBlank(drugA) || drugA.length() > 100) {
            throw new IllegalArgumentException("Tên thuốc A không được để trống (tối đa 100 ký tự).");
        }
        if (StringUtils.isBlank(drugB) || drugB.length() > 100) {
            throw new IllegalArgumentException("Tên thuốc B không được để trống (tối đa 100 ký tự).");
        }
        if (normalize(drugA).equals(normalize(drugB))) {
            throw new IllegalArgumentException("Hai thuốc trong một quy tắc phải khác nhau.");
        }
        if (!"CANH_BAO".equals(severity) && !"TRANH".equals(severity)) {
            throw new IllegalArgumentException("Mức độ chỉ nhận CANH_BAO hoặc TRANH.");
        }
        if (StringUtils.isBlank(message) || message.length() > 500) {
            throw new IllegalArgumentException("Nội dung cảnh báo không được để trống (tối đa 500 ký tự).");
        }
        Company company = new Company();
        company.setId(companyId);
        input.setCompany(company);
        input.setDrugAName(drugA);
        input.setDrugBName(drugB);
        input.setSeverity(severity);
        input.setMessage(message);
        input.preInsert();
        dao.insert(input);
        return input;
    }

    public List<JSONObject> checkInteractions(String companyId, List<String> drugNames) {
        List<JSONObject> matches = new ArrayList<>();
        if (StringUtils.isBlank(companyId) || drugNames == null) {
            return matches;
        }
        Set<String> drugs = new LinkedHashSet<>();
        for (String name : drugNames) {
            String norm = normalize(name);
            if (!norm.isEmpty()) {
                drugs.add(norm);
            }
        }
        if (drugs.size() < 2) {
            return matches;
        }
        List<String> drugList = new ArrayList<>(drugs);
        List<DrugInteractionRule> rules = listRules(companyId);
        Set<String> seenRuleIds = new HashSet<>();
        for (int i = 0; i < drugList.size(); i++) {
            for (int j = i + 1; j < drugList.size(); j++) {
                String first = drugList.get(i);
                String second = drugList.get(j);
                for (DrugInteractionRule rule : rules) {
                    String a = normalize(rule.getDrugAName());
                    String b = normalize(rule.getDrugBName());
                    if ((a.equals(first) && b.equals(second))
                            || (a.equals(second) && b.equals(first))) {
                        if (rule.getId() != null && !seenRuleIds.add(rule.getId())) {
                            continue;
                        }
                        JSONObject match = new JSONObject();
                        match.put("drugA", rule.getDrugAName());
                        match.put("drugB", rule.getDrugBName());
                        match.put("severity", rule.getSeverity());
                        match.put("message", rule.getMessage());
                        match.put("ruleId", rule.getId());
                        matches.add(match);
                    }
                }
            }
        }
        return matches;
    }
}
