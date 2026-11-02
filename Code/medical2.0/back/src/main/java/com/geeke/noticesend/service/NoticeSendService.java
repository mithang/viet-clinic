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
        if (StringUtils.isNotBlank(entity.getId())) {
            checkNoticeOwnership(entity.getId());
        }
        return super.save(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public int delete(NoticeSend entity) {
        checkNoticeOwnership(entity.getId());
        return super.delete(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<NoticeSend> entitys) {
        forceSessionCompany(entitys);
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<NoticeSend> entitys) {
        for (NoticeSend entity : entitys) {
            checkNoticeOwnership(entity.getId());
        }
        return super.bulkUpdate(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<NoticeSend> entitys) {
        for (NoticeSend entity : entitys) {
            checkNoticeOwnership(entity.getId());
        }
        return super.bulkDelete(entitys);
    }
}
