package com.geeke.daydashboard.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.chargesummary.service.ChargeSummaryService;
import com.geeke.common.data.Page;
import com.geeke.common.data.Parameter;
import com.geeke.common.persistence.BaseEntity;
import com.geeke.daydashboard.dao.DayDashboardDao;
import com.geeke.org.service.CompanyService;
import com.geeke.stock.service.DrugService;
import com.geeke.stock.service.StuffService;
import com.geeke.stock.service.SupplierStockService;
import com.geeke.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Tổng quan ngày cho chủ phòng khám Service (H5 research loop).
 *
 * Ranh giới an toàn:
 * - Chỉ đọc, luôn giới hạn đúng tenant đang đăng nhập. Không ghi
 *   DB, không tạo bảng mới, không đổi quyền vai trò gốc.
 * - Thu phí / hoàn phí / doanh thu theo phương thức: tái sử dụng
 *   NGUYÊN VẸN ChargeSummaryService.daySummary (G6) — cùng một
 *   cửa sổ ngày, cùng một truy vấn — nên tổng dashboard bằng
 *   đúng tổng trang tổng kết thu phí cuối ngày, không cộng lại
 *   bằng một truy vấn thứ hai có thể lệch.
 * - Cảnh báo tồn kho: tái sử dụng nguyên vẹn các service cảnh
 *   báo sẵn có (DrugService, StuffService,
 *   SupplierStockService) với limit 1, chỉ lấy Page.getTotal()
 *   — tổng đó bằng đúng total của trang cảnh báo tương ứng.
 *   Ngoại lệ duy nhất: đếm sắp hết hạn vật tư, vì truy vấn gốc
 *   (SupplierStockDao.countStuffWarning) không có điều kiện
 *   company nào; dashboard dùng đếm có giới hạn tenant riêng
 *   (DayDashboardDao.countStuffIndateWarningScoped) với cùng
 *   định nghĩa DATEDIFF(end_date, NOW()) &lt;= stuff.indate.
 *
 * Múi giờ — cùng hiệu chuẩn G6, không tự suy đoán lại (H1 2026-11-02):
 * create_date trong DB là giờ tường GMT+7 (JDBC
 * serverTimezone=GMT+7, Jackson time-zone GMT+7). Trước H1 cả hai
 * là GMT+8 nên cửa sổ ngày là [01:00 +08 ngày đó, 01:00 +08 ngày
 * hôm sau); sau H1, phiếu/lượt mới lưu đúng giờ tường phòng khám
 * nên một ngày phòng khám Asia/Ho_Chi_Minh [00:00 +07, 24:00 +07)
 * đúng bằng cửa sổ [00:00 ngày đó, 00:00 ngày hôm sau), và service
 * dùng đúng cửa sổ đó cho cả lượt khám (registration.create_date)
 * lẫn thu phí (toll_info.create_date qua G6). Caveat lịch sử như G6:
 * dòng tạo trước H1 ở giờ 23:00–24:00 +07 sẽ rơi sang ngày hôm sau
 * dưới cửa sổ mới; DB test đã kiểm chứng không còn dòng nào ở giờ
 * 00:00–01:00 nên không dòng nào đổi ngày khi đổi cửa sổ. Lưu ý: cửa sổ này áp cho các cột create_date; các
 * đếm tồn kho / sắp hết hạn KHÔNG theo ngày — chúng là trạng
 * thái hiện tại (tồn kho lúc truy vấn, hạn dùng so với NOW()),
 * không phải số liệu của riêng ngày được chọn — và response
 * ghi rõ điều đó trong definitions để không ai đọc nhầm thành
 * "số cảnh báo phát sinh trong ngày".
 */
