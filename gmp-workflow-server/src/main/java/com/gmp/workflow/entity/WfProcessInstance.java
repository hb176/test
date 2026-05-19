package com.gmp.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.framework.base.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 流程实例扩展实体 - 对标ct1.wf_recorder_his_info（原Snaker的Recorder模型）
 * 映射表: wf_process_instance_ext
 *
 * Recorder = 流程实例 = 一次流程运行的完整记录
 * 包含：发起人信息（5维度: 用户+部门+子公司+公司+集团）、流程状态、表单数据快照
 *
 * @author hb176
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_process_instance_ext")
public class WfProcessInstance extends CommonEntity {

    /** 流程实例唯一编码（同原snaker的recorderId） */
    private String recorderId;

    /** 流程实例名称/标题 */
    private String recorderName;

    /** 流程名称 */
    private String workflowName;

    /** 当前状态（APPROVING=审批中, COMPLETED=已完成, REJECTED=已驳回...） */
    private String curStatus;

    /** 流程定义ID */
    private Long processId;

    /** 当前节点名称 */
    private String parentNodeName;

    /** 父流程实例编码（子流程场景） */
    private String parentRecorderId;

    /** 根流程实例编码（追溯最顶层的父流程） */
    private String rootRecorderId;

    /** 表单内容（编辑态数据） */
    private String theContent;

    /** 表单临时内容 */
    private String tmpTheContent;

    /** 流程配置内容（节点定义快照JSON） */
    private String processContent;

    // ========== 发起人信息（5维度） ==========
    /** 发起人ID */
    private String createrId;

    /** 发起人名称 */
    private String createrName;

    /** 发起人部门ID */
    private Long createrDeptId;

    /** 发起人部门编码 */
    private String createrDeptCode;

    /** 发起人部门名称 */
    private String createrDeptName;

    /** 发起人子公司ID */
    private Long createrSubsiId;

    /** 发起人子公司编码 */
    private String createrSubsiCode;

    /** 发起人子公司名称 */
    private String createrSubsiName;

    /** 发起人公司ID */
    private Long createrCompId;

    /** 发起人公司编码 */
    private String createrCompCode;

    /** 发起人公司名称 */
    private String createrCompName;

    // ========== 当前处理部门 ==========
    /** 当前处理部门ID */
    private Long curDeptId;

    /** 当前处理部门编码 */
    private String curDeptCode;

    /** 当前处理部门名称 */
    private String curDeptName;

    // ========== 时间 ==========
    /** 创建日期 */
    private LocalDate createDate;

    /** 过期日期 */
    private LocalDate expireDate;

    // ========== 状态 ==========
    /** 异常关闭状态 */
    private String abnormalCloseStatus;

    /** 附件ID（关联file_accessory） */
    private String accessoryId;

    /** 系统模块编码 */
    private String sysModuleCode;

    // ========== 扩展字段（兼容新Flowable） ==========
    /** Flowable流程实例ID */
    private String flowableProcessInstanceId;

    /** 业务键 */
    private String businessKey;

    /** 关联表单数据ID */
    private Long formDataId;

    /** 关联表单Key */
    private String formKey;
}
