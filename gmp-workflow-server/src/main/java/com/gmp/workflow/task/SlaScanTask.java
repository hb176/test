package com.gmp.workflow.task;

import com.gmp.framework.workflow.ProcessSlaService;
import com.gmp.framework.workflow.ProcessSlaService.SlaAlert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.TaskService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SLA超时扫描任务 — 由定时任务框架调用
 * 扫描所有运行中的任务，检测SLA超时并发送通知/执行自动动作
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SlaScanTask {

    private final ProcessSlaService slaService;
    private final TaskService taskService;

    /**
     * 执行SLA扫描（供定时任务框架调用）
     */
    public void execute() {
        List<SlaAlert> alerts = slaService.checkSlaStatus();
        for (SlaAlert alert : alerts) {
            log.warn("SLA超时: taskId={}, level={}, elapsed={}min, action={}",
                    alert.taskId(), alert.level(), alert.elapsedMinutes(), alert.autoAction());

            if ("AUTO_PASS".equals(alert.autoAction())) {
                try {
                    taskService.complete(alert.taskId());
                    log.info("SLA自动通过: taskId={}", alert.taskId());
                } catch (Exception e) {
                    log.error("SLA自动通过失败: taskId={}", alert.taskId(), e);
                }
            } else if ("AUTO_REJECT".equals(alert.autoAction())) {
                try {
                    taskService.addComment(alert.taskId(), null, "SLA_AUTO_REJECT",
                            "超时自动驳回 - 已超过" + alert.elapsedMinutes() + "分钟");
                    log.info("SLA自动驳回(已添加备注): taskId={}", alert.taskId());
                } catch (Exception e) {
                    log.error("SLA自动驳回失败: taskId={}", alert.taskId(), e);
                }
            }
            // NOTIFY: 由消息中心发送通知（TODO: 集成消息中心）
        }
        if (!alerts.isEmpty()) {
            log.info("SLA扫描完成: 发现{}个超时任务", alerts.size());
        }
    }
}
