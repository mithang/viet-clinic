package com.geeke.booking.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.booking.dao.BookingRequestDao;
import com.geeke.booking.entity.BookingRequest;
import com.geeke.common.data.Page;
import com.geeke.common.persistence.BaseEntity;
import com.geeke.common.service.CrudService;
import com.geeke.org.entity.Company;
import com.geeke.outpatient.entity.Registration;
import com.geeke.outpatient.service.RegistrationService;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Yêu cầu đặt lịch Service (G2 research loop).
 *
 * Múi giờ phòng khám cố định Asia/Ho_Chi_Minh cho mọi phép tính "hôm
 * nay / ngày quá khứ / ngày muốn khám" và cho mọi giá trị ngày ghi DB
 * (bài học G1: không dùng java.sql.Date, không để JDBC/JVM lệch ngày).
 * wanted_date là cột DATE: service luôn chuẩn hóa về đầu ngày theo giờ
 * phòng khám trước khi ghi, nên ngày lưu không bao giờ lệch sang hôm
 * trước/sau dù JDBC đang chạy GMT+8.
 *
 * Ranh giới an toàn: intake CHỈ đọc/ghi bảng booking_request (chống
 * trùng cũng chỉ tra bảng này). Intake không đọc/ghi patient,
 * registration hay bất kỳ bảng clinical nào; request không có trường
 * chẩn đoán. Xác nhận mới tạo registration (lịch hẹn hành chính, không
 * phải hồ sơ khám) qua RegistrationService, trong cùng transaction với
 * claim — claim trước, insert sau (bài học G1).
 */
@Service("bookingRequestService")
@Transactional(readOnly = true)
@Slf4j
public class BookingRequestService extends CrudService<BookingRequestDao, BookingRequest> {

    private static final ZoneId CLINIC_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    /**
     * Múi giờ mà cột DATE đi qua: JDBC (serverTimezone=GMT+8) và Jackson
     * (application.yml time-zone GMT+8) đều dùng GMT+8. Một chuỗi ngày
     * "yyyy-MM-dd" mà người dùng chọn được Jackson parse thành nửa đêm
     * GMT+8, tức 23:00 hôm trước theo giờ phòng khám (+07) — hiểu instant
     * đó theo +07 sẽ lùi ngày đi 1 (đã gặp thực tế: chọn 2026-11-05 mà
     * lưu thành 2026-11-04, khiến confirm trượt lịch trực). Vì vậy ngày
     * của cột DATE (wanted_date, schedulingtime) luôn được khôi phục
     * bằng GMT+8 — ngày viết ra bằng ngày đọc về. Quy tắc này CHỈ áp
     * cho cột DATE; cột datetime (như subscribe_date) vẫn dùng đầu ngày
     * theo giờ phòng khám như G1.
     */
    private static final ZoneId DB_ZONE = ZoneId.of("GMT+8");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Chống spam tối thiểu: tối đa 5 yêu cầu/giờ cho một SĐT (rào chắn
     * chính), 100/giờ cho một IP. Mức IP phải cao vì phòng khám có thể
     * ngồi sau một NAT (mọi bệnh nhân + lễ tân chung ít IP) và lễ tân
     * nhập hộ nhiều yêu cầu từ cùng một máy — mức 20/giờ đã cạn quota
     * ngay trong kiểm chứng khi mọi traffic đều từ 127.0.0.1.
     */
    private static final int MAX_PER_PHONE_PER_HOUR = 5;
    private static final int MAX_PER_IP_PER_HOUR = 100;
    private static final long ONE_HOUR_MS = 3600_000L;
    private static final Map<String, Deque<Long>> RATE_BUCKETS = new ConcurrentHashMap<>();

    @Autowired
    private RegistrationService registrationService;

