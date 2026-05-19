package com.gmp.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.framework.base.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务记录实体 - DTQ业务操作的统一记录
 * 映射表: business_record
 *
 * 设计理念：
 * 所有业务类型（QMS偏差/CAPA/变更、DMS文件、TMS培训等）共用此表
 * 通过 businessType 区分业务类型，通过 formDataId 关联表单数据
 * 通过 processInstanceExtId 关联流程实例
 *
 * 这种设计实现了"可配置"的核心目标：
 * - 新增业务类型无需新建数据库表
 * - 业务字段完全由表单Schema定义
 * - 业务流程完全由流程定义控制
 * - 业务规则完全由配置驱动
 *
 * @author hb176
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("business_record")
public class BusinessRecord extends CommonEntity {

    /** 业务类型: QMS_DEVIATION/QMS_CAPA/QMS_CHANGE/DMS_DOCUMENT/TMS_COURSE/QRS_REVIEW... */
    private String businessType;

    /** 业务编号（自动生成，如 DEV-2026-001, CAPA-2026-001） */
    private String businessNo;

    /** 业务标题 */
    private String title;

    /** 业务状态（与流程状态同步） */
    private String businessStatus;

    /** 关联的表单定义Key */
    private String formKey;

    /** 关联的表单数据ID */
    private Long formDataId;

    /** 关联的流程实例扩展ID */
    private Long processInstanceExtId;

    /** 关联的流程实例ID */
    private String processInstanceId;

    /** 发起人ID */
    private Long initiatorId;

    /** 发起人用户名 */
    private String initiatorName;

    /** 发起人部门ID */
    private Long initiatorDeptId;

    /** 发起时间 */
    private java.time.LocalDateTime initiatedAt;

    /** 完成时间 */
    private java.time.LocalDateTime completedAt;

    /** 紧急程度: NORMAL/URGENT/CRITICAL */
    private String urgency;

    /** 关联的上级业务记录ID（如CAPA关联到偏差） */
    private Long parentBusinessId;

    /** 关联的产品ID */
    private Long productId;

    /** 关联的产品名称 */
    private String productName;

    /** 关联的批次号 */
    private String batchNo;

    /** 业务标签（JSON数组，用于分类检索） */
    private String tags;

    /** 业务摘要（从表单数据中提取的关键信息，用于列表展示） */
    private String summary;
}
