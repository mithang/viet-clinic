package com.geeke.noticesend.service;

import com.geeke.common.service.CrudService;
import com.geeke.noticesend.dao.NoticeSendDao;
import com.geeke.noticesend.entity.NoticeSend;
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
 */
@Service("noticeSendService")
@Transactional(readOnly = true)
public class NoticeSendService extends CrudService<NoticeSendDao, NoticeSend> {

    @Override
    @Transactional(readOnly = false)
    public NoticeSend save(NoticeSend entity) {
        return super.save(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public int delete(NoticeSend entity) {
        return super.delete(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<NoticeSend> entitys) {
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<NoticeSend> entitys) {
        return super.bulkUpdate(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<NoticeSend> entitys) {
        return super.bulkDelete(entitys);
    }
}
