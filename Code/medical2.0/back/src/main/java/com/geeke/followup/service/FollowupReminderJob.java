package com.geeke.followup.service;

import com.geeke.task.ScheduleUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * Job nhắc lịch tái khám hằng ngày (G1 research loop).
 *
 * Chạy 08:00 giờ phòng khám (Asia/Ho_Chi_Minh, không phụ thuộc múi giờ
 * của JVM/MySQL) và tạo một thông báo nội bộ cho mỗi nhắc lịch đến hạn
 * chưa có thông báo trong ngày. Idempotent: chạy lại cùng ngày không
 * tạo trùng; backend restart không backfill spam (khóa theo
 * last_notice_date). Lỡ nhịp (backend tắt lúc 08:00) thì nhân viên bấm
 * nút "Chạy nhắc hôm nay" ở card workbench để chạy bù trong ngày.
 */
@Component
@Slf4j
public class FollowupReminderJob {

    @Autowired
    private FollowupReminderService followupReminderService;

    @PostConstruct
    public void register() {
        ScheduleUtil.start(() -> {
            try {
                int created = followupReminderService.createDueNotices(null);
                log.info("followup-reminder daily job done: created={}", created);
            } catch (Exception e) {
                log.error("followup-reminder daily job failed", e);
            }
        }, new CronTrigger("0 0 8 * * ?", TimeZone.getTimeZone("Asia/Ho_Chi_Minh")),
                "followup-reminder-daily");
    }
}
