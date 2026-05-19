package com.gmp.framework.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 框架自动配置类 - 核心框架的Spring Boot自动装配入口
 * 启用：事务管理、AOP代理、异步处理、组件扫描
 *
 * 使用时在Spring Boot启动类上添加：
 * @Import(FrameworkAutoConfiguration.class) 或
 * 在spring.factories中配置org.springframework.boot.autoconfigure.EnableAutoConfiguration
 *
 * @author hb176
 * @since 1.0.0
 */
@Configuration
// 扫描框架模块的所有组件
@ComponentScan(basePackages = "com.gmp.framework")
// 开启事务管理
@EnableTransactionManagement
// 开启AOP自动代理(exposeProxy=true允许在方法内通过AopContext获取当前代理对象)
@EnableAspectJAutoProxy(exposeProxy = true)
// 开启异步方法支持(配合@Async注解使用)
@EnableAsync
public class FrameworkAutoConfiguration {

    /*
     * 框架核心能力清单：
     * 1. 可配置表单引擎 (FormEngineService) - JSON Schema驱动的动态表单
     * 2. 流程引擎集成 (WorkflowBaseService) - Flowable BPMN 2.0封装
     * 3. 动态多数据源 (DynamicDataSourceConfig) - 运行时切换数据源
     * 4. 自动建表 (TableAutoBuilder) - 基于实体注解自动创建/更新数据库表
     * 5. 分布式锁 (DistributedLockAspect) - 基于Redisson的分布式锁
     * 6. 通用安全配置 (SecurityBaseConfig) - Spring Security基础配置
     * 7. 表达式引擎 (ExpressionEngine) - 支持SpEL和自定义表达式
     */
}
