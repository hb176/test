package com.gmp.workflow.service.flowable.impl;

import com.gmp.workflow.service.flowable.IBpmnModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.Activity;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.FlowableListener;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.GraphicInfo;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.ServiceTask;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.RepositoryService;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * BPMN 模型内省服务实现
 * <p>
 * 通过 Flowable 的 RepositoryService 和 BpmnModel API，
 * 提供对 BPMN 流程定义的程序化读取能力。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BpmnModelServiceImpl implements IBpmnModelService {

    private final RepositoryService repositoryService;

    /**
     * 根据流程定义 ID 获取 BpmnModel 对象
     *
     * @param processDefId 流程定义 ID
     * @return BpmnModel，不存在返回 null
     */
    @Override
    public BpmnModel getBpmnModelByProcessDefId(String processDefId) {
        return repositoryService.getBpmnModel(processDefId);
    }

    /**
     * 检查指定活动节点是否位于子流程中
     * <p>
     * 通过在主流程的 Process 列表中查找该 activityId，
     * 如果找不到则说明它位于子流程（SubProcess）内。
     * </p>
     *
     * @param processDefId 流程定义 ID
     * @param activityId   活动节点 ID
     * @return true 表示在子流程中
     */
    @Override
    public boolean checkActivitySubprocessByActivityId(String processDefId, String activityId) {
        List<FlowNode> nodes = findFlowNodesByActivityId(processDefId, activityId);
        return CollectionUtils.isEmpty(nodes);
    }

    /**
     * 在主流程的所有 Process 中查找指定 activityId 的 FlowNode
     */
    private List<FlowNode> findFlowNodesByActivityId(String processDefId, String activityId) {
        List<FlowNode> activities = new ArrayList<>();
        BpmnModel bpmnModel = getBpmnModelByProcessDefId(processDefId);
        if (bpmnModel == null) return activities;

        for (Process process : bpmnModel.getProcesses()) {
            FlowElement flowElement = process.getFlowElement(activityId);
            if (flowElement instanceof FlowNode) {
                activities.add((FlowNode) flowElement);
            }
        }
        return activities;
    }

    /**
     * 检查任务是否为多实例任务
     *
     * @param task 任务实体
     * @return true 表示该任务定义了多实例循环特性
     */
    @Override
    public boolean checkMultiInstance(TaskEntity task) {
        BpmnModel bpmnModel = getBpmnModelByProcessDefId(task.getProcessDefinitionId());
        List<UserTask> userTasks = findUserTasksByBpmnModel(bpmnModel);
        if (CollectionUtils.isNotEmpty(userTasks)) {
            for (UserTask userTask : userTasks) {
                if (userTask.getId().equals(task.getTaskDefinitionKey())
                        && userTask.hasMultiInstanceLoopCharacteristics()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取用户任务节点的监听器实现类名称列表
     *
     * @param activityId          节点 ID
     * @param processDefinitionId 流程定义 ID
     * @return 监听器实现类名称列表
     */
    @Override
    public List<String> getStrUserTaskListeners(String activityId, String processDefinitionId) {
        List<String> listeners = new ArrayList<>();
        List<FlowableListener> taskListeners = getUserTaskListeners(activityId, processDefinitionId);
        if (CollectionUtils.isNotEmpty(taskListeners)) {
            taskListeners.forEach(l -> listeners.add(l.getImplementation()));
        }
        return listeners;
    }

    /**
     * 获取用户任务节点的 FlowableListener 列表
     *
     * @param activityId          节点 ID
     * @param processDefinitionId 流程定义 ID
     * @return FlowableListener 列表，节点不存在或非 UserTask 返回 null
     */
    @Override
    public List<FlowableListener> getUserTaskListeners(String activityId, String processDefinitionId) {
        FlowElement flowElement = getFlowElementByActivityIdAndProcessDefinitionId(activityId, processDefinitionId);
        if (flowElement instanceof UserTask userTask) {
            return userTask.getTaskListeners();
        }
        return null;
    }

    /**
     * 根据活动 ID 和 BpmnModel 查找流程元素
     *
     * @param activityId 活动节点 ID
     * @param bpmnModel  BPMN 模型
     * @return 流程元素，未找到返回 null
     */
    @Override
    public FlowElement getFlowElementByActivityIdAndProcessDefinitionId(String activityId, BpmnModel bpmnModel) {
        if (bpmnModel == null) return null;
        List<Process> processes = bpmnModel.getProcesses();
        if (CollectionUtils.isNotEmpty(processes)) {
            for (Process process : processes) {
                FlowElement flowElement = process.getFlowElement(activityId);
                if (flowElement != null) {
                    return flowElement;
                }
            }
        }
        return null;
    }

    /**
     * 根据活动 ID 和流程定义 ID 查找流程元素
     *
     * @param activityId          活动节点 ID
     * @param processDefinitionId 流程定义 ID
     * @return 流程元素，未找到返回 null
     */
    @Override
    public FlowElement getFlowElementByActivityIdAndProcessDefinitionId(String activityId, String processDefinitionId) {
        BpmnModel bpmnModel = getBpmnModelByProcessDefId(processDefinitionId);
        return getFlowElementByActivityIdAndProcessDefinitionId(activityId, bpmnModel);
    }

    /**
     * 获取用户任务节点的单个自定义扩展属性值（按流程定义 ID）
     *
     * @param activityId          节点 ID
     * @param processDefinitionId 流程定义 ID
     * @param customPropertyName  扩展属性名称
     * @return 属性值，不存在返回 null
     */
    @Override
    public String getSingleCustomProperty(String activityId, String processDefinitionId, String customPropertyName) {
        List<ExtensionElement> customProperty = getCustomProperty(activityId, processDefinitionId, customPropertyName);
        return getSingleCustomPropertyValue(customProperty);
    }

    /**
     * 获取用户任务节点的单个自定义扩展属性值（使用 BpmnModel）
     *
     * @param activityId         节点 ID
     * @param bpmnModel          BPMN 模型
     * @param customPropertyName 扩展属性名称
     * @return 属性值，不存在返回 null
     */
    @Override
    public String getSingleCustomProperty(String activityId, BpmnModel bpmnModel, String customPropertyName) {
        List<ExtensionElement> customProperty = getCustomProperty(activityId, bpmnModel, customPropertyName);
        return getSingleCustomPropertyValue(customProperty);
    }

    /**
     * 从扩展属性元素列表中提取第一个元素的文本值
     */
    private String getSingleCustomPropertyValue(List<ExtensionElement> customProperty) {
        if (CollectionUtils.isNotEmpty(customProperty)) {
            ExtensionElement element = customProperty.get(0);
            if (element != null) {
                return element.getElementText();
            }
        }
        return null;
    }

    /**
     * 获取用户任务节点的自定义扩展属性元素列表（按流程定义 ID）
     *
     * @param activityId          节点 ID
     * @param processDefinitionId 流程定义 ID
     * @param customPropertyName  扩展属性名称
     * @return ExtensionElement 列表
     */
    @Override
    public List<ExtensionElement> getCustomProperty(String activityId, String processDefinitionId, String customPropertyName) {
        FlowElement flowElement = getFlowElementByActivityIdAndProcessDefinitionId(activityId, processDefinitionId);
        return getExtensionElements(flowElement, customPropertyName);
    }

    /**
     * 获取用户任务节点的自定义扩展属性元素列表（使用 BpmnModel）
     *
     * @param activityId         节点 ID
     * @param bpmnModel          BPMN 模型
     * @param customPropertyName 扩展属性名称
     * @return ExtensionElement 列表
     */
    @Override
    public List<ExtensionElement> getCustomProperty(String activityId, BpmnModel bpmnModel, String customPropertyName) {
        FlowElement flowElement = getFlowElementByActivityIdAndProcessDefinitionId(activityId, bpmnModel);
        return getExtensionElements(flowElement, customPropertyName);
    }

    /**
     * 从 FlowElement 中获取指定名称的扩展属性元素列表
     */
    private List<ExtensionElement> getExtensionElements(FlowElement flowElement, String customPropertyName) {
        if (flowElement instanceof UserTask userTask) {
            Map<String, List<ExtensionElement>> extensionElements = userTask.getExtensionElements();
            if (MapUtils.isNotEmpty(extensionElements)) {
                List<ExtensionElement> values = extensionElements.get(customPropertyName);
                if (CollectionUtils.isNotEmpty(values)) {
                    return values;
                }
            }
        }
        return null;
    }

    /**
     * 查找 BpmnModel 中所有的 ServiceTask 节点
     *
     * @param bpmnModel BPMN 模型
     * @return ServiceTask 列表
     */
    @Override
    public List<ServiceTask> findServiceTasksByBpmnModel(BpmnModel bpmnModel) {
        List<ServiceTask> datas = new ArrayList<>();
        if (bpmnModel == null) return datas;
        for (Process process : bpmnModel.getProcesses()) {
            datas.addAll(process.findFlowElementsOfType(ServiceTask.class));
        }
        return datas;
    }

    /**
     * 查找 BpmnModel 中所有的 UserTask 节点
     *
     * @param bpmnModel BPMN 模型
     * @return UserTask 列表
     */
    @Override
    public List<UserTask> findUserTasksByBpmnModel(BpmnModel bpmnModel) {
        List<UserTask> datas = new ArrayList<>();
        if (bpmnModel == null) return datas;
        for (Process process : bpmnModel.getProcesses()) {
            datas.addAll(process.findFlowElementsOfType(UserTask.class));
        }
        return datas;
    }

    /**
     * 根据流程定义 ID 查找所有的 UserTask 节点
     *
     * @param processDefId 流程定义 ID
     * @return UserTask 列表
     */
    @Override
    public List<UserTask> findUserTasksByProcessDefId(String processDefId) {
        BpmnModel bpmnModel = getBpmnModelByProcessDefId(processDefId);
        return findUserTasksByBpmnModel(bpmnModel);
    }

    /**
     * 查找主流程的开始事件节点
     *
     * @param process BPMN Process 对象
     * @return 开始事件节点，不存在返回 null
     */
    @Override
    public StartEvent findStartFlowElement(Process process) {
        List<StartEvent> startEvents = process.findFlowElementsOfType(StartEvent.class);
        return CollectionUtils.isNotEmpty(startEvents) ? startEvents.get(0) : null;
    }

    /**
     * 查找流程定义的所有结束事件节点
     *
     * @param processDefId 流程定义 ID
     * @return 结束事件节点列表
     */
    @Override
    public List<EndEvent> findEndFlowElement(String processDefId) {
        BpmnModel bpmnModel = getBpmnModelByProcessDefId(processDefId);
        if (bpmnModel != null) {
            Process process = bpmnModel.getMainProcess();
            return process.findFlowElementsOfType(EndEvent.class);
        }
        return null;
    }

    /**
     * 通过名称查找活动节点
     *
     * @param processDefId 流程定义 ID
     * @param name         节点名称
     * @return 活动节点，未找到返回 null
     */
    @Override
    public Activity findActivityByName(String processDefId, String name) {
        if (StringUtils.isBlank(name)) return null;
        BpmnModel bpmnModel = getBpmnModelByProcessDefId(processDefId);
        if (bpmnModel == null) return null;
        Process process = bpmnModel.getMainProcess();
        for (FlowElement f : process.getFlowElements()) {
            if (name.equals(f.getName()) && f instanceof Activity activity) {
                return activity;
            }
        }
        return null;
    }

    /**
     * 通过 ID 查找活动节点
     *
     * @param processDefId 流程定义 ID
     * @param activityId   节点 ID
     * @return 活动节点，未找到返回 null
     */
    @Override
    public Activity findActivityById(String processDefId, String activityId) {
        if (StringUtils.isBlank(activityId)) return null;
        BpmnModel bpmnModel = getBpmnModelByProcessDefId(processDefId);
        return findActivityByBpmnModelAndId(bpmnModel, activityId);
    }

    /**
     * 通过 BpmnModel 和 ID 查找活动节点
     *
     * @param bpmnModel  BPMN 模型
     * @param activityId 节点 ID
     * @return 活动节点，未找到返回 null
     */
    @Override
    public Activity findActivityByBpmnModelAndId(BpmnModel bpmnModel, String activityId) {
        if (bpmnModel == null || StringUtils.isBlank(activityId)) return null;
        Process process = bpmnModel.getMainProcess();
        for (FlowElement f : process.getFlowElements()) {
            if (activityId.equals(f.getId()) && f instanceof Activity activity) {
                return activity;
            }
        }
        return null;
    }

    /**
     * 根据 ID 列表查找流程元素
     *
     * @param bpmnModel   BPMN 模型
     * @param activityIds 节点 ID 列表
     * @return 匹配的流程元素列表
     */
    @Override
    public List<FlowElement> findFlowElementByIds(BpmnModel bpmnModel, List<String> activityIds) {
        List<FlowElement> flowElements = new ArrayList<>();
        if (bpmnModel == null || CollectionUtils.isEmpty(activityIds)) return flowElements;
        Process process = bpmnModel.getMainProcess();
        for (FlowElement f : process.getFlowElements()) {
            if (activityIds.contains(f.getId())) {
                flowElements.add(f);
            }
        }
        return flowElements;
    }

    /**
     * 查找流程定义中的所有 FlowNode（包括所有类型的节点）
     *
     * @param processDefId 流程定义 ID
     * @return FlowNode 列表
     */
    @Override
    public List<FlowNode> findFlowNodes(String processDefId) {
        List<FlowNode> flowNodes = new ArrayList<>();
        BpmnModel bpmnModel = getBpmnModelByProcessDefId(processDefId);
        if (bpmnModel == null) return flowNodes;
        Process process = bpmnModel.getMainProcess();
        for (FlowElement f : process.getFlowElements()) {
            if (f instanceof FlowNode flowNode) {
                flowNodes.add(flowNode);
            }
        }
        return flowNodes;
    }

    /**
     * 获取节点的图形坐标信息（用于流程图渲染）
     *
     * @param bpmnModel  BPMN 模型
     * @param activityId 节点 ID
     * @return GraphicInfo 包含 x, y, width, height，不存在返回 null
     */
    @Override
    public GraphicInfo getGraphicInfo(BpmnModel bpmnModel, String activityId) {
        return bpmnModel != null ? bpmnModel.getGraphicInfo(activityId) : null;
    }
}
