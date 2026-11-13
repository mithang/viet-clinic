package com.geeke.booking.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.booking.dao.BookingRequestDao;
import com.geeke.booking.entity.BookingRequest;
import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.persistence.BaseEntity;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
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
 * trước/sau dù JDBC đang chạy GMT+7 (sau H1).
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
     * Múi giờ mà cột DATE đi qua: JDBC (serverTimezone=GMT+7) và Jackson
     * (application.yml time-zone GMT+7) đều dùng GMT+7 = giờ phòng khám.
     * Một chuỗi ngày "yyyy-MM-dd" mà người dùng chọn được Jackson parse
     * thành nửa đêm GMT+7, tức đầu ngày phòng khám; driver đọc cột DATE
     * về ở nửa đêm GMT+7, nên hiểu instant đó theo GMT+7 sẽ khôi phục
     * đúng ngày đã viết. Bất biến: vùng hiểu ngày phải bằng vùng phiên
     * JDBC — trước H1 cả hai là GMT+8 và hiểu theo +07 sẽ lùi ngày đi 1
     * (đã gặp thực tế: chọn 2026-11-05 mà lưu thành 2026-11-04, khiến
     * confirm trượt lịch trực). Quy tắc này CHỈ áp cho cột DATE; cột
     * datetime (như subscribe_date) vẫn dùng đầu ngày theo giờ phòng
     * khám như G1.
     */
    private static final ZoneId DB_ZONE = ZoneId.of("GMT+7");
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

    /**
     * AA4: chan ghi xuyen tenant qua bay getter getCompany(). CrudService
     * chi verify company tren payload, ma getter BookingRequest tu tong hop
     * tenant session khi field null (va UPDATE con viet lai company_id
     * thanh tenant own) nen payload id dong la + khong company luon lot.
     * Check dong cu bang company_id tho; dong khong ton tai / session rong
     * giu hanh vi cu (mau Z3k NoticeSend).
     */
    private void checkRowOwnership(String id) {
        if (StringUtils.isBlank(id)) {
            return;
        }
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return;
        }
        String oldCompanyId = dao.getCompanyIdById(id);
        if (StringUtils.isNotBlank(oldCompanyId) && !tenantId.equals(oldCompanyId)) {
            throw new ServiceException("Yêu cầu đặt lịch không thuộc phòng khám hiện tại");
        }
    }

    /**
     * AA4: duong tao moi ep company = tenant session, ke ca khi payload
     * ghi tuong minh company la (save-insert khong qua verify vi id rong).
     */
    private void forceSessionCompany(BookingRequest entity) {
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return;
        }
        Company company = new Company();
        company.setId(tenantId);
        entity.setCompany(company);
    }

    /**
     * DS180: mau FollowupReminderService.checkExists — update voi id
     * khong ton tai truoc day roi qua super.save -> doUpdate 0 dong
     * roi van 200 silent no-op (checkRowOwnership cho qua ghost vi
     * dao.getCompanyIdById tra blank). Chi chap nhan update khi dong
     * da ton tai.
     */
    private void checkExists(String id) {
        if (StringUtils.isBlank(id)) {
            return;
        }
        if (dao.get(id) == null) {
            throw new ServiceException("Yêu cầu đặt lịch không tồn tại: " + id);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public BookingRequest save(BookingRequest entity) {
        if (StringUtils.isNotBlank(entity.getId())) {
            checkRowOwnership(entity.getId());
            checkExists(entity.getId());
        } else {
            forceSessionCompany(entity);
        }
        if (entity.getStatus() == null) {
            entity.setStatus("0");
        }
        // DS266: ngay muon kham khong duoc la ngay qua khu — luat cua
        // chinh submitIntake (G2) nhung 3 duong CRUD chung khong kiem
        // tra; null de @Valid/DB xu ly (DS79), chi chan ngay qua khu.
        checkWantedDateNotPast(entity);
        // DS149 C: chan do dai truoc moi ghi (mau DS148 A). Tat ca cac
        // truong chu deu KHONG co @Length: patientName/service ->
        // varchar(100), phone -> varchar(20), status -> varchar(1)
        // (probe C13/C14), rejectReason/remarks -> varchar(255).
        // Gioi han theo cot DB; null-safe. Guard dat sau mac dinh
        // status "0" o tren.
        checkBookingRequestLengths(entity);
        return super.save(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public int delete(BookingRequest entity) {
        // DS130 A: checkRowOwnership cho qua im lang id trong/ghost
        // (getCompanyIdById tra blank) nen ghost delete 200 rows 0 im
        // lang, blank 200, xoa lap 200 rows 1 (mapper soft-delete khong
        // loc del_flag) -- cung lop DS123/DS129. Chan truoc bang
        // dao.get (khong loc del_flag nen tra ca dong da xoa mem,
        // delFlag phan biet) theo mau CodeRuleService.delete (DS129 A);
        // giu checkRowOwnership sau do (dong live la tenant van 403).
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được yêu cầu đặt lịch cần xóa.");
        }
        BookingRequest stored = dao.get(entity.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Yêu cầu đặt lịch không tồn tại: " + entity.getId());
        }
        checkRowOwnership(entity.getId());
        return super.delete(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<BookingRequest> entitys) {
        for (BookingRequest entity : entitys) {
            forceSessionCompany(entity);
            checkWantedDateNotPast(entity);
            checkBookingRequestLengths(entity);
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<BookingRequest> entitys) {
        for (BookingRequest entity : entitys) {
            checkRowOwnership(entity.getId());
            checkWantedDateNotPast(entity);
            checkBookingRequestLengths(entity);
        }
        return super.bulkUpdate(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<BookingRequest> entitys) {
        for (BookingRequest entity : entitys) {
            checkRowOwnership(entity.getId());
        }
        return super.bulkDelete(entitys);
    }

    // DS266: ngay muon kham phai la hom nay tro di (cung luat nhu
    // submitIntake). Null-safe: null de @Valid (save) / NOT NULL cua DB
    // xu ly, khong thuoc vong nay. Chi chan ngay qua khu; bound +90
    // ngay cua intake la chinh sach chong spam form cong khai, KHONG
    // mirror sang duong le tan.
    private static void checkWantedDateNotPast(BookingRequest entity) {
        if (entity == null || entity.getWantedDate() == null) {
            return;
        }
        if (dateColumnDay(entity.getWantedDate()).isBefore(clinicToday())) {
            throw new ServiceException("Ngày muốn khám không được là ngày quá khứ.");
        }
    }

    // DS149 C: xem ghi chu o save().
    private static void checkBookingRequestLengths(BookingRequest entity) {
        if (entity == null) {
            return;
        }
        checkLength(entity.getPatientName(), 100, "Tên bệnh nhân Độ dài phải nằm trong khoảng 0 đến 100");
        checkLength(entity.getPhone(), 20, "Số điện thoại Độ dài phải nằm trong khoảng 0 đến 20");
        checkLength(entity.getService(), 100, "Dịch vụ Độ dài phải nằm trong khoảng 0 đến 100");
        checkLength(entity.getStatus(), 1, "Trạng thái Độ dài phải nằm trong khoảng 0 đến 1");
        checkLength(entity.getRejectReason(), 255, "Lý do từ chối Độ dài phải nằm trong khoảng 0 đến 255");
        checkLength(entity.getRemarks(), 255, "Ghi chú Độ dài phải nằm trong khoảng 0 đến 255");
    }

    private static void checkLength(String value, int max, String message) {
        if (value != null && value.length() > max) {
            throw new ServiceException(message);
        }
    }

    public static LocalDate clinicToday() {
        return LocalDate.now(CLINIC_ZONE);
    }

    /**
     * Ngày dương lịch của một giá trị cột DATE (xem DB_ZONE): instant
     * được hiểu theo GMT+7 (= giờ phòng khám, = vùng phiên JDBC sau H1)
     * để khôi phục đúng ngày đã viết.
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
     * DS259: chan limit/offset khong tran tren 2 funnel list con sot sau
     * DS203/DS204 (pendingPage/priorPage dua offset/limit tho vao Map
     * roi LIMIT #{limit} OFFSET #{offset} truc tiep, khong qua
     * PageRequest): limit > 10000 lay het bang mot trang, limit/offset
     * am lot toi MySQL moi bi DB tu choi -> 400 message chung (DS94).
     * limit trong [0, 10000] (0 giu hop le nhu DS203/DS204), offset >= 0;
     * sai -> IllegalArgumentException -> 400 qua GlobalExceptionHandler
     * (DS160). Dat o ca 2 phuong thuc list (pendingPage + priorPage).
     */
    private static void checkPageBounds(int offset, int limit) {
        if (limit < 0 || limit > PageRequest.MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Invalid limit: " + limit);
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Invalid offset: " + offset);
        }
    }

    /**
     * Hàng chờ pending của tenant đang đăng nhập, ngày muốn khám tăng dần.
     */
    public Page<BookingRequest> pendingPage(String companyId, int offset, int limit) {
        checkPageBounds(offset, limit);
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
        checkPageBounds(offset, limit);
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
        // DS266: dong legacy tao truoc fix co the mang ngay qua khu;
        // xac nhan no se tao lich hen qua khu nen chan truoc kiem tra
        // lich truc (controller map IllegalArgumentException -> 400).
        if (wanted.isBefore(clinicToday())) {
            throw new IllegalArgumentException("Ngày muốn khám không được là ngày quá khứ.");
        }
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