@Service("dayDashboardService")
@Transactional(readOnly = true)
@Slf4j
public class DayDashboardService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private DayDashboardDao dayDashboardDao;

    @Autowired
    private ChargeSummaryService chargeSummaryService;

    @Autowired
    private DrugService drugService;

    @Autowired
    private StuffService stuffService;

    @Autowired
    private SupplierStockService supplierStockService;

    @Autowired
    private CompanyService companyService;

    public JSONObject dayDashboard(String companyId, String dateParam) {
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
        // Cửa sổ giờ lưu trữ GMT+7 = ngày phòng khám +07 (sau H1, hiệu chuẩn G6).
        String startDate = date.format(DATE_FMT) + " 00:00:00";
        String endDate = next.format(DATE_FMT) + " 00:00:00";

        Map<String, Object> params = new HashMap<>();
        params.put("companyId", companyId);
        params.put("delFlag", BaseEntity.DEL_FLAG_NORMAL);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        try {
            params.put("institution", companyService.getInstitution(companyId));
        } catch (Exception e) {
            log.debug("Không lấy được institution của tenant: {}", e.getMessage());
            params.put("institution", null);
        }

        int visitCount = dayDashboardDao.countVisits(params);
        int pendingChargeCount = dayDashboardDao.countPendingCharges(params);

        // Thu phí: tái sử dụng G6 nguyên vẹn (cùng cửa sổ, cùng truy vấn).
        JSONObject chargeDay = chargeSummaryService.daySummary(companyId, dateParam);
        JSONObject chargeTotals = chargeDay.getJSONObject("totals");

        // Tồn kho / sắp hết hạn: trạng thái hiện tại, không theo ngày chọn.
        Long lowStockDrug = safeTotal(() -> drugService
                .getDrugInventoryWarning(new ArrayList<Parameter>(), 0, 1, null));
        Long lowStockStuff = safeTotal(() -> stuffService
                .getStuffInventoryWarning(new ArrayList<Parameter>(), 0, 1, null));
        Long nearExpiryDrug = safeTotal(() -> supplierStockService
                .getDrugIndateWarning(new ArrayList<Parameter>(), 0, 1, null));
        Long nearExpiryStuff = null;
        try {
            nearExpiryStuff = (long) dayDashboardDao.countStuffIndateWarningScoped(params);
        } catch (Exception e) {
            log.debug("Không đếm được sắp hết hạn vật tư: {}", e.getMessage());
        }

        JSONObject visits = new JSONObject();
        visits.put("visitCount", visitCount);
        visits.put("pendingChargeCount", pendingChargeCount);

        JSONObject charges = new JSONObject();
        charges.put("chargeCount", chargeTotals.get("chargeCount"));
        charges.put("totalReceivable", chargeTotals.get("totalReceivable"));
        charges.put("totalReceived", chargeTotals.get("totalReceived"));
        charges.put("totalDiscounted", chargeTotals.get("totalDiscounted"));
        charges.put("byPaymentMethod", chargeTotals.get("byPaymentMethod"));
        charges.put("refundCount", chargeTotals.get("refundCount"));
        charges.put("refundTotal", chargeTotals.get("refundTotal"));

        JSONObject stock = new JSONObject();
        stock.put("lowStockDrugCount", lowStockDrug);
        stock.put("lowStockStuffCount", lowStockStuff);
        stock.put("nearExpiryDrugCount", nearExpiryDrug);
        stock.put("nearExpiryStuffCount", nearExpiryStuff);
        stock.put("lowStockTotal", sumOrNull(lowStockDrug, lowStockStuff));
        stock.put("nearExpiryTotal", sumOrNull(nearExpiryDrug, nearExpiryStuff));

        JSONObject definitions = new JSONObject();
        definitions.put("visitCount", "Số lượt đăng ký khám tạo trong ngày phòng khám "
                + "(create_date trong cửa sổ giờ lưu trữ, del_flag = '0', "
                + "không phải lượt đặt trước, loại trừ trạng thái đã hủy).");
        definitions.put("pendingChargeCount", "Trong các lượt trên, số lượt có "
                + "charge_status '0' (chờ thu phí) hoặc '1' (thu một phần).");
        definitions.put("charges", "Tái sử dụng nguyên vẹn tổng kết G6 cùng ngày: "
                + "chargeCount/tổng tiền/byPaymentMethod từ toll_info,"
                + "refundCount/refundTotal từ toll_audit_log action = 'REFUND'. "
                + "Hoàn phí trình bày riêng, KHÔNG trừ ngầm vào tổng thu.");
        definitions.put("stock", "Trạng thái HIỆN TẠI lúc truy vấn, không phải số "
                + "phát sinh trong ngày chọn: tồn kho thấp = đúng total trang "
                + "cảnh báo tồn kho (thuốc: tồn kho <= inventory_floor * "
                + "preparation; vật tư: tồn kho <= inventory_floor); sắp hết "
                + "hạn = DATEDIFF(end_date, NOW()) <= indate (thuốc/vật tư). "
                + "Đếm sắp hết hạn vật tư có giới hạn tenant; truy vấn cảnh "
                + "báo gốc của vật tư không có điều kiện company nên tổng "
                + "dashboard có thể khác total trang đó trên dữ liệu đa tenant. "
                + "Giá trị null nghĩa là nguồn đó lỗi khi truy vấn, KHÔNG phải 0.");

        JSONObject data = new JSONObject();
        data.put("date", date.format(DATE_FMT));
        data.put("timezone", "Asia/Ho_Chi_Minh");
        data.put("windowStart", startDate + " (giờ lưu trữ GMT+7)");
        data.put("windowEnd", endDate + " (giờ lưu trữ GMT+7)");
        data.put("visits", visits);
        data.put("charges", charges);
        data.put("stock", stock);
        data.put("definitions", definitions);
        return data;
    }

    private static Long safeTotal(TotalSupplier supplier) {
        try {
            Page<?> page = supplier.get();
            return page == null ? null : page.getTotal();
        } catch (Exception e) {
            log.debug("Không đếm được cảnh báo tồn kho: {}", e.getMessage());
            return null;
        }
    }

    private static Long sumOrNull(Long a, Long b) {
        if (a == null || b == null) {
            return null;
        }
        return a + b;
    }

    private interface TotalSupplier {
        Page<?> get();
    }
}
