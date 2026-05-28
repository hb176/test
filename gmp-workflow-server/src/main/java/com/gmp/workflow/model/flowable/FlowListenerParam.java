package com.gmp.workflow.model.flowable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.workflow.tools.common.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程监听器参数实体
 * <p>
 * 存储监听器的配置参数，与 FlowListener 通过 listenerId 关联。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tbl_flow_listener_param")
public class FlowListenerParam extends BaseModel {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 所属监听器 ID */
    private String listenerId;

    /** 参数名称 */
    private String name;

    /** 参数值 */
    private String value;

    /** 备注说明 */
    private String remark;
}
