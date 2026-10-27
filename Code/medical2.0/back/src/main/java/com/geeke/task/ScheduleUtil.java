package com.geeke.task;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

/**
 * 利dùngluồng池Triển khaitác vụđiềuđộ
 * TaskBộ lập lịch tác vụ có thể thực hiện lập lịch và xóa tác vụ
 * gốclý:
 * Triển khaimộtCáiLớp: ThreadPoolTaskSchedulerBộ lập lịch tác vụ của pool luồng, Có thể mở pool luồng để tiến hành dòngtác vụđiềuđộ
 * ThreadPoolTaskScheduler.schedule()Phương thứcsẽtạomộtCáichỉ định giờkế hoạchScheduleFuture,
 * tạinàyCáiPhương thức trong ThêmhaiCáiTham sốmộtCái có Runable:luồnggiao diệnLớp,  và CronTrigger(chỉ định giờtác vụkích hoạtthiết bị)
 * tạiScheduleFuture trong cómộtCáicancelCó thểdừngdừngchỉ định giờtác vụ
 * @Author: shenzy
 * @DateTime: 2021/10/28 10:51
 * @Description: TODO
 */
@Component
@EnableScheduling
@Slf4j
public class ScheduleUtil {

    private static ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    private static Map<String, ScheduledFuture<?>> scheduledFutureMap = new HashMap<String, ScheduledFuture<?>>();

    static {
        threadPoolTaskScheduler.initialize();
        log.info("chỉ định giờtác vụkhởi động");
    }

    /**
     * chỉ định giờtác vụ của khởi động
     * @param runnable  hành nghề dòng của tác vụ
     * @param trigger   Biểu thức
     * @param code      Mã
     */
    public static void start(Runnable runnable, Trigger trigger, String code) {
        ScheduledFuture<?> scheduledFuture = threadPoolTaskScheduler.schedule(runnable, trigger);
        scheduledFutureMap.put(code, scheduledFuture);
        log.info("khởi độngchỉ định giờtác vụ" + code);
    }

    /**
     * chỉ định giờtác vụđóng
     * @param code
     */
    public static void cancel(String code) {
        ScheduledFuture<?> scheduledFuture = scheduledFutureMap.get(code);
        if (scheduledFuture != null/* && scheduledFuture.isCancelled()*/) {
            scheduledFuture.cancel(true);
            scheduledFutureMap.remove(code);
        }
        log.info("lấyhủychỉ định giờtác vụ" + code);
    }

    /**
     * chỉ định giờtác vụkhởi động lại
     * @param runnable
     * @param trigger
     * @param code
     */
    public static void reset(Runnable runnable, Trigger trigger, String code) {
        ScheduledFuture<?> scheduledFuture = scheduledFutureMap.get(code);
        if (scheduledFuture != null && scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);
        }
        scheduledFutureMap.remove(code);

        scheduledFuture = threadPoolTaskScheduler.schedule(runnable, trigger);
        scheduledFutureMap.put(code, scheduledFuture);
        log.info("Sửachỉ định giờtác vụ" + code);
    }
}

