package com.gmp.workflow.service.flowable;

import com.gmp.workflow.vo.flowable.model.HighLightedNodeVo;
import com.gmp.workflow.vo.flowable.task.ActivityVo;

import java.util.List;

/**
 * 流程图可视化服务接口
 * <p>
 * 提供流程实例的高亮节点、活动节点详情、流程轨迹等可视化数据，
 * 用于前端流程图组件的渲染和流程追踪。
 * </p>
 */
public interface IFlowProcessDiagramService {

    /**
     * 生成并缓存流程实例的高亮节点信息
     *
     * @param processInstanceId 流程实例 ID
     * @return 高亮节点信息，包含已执行的流程线和当前活跃节点
     */
    HighLightedNodeVo createCacheHighLightedNodeVoByProcessInstanceId(String processInstanceId);

    /**
     * 获取流程实例的高亮节点信息（缓存优先）
     *
     * @param processInstanceId 流程实例 ID
     * @return 高亮节点信息
     */
    HighLightedNodeVo getHighLightedNodeVoByProcessInstanceId(String processInstanceId);

    /**
     * 获取流程实例中指定活动节点的详细信息
     *
     * @param processInstanceId 流程实例 ID
     * @param activityId        活动节点 ID（即 taskDefinitionKey）
     * @return 活动节点详情，包含坐标、状态、审批人、时间等
     */
    ActivityVo getOneActivityVoByProcessInstanceIdAndActivityId(String processInstanceId, String activityId);

    /**
     * 获取流程实例中所有活动节点的详细信息
     *
     * @param processInstanceId 流程实例 ID
     * @return 所有活动节点详情列表
     */
    List<ActivityVo> getProcessActivityVosByProcessInstanceId(String processInstanceId);
}