    @Override
    @Transactional(readOnly = false)
    public BookingRequest save(BookingRequest entity) {
        if (entity.getStatus() == null) {
            entity.setStatus("0");
        }
        return super.save(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public int delete(BookingRequest entity) {
        return super.delete(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<BookingRequest> entitys) {
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<BookingRequest> entitys) {
        return super.bulkUpdate(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<BookingRequest> entitys) {
        return super.bulkDelete(entitys);
    }

    public static LocalDate clinicToday() {
        return LocalDate.now(CLINIC_ZONE);
    }

    /**
     * Ngày dương lịch của một giá trị cột DATE (xem DB_ZONE): instant
     * được hiểu theo GMT+8 để khôi phục đúng ngày đã viết, không theo
     * giờ phòng khám (+07) — hiểu theo +07 sẽ lùi ngày đi 1.
     */
    public static LocalDate dateColumnDay(Date date) {
        return date.toInstant().atZone(DB_ZONE).toLocalDate();
    }

    /** Đầu ngày theo giờ phòng khám, dùng làm giá trị ghi cho cột DATE/datetime ngày hẹn. */
    public static Date clinicStartOfDay(LocalDate date) {
        return Date.from(date.atStartOfDay(CLINIC_ZONE).toInstant());
    }

    /**
     * Chuẩn hóa SĐT Việt Nam: bỏ khoảng trắng, dấu chấm/gạch/ngoặc;
     * +84 hoặc 84 đầu số -> 0. Kết quả phải là 9–11 chữ số bắt đầu bằng 0.
     */
    public static String normalizePhone(String raw) {
        if (raw == null) {
            return null;
        }
        String digits = raw.replaceAll("[^0-9+]", "");
        if (digits.startsWith("+84")) {
            digits = "0" + digits.substring(3);
        } else if (digits.startsWith("84") && digits.length() >= 11) {
            digits = "0" + digits.substring(2);
        }
        if (!digits.matches("0[0-9]{8,10}")) {
            return null;
        }
        return digits;
    }

    private static boolean checkRate(String key, int maxPerHour) {
        long now = System.currentTimeMillis();
        Deque<Long> bucket = RATE_BUCKETS.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (bucket) {
            while (!bucket.isEmpty() && now - bucket.peekFirst() > ONE_HOUR_MS) {
                bucket.pollFirst();
            }
            if (bucket.size() >= maxPerHour) {
                return false;
            }
            bucket.addLast(now);
            return true;
        }
    }

    /**
     * Intake yêu cầu đặt lịch. companyId hiệu lực = tenant của phiên đăng
     * nhập (nếu lễ tân nhập hộ) nếu không thì companyId trong body (form
     * công khai của phòng khám tự gắn id phòng khám của mình); thiếu cả
     * hai thì từ chối — không đoán phòng khám mặc định.
     * Trùng pending (cùng tenant, SĐT, ngày) thì trả lại bản ghi cũ,
     * không tạo trùng (cờ duplicate = true, không ghi thêm dòng nào).
     */
    @Transactional(readOnly = false)
    public BookingRequest submitIntake(BookingRequest entity, String bodyCompanyId, String remoteAddr) {
        if (entity == null || StringUtils.isBlank(entity.getPatientName())
                || entity.getPatientName().trim().length() > 100) {
            throw new IllegalArgumentException("Tên người đặt lịch không được để trống (tối đa 100 ký tự).");
        }
        String phone = normalizePhone(entity.getPhone());
        if (phone == null) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ (cần 9–11 chữ số, bắt đầu bằng 0).");
        }
        if (entity.getWantedDate() == null) {
            throw new IllegalArgumentException("Ngày muốn khám không được để trống.");
        }
        LocalDate wanted = dateColumnDay(entity.getWantedDate());
        LocalDate today = clinicToday();
        if (wanted.isBefore(today)) {
            throw new IllegalArgumentException("Ngày muốn khám không được là ngày quá khứ.");
        }
        if (wanted.isAfter(today.plusDays(90))) {
            throw new IllegalArgumentException("Ngày muốn khám không được quá 90 ngày kể từ hôm nay.");
        }
        if (StringUtils.isNotBlank(entity.getService()) && entity.getService().trim().length() > 100) {
            throw new IllegalArgumentException("Dịch vụ tối đa 100 ký tự.");
        }
        String sessionTenant = null;
        try {
            sessionTenant = SessionUtils.getLoginTenantId();
        } catch (Exception e) {
            log.debug("booking intake: no session tenant", e);
        }
        String companyId = StringUtils.isNotBlank(sessionTenant) && !"null".equals(sessionTenant)
                ? sessionTenant : bodyCompanyId;
        if (StringUtils.isBlank(companyId)) {
            throw new IllegalArgumentException("Chưa xác định được phòng khám nhận yêu cầu.");
        }
        if (!checkRate("phone:" + phone, MAX_PER_PHONE_PER_HOUR)) {
            throw new IllegalArgumentException("Số điện thoại này đã gửi quá nhiều yêu cầu trong 1 giờ, vui lòng thử lại sau.");
        }
        if (StringUtils.isNotBlank(remoteAddr) && !checkRate("ip:" + remoteAddr, MAX_PER_IP_PER_HOUR)) {
            throw new IllegalArgumentException("Đã gửi quá nhiều yêu cầu trong 1 giờ, vui lòng thử lại sau.");
        }
        Map<String, Object> dupParams = new HashMap<>();
        dupParams.put("companyId", companyId);
        dupParams.put("phone", phone);
        dupParams.put("wantedDate", wanted.format(DATE_FMT));
        dupParams.put("DEL_FLAG_NORMAL", BaseEntity.DEL_FLAG_NORMAL);
        BookingRequest existing = dao.findPendingDuplicate(dupParams);
        if (existing != null) {
            existing.setDuplicate(true);
            return existing;
        }
        Company company = new Company();
        company.setId(companyId);
        entity.setCompany(company);
        entity.setPatientName(entity.getPatientName().trim());
        entity.setPhone(phone);
        entity.setWantedDate(clinicStartOfDay(wanted));
        if (StringUtils.isNotBlank(entity.getService())) {
            entity.setService(entity.getService().trim());
        }
        entity.setStatus("0");
        entity.setRejectReason(null);
        entity.setRegistrationId(null);
        // Intake có thể ẩn danh (không phiên người dùng): CrudService.save
        // sẽ ghi sys_action với create_by NULL và vi phạm NOT NULL của
        // bảng đó. Vì vậy intake ghi trực tiếp qua DAO, bỏ qua action
        // log — bản ghi booking_request đã mang create_by/create_date
        // làm dấu vết. Nhãn người tạo cố định cho intake ẩn danh;
        // preInsert sẽ ghi đè bằng nhãn lễ tân khi có phiên đăng nhập.
        entity.setCreateBy("Đặt lịch online");
        entity.setUpdateBy("Đặt lịch online");
        entity.preInsert();
        dao.insert(entity);
        entity.setDuplicate(false);
        return entity;
    }

    /**
     * Hàng chờ pending của tenant đang đăng nhập, ngày muốn khám tăng dần.
     */
    public Page<BookingRequest> pendingPage(String companyId, int offset, int limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("companyId", companyId);
        params.put("offset", offset);
        params.put("limit", limit);
        // Bài học G1: Map thuần không resolve #{DEL_FLAG_NORMAL} -> đưa tường minh.
        params.put("DEL_FLAG_NORMAL", BaseEntity.DEL_FLAG_NORMAL);
        int total = dao.countPending(params);
        List<BookingRequest> rows = total > 0 ? dao.listPending(params) : Collections.emptyList();
        return new Page<>(total, rows);
    }

    /**
     * Các yêu cầu trước đó của cùng một SĐT (mọi trạng thái) — lễ tân
     * xem khi xử lý trùng SĐT. Chỉ tra bảng booking_request.
     */
    public Page<BookingRequest> priorPage(String companyId, String phone, int offset, int limit) {
        String normalized = normalizePhone(phone);
        if (normalized == null) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ.");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("companyId", companyId);
        params.put("phone", normalized);
        params.put("offset", offset);
        params.put("limit", limit);
        params.put("DEL_FLAG_NORMAL", BaseEntity.DEL_FLAG_NORMAL);
        int total = dao.countPriorByPhone(params);
        List<BookingRequest> rows = total > 0 ? dao.listPriorByPhone(params) : Collections.emptyList();
        return new Page<>(total, rows);
    }

    /**
     * Xác nhận 1 thao tác thành lịch hẹn thật. Thứ tự trong transaction:
     * (1) tải + kiểm tra pending/tenant, (2) kiểm tra lịch trực ngày đó
     * (chỉ đọc, không ghi), (3) claim nguyên tử (chỉ pending + đúng
     * tenant mới giành được), (4) tạo registration qua đường đăng ký
     * hiện có, (5) ghi registration_id vào yêu cầu. Claim luôn đứng
     * trước mọi INSERT — không tạo registration khi chưa giành được claim.
     * Bất kỳ bước nào thất bại thì transaction rollback, yêu cầu vẫn pending.
     * Trả về registration_id đã tạo.
     */
    @Transactional(readOnly = false)
    public String confirm(String id, String companyId) {
        BookingRequest request = dao.get(id);
        if (request == null || request.getCompany() == null
                || !companyId.equals(request.getCompany().getId())
                || !"0".equals(request.getStatus())) {
            throw new IllegalArgumentException("Yêu cầu không tồn tại, đã được xử lý trước đó, hoặc không thuộc phòng khám này.");
        }
        LocalDate wanted = dateColumnDay(request.getWantedDate());
        int roster = dao.countRosterForDate(companyId, wanted.format(DATE_FMT));
        if (roster <= 0) {
            throw new IllegalArgumentException("Ngày " + wanted.format(DATE_FMT)
                    + " chưa có lịch trực bác sĩ nên chưa thể xác nhận lịch hẹn.");
        }
        String updateBy = currentUserLabel();
        int claimed = dao.claimConfirm(id, companyId, updateBy, new Date());
        if (claimed <= 0) {
            throw new IllegalArgumentException("Yêu cầu đã được xử lý trước đó.");
        }
        Registration registration = new Registration();
        Company company = new Company();
        company.setId(companyId);
        registration.setCompany(company);
        registration.setSubscribeDate(clinicStartOfDay(wanted));
        registration.setSubscribeProject(StringUtils.isNotBlank(request.getService())
                ? request.getService() : "Đặt lịch online");
        registration.setRemarks("Yêu cầu đặt lịch online: " + request.getPatientName()
                + " — " + request.getPhone());
        Registration saved = registrationService.save(registration);
        dao.linkRegistration(id, companyId, saved.getId(), updateBy, new Date());
        log.info("booking confirm: request={} registration={} company={}", id, saved.getId(), companyId);
        return saved.getId();
    }

    /**
     * Từ chối có lưu lý do; bản ghi rời hàng chờ nhưng không bị xóa.
     * Trả về số dòng cập nhật (0 = đã xử lý trước đó hoặc sai tenant).
     */
    @Transactional(readOnly = false)
    public int reject(String id, String companyId, String reason) {
        if (StringUtils.isBlank(reason) || reason.trim().length() > 255) {
            throw new IllegalArgumentException("Lý do từ chối không được để trống (tối đa 255 ký tự).");
        }
        return dao.reject(id, companyId, reason.trim(), currentUserLabel(), new Date());
    }

    private String currentUserLabel() {
        try {
            JSONObject user = SessionUtils.getUserJson();
            if (user != null && StringUtils.isNotBlank(user.getString("id"))) {
                return user.getString("name") + "(" + user.getString("loginName") + ")";
            }
        } catch (Exception e) {
            log.debug("booking: no session user", e);
        }
        return "system";
    }
}
