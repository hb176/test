package com.gmp.workflow.service.flowable;

import org.flowable.bpmn.model.Activity;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.FlowableListener;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.GraphicInfo;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.ServiceTask;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;

import java.util.List;

/**
 * BPMN 模型内省服务接口
 * <p>
 * 提供对 BPMN 模型的程序化读取能力，包括节点查找、属性读取、
 * 监听器查询、多实例检测等。是流程图可视化和动态审批人解析的基础。
 * </p>
 */
public interface IBpmnModelService {

    /**
     * 根据流程定义 ID 获取 BpmnModel
     *
     * @param processDefId 流程定义 ID
     * @return BpmnModel 对象
     */
    BpmnModel getBpmnModelByProcessDefId(String processDefId);

    /**
     * 检查指定活动节点是否位于子流程中
     *
     * @param processDefId 流程定义 ID
     * @param activityId   活动节点 ID
     * @return true 表示在子流程中，false 表示在主流程中
     */
    boolean checkActivitySubprocessByActivityId(String processDefId, String activityId);

    /**
     * 检查任务是否为多实例任务
     *
     * @param task 任务实体
     * @return true 表示是多实例任务
     */
    boolean checkMultiInstance(TaskEntity task);

    /**
     * 获取用户任务节点的监听器名称列表
     *
     * @param activityId          节点 ID
     * @param processDefinitionId 流程定义 ID
     * @return 监听器实现类名称列表
     */
    List<String> getStrUserTaskListeners(String activityId, String processDefinitionId);

    /**
     * 获取用户任务节点的监听器列表
     *
     * @param activityId          节点 ID
     * @param processDefinitionId 流程定义 ID
     * @return FlowableListener 列表
     */
    List<FlowableListener> getUserTaskListeners(String activityId, String processDefinitionId);

    /**
     * 根据活动 ID 和 BpmnModel 查找流程元素
     *
     * @param activityId 活动节点 ID
     * @param bpmnModel  BPMN 模型
     * @return 流程元素，未找到返回 null
     */
    FlowElement getFlowElementByActivityIdAndProcessDefinitionId(String activityId, BpmnModel bpmnModel);

    /**
     * 根据活动 ID 和流程定义 ID 查找流程元素
     *
     * @param activityId          活动节点 ID
     * @param processDefinitionId 流程定义 ID
     * @return 流程元素，未找到返回 null
     */
    FlowElement getFlowElementByActivityIdAndProcessDefinitionId(String activityId, String processDefinitionId);

    /**
     * 获取用户任务节点的单个自定义扩展属性值
     *
     * @param activityId          节点 ID
     * @param processDefinitionId 流程定义 ID
     * @param customPropertyName  属性名称
     * @return 属性值，不存在返回 null
     */
    String getSingleCustomProperty(String activityId, String processDefinitionId, String customPropertyName);

    /**
     * 获取用户任务节点的单个自定义扩展属性值（使用 BpmnModel）
     *
     * @param activityId         节点 ID
     * @param bpmnModel          BPMN 模型
     * @param customPropertyName 属性名称
     * @return 属性值，不存在返回 null
     */
    String getSingleCustomProperty(String activityId, BpmnModel bpmnModel, String customPropertyName);

    /**
     * 获取用户任务节点的自定义扩展属性元素列表
     *
     * @param activityId          节点 ID
     * @param processDefinitionId 流程定义 ID
     * @param customPropertyName  属性名称
     * @return ExtensionElement 列表
     */
    List<ExtensionElement> getCustomProperty(String activityId, String processDefinitionId, String customPropertyName);

    /**
     * 获取用户任务节点的自定义扩展属性元素列表（使用 BpmnModel）
     *
     * @param activityId         节点 ID
     * @param bpmnModel          BPMN 模型
     * @param customPropertyName 属性名称
     * @return ExtensionElement 列表
     */
    List<ExtensionElement> getCustomProperty(String activityId, BpmnModel bpmnModel, String customPropertyName);

    /**
     * 查找 BpmnModel 中所有的 ServiceTask
     *
     * @param bpmnModel BPMN 模型
     * @return ServiceTask 列表
     */
    List<ServiceTask> findServiceTasksByBpmnModel(BpmnModel bpmnModel);

    /**
     * 查找 BpmnModel 中所有的 UserTask
     *
     * @param bpmnModel BPMN 模型
     * @return UserTask 列表
     */
    List<UserTask> findUserTasksByBpmnModel(BpmnModel bpmnModel);

    /**
     * 根据流程定义 ID 查找所有的 UserTask
     *
     * @param processDefId 流程定义 ID
     * @return UserTask 列表
     */
    List<UserTask> findUserTasksByProcessDefId(String processDefId);

    /**
     * 查找主流程的开始事件节点
     *
     * @param process BPMN Process 对象
     * @return 开始事件节点，不存在返回 null
     */
    StartEvent findStartFlowElement(Process process);

    /**
     * 查找流程定义的所有结束事件节点
     *
     * @param processDefId 流程定义 ID
     * @return 结束事件节点列表
     */
    List<EndEvent> findEndFlowElement(String processDefId);

    /**
     * 通过名称查找活动节点
     *
     * @param processDefId 流程定义 ID
     * @param name         节点名称
     * @return 活动节点，未找到返回 null
     */
    Activity findActivityByName(String processDefId, String name);

    /**
     * 通过 ID 查找活动节点
     *
     * @param processDefId 流程定义 ID
     * @param activityId   节点 ID
     * @return 活动节点，未找到返回 null
     */
    Activity findActivityById(String processDefId, String activityId);

    /**
     * 通过 BpmnModel 和 ID 查找活动节点
     *
     * @param bpmnModel  BPMN 模型
     * @param activityId 节点 ID
     * @return 活动节点，未找到返回 null
     */
    Activity findActivityByBpmnModelAndId(BpmnModel bpmnModel, String activityId);

    /**
     * 根据 ID 列表查找流程元素
     *
     * @param bpmnModel   BPMN 模型
     * @param activityIds 节点 ID 列表
     * @return 匹配的流程元素列表
     */
    List<FlowElement> findFlowElementByIds(BpmnModel bpmnModel, List<String> activityIds);

    /**
     * 查找流程定义中的所有 FlowNode
     *
     * @param processDefId 流程定义 ID
     * @return FlowNode 列表
     */
    List<FlowNode> findFlowNodes(String processDefId);

    /**
     * 获取节点的图形坐标信息（用于流程图渲染）
     *
     * @param bpmnModel  BPMN 模型
     * @param activityId 节点 ID
     * @return GraphicInfo 包含 x, y, width, height
     */
    GraphicInfo getGraphicInfo(BpmnModel bpmnModel, String activityId);
}
