package com.gmp.workflow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * GMP流程管理服务 - 基于Flowable BPMN 2.0的工作流引擎
 *
 * 核心能力：
 * 1. 流程定义管理：BPMN XML的上传/解析/部署/版本管理
 * 2. 流程实例管理：启动、挂起、激活、终止、跳转
 * 3. 任务管理：待办/已办查询、认领、办理、驳回、委派、转办、加签
 * 4. 历史查询：审批轨迹、已办任务、流转记录
 * 5. 表单绑定：流程节点绑定表单定义（流程启动时自动加载对应表单）
 * 6. 动态节点分配：基于表达式/角色的动态审批人分配
 * 7. 流程监控：运行中/已完成流程的统计与监控
 * 8. 流程变量：运行时变量存取，支持序列化Java对象
 *
 * 与原sisqp系统Snaker引擎的对比：
 * - Snaker: 轻量级XML流程引擎（项目已停止维护）
 * - Flowable: BPMN 2.0标准引擎（Activiti的官方继承者，活跃维护）
 * - Flowable优势: 标准化BPMN、更好的性能、更丰富的API、微服务支持
 *
 * @author hb176
 * @since 1.0.0
 */
@EnableCaching
@SpringBootApplication(scanBasePackages = {"com.gmp.workflow", "com.gmp.framework", "com.gmp.common"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.gmp.workflow.mapper")
public class WorkflowServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkflowServerApplication.class, args);
        System.out.println("GMP Workflow-Server 启动成功 - Flowable BPMN 2.0流程引擎");
    }
}
