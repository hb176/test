package com.gmp.system.task;

import com.gmp.system.service.SysScheduledTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTaskBootstrap {

    private final SysScheduledTaskService taskService;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("开始加载启用的定时任务...");
        taskService.loadAllEnabledTasks();
    }
}
