package com.gmp.workflow.model.flowable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.workflow.tools.common.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 流程监听器实体
 * <p>
 * 记录 BPMN 流程中注册的执行监听器和任务监听器，
 * 支持按节点事件类型（create/assignment/complete/delete）配置。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tbl_flow_listener")
public class FlowListener extends BaseModel {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 监听器名称 */
    private String name;

    /** 监听器类型：execution（执行监听器）、task（任务监听器） */
    private String listenerType;

    /** 事件类型：create、assignment、complete、delete、take 等 */
    private String eventType;

    /** 监听器实现类全限定名或 Spring Bean 名称 */
    private String value;

    /** 备注说明 */
    private String remark;

    /** 排序号 */
    private Integer orderNo;

    /**
     * 监听器参数列表（非数据库字段，由服务层填充）
     */
    @TableField(exist = false)
    private List<FlowListenerParam> params;
}
