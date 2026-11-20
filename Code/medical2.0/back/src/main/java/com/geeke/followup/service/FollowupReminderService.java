package com.geeke.followup.service;

import com.geeke.common.data.Page;
import com.geeke.common.persistence.BaseEntity;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.followup.dao.FollowupReminderDao;
import com.geeke.followup.entity.FollowupReminder;
import com.geeke.noticesend.entity.NoticeSend;
import com.geeke.noticesend.service.NoticeSendService;
import com.geeke.org.entity.Company;
import com.geeke.sys.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Nhắc lịch tái khám Service (G1 research loop).
 *
 * Múi giờ phòng khám cố định Asia/Ho_Chi_Minh cho mọi phép tính "hôm
 * nay / quá hạn bao nhiêu ngày", không phụ thuộc múi giờ của JVM hay
 * MySQL (sau H1: backend lưu giờ tường GMT+7 — JDBC serverTimezone
 * GMT+7 + Jackson time-zone GMT+7; cột next_date DATETIME lưu đầu ngày
 * phòng khám, xem ghi chú H1 trong ChargeSummaryService).
 */
@Service("followupReminderService")
@Transactional(readOnly = true)
@Slf4j
public class FollowupReminderService extends CrudService<FollowupReminderDao, FollowupReminder> {

    private static final ZoneId CLINIC_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Autowired
    private NoticeSendService noticeSendService;

