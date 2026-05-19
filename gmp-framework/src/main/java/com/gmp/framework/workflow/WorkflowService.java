package com.gmp.framework.workflow;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

/**
 * 工作流服务 — 仅在 Flowable 存在时加载
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@Service
@ConditionalOnClass(name = "org.flowable.engine.ProcessEngine")
public class WorkflowService extends WorkflowBaseService {

    public WorkflowService(RepositoryService repositoryService,
                           RuntimeService runtimeService,
                           TaskService taskService,
                           HistoryService historyService,
                           ManagementService managementService) {
        super(repositoryService, runtimeService, taskService, historyService, managementService);
    }
}
