package com.gmp.workflow.config;

import com.gmp.workflow.listener.GlobalProcistEndListener;
import com.gmp.workflow.listener.GlobalProcistStartListener;
import com.gmp.workflow.listener.GlobalTaskCreateListener;
import lombok.RequiredArgsConstructor;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEventDispatcher;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Flowable 全局监听器注册配置
 * <p>
 * 在 Spring 容器初始化完成后，将全局监听器注册到 Flowable 事件分发器。
 * 监听的事件类型：
 * - PROCESS_STARTED: 流程实例启动（处理子流程扩展信息）
 * - PROCESS_COMPLETED: 流程实例结束（更新状态、清理运行时数据）
 * - TASK_CREATED: 任务创建（预留扩展点）
 * </p>
 */
@Configuration
@RequiredArgsConstructor
public class FlowableGlobalListenerConfig implements ApplicationListener<ContextRefreshedEvent> {

    private final SpringProcessEngineConfiguration configuration;
    private final GlobalProcistStartListener globalProcistStartListener;
    private final GlobalProcistEndListener globalProcistEndListener;
    private final GlobalTaskCreateListener globalTaskCreateListener;

    /**
     * Spring 容器初始化完成后，将全局监听器注册到 Flowable 事件分发器
     * <p>
     * 注册的监听器：
     * - PROCESS_STARTED → GlobalProcistStartListener
     * - PROCESS_COMPLETED → GlobalProcistEndListener
     * - TASK_CREATED → GlobalTaskCreateListener
     * </p>
     *
     * @param event Spring 上下文刷新事件
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        FlowableEventDispatcher dispatcher = configuration.getEventDispatcher();
        // 注册流程实例启动监听
        dispatcher.addEventListener(globalProcistStartListener, FlowableEngineEventType.PROCESS_STARTED);
        // 注册流程实例结束监听
        dispatcher.addEventListener(globalProcistEndListener, FlowableEngineEventType.PROCESS_COMPLETED);
        // 注册任务创建监听
        dispatcher.addEventListener(globalTaskCreateListener, FlowableEngineEventType.TASK_CREATED);
    }
}
