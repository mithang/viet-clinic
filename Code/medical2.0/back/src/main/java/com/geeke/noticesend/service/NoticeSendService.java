package com.geeke.noticesend.service;

import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.noticesend.dao.NoticeSendDao;
import com.geeke.noticesend.entity.NoticeSend;
import com.geeke.org.entity.Company;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gửi thông báo Service.
 *
 * Lớp để readOnly=true cho các method đọc (get/list). Các method ghi
 * phải được ghi đè tường minh với readOnly=false: nếu không, transaction
 * read-only của lớp sẽ làm INSERT/UPDATE bị từ chối ("Connection is
 * read-only") — đúng lỗi đã gặp ở RecipetemplateService.allSave.
 *
 * Cách ly tenant (Z3k, mẫu Z3f/Z3h + His): verifyTenantOwnership của
 * CrudService chỉ kiểm tra company trên payload, còn SQL update/delete/
 * bulk của notice_send chỉ WHERE id — nên payload mang id của dòng tenant
 * lạ + company của tenant mình vẫn lọt và sửa/xóa được dòng lạ. Vì vậy
 * save/delete/bulkUpdate/bulkDelete đều kiểm tra dòng cũ trong DB trước
 * (checkNoticeOwnership): dòng lạ -> ServiceException -> 400 fail-closed;
 * dòng không tồn tại / session rỗng giữ hành vi cũ. bulkInsert thì ép
 * company = tenant session cho mọi dòng (mẫu His), vì bulkInsert của
 * CrudService không gọi verifyTenantOwnership và DAO ghi thẳng
 * company_id từ payload — payload company lạ sẽ chèn thẳng dòng tenant
 * lạ. Caller hợp lệ (form frontend không gửi company) không đổi.
 */
@Service("noticeSendService")
@Transactional(readOnly = true)
public class NoticeSendService extends CrudService<NoticeSendDao, NoticeSend> {

    private void checkNoticeOwnership(String id) {
        if (StringUtils.isBlank(id)) {
            return;
        }
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return;
        }
        String oldCompanyId = dao.getCompanyIdById(id);
        if (StringUtils.isNotBlank(oldCompanyId)
                && !tenantId.equals(oldCompanyId)) {
            throw new ServiceException("Thông báo không thuộc phòng khám hiện tại");
        }
    }

    /** DS103: fail-closed 400 khi update id khong ton tai (ho DS94/DS99).
     * checkNoticeOwnership pass khi dong khong ton tai (getCompanyIdById
     * tra null), CrudService.save sau do doUpdate cham 0 dong ma van 200
     * + ghi audit ACTION_UPDATED ma; bulkUpdate CASE cung cham 0 dong.
     * Kiem tra ton tai qua dao.get (LEFT JOIN company -- null chi khi
     * dong khong ton tai). */
    private void checkExists(String id) {
        if (StringUtils.isBlank(id)) {
            return;
        }
        if (dao.get(id) == null) {
            throw new ServiceException("Thông báo không tồn tại: " + id);
        }
    }

    private void forceSessionCompany(List<NoticeSend> entitys) {
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return;
        }
        for (NoticeSend entity : entitys) {
            Company tenant = new Company();
            tenant.setId(tenantId);
            entity.setCompany(tenant);
        }
    }

    @Override
    public NoticeSend get(String id) {
        // Cùng bẫy getter: CrudService.get chỉ verify qua entity.getCompany()
        // (tự tổng hợp tenant session khi field null) nên getById dòng lạ
        // vẫn trả 200 kèm tiêu đề/nội dung lạ — đã chứng minh live.
        checkNoticeOwnership(id);
        return super.get(id);
    }

    @Override
    @Transactional(readOnly = false)
    public NoticeSend save(NoticeSend entity) {
        // DS693: form noticeSendForm.vue bat buoc title (rule required),
        // nhung save({}) / title '   ' / title thieu van 200 + dong
        // notice_send rac (title NULL) vi @Length(min=1) chi chan ''
        // chu khong chan null hay khoang trang, CrudService khong guard.
        // Chi guard title: receivedBy KHONG guard — ds103-verify
        // valid_notice() (fixture chuan tu DS103) khong gui receivedBy
        // ma van la notice hop le, guard receivedBy lam vo 3 control
        // ds103 B8/B9/B11. Chan fail-closed 400 o ca create lan update,
        // truoc moi ghi (mau DS683/DS692).
        checkNoticeSendRequired(entity);
        if (StringUtils.isNotBlank(entity.getId())) {
            checkNoticeOwnership(entity.getId());
            checkExists(entity.getId());
        }
        // DS149 C: chan do dai truoc moi ghi (mau DS148 A). Tat ca cac
        // truong chu deu KHONG co @Length: senderId -> varchar(100)
        // (probe C11), title -> varchar(50), receivedBy -> varchar(255)
        // (probe C12), isSite/isWechat/isEmail/isSms -> varchar(1),
        // remarks -> varchar(255). Cac truong noi dung (site/wechat/
        // email/smsContent) la cot text, khong guard. Gioi han theo cot
        // DB; null-safe.
        checkNoticeSendLengths(entity);
        return super.save(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public int delete(NoticeSend entity) {
        // DS130 C: checkNoticeOwnership cho qua im lang id trong/ghost
        // (getCompanyIdById tra blank) nen ghost delete 200 rows 0 im
        // lang, blank 200, xoa lap 200 rows 1 (mapper soft-delete khong
        // loc del_flag) -- cung lop DS123/DS129. Chan truoc bang
        // dao.get (khong loc del_flag nen tra ca dong da xoa mem,
        // delFlag phan biet) theo mau CodeRuleService.delete (DS129 A);
        // KHONG tai dung checkExists o day: no cho qua blank va khong
        // check delFlag, lai dung chung voi save/bulkUpdate; giu
        // checkNoticeOwnership sau do (dong live la tenant van 403).
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được thông báo cần xóa.");
        }
        NoticeSend stored = dao.get(entity.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Thông báo không tồn tại: " + entity.getId());
        }
        checkNoticeOwnership(entity.getId());
        return super.delete(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<NoticeSend> entitys) {
        // DS639: phan tu null -> 400 fail-closed, thay vi NPE 500 o
        // forceSessionCompany (entity.setCompany) ben duoi. Mau DS638
        // (StuffService): guard ngay dau, truoc moi deref; super van
        // chan list rong/null o CrudService.
        for (NoticeSend entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Thông báo không được để trống");
            }
        }
        forceSessionCompany(entitys);
        for (NoticeSend entity : entitys) {
            checkNoticeSendRequired(entity);
            checkNoticeSendLengths(entity);
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<NoticeSend> entitys) {
        for (NoticeSend entity : entitys) {
            // DS639: nhu bulkInsert tren -- phan tu null nem 400 truoc
            // khi checkNoticeOwnership deref entity.getId().
            if (entity == null) {
                throw new ServiceException("Thông báo không được để trống");
            }
            checkNoticeOwnership(entity.getId());
            // DS103: vong pre-check chay truoc dao.bulkUpdate (mot
            // statement CASE duy nhat) nen [valid, khong ton tai] nem
            // truoc khi ghi gi -- dong valid khong bi doi (probe B8).
            checkExists(entity.getId());
            checkNoticeSendRequired(entity);
            checkNoticeSendLengths(entity);
        }
        return super.bulkUpdate(entitys);
    }

    // DS693: xem ghi chu o save(). Chi title; receivedBy tuy chon
    // theo fixture chuan DS103 (valid_notice khong gui receivedBy).
    private static void checkNoticeSendRequired(NoticeSend entity) {
        if (entity == null) {
            return;
        }
        if (StringUtils.isBlank(entity.getTitle())) {
            throw new ServiceException("Tiêu đề không được để trống");
        }
    }

    // DS149 C: xem ghi chu o save().
    private static void checkNoticeSendLengths(NoticeSend entity) {
        if (entity == null) {
            return;
        }
        checkLength(entity.getSenderId(), 100, "Người gửi Độ dài phải nằm trong khoảng 0 đến 100");
        checkLength(entity.getTitle(), 50, "Tiêu đề Độ dài phải nằm trong khoảng 0 đến 50");
        checkLength(entity.getIsSite(), 1, "Gửi nội bộ Độ dài phải nằm trong khoảng 0 đến 1");
        checkLength(entity.getIsWechat(), 1, "Gửi WeChat Độ dài phải nằm trong khoảng 0 đến 1");
        checkLength(entity.getIsEmail(), 1, "Gửi email Độ dài phải nằm trong khoảng 0 đến 1");
        checkLength(entity.getIsSms(), 1, "Gửi SMS Độ dài phải nằm trong khoảng 0 đến 1");
        checkLength(entity.getReceivedBy(), 255, "Người nhận Độ dài phải nằm trong khoảng 0 đến 255");
        checkLength(entity.getRemarks(), 255, "Ghi chú Độ dài phải nằm trong khoảng 0 đến 255");
    }

    private static void checkLength(String value, int max, String message) {
        if (value != null && value.length() > max) {
            throw new ServiceException(message);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<NoticeSend> entitys) {
        for (NoticeSend entity : entitys) {
            // DS639: nhu bulkInsert/bulkUpdate tren -- phan tu null nem
            // 400 truoc khi checkNoticeOwnership deref entity.getId().
            if (entity == null) {
                throw new ServiceException("Thông báo không được để trống");
            }
            checkNoticeOwnership(entity.getId());
        }
        return super.bulkDelete(entitys);
    }
}
