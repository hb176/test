package com.gmp.system.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 动态定时任务管理器 — 在运行时注册/取消/重调度 Cron 任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicTaskManager {

    private final TaskScheduler taskScheduler;
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public void register(String taskCode, Runnable task, String cronExpression) {
        cancel(taskCode);
        ScheduledFuture<?> future = taskScheduler.schedule(task, new CronTrigger(cronExpression));
        scheduledTasks.put(taskCode, future);
        log.info("定时任务已注册: code={}, cron={}", taskCode, cronExpression);
    }

    public void cancel(String taskCode) {
        ScheduledFuture<?> existing = scheduledTasks.remove(taskCode);
        if (existing != null && !existing.isCancelled()) {
            existing.cancel(false);
            log.info("定时任务已取消: code={}", taskCode);
        }
    }

    public boolean isRunning(String taskCode) {
        ScheduledFuture<?> future = scheduledTasks.get(taskCode);
        return future != null && !future.isCancelled();
    }

    public int getActiveCount() {
        return scheduledTasks.size();
    }
}
