package com.gmp.system.controller;

import com.gmp.framework.base.CommonController;
import com.gmp.framework.base.Result;
import com.gmp.common.base.ResultCode;
import com.gmp.system.entity.SysScheduledTask;
import com.gmp.system.entity.SysScheduledTaskLog;
import com.gmp.system.service.SysScheduledTaskLogService;
import com.gmp.system.service.SysScheduledTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/scheduled-task")
public class SysScheduledTaskController extends CommonController<SysScheduledTaskService, SysScheduledTask> {

    private final SysScheduledTaskService taskService;
    private final SysScheduledTaskLogService taskLogService;

    @Override
    protected SysScheduledTaskService getService() {
        return taskService;
    }

    @GetMapping("/list")
    public Result<?> queryList(@RequestParam(required = false) String sysModule,
                               @RequestParam(required = false) Integer status) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysScheduledTask>();
        if (sysModule != null && !sysModule.isEmpty()) wrapper.eq(SysScheduledTask::getSysModule, sysModule);
        if (status != null) wrapper.eq(SysScheduledTask::getStatus, status);
        wrapper.orderByAsc(SysScheduledTask::getSysModule, SysScheduledTask::getTaskCode);
        return Result.ok(taskService.list(wrapper));
    }

    @GetMapping("/{id}")
    public Result<SysScheduledTask> getById(@PathVariable Long id) {
        SysScheduledTask task = taskService.getById(id);
        return task != null ? Result.ok(task) : Result.fail(ResultCode.NOT_FOUND, "任务不存在");
    }

    @PreAuthorize("hasAuthority('system:task:add')")
    @PostMapping
    public Result<SysScheduledTask> create(@RequestBody SysScheduledTask task) {
        task.setStatus(0);
        taskService.save(task);
        return Result.ok("创建成功", task);
    }

    @PreAuthorize("hasAuthority('system:task:edit')")
    @PutMapping("/{id}")
    public Result<SysScheduledTask> update(@PathVariable Long id, @RequestBody SysScheduledTask task) {
        SysScheduledTask existing = taskService.getById(id);
        if (existing == null) return Result.fail(ResultCode.NOT_FOUND, "任务不存在");

        // 校验 Cron 表达式
        if (task.getCronExpression() != null && !task.getCronExpression().isEmpty()) {
            if (!taskService.isValidCron(task.getCronExpression())) {
                return Result.fail(ResultCode.BAD_REQUEST, "Cron 表达式格式不正确");
            }
        }

        boolean needReschedule = existing.getStatus() == 1
                && (notEquals(existing.getCronExpression(), task.getCronExpression())
                    || notEquals(existing.getBeanName(), task.getBeanName())
                    || notEquals(existing.getMethodName(), task.getMethodName()));

        task.setId(id);
        task.setStatus(existing.getStatus());
        taskService.updateById(task);

        // 如果关键字段变更且任务已启用，重新注册调度
        if (needReschedule) {
            taskService.disableTask(id);
            taskService.enableTask(id);
        }

        return Result.ok("更新成功", task);
    }

    private boolean notEquals(String a, String b) {
        if (a == null && b == null) return false;
        if (a == null || b == null) return true;
        return !a.equals(b);
    }

    @PreAuthorize("hasAuthority('system:task:edit')")
    @PutMapping("/{id}/enable")
    public Result<Void> enable(@PathVariable Long id) {
        taskService.enableTask(id);
        return Result.okMsg("任务已启用");
    }

    @PreAuthorize("hasAuthority('system:task:edit')")
    @PutMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable Long id) {
        taskService.disableTask(id);
        return Result.okMsg("任务已禁用");
    }

    @PreAuthorize("hasAuthority('system:task:edit')")
    @PostMapping("/{id}/execute")
    public Result<Void> executeNow(@PathVariable Long id) {
        taskService.executeOnce(id);
        return Result.okMsg("任务已触发执行");
    }

    @PreAuthorize("hasAuthority('system:task:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        SysScheduledTask task = taskService.getById(id);
        if (task == null) return Result.fail(ResultCode.NOT_FOUND, "任务不存在");
        // 先从调度器移除，再删除记录
        if (task.getStatus() == 1) {
            taskService.disableTask(id);
        }
        taskService.removeById(id);
        return Result.okMsg("删除成功");
    }

    @GetMapping("/{taskId}/logs")
    public Result<?> getLogs(@PathVariable Long taskId,
                             @RequestParam(defaultValue = "1") int pageNum,
                             @RequestParam(defaultValue = "20") int pageSize) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysScheduledTaskLog>()
                .eq(SysScheduledTaskLog::getTaskId, taskId)
                .orderByDesc(SysScheduledTaskLog::getStartTime);
        return Result.ok(taskLogService.page(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize), wrapper));
    }

    @GetMapping("/validate-cron")
    public Result<?> validateCron(@RequestParam String expression) {
        boolean valid = taskService.isValidCron(expression);
        return valid ? Result.ok("Cron 表达式合法") : Result.fail(ResultCode.BAD_REQUEST, "Cron 表达式格式不正确");
    }
}
