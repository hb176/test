package com.gmp.system.service;

import com.gmp.framework.base.CommonService;
import com.gmp.system.entity.SysScheduledTask;
import com.gmp.system.entity.SysScheduledTaskLog;
import com.gmp.system.mapper.SysScheduledTaskMapper;
import com.gmp.system.task.DynamicTaskManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import org.springframework.scheduling.support.CronExpression;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysScheduledTaskService extends CommonService<SysScheduledTaskMapper, SysScheduledTask> {

    private final DynamicTaskManager dynamicTaskManager;
    private final SysScheduledTaskLogService taskLogService;
    private final ApplicationContext applicationContext;

    /**
     * 启用任务 — 注册到动态调度器
     */
    public void enableTask(Long taskId) {
        SysScheduledTask task = getById(taskId);
        if (task == null || task.getStatus() == 1) return;

        task.setStatus(1);
        updateById(task);
        registerToScheduler(task);
        log.info("定时任务已启用: code={}", task.getTaskCode());
    }

    /**
     * 禁用任务 — 从调度器移除
     */
    public void disableTask(Long taskId) {
        SysScheduledTask task = getById(taskId);
        if (task == null || task.getStatus() == 0) return;

        task.setStatus(0);
        updateById(task);
        dynamicTaskManager.cancel(task.getTaskCode());
        log.info("定时任务已禁用: code={}", task.getTaskCode());
    }

    /**
     * 立即执行一次（手动触发）
     */
    public void executeOnce(Long taskId) {
        SysScheduledTask task = getById(taskId);
        if (task == null) return;
        executeTask(task);
    }

    /**
     * 启动时加载所有启用的任务
     */
    public void loadAllEnabledTasks() {
        lambdaQuery().eq(SysScheduledTask::getStatus, 1).list().forEach(this::registerToScheduler);
        log.info("已加载 {} 个定时任务", dynamicTaskManager.getActiveCount());
    }

    /**
     * 校验 Cron 表达式是否合法
     */
    public boolean isValidCron(String cronExpression) {
        try {
            CronExpression.parse(cronExpression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void registerToScheduler(SysScheduledTask task) {
        dynamicTaskManager.register(task.getTaskCode(), () -> executeTask(task), task.getCronExpression());
    }

    private void executeTask(SysScheduledTask task) {
        LocalDateTime startTime = LocalDateTime.now();
        SysScheduledTaskLog logRecord = new SysScheduledTaskLog();
        logRecord.setTaskId(task.getId());
        logRecord.setTaskCode(task.getTaskCode());
        logRecord.setStartTime(startTime);

        try {
            if (task.getBeanName() != null && task.getMethodName() != null) {
                Object bean = applicationContext.getBean(task.getBeanName());
                Method method = bean.getClass().getMethod(task.getMethodName());
                method.invoke(bean);
            }
            logRecord.setResult("SUCCESS");
            log.info("定时任务执行成功: code={}", task.getTaskCode());
        } catch (Exception e) {
            logRecord.setResult("FAIL");
            logRecord.setErrorMsg(e.getMessage());
            log.error("定时任务执行失败: code={}, error={}", task.getTaskCode(), e.getMessage());
        }

        LocalDateTime endTime = LocalDateTime.now();
        logRecord.setEndTime(endTime);
        logRecord.setCostMs(java.time.Duration.between(startTime, endTime).toMillis());
        logRecord.setCreateTime(endTime);
        taskLogService.save(logRecord);

        task.setLastExecTime(endTime);
        updateById(task);
    }
}
