package com.gmp.business;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * GMP业务模块服务 - DTQ对标：QMS/DMS/TMS/QRS
 *
 * 全部基于"可配置表单+可配置流程"双引擎驱动：
 * - 每种业务类型对应一个表单定义（form_definition）+ 一个流程定义（wf_process_definition）
 * - 业务数据存储在form_data表中（JSON格式）
 * - 业务流程由Flowable引擎驱动
 * - 业务逻辑以可配置规则（校验规则、联动规则、表达式）实现，不硬编码
 *
 * 四大业务模块（对标明度智云Atlas质量管理套件）：
 *
 * QMS（质量过程管理）
 * - 偏差管理 (Deviation)
 * - CAPA管理 (Corrective and Preventive Action)
 * - 变更控制 (Change Control)
 * - 供应商管理 (Supplier Management)
 * - 审计管理 (Audit Management)
 * - 投诉处理 (Complaint Handling)
 * - 风险管理 (Risk Management)
 * - 实验室调查 (Lab Investigation)
 *
 * DMS（文件管理系统）
 * - 文件起草/审核/审批/生效/复审/作废生命周期
 * - 文件打印/发放/回收管理
 * - 文件权限控制（读/写/下载/打印）
 * - 文件培训关联
 * - 21 CFR Part 11电子签名
 *
 * TMS（培训管理系统）
 * - 培训矩阵（岗位→课程映射）
 * - 培训计划制定
 * - 在线/课堂/外部培训管理
 * - 培训效果评估
 * - 资质证书管理
 *
 * QRS（质量回顾系统）
 * - 产品质量定期回顾
 * - 稳定性数据管理
 * - 趋势分析与预警
 * - 统计报表
 *
 * @author hb176
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.gmp.business", "com.gmp.framework", "com.gmp.common"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.gmp.business.mapper")
public class BusinessServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(BusinessServerApplication.class, args);
        System.out.println("GMP Business-Server 启动成功 - QMS/DMS/TMS/QRS (可配置表单+流程驱动)");
    }
}
