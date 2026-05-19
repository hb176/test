package com.gmp.framework.workflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流程SLA超时管理 - 监控流程节点处理时间，超时自动升级/通知
 *
 * SLA配置（在流程定义中配置每个节点的时限）：
 * - warningThreshold: 预警阈值（如任务创建后2小时未处理→发送预警通知）
 * - escalationThreshold: 升级阈值（如4小时未处理→通知上级）
 * - autoAction: 超时自动处理策略（AUTO_PASS=自动通过, AUTO_REJECT=自动驳回, NOTIFY=仅通知）
 *
 * 使用场景（对应DTQ系统）：
 * - 偏差处理必须在24小时内响应
 * - CAPA措施必须在7天内完成
 * - 文件审批每个节点不得超过48小时
 * - 超时自动升级到质量负责人
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@Component
public class ProcessSlaService {

    /**
     * SLA配置（内存存储，生产环境应从数据库/Nacos动态加载）
     * Key: processKey_nodeKey, Value: SLA配置
     */
    private final Map<String, SlaConfig> slaConfigs = new ConcurrentHashMap<>();

    /**
     * 任务开始时间记录（内存存储）
     * Key: taskId, Value: 任务创建时间
     */
    private final Map<String, TaskTimer> taskTimers = new ConcurrentHashMap<>();

    /**
     * SLA配置
     */
    public record SlaConfig(
            /** 预警阈值（分钟） */
            int warningMinutes,
            /** 升级阈值（分钟） */
            int escalationMinutes,
            /** 超时动作: AUTO_PASS/AUTO_REJECT/NOTIFY */
            String autoAction,
            /** 通知接收人角色编码 */
            String notifyRoleCode
    ) {}

    /**
     * 任务计时器
     */
    private record TaskTimer(LocalDateTime startTime, String processKey, String nodeKey) {
        /** 计算已用分钟数 */
        long elapsedMinutes() {
            return Duration.between(startTime, LocalDateTime.now()).toMinutes();
        }
    }

    /**
     * 注册SLA配置
     */
    public void registerSla(String processKey, String nodeKey, SlaConfig config) {
        String key = processKey + "_" + nodeKey;
        slaConfigs.put(key, config);
        log.info("SLA配置已注册 - processKey: {}, nodeKey: {}, warning: {}min, escalation: {}min",
                processKey, nodeKey, config.warningMinutes, config.escalationMinutes);
    }

    /**
     * 任务开始时记录（任务创建时调用）
     */
    public void onTaskCreated(String taskId, String processKey, String nodeKey) {
        taskTimers.put(taskId, new TaskTimer(LocalDateTime.now(), processKey, nodeKey));
        log.debug("任务SLA计时开始 - taskId: {}, nodeKey: {}", taskId, nodeKey);
    }

    /**
     * 任务完成时清理（任务办理完成时调用）
     */
    public void onTaskCompleted(String taskId) {
        TaskTimer timer = taskTimers.remove(taskId);
        if (timer != null) {
            long elapsed = timer.elapsedMinutes();
            log.debug("任务SLA计时结束 - taskId: {}, 耗时: {} 分钟", taskId, elapsed);
        }
    }

    /**
     * 检查任务是否超时（定时任务调用，如每5分钟扫描一次）
     *
     * @return 需要通知/升级的任务列表
     */
    public List<SlaAlert> checkSlaStatus() {
        List<SlaAlert> alerts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Map.Entry<String, TaskTimer> entry : taskTimers.entrySet()) {
            String taskId = entry.getKey();
            TaskTimer timer = entry.getValue();
            String slaKey = timer.processKey + "_" + timer.nodeKey;
            SlaConfig config = slaConfigs.get(slaKey);

            if (config == null) {
                continue; // 未配置SLA，跳过
            }

            long elapsed = timer.elapsedMinutes();

            if (elapsed >= config.escalationMinutes) {
                // 超过升级阈值 → 执行超时动作
                alerts.add(new SlaAlert(taskId, timer.processKey, timer.nodeKey,
                        SlaAlert.Level.ESCALATION, elapsed, config.autoAction));
            } else if (elapsed >= config.warningMinutes) {
                // 超过预警阈值 → 发送预警
                alerts.add(new SlaAlert(taskId, timer.processKey, timer.nodeKey,
                        SlaAlert.Level.WARNING, elapsed, null));
            }
        }

        if (!alerts.isEmpty()) {
            log.warn("SLA超时检查发现 {} 个超时任务", alerts.size());
        }
        return alerts;
    }

    /**
     * 获取所有运行中的任务列表及耗时
     */
    public Map<String, Long> getTaskElapsedTimes() {
        Map<String, Long> times = new LinkedHashMap<>();
        taskTimers.forEach((taskId, timer) -> times.put(taskId, timer.elapsedMinutes()));
        return times;
    }

    /**
     * SLA告警信息
     */
    public record SlaAlert(
            String taskId,
            String processKey,
            String nodeKey,
            Level level,
            long elapsedMinutes,
            String autoAction
    ) {
        public enum Level { WARNING, ESCALATION }
    }
}
