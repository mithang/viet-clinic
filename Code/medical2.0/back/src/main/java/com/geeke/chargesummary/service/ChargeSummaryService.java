package com.geeke.chargesummary.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.chargesummary.dao.ChargeSummaryDao;
import com.geeke.common.persistence.BaseEntity;
import com.geeke.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tổng kết thu phí cuối ngày Service (G6 research loop).
 *
 * Ranh giới an toàn:
 * - Chỉ đọc toll_info + toll_audit_log, luôn giới hạn đúng tenant đang
 *   đăng nhập (company_id = tenant phiên). Không ghi DB, không tạo
 *   bảng mới, không đổi quyền vai trò gốc.
 * - Tổng tiền cộng ở service bằng BigDecimal (không cộng bằng float),
 *   null coi như 0. Hoàn phí trong totals KHÔNG trừ vào tổng thu:
 *   totals phản ánh đúng các phiếu trong rows (kể cả phiếu hoàn có
 *   returnType = 1); phần hoàn phí được trình bày riêng ở mục refunds
 *   kèm refundCount/refundTotal để đối chiếu, tránh cộng/trừ ngầm
 *   làm lệch đối chiếu với endpoint danh sách thu phí.
 *
 * Múi giờ (xem thêm ghi chú trong ChargeSummaryDao.xml):
 * create_date trong DB là giờ tường GMT+8 (JDBC serverTimezone=GMT+8,
 * Jackson time-zone GMT+8; kiểm chứng 2026-11-02: tạo lúc 02:58 +07
 * lưu thành 03:58). Một ngày phòng khám Asia/Ho_Chi_Minh
 * [00:00 +07, 24:00 +07) tương đương cửa sổ giờ lưu trữ
 * [01:00 +08 ngày đó, 01:00 +08 ngày hôm sau), nên service dùng đúng
 * cửa sổ đó. Giờ hiển thị createDate giữ nguyên giờ lưu trữ GMT+8,
 * khớp định dạng mà endpoint danh sách thu phí đang trả.
 */
@Service("chargeSummaryService")
@Transactional(readOnly = true)
@Slf4j
public class ChargeSummaryService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private ChargeSummaryDao chargeSummaryDao;

    public JSONObject daySummary(String companyId, String dateParam) {
        if (StringUtils.isBlank(companyId)) {
            throw new IllegalArgumentException("Chưa xác định được phòng khám.");
        }
        if (StringUtils.isBlank(dateParam)) {
            throw new IllegalArgumentException("Thiếu tham số ngày (date=YYYY-MM-DD).");
        }
        final LocalDate date;
        try {
            date = LocalDate.parse(dateParam.trim(), DATE_FMT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày phải có dạng YYYY-MM-DD.");
        }
        LocalDate next = date.plusDays(1);
        // Cửa sổ giờ lưu trữ GMT+8 tương đương ngày phòng khám +07.
        String startDate = date.format(DATE_FMT) + " 01:00:00";
        String endDate = next.format(DATE_FMT) + " 01:00:00";

        Map<String, Object> params = new HashMap<>();
        params.put("companyId", companyId);
        params.put("delFlag", BaseEntity.DEL_FLAG_NORMAL);
        params.put("startDate", startDate);
        params.put("endDate", endDate);

        List<Map<String, Object>> rows = chargeSummaryDao.findCharges(params);
        if (rows == null) {
            rows = new ArrayList<>();
        }
        List<Map<String, Object>> refunds = chargeSummaryDao.findRefunds(params);
        if (refunds == null) {
            refunds = new ArrayList<>();
        }

        BigDecimal totalReceivable = BigDecimal.ZERO;
        BigDecimal totalReceived = BigDecimal.ZERO;
        BigDecimal totalDiscounted = BigDecimal.ZERO;
        Map<String, JSONObject> byPaymentMethod = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            BigDecimal receivable = toDecimal(row.get("amountReceivable"));
            BigDecimal received = toDecimal(row.get("amountReceived"));
            BigDecimal discounted = toDecimal(row.get("amountDiscounted"));
            totalReceivable = totalReceivable.add(receivable);
            totalReceived = totalReceived.add(received);
            totalDiscounted = totalDiscounted.add(discounted);
            String methodName = str(row.get("paymentTypeName"));
            if (methodName.isEmpty()) {
                methodName = str(row.get("paymentType"));
            }
            if (methodName.isEmpty()) {
                methodName = "Không rõ";
            }
            JSONObject bucket = byPaymentMethod.get(methodName);
            if (bucket == null) {
                bucket = new JSONObject();
                bucket.put("paymentMethod", methodName);
                bucket.put("count", 0);
                bucket.put("totalReceivable", BigDecimal.ZERO);
                bucket.put("totalReceived", BigDecimal.ZERO);
                bucket.put("totalDiscounted", BigDecimal.ZERO);
                byPaymentMethod.put(methodName, bucket);
            }
            bucket.put("count", bucket.getIntValue("count") + 1);
            bucket.put("totalReceivable", toDecimal(bucket.get("totalReceivable")).add(receivable));
            bucket.put("totalReceived", toDecimal(bucket.get("totalReceived")).add(received));
            bucket.put("totalDiscounted", toDecimal(bucket.get("totalDiscounted")).add(discounted));
        }

        BigDecimal refundTotal = BigDecimal.ZERO;
        for (Map<String, Object> refund : refunds) {
            refundTotal = refundTotal.add(toDecimal(refund.get("amountReceived")));
        }

        JSONObject totals = new JSONObject();
        totals.put("chargeCount", rows.size());
        totals.put("totalReceivable", totalReceivable);
        totals.put("totalReceived", totalReceived);
        totals.put("totalDiscounted", totalDiscounted);
        totals.put("byPaymentMethod", new ArrayList<>(byPaymentMethod.values()));
        totals.put("refundCount", refunds.size());
        totals.put("refundTotal", refundTotal);

        JSONObject data = new JSONObject();
        data.put("date", date.format(DATE_FMT));
        data.put("timezone", "Asia/Ho_Chi_Minh");
        data.put("windowStart", startDate + " (giờ lưu trữ GMT+8)");
        data.put("windowEnd", endDate + " (giờ lưu trữ GMT+8)");
        data.put("rows", rows);
        data.put("totals", totals);
        data.put("refunds", refunds);
        return data;
    }

    private static BigDecimal toDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }
        try {
            return new BigDecimal(String.valueOf(value).trim());
        } catch (Exception e) {
            log.debug("Không cộng được số tiền: {}", value);
            return BigDecimal.ZERO;
        }
    }

    private static String str(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