    /**
     * AA4: chan ghi xuyen tenant qua bay getter getCompany(), mau Z3k.
     * File nay da import com.geeke.sys.utils.SessionUtils (chi co
     * getUserJson) nen dung fully-qualified com.geeke.utils.SessionUtils
     * cho getLoginTenantId. Dong khong ton tai / session rong giu hanh vi cu.
     */
    private void checkRowOwnership(String id) {
        if (StringUtils.isBlank(id)) {
            return;
        }
        String tenantId = com.geeke.utils.SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return;
        }
        String oldCompanyId = dao.getCompanyIdById(id);
        if (StringUtils.isNotBlank(oldCompanyId) && !tenantId.equals(oldCompanyId)) {
            throw new ServiceException("Nhắc lịch tái khám không thuộc phòng khám hiện tại");
        }
    }

    /** AA4: duong tao moi ep company = tenant session (mau Z3k bulkInsert). */
    private void forceSessionCompany(FollowupReminder entity) {
        String tenantId = com.geeke.utils.SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return;
        }
        Company company = new Company();
        company.setId(tenantId);
        entity.setCompany(company);
    }

    /** DS103: fail-closed 400 khi update id khong ton tai (ho DS94/DS99).
     * checkRowOwnership pass khi dong khong ton tai (getCompanyIdById tra
     * null), CrudService.save sau do doUpdate cham 0 dong ma van 200 +
     * ghi audit ACTION_UPDATED ma; bulkUpdate CASE cung cham 0 dong.
     * Kiem tra ton tai qua dao.get (WHERE a.id, LEFT JOIN company chi
     * them cot -- khong tao/an dong). */
    private void checkExists(String id) {
        if (StringUtils.isBlank(id)) {
            return;
        }
        if (dao.get(id) == null) {
            throw new ServiceException("Nhắc lịch tái khám không tồn tại: " + id);
        }
    }

    /** DS319: status chi co 2 gia tri hop le '0' (cho xu ly) / '1' (da
     * xac nhan); '1' chi duoc ghi boi endpoint acknowledge rieng
     * (dao.acknowledge WHERE status='0'), khong bao gio qua save/
     * bulk*. Tao moi chi duoc '0' (null -> '0'); gia tri khac -> 400.
     * Gia tri rac 1 ky tu ('2', 'x') lot qua check do dai DS149
     * nhung roi khoi listDue (chi lay '0') va acknowledge khong sua
     * duoc -- ket vinh vien. */
    private void checkStatusNotForgedOnCreate(FollowupReminder entity) {
        if (entity.getStatus() != null && !"0".equals(entity.getStatus())) {
            throw new ServiceException("Trạng thái nhắc lịch không hợp lệ khi tạo mới: chỉ được tạo ở trạng thái chờ xử lý");
        }
        entity.setStatus("0");
    }

    /** DS319: cap nhat qua save()/bulkUpdate() khong duoc doi status --
     * chi acknowledge moi duoc 0->1. Payload phai bang status da luu
     * (null coi nhu '0', theo mac dinh constructor); khac -> 400.
     * checkExists da chay truoc o cac duong goi nen stored != null. */
    private void checkStatusNotChanged(FollowupReminder entity) {
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            return;
        }
        FollowupReminder stored = dao.get(entity.getId());
        if (stored == null) {
            return;
        }
        String wanted = entity.getStatus() == null ? "0" : entity.getStatus();
        if (!wanted.equals(stored.getStatus())) {
            throw new ServiceException("Không được đổi trạng thái nhắc lịch qua lưu thường; dùng xác nhận nhắc lịch");
        }
        entity.setStatus(stored.getStatus());
    }

    /** DS103: bulkInsert bo qua @Valid nen thieu truong NOT NULL se 500
     * o SQL thay vi 400 (probe A9/A10). Kiem tra truoc khi ghi: ca ba
     * truong @NotNull cua entity (patient_id/patient_name/followup_type
     * deu NOT NULL trong DB) + company (company_id NOT NULL). */
    private void validateRequired(FollowupReminder entity) {
        if (entity.getPatientId() == null) {
            throw new ServiceException("Bệnh nhân không được để trống");
        }
        if (StringUtils.isBlank(entity.getPatientName())) {
            throw new ServiceException("Tên bệnh nhân không được để trống");
        }
        if (StringUtils.isBlank(entity.getFollowupType())) {
            throw new ServiceException("Loại tái khám không được để trống");
        }
        if (entity.getCompany() == null
                || StringUtils.isBlank(entity.getCompany().getId())) {
            throw new ServiceException("Phòng khám không được để trống");
        }
    }

    @Override
    @Transactional(readOnly = false)
    public FollowupReminder save(FollowupReminder entity) {
        if (StringUtils.isNotBlank(entity.getId())) {
            checkRowOwnership(entity.getId());
            checkExists(entity.getId());
            // DS319: cap nhat khong duoc doi status qua save.
            checkStatusNotChanged(entity);
        } else {
            forceSessionCompany(entity);
            // DS319: tao moi chi duoc status '0' (mac dinh cu null->'0'
            // nay di kem 400 khi payload gia mao '1'/rac).
            checkStatusNotForgedOnCreate(entity);
        }
        // DS149 C: chan do dai truoc moi ghi (mau DS148 A). Tat ca cac
        // truong chu deu KHONG co @Length: patientName -> varchar(100),
        // followupType -> varchar(50), status -> varchar(1) (probe
        // C15/C16), remarks -> varchar(255). Gioi han theo cot DB;
        // null-safe. Guard dat sau mac dinh status "0" o tren.
        checkFollowupReminderLengths(entity);
        checkFollowupReminderFollowupTypeValues(entity); // DS480
        checkNextDateNotPast(entity);
        checkLastNoticeDateNotFuture(entity);
        return super.save(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public int delete(FollowupReminder entity) {
        // DS130 B: checkRowOwnership cho qua im lang id trong/ghost
        // (getCompanyIdById tra blank) nen ghost delete 200 rows 0 im
        // lang, blank 200, xoa lap 200 rows 1 (mapper soft-delete khong
        // loc del_flag) -- cung lop DS123/DS129. Chan truoc bang
        // dao.get (khong loc del_flag nen tra ca dong da xoa mem,
        // delFlag phan biet) theo mau CodeRuleService.delete (DS129 A);
        // KHONG tai dung checkExists o day: no cho qua blank va khong
        // check delFlag, lai dung chung voi save/bulkUpdate; giu
        // checkRowOwnership sau do (dong live la tenant van 403).
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được nhắc lịch tái khám cần xóa.");
        }
        FollowupReminder stored = dao.get(entity.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Nhắc lịch tái khám không tồn tại: " + entity.getId());
        }
        checkRowOwnership(entity.getId());
        return super.delete(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<FollowupReminder> entitys) {
        for (FollowupReminder entity : entitys) {
            forceSessionCompany(entity);
            // DS319: tao moi chi duoc status '0' (thay mac dinh null->'0'
            // cu cua DS103, nay di kem 400 khi payload gia mao '1'/rac).
            checkStatusNotForgedOnCreate(entity);
            validateRequired(entity);
            checkFollowupReminderLengths(entity);
            checkFollowupReminderFollowupTypeValues(entity); // DS480
            checkNextDateNotPast(entity);
            checkLastNoticeDateNotFuture(entity);
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<FollowupReminder> entitys) {
        for (FollowupReminder entity : entitys) {
            checkRowOwnership(entity.getId());
            // DS103: vong pre-check chay truoc dao.bulkUpdate (mot
            // statement CASE duy nhat) nen [valid, khong ton tai] nem
            // truoc khi ghi gi -- dong valid khong bi doi (probe A8).
            checkExists(entity.getId());
            // DS319: cap nhat khong duoc doi status qua bulkUpdate.
            checkStatusNotChanged(entity);
            checkFollowupReminderLengths(entity);
            checkFollowupReminderFollowupTypeValues(entity); // DS480
            checkNextDateNotPast(entity);
        }
        return super.bulkUpdate(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<FollowupReminder> entitys) {
        for (FollowupReminder entity : entitys) {
            checkRowOwnership(entity.getId());
        }
        return super.bulkDelete(entitys);
    }

    // DS277: ngay hen tai kham tiep theo khong duoc la ngay qua khu.
    // nextDate la ngay hen TUONG LAI theo dinh nghia (frontend
    // FollowupReminderCard.vue:38 el-date-picker "Chon ngay hen");
    // ngay da qua la du lieu chet -- job due/daysOverdue chi xu ly
    // dong da ton tai tro nen qua han theo thoi gian, khong phai de
    // tao moi mot lich hen da qua. So theo ngay phong kham GMT+7
    // (CLINIC_ZONE, nhu clinicToday/dueBefore); hom nay/tuong lai/
    // null giu nguyen (cot nullable, frontend bat buoc nhung API cho
    // phep thieu). lastNoticeDate: xem DS287 (guard tuong lai o
    // checkLastNoticeDateNotFuture, goi o save()/bulkInsert()).
    private static void checkNextDateNotPast(FollowupReminder entity) {
        if (entity == null || entity.getNextDate() == null) {
            return;
        }
        if (entity.getNextDate().toInstant().atZone(CLINIC_ZONE).toLocalDate()
                .isBefore(clinicToday())) {
            throw new ServiceException("Ngày hẹn tái khám không được là ngày quá khứ.");
        }
    }

    // DS287: ngay da tao thong bao gan nhat khong duoc o tuong lai.
    // lastNoticeDate ghi lai lan job tao thong bao noi bo GAN NHAT --
    // theo dinh nghia no la mot ngay da xay ra. DS277 da hoan truong
    // nay voi ly do "chi dao.markNoticed moi ghi", nhung ly do do
    // SAI o tang mapper: insert :160, update :193 va bulkInsert :217
    // cua FollowupReminderDao.xml deu ghi last_notice_date =
    // #{lastNoticeDate} VO DIEU KIEN tu payload, nen API save/
    // bulkInsert trong duoc gia tri tuong lai. Hau qua that, khong
    // chi du lieu xau: dao.markNoticed chi chay khi
    // (last_notice_date IS NULL OR last_notice_date < today)
    // (:148), nen mot gia tri tuong lai lam job im lang voi nhac
    // lich do cho toi qua ngay do (2030) -- tat ca thong bao tai
    // kham cua benh nhan bi ngat. So theo ngay phong kham GMT+7;
    // hom nay (job vua tao thong bao) / qua khu / null giu 200.
    // Chi goi o save()/bulkInsert() -- bulkUpdate KHONG co CASE
    // last_notice_date nen khong ghi cot nay, khong can guard.
    private static void checkLastNoticeDateNotFuture(FollowupReminder entity) {
        if (entity == null || entity.getLastNoticeDate() == null) {
            return;
        }
        if (entity.getLastNoticeDate().toInstant().atZone(CLINIC_ZONE).toLocalDate()
                .isAfter(clinicToday())) {
            throw new ServiceException("Ngày đã tạo thông báo gần nhất không được là ngày tương lai.");
        }
    }

    // DS480: loai tai kham chi co 5 gia tri UI co dinh (sole writer
    // FollowupReminderCard.vue:69 typeOptions, khong dict). Gia tri
    // gia mao (chuoi bat ky <=50 ky tu) lot qua @Valid/@Length va
    // duoc hien thi verbatim tren card workbench + nhung vao
    // NoticeSend.siteContent. Blank/null giu qua day -- cac duong
    // 400 hien co (validateRequired/@Valid/NOT NULL) xu ly.
    private static final Set<String> VALID_FOLLOWUP_TYPES =
            new HashSet<>(Arrays.asList(
                    "Tái khám định kỳ", "Tiêm chủng nhắc lại",
                    "Xét nghiệm lại", "Khám chuyên khoa", "Khác"));

    private static void checkFollowupReminderFollowupTypeValues(
            FollowupReminder entity) {
        if (entity == null
                || StringUtils.isBlank(entity.getFollowupType())) {
            return;
        }
        if (!VALID_FOLLOWUP_TYPES.contains(entity.getFollowupType())) {
            throw new ServiceException(
                    "Loại tái khám không hợp lệ: chỉ được chọn trong danh sách loại tái khám");
        }
    }

    // DS149 C: xem ghi chu o save().
    private static void checkFollowupReminderLengths(FollowupReminder entity) {
        if (entity == null) {
            return;
        }
        checkLength(entity.getPatientName(), 100, "Tên bệnh nhân Độ dài phải nằm trong khoảng 0 đến 100");
        checkLength(entity.getFollowupType(), 50, "Loại tái khám Độ dài phải nằm trong khoảng 0 đến 50");
        checkLength(entity.getStatus(), 1, "Trạng thái Độ dài phải nằm trong khoảng 0 đến 1");
        checkLength(entity.getRemarks(), 255, "Ghi chú Độ dài phải nằm trong khoảng 0 đến 255");
    }

    private static void checkLength(String value, int max, String message) {
        if (value != null && value.length() > max) {
            throw new ServiceException(message);
        }
    }

    /**
     * Ngày hôm nay theo giờ phòng khám.
     */
    public static LocalDate clinicToday() {
        return LocalDate.now(CLINIC_ZONE);
    }

    /**
     * Đầu ngày mai theo giờ phòng khám (mốc "đến hạn": next_date trước
     * mốc này tức là đến hạn hôm nay hoặc đã quá hạn).
     */
    public static Date dueBefore() {
        return Date.from(clinicToday().plusDays(1).atStartOfDay(CLINIC_ZONE).toInstant());
    }

    /**
     * Số ngày quá hạn của một ngày hẹn (0 = đến hạn hôm nay). Ngày hẹn
     * NULL không bao giờ được gọi tới (đã lọc ở SQL).
     */
    public static int daysOverdue(Date nextDate) {
        LocalDate due = nextDate.toInstant().atZone(CLINIC_ZONE).toLocalDate();
        long days = java.time.temporal.ChronoUnit.DAYS.between(due, clinicToday());
        return (int) Math.max(0, days);
    }

    private void fillDaysOverdue(List<FollowupReminder> rows) {
        if (rows == null) {
            return;
        }
        for (FollowupReminder row : rows) {
            if (row.getNextDate() != null) {
                row.setDaysOverdue(daysOverdue(row.getNextDate()));
            }
        }
    }

    /**
     * DS259: chan limit/offset khong tran tren duePage — funnel list con
     * sot sau DS203/DS204 (offset/limit tho vao Map roi LIMIT #{limit}
     * OFFSET #{offset} truc tiep, khong qua PageRequest): limit > 10000
     * lay het bang mot trang, limit/offset am lot toi MySQL moi bi DB
     * tu choi -> 400 message chung (DS94). limit trong [0, 10000]
     * (0 giu hop le nhu DS203/DS204), offset >= 0; sai ->
     * IllegalArgumentException -> 400 qua GlobalExceptionHandler (DS160).
     */
    private static void checkPageBounds(int offset, int limit) {
        if (limit < 0 || limit > com.geeke.common.data.PageRequest.MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Invalid limit: " + limit);
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Invalid offset: " + offset);
        }
    }

    /**
     * Trang đến hạn/quá hạn, lọc theo tenant đang đăng nhập.
     * companyId null = mọi tenant (chỉ job hệ thống dùng).
     */
    public Page<FollowupReminder> duePage(String companyId, int offset, int limit) {
        checkPageBounds(offset, limit);
        Map<String, Object> params = new HashMap<>();
        params.put("companyId", companyId);
        params.put("dueBefore", dueBefore());
        params.put("offset", offset);
        params.put("limit", limit);
        // Mapper XML dùng #{DEL_FLAG_NORMAL}; biến này chỉ có sẵn khi
        // tham số là PageRequest (getter của nó), còn Map thuần thì
        // MyBatis bind thành NULL -> del_flag = NULL -> 0 dòng. Vì vậy
        // phải đưa tường minh vào Map (giá trị = BaseEntity.DEL_FLAG_NORMAL).
        params.put("DEL_FLAG_NORMAL", BaseEntity.DEL_FLAG_NORMAL);
        int total = dao.countDue(params);
        List<FollowupReminder> rows = total > 0 ? dao.listDue(params) : Collections.emptyList();
        fillDaysOverdue(rows);
        return new Page<>(total, rows);
    }

    /**
     * Xác nhận một nhắc lịch. Trả về số dòng cập nhật (0 = không tìm
     * thấy, đã xác nhận trước đó, hoặc sai tenant).
     */
    @Transactional(readOnly = false)
    public int acknowledge(String id, String companyId) {
        String updateBy = currentUserLabel();
        return dao.acknowledge(id, companyId, updateBy, new Date());
    }

    /**
     * Tạo thông báo nội bộ cho mọi nhắc lịch đến hạn chưa có thông báo
     * trong ngày. Idempotent: chạy lại cùng ngày không tạo trùng
     * (khóa bằng last_notice_date ở cả SQL markNoticed và kiểm tra trước
     * khi insert). companyId null = mọi tenant (job hệ thống); khác null
     * = chỉ tenant đó (nút chạy tay của nhân viên).
     * Trả về số thông báo đã tạo. Không gửi SMS/Zalo, không ra ngoài.
     * Nội dung thông báo chỉ có tên bệnh nhân + loại tái khám + ngày hẹn.
     */
    @Transactional(readOnly = false)
    public int createDueNotices(String companyId) {
        LocalDate today = clinicToday();
        // Chuỗi ngày phòng khám, dùng cho cả claim SQL lẫn giá trị lưu.
        // Không dùng java.sql.Date: driver JDBC chuyển nó theo múi giờ
        // JVM/server làm lệch ngày (đã gặp thực tế: lưu 2026-10-31 cho
        // ngày phòng khám 2026-11-01, và vì insert chạy trước claim nên
        // job chạy lần 2 vẫn insert trùng dù trả về 0).
        String todayStr = today.toString();
        Map<String, Object> params = new HashMap<>();
        params.put("companyId", companyId);
        params.put("dueBefore", dueBefore());
        params.put("offset", 0);
        params.put("limit", 5000);
        params.put("DEL_FLAG_NORMAL", BaseEntity.DEL_FLAG_NORMAL);
        List<FollowupReminder> due = dao.listDue(params);
        int created = 0;
        for (FollowupReminder reminder : due) {
            // Claim trước, insert sau: chỉ dòng nào giành được claim
            // (chưa có thông báo trong ngày) mới được insert thông báo.
            // Nhờ đó chạy lại cùng ngày, kể cả đồng thời, cũng không
            // bao giờ tạo trùng.
            int marked = dao.markNoticed(reminder.getId(), reminder.getCompany().getId(),
                    todayStr, currentUserLabel(), new Date());
            if (marked <= 0) {
                continue;
            }
            NoticeSend notice = new NoticeSend();
            Company company = new Company();
            company.setId(reminder.getCompany().getId());
            notice.setCompany(company);
            notice.setSenderId(currentUserId());
            String title = "Nhắc tái khám: " + reminder.getPatientName();
            if (title.length() > 50) {
                title = title.substring(0, 50);
            }
            notice.setTitle(title);
            notice.setIsSite("1");
            notice.setIsWechat("0");
            notice.setIsEmail("0");
            notice.setIsSms("0");
            notice.setSiteContent(reminder.getPatientName() + " | " + reminder.getFollowupType()
                    + " | Hạn: " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(reminder.getNextDate()));
            noticeSendService.save(notice);
            created++;
        }
        log.info("followup-reminder job: date={} company={} created={}", today, companyId, created);
        return created;
    }

    private String currentUserLabel() {
        try {
            com.alibaba.fastjson.JSONObject user = SessionUtils.getUserJson();
            if (user != null && StringUtils.isNotBlank(user.getString("id"))) {
                return user.getString("name") + "(" + user.getString("loginName") + ")";
            }
        } catch (Exception e) {
            log.debug("followup-reminder: no session user", e);
        }
        return "system";
    }

    private String currentUserId() {
        try {
            com.alibaba.fastjson.JSONObject user = SessionUtils.getUserJson();
            if (user != null) {
                return user.getString("id");
            }
        } catch (Exception e) {
            log.debug("followup-reminder: no session user id", e);
        }
        return null;
    }
}
