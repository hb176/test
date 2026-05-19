package com.gmp.form.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.framework.base.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 表单数据实体 - 存储基于表单定义提交的业务数据
 * 映射表: form_data
 *
 * 存储策略：
 * 1. formData (JSON): 完整表单数据，灵活存储所有字段值
 * 2. indexed_* 列: 为需要查询的字段建索引列（如标题、编号、提交人等）
 *    由FormDefinition.formConfig中的indexedFields配置决定
 * 3. 关联信息: 记录所属表单定义、版本、流程实例等
 *
 * 查询策略：indexed_* 列用于SQL查询条件，formData用于获取完整数据
 *
 * @author hb176
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("form_data")
public class FormData extends CommonEntity {

    /** 所属表单定义ID（关联form_definition表） */
    private Long formDefId;

    /** 表单Key（冗余，方便查询） */
    private String formKey;

    /** 创建时使用的表单版本号 */
    private Integer formVersion;

    /** 表单数据（JSON格式，完整业务数据） */
    private String formData;

    /** 业务键（用于关联外部业务对象） */
    private String businessKey;

    /** 关联的流程实例ID（通过流程提交的表单数据） */
    private String processInstanceId;

    /** 数据状态: DRAFT=草稿, SUBMITTED=已提交, APPROVED=已批准, REJECTED=已驳回 */
    private String dataStatus;

    /** 提交人ID */
    private Long submitUserId;

    /** 提交人用户名 */
    private String submitUserName;

    /** 提交时间 */
    private java.time.LocalDateTime submitTime;

    // ==================== 索引字段（用于SQL查询） ====================
    /** 索引字段1：通常存储标题/名称等主标识字段 */
    private String indexedTitle;

    /** 索引字段2：通常存储编号/编码 */
    private String indexedCode;

    /** 索引字段3：业务扩展索引 */
    private String indexedField1;

    /** 索引字段4：业务扩展索引 */
    private String indexedField2;

    /** 索引字段5：业务扩展索引 */
    private String indexedField3;
}
