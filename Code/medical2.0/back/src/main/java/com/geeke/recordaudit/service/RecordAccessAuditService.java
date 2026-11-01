package com.geeke.recordaudit.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.persistence.BaseEntity;
import com.geeke.common.service.CrudService;
import com.geeke.org.entity.Company;
import com.geeke.recordaudit.dao.RecordAccessAuditDao;
import com.geeke.recordaudit.entity.RecordAccessAudit;
import com.geeke.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Nhật ký truy cập hồ sơ Service (G8 research loop).
 *
 * Ranh giới an toàn:
 * - Chỉ đọc/ghi bảng record_access_audit, luôn giới hạn đúng tenant
 *   đang đăng nhập. Không đọc/ghi dữ liệu khám chữa khác.
 * - Append-only: logAccess chỉ INSERT; listAudits chỉ SELECT. Không có
 *   phương thức xóa/sửa nào, và controller cũng không mở route đó.
 * - LIST ghi đúng 1 dòng cho cả lần mở danh sách: controller gọi
 *   logAccess một lần với patientId null, không lặp theo từng dòng.
 * - Ghi audit không bao giờ làm hỏng luồng khám chữa: lỗi ghi được
 *   log lại và nuốt ở controller (xem PatientController.logSafely);
 *   ở đây logAccess vẫn ném lỗi nếu dữ liệu đầu vào sai để controller
 *   quyết định, nhưng controller đã bọc try/catch.
 * - Múi giờ: giữ nguyên giờ lưu trữ như mọi create_date khác
 *   (sau H1: new Date() của JVM, JDBC lưu theo GMT+7 — giống G6);
 *   không tự đổi múi giờ, frontend hiển thị đúng giờ lưu trữ đó.
 */
@Service("recordAccessAuditService")
@Transactional(readOnly = true)
@Slf4j
public class RecordAccessAuditService extends CrudService<RecordAccessAuditDao, RecordAccessAudit> {

    public static final String ACTION_VIEW = "VIEW";
    public static final String ACTION_LIST = "LIST";
    public static final String ACTION_EDIT = "EDIT";

    /**
     * Ghi 1 dòng audit. companyId hoặc userJson trống thì bỏ qua lặng
     * (không ghi dòng vô chủ); action ngoài 3 giá trị cho phép thì
     * ném IllegalArgumentException để caller không ghi nhầm nhãn.
     */
    @Transactional(readOnly = false)
    public RecordAccessAudit logAccess(String companyId, JSONObject userJson,
                                       String patientId, String patientName, String action) {
        if (StringUtils.isBlank(companyId)) {
            log.warn("Bỏ qua ghi audit: chưa xác định được phòng khám.");
            return null;
        }
        if (!ACTION_VIEW.equals(action) && !ACTION_LIST.equals(action) && !ACTION_EDIT.equals(action)) {
            throw new IllegalArgumentException("Hành động audit không hợp lệ.");
        }
        RecordAccessAudit audit = new RecordAccessAudit();
        Company company = new Company();
        company.setId(companyId);
        audit.setCompany(company);
        if (userJson != null) {
            audit.setUserId(userJson.getString("id"));
            audit.setUserLoginName(userJson.getString("loginName"));
            audit.setUserName(userJson.getString("name"));
        }
        audit.setPatientId(StringUtils.isBlank(patientId) ? null : patientId.trim());
        audit.setPatientName(StringUtils.isBlank(patientName) ? null : patientName.trim());
        audit.setAction(action);
        audit.preInsert();
        dao.insert(audit);
        return audit;
    }

    /**
     * Liệt kê audit của tenant, mới nhất trước. patientId/action trống
     * nghĩa là không lọc theo trường đó. limit kẹp trong [1, 200],
     * mặc định 100 — nhật ký không trả toàn bộ bảng về frontend.
     */
    public List<RecordAccessAudit> listAudits(String companyId, String patientId,
                                              String action, Integer limit) {
        if (StringUtils.isBlank(companyId)) {
            return new ArrayList<>();
        }
        int safeLimit = limit == null ? 100 : limit;
        if (safeLimit < 1) {
            safeLimit = 1;
        }
        if (safeLimit > 200) {
            safeLimit = 200;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("companyId", companyId);
        params.put("DEL_FLAG_NORMAL", BaseEntity.DEL_FLAG_NORMAL);
        if (StringUtils.isNotBlank(patientId)) {
            params.put("patientId", patientId.trim());
        }
        if (StringUtils.isNotBlank(action)) {
            params.put("action", action.trim());
        }
        params.put("limit", safeLimit);
        return dao.findByTenant(params);
    }
}
