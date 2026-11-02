package com.geeke.diagnosis.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.persistence.BaseEntity;
import com.geeke.diagnosis.dao.DiagnosisDao;
import com.geeke.diagnosis.entity.DiagnosisCatalog;
import com.geeke.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chẩn đoán ICD-10 Service (H3 research loop).
 *
 * Ranh giới an toàn:
 * - Catalog chỉ đọc, dùng chung mọi tenant (chuẩn chung, không phải
 *   dữ liệu phòng khám); ghim chỉ đọc/ghi theo đúng doctor_id của
 *   bác sĩ đang đăng nhập — không bao giờ liệt kê hay sửa ghim của
 *   bác sĩ khác.
 * - Tìm kiếm phía server, phân trang: pageSize kẹp trong [1, 50],
 *   mặc định 20. Client không tải toàn bộ catalog.
 * - Ký tự LIKE (%, _) trong từ khóa được escape trước khi đưa vào
 *   pattern, kèm ESCAPE '\' ở SQL: gõ "%" không biến thành wildcard
 *   khớp mọi dòng.
 * - Không có endpoint ghi catalog: danh mục chỉ lớn lên bằng
 *   migration có kiểm chứng (xem migration v11 — NEED MORE INFO).
 * - Ghim/bỏ ghim đều idempotent: ghim lại không lỗi, bỏ ghim khi
 *   chưa ghim không lỗi.
 */
@Service("diagnosisService")
@Transactional(readOnly = true)
@Slf4j
public class DiagnosisService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    @Autowired
    private DiagnosisDao diagnosisDao;

    /**
     * Tìm kiếm phân trang. q trống/null nghĩa là trang đầu theo mã
     * (không phải lỗi). Trả {records, total, page, pageSize}.
     */
    public JSONObject search(String q, int page, int pageSize) {
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("delFlag", BaseEntity.DEL_FLAG_NORMAL);
        params.put("pattern", toLikePattern(q));
        params.put("offset", (page - 1) * pageSize);
        params.put("limit", pageSize);
        List<DiagnosisCatalog> records = diagnosisDao.search(params);
        int total = diagnosisDao.countSearch(params);
        JSONObject data = new JSONObject();
        data.put("records", records);
        data.put("total", total);
        data.put("page", page);
        data.put("pageSize", pageSize);
        return data;
    }

    /**
     * Danh sách ghim của một bác sĩ. doctorId trống thì trả rỗng
     * (không liệt kê ghim của người khác, không lỗi).
     */
    public List<DiagnosisCatalog> listPinned(String doctorId) {
        if (StringUtils.isBlank(doctorId)) {
            return new java.util.ArrayList<>();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("delFlag", BaseEntity.DEL_FLAG_NORMAL);
        params.put("doctorId", doctorId.trim());
        return diagnosisDao.listPinned(params);
    }

    /**
     * Ghim một chẩn đoán cho bác sĩ. Chẩn đoán không tồn tại hoặc đã
     * xóa mềm thì ném IllegalArgumentException (400) — không ghim
     * vào mã ảo.
     */
    @Transactional(readOnly = false)
    public DiagnosisCatalog pin(String doctorId, Long diagnosisId) {
        if (StringUtils.isBlank(doctorId)) {
            throw new IllegalArgumentException("Chưa xác định được bác sĩ.");
        }
        if (diagnosisId == null) {
            throw new IllegalArgumentException("Thiếu id chẩn đoán cần ghim.");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("delFlag", BaseEntity.DEL_FLAG_NORMAL);
        params.put("diagnosisId", diagnosisId);
        DiagnosisCatalog catalog = diagnosisDao.getById(params);
        if (catalog == null) {
            throw new IllegalArgumentException("Chẩn đoán cần ghim không tồn tại.");
        }
        Map<String, Object> pinParams = new HashMap<>();
        pinParams.put("doctorId", doctorId.trim());
        pinParams.put("diagnosisId", diagnosisId);
        diagnosisDao.insertPin(pinParams);
        return catalog;
    }

    /**
     * Bỏ ghim. Chưa ghim thì không làm gì, không lỗi.
     */
    @Transactional(readOnly = false)
    public void unpin(String doctorId, Long diagnosisId) {
        if (StringUtils.isBlank(doctorId) || diagnosisId == null) {
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("doctorId", doctorId.trim());
        params.put("diagnosisId", diagnosisId);
        diagnosisDao.deletePin(params);
    }

    /**
     * Escape ký tự đặc biệt của LIKE rồi bọc % hai đầu. Chuỗi rỗng
     * thành "%%" (khớp mọi dòng, vẫn phân trang) — picker dùng nhánh
     * này để hiện trang đầu khi chưa gõ gì, thay vì tải toàn bộ.
     */
    static String toLikePattern(String q) {
        if (q == null) {
            return "%%";
        }
        String escaped = q.trim()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
