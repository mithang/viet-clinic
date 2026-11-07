package com.geeke.schedule.service;

import java.util.Map;

import com.geeke.utils.CronUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.schedule.dao.ScheduleJobDao;
import com.geeke.schedule.entity.ScheduleJob;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Maps;

/**
 * Quản lý tác vụ định kỳService
 * @author shenzy
 * @version 2021-10-28
 */
 
@Service("scheduleJobService")
@Transactional(readOnly = true)
public class ScheduleJobService extends CrudService<ScheduleJobDao, ScheduleJob>{


    @Override
    @Transactional
    public ScheduleJob save(ScheduleJob entity) {
        String cron = entity.getCron();
        if (StringUtils.isEmpty(cron)|| ! CronUtils.isValid(cron)){
            throw new ServiceException("cronbiểu thứcTham sốngoại lệ");
        }
        // DS120 C: save voi id khong ton tai truoc day roi qua
        // CrudService.save -> doUpdate 0 dong roi van 200 silent no-op
        // (verifyTenantOwnershipAgainstStoredRow bo qua khi stored==null,
        // cung mau DS94/DS99; schedule_job khong co cot company_id nen
        // guard tenant khong chan duoc, chi co kiem tra ton tai nay).
        if (StringUtils.isNotBlank(entity.getId())
                && dao.get(entity.getId()) == null) {
            throw new ServiceException("Tác vụ định kỳ không tồn tại: " + entity.getId());
        }
        return super.save(entity);
    }

    // DS120 C: bulkInsert/bulkUpdate generic bypass save() nen cron
    // khong duoc kiem tra: bulkInsert tao job cron sai/thieu voi 200
    // (tat ca cot schedule_job deu nullable, khong 500 -- chi tao dong
    // hong toan cuc), bulkUpdate id khong ton tai -> 200 silent / hon
    // hop ghi mot phan, co the NULL hoa hoac lam hong cron cua job live,
    // null/rong/[null] -> 500 NPE. Chan het o service theo dung luat cua
    // save() (cron bat buoc + hop le), kiem tra truoc moi ghi de batch
    // hon hop khong doi dong hop le. Name/code KHONG bat buoc: save don
    // cung chap nhan null cho ca hai (@Length(min=1) khong @NotNull, cot
    // nullable) -- khong bia them luat.
    @Override
    @Transactional(readOnly = false)
    public java.util.List<String> bulkInsert(java.util.List<ScheduleJob> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách tác vụ định kỳ không được để trống");
        }
        for (ScheduleJob entity : entitys) {
            validateBulkJob(entity);
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public java.util.List<String> bulkUpdate(java.util.List<ScheduleJob> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách tác vụ định kỳ không được để trống");
        }
        for (ScheduleJob entity : entitys) {
            if (entity == null || StringUtils.isBlank(entity.getId())) {
                throw new ServiceException("Tác vụ định kỳ không xác định được id cần cập nhật");
            }
            if (dao.get(entity.getId()) == null) {
                throw new ServiceException("Tác vụ định kỳ không tồn tại: " + entity.getId());
            }
            validateBulkJob(entity);
        }
        return super.bulkUpdate(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(java.util.List<ScheduleJob> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách tác vụ định kỳ không được để trống");
        }
        return super.bulkDelete(entitys);
    }

    private void validateBulkJob(ScheduleJob entity) {
        if (entity == null) {
            throw new ServiceException("Tác vụ định kỳ không được để trống");
        }
        String cron = entity.getCron();
        if (StringUtils.isEmpty(cron) || !CronUtils.isValid(cron)) {
            throw new ServiceException("cronbiểu thứcTham sốngoại lệ");
        }
    }
}