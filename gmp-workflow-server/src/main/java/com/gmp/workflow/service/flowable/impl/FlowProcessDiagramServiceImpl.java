package com.gmp.workflow.service.flowable.impl;

import com.gmp.workflow.constant.FlowConstant;
import com.gmp.workflow.enums.flowable.task.NodeStatusEnum;
import com.gmp.workflow.enums.flowable.task.NodeTypeEnum;
import com.gmp.workflow.exception.FlowException;
import com.gmp.workflow.model.flowable.ExtendHisprocinst;
import com.gmp.workflow.service.flowable.IBpmnModelService;
import com.gmp.workflow.service.flowable.IExtendHisprocinstService;
import com.gmp.workflow.service.flowable.IFlowProcessDiagramService;
import com.gmp.workflow.tools.utils.DurationUtils;
import com.gmp.workflow.vo.flowable.model.HighLightedNodeVo;
import com.gmp.workflow.vo.flowable.task.ActivityVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.constants.BpmnXMLConstants;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.Activity;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.GraphicInfo;
import org.flowable.bpmn.model.MultiInstanceLoopCharacteristics;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.impl.de.odysseus.el.misc.TypeConverter;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程图可视化服务实现
 * <p>
 * 基于 Flowable BpmnModel 和历史数据，生成流程图所需的高亮节点、
 * 活动节点详情等可视化信息。使用 Spring Cache 缓存结果以提升性能。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowProcessDiagramServiceImpl implements IFlowProcessDiagramService {

    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final RepositoryService repositoryService;
    private final IExtendHisprocinstService extendHisprocinstService;
    private final IBpmnModelService bpmnModelService;
    private final CacheManager cacheManager;

    /**
     * 强制刷新并缓存流程实例的高亮节点信息
     * <p>
     * 无论缓存中是否存在数据，都会重新查询并覆盖缓存。
     * </p>
     *
     * @param processInstanceId 流程实例 ID
     * @return 高亮节点信息
     */
    @Override
    public HighLightedNodeVo createCacheHighLightedNodeVoByProcessInstanceId(String processInstanceId) {
        HighLightedNodeVo highLightedNodeVo = findHighLightedNodeVoByProcessInstanceId(processInstanceId);
        Cache cache = cacheManager.getCache(FlowConstant.CACHE_PROCESS_HIGHLIGHTEDNODES);
        if (cache != null) {
            cache.put(processInstanceId, highLightedNodeVo);
        }
        return highLightedNodeVo;
    }

    /**
     * 获取流程实例的高亮节点信息（缓存优先）
     * <p>
     * 优先从 Spring Cache 读取，缓存未命中时查询数据库并写入缓存。
     * 返回值包含：已执行的顺序流、当前活跃节点、BPMN XML、流程名称。
     * </p>
     *
     * @param processInstanceId 流程实例 ID
     * @return 高亮节点信息
     */
    @Override
    public HighLightedNodeVo getHighLightedNodeVoByProcessInstanceId(String processInstanceId) {
        Cache cache = cacheManager.getCache(FlowConstant.CACHE_PROCESS_HIGHLIGHTEDNODES);
        if (cache != null) {
            Cache.ValueWrapper valueWrapper = cache.get(processInstanceId);
            if (valueWrapper != null && valueWrapper.get() instanceof HighLightedNodeVo vo) {
                return vo;
            }
        }
        HighLightedNodeVo highLightedNodeVo = findHighLightedNodeVoByProcessInstanceId(processInstanceId);
        if (cache != null) {
            cache.put(processInstanceId, highLightedNodeVo);
        }
        return highLightedNodeVo;
    }

    /**
     * 构建高亮节点信息：查询已执行的流程线、当前活跃节点、BPMN XML
     */
    private HighLightedNodeVo findHighLightedNodeVoByProcessInstanceId(String processInstanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();

        List<String> activeActivityIds = new ArrayList<>();
        List<String> highLightedFlows = new ArrayList<>();

        // 查询已执行的顺序流
        List<HistoricActivityInstance> historicSequenceFlows = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .activityType(BpmnXMLConstants.ELEMENT_SEQUENCE_FLOW).list();
        for (HistoricActivityInstance h : historicSequenceFlows) {
            highLightedFlows.add(h.getActivityId());
        }

        String processDefinitionId;
        String modelName;

        if (processInstance == null) {
            // 流程已结束，从历史扩展表获取信息
            ExtendHisprocinst extendHisprocinst = extendHisprocinstService
                    .findExtendHisprocinstByProcessInstanceId(processInstanceId);
            if (extendHisprocinst == null) {
                throw new FlowException("通过流程实例ID【" + processInstanceId + "】未找到扩展信息");
            }
            processDefinitionId = extendHisprocinst.getProcessDefinitionId();
            // 已结束的流程，用结束事件作为"活跃"节点
            List<HistoricActivityInstance> historicEnds = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .activityType(BpmnXMLConstants.ELEMENT_EVENT_END).list();
            for (HistoricActivityInstance h : historicEnds) {
                activeActivityIds.add(h.getActivityId());
            }
            modelName = extendHisprocinst.getProcessName();
        } else {
            processDefinitionId = processInstance.getProcessDefinitionId();
            activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);
            modelName = processInstance.getName();
        }

        // 获取 BPMN XML
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        String modelXml = convertBpmnModelToXml(bpmnModel);

        return new HighLightedNodeVo(highLightedFlows, activeActivityIds, modelXml, modelName);
    }

    /**
     * 将 BpmnModel 转换为 XML 字符串
     */
    private String convertBpmnModelToXml(BpmnModel bpmnModel) {
        try {
            BpmnXMLConverter converter = new BpmnXMLConverter();
            byte[] xmlBytes = converter.convertToXML(bpmnModel);
            return new String(xmlBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("BpmnModel 转 XML 失败", e);
            return "";
        }
    }

    /**
     * 获取单个活动节点的详细信息（缓存优先）
     * <p>
     * 根据流程实例 ID 和活动节点 ID，返回节点的坐标、状态、审批人、耗时等信息。
     * 支持三种状态：未开始（PENDING）、进行中（PROCESSING）、已完成（FINISH）。
     * </p>
     *
     * @param processInstanceId 流程实例 ID
     * @param activityId        活动节点 ID（taskDefinitionKey）
     * @return 活动节点详情，不存在返回 null
     */
    @Override
    public ActivityVo getOneActivityVoByProcessInstanceIdAndActivityId(String processInstanceId, String activityId) {
        if (StringUtils.isBlank(processInstanceId) || StringUtils.isBlank(activityId)) {
            return null;
        }

        // 缓存优先
        Cache cache = cacheManager.getCache(FlowConstant.CACHE_PROCESS_ACTIVITYS);
        String cacheKey = processInstanceId + "-" + activityId;
        if (cache != null) {
            Cache.ValueWrapper valueWrapper = cache.get(cacheKey);
            if (valueWrapper != null && valueWrapper.get() instanceof ActivityVo vo) {
                return vo;
            }
        }

        // 查询历史任务实例
        List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .taskDefinitionKey(activityId)
                .orderByTaskCreateTime().desc().list();

        ExtendHisprocinst extendHisprocinst = extendHisprocinstService
                .findExtendHisprocinstByProcessInstanceId(processInstanceId);
        if (extendHisprocinst == null) {
            return null;
        }

        // 优先取未结束的任务实例
        HistoricTaskInstance historicTaskInstance = null;
        if (CollectionUtils.isNotEmpty(historicTaskInstances)) {
            for (HistoricTaskInstance hisTask : historicTaskInstances) {
                if (hisTask.getEndTime() == null) {
                    historicTaskInstance = hisTask;
                    break;
                }
            }
            if (historicTaskInstance == null) {
                historicTaskInstance = historicTaskInstances.get(0);
            }
        }

        BpmnModel bpmnModel = bpmnModelService.getBpmnModelByProcessDefId(extendHisprocinst.getProcessDefinitionId());
        Activity activity = bpmnModelService.findActivityByBpmnModelAndId(bpmnModel, activityId);

        ActivityVo vo = null;
        if (activity instanceof UserTask userTask) {
            if (historicTaskInstance == null) {
                // 未开始的节点
                vo = buildUnstartedTaskNodeInfo(userTask, bpmnModel, extendHisprocinst);
                vo.setStatus(NodeStatusEnum.PENDING.getDescription());
            } else {
                if (checkUserTaskInExecutionPath(processInstanceId, bpmnModel, userTask)) {
                    vo = buildUserTaskVo(historicTaskInstances, historicTaskInstance, userTask, bpmnModel, extendHisprocinst);
                } else {
                    vo = buildUnstartedTaskNodeInfo(userTask, bpmnModel, extendHisprocinst);
                    vo.setStatus(NodeStatusEnum.PENDING.getDescription());
                }
            }
        }

        if (cache != null && vo != null) {
            cache.put(cacheKey, vo);
        }
        return vo;
    }

    /**
     * 检查 UserTask 是否在流程执行路径上（通过已执行的顺序流判断）
     */
    private boolean checkUserTaskInExecutionPath(String processInstanceId, BpmnModel bpmnModel, UserTask userTask) {
        List<HistoricActivityInstance> sequenceFlows = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .activityType(BpmnXMLConstants.ELEMENT_SEQUENCE_FLOW).list();
        List<String> sequenceFlowIds = sequenceFlows.stream()
                .map(HistoricActivityInstance::getActivityId).collect(Collectors.toList());
        List<FlowElement> flowElements = bpmnModelService.findFlowElementByIds(bpmnModel, sequenceFlowIds);
        for (FlowElement flowElement : flowElements) {
            if (flowElement instanceof SequenceFlow sequenceFlow) {
                FlowElement source = sequenceFlow.getSourceFlowElement();
                FlowElement target = sequenceFlow.getTargetFlowElement();
                if (userTask.getId().equals(source.getId()) || userTask.getId().equals(target.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取流程实例的所有活动节点列表（缓存优先）
     * <p>
     * 遍历 BPMN 模型中的所有 UserTask，结合历史任务实例，
     * 为每个节点生成包含状态、审批人、坐标等信息的 ActivityVo。
     * </p>
     *
     * @param processInstanceId 流程实例 ID
     * @return 活动节点列表
     */
    @Override
    public List<ActivityVo> getProcessActivityVosByProcessInstanceId(String processInstanceId) {
        // 缓存优先
        Cache cache = cacheManager.getCache(FlowConstant.CACHE_PROCESS_ACTIVITYS);
        if (cache != null) {
            Cache.ValueWrapper valueWrapper = cache.get(processInstanceId);
            if (valueWrapper != null && valueWrapper.get() instanceof List<?> list) {
                @SuppressWarnings("unchecked")
                List<ActivityVo> cached = (List<ActivityVo>) list;
                return cached;
            }
        }

        ExtendHisprocinst extendHisprocinst = extendHisprocinstService
                .findExtendHisprocinstByProcessInstanceId(processInstanceId);
        if (extendHisprocinst == null) {
            throw new FlowException("通过流程实例ID【" + processInstanceId + "】未找到扩展信息");
        }

        BpmnModel bpmnModel = bpmnModelService.getBpmnModelByProcessDefId(extendHisprocinst.getProcessDefinitionId());
        List<UserTask> userTasks = bpmnModelService.findUserTasksByBpmnModel(bpmnModel);

        List<ActivityVo> datas = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(userTasks)) {
            // 获取所有历史任务实例，按 taskDefinitionKey 分组
            List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(processInstanceId).list();
            Map<String, HistoricTaskInstance> taskMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(historicTaskInstances)) {
                taskMap = historicTaskInstances.stream()
                        .collect(Collectors.toMap(HistoricTaskInstance::getTaskDefinitionKey,
                                t -> t, (t1, t2) -> t2));
            }

            for (UserTask userTask : userTasks) {
                ActivityVo vo;
                if (!taskMap.containsKey(userTask.getId())) {
                    vo = buildUnstartedTaskNodeInfo(userTask, bpmnModel, extendHisprocinst);
                    vo.setStatus(NodeStatusEnum.PENDING.getDescription());
                } else {
                    HistoricTaskInstance hti = taskMap.get(userTask.getId());
                    vo = buildUserTaskVo(historicTaskInstances, hti, userTask, bpmnModel, extendHisprocinst);
                }
                datas.add(vo);
            }
        }

        if (cache != null) {
            cache.put(processInstanceId, datas);
        }
        return datas;
    }

    /**
     * 构建已开始/已完成的 UserTask 节点信息
     */
    private ActivityVo buildUserTaskVo(List<HistoricTaskInstance> allTasks,
                                       HistoricTaskInstance currentTask,
                                       UserTask userTask, BpmnModel bpmnModel,
                                       ExtendHisprocinst extendHisprocinst) {
        ActivityVo vo;

        if (currentTask.getEndTime() == null) {
            // 进行中的节点
            vo = buildUnstartedTaskNodeInfo(userTask, bpmnModel, extendHisprocinst, currentTask.getAssignee());
            List<Date> createTimes = allTasks.stream()
                    .map(HistoricTaskInstance::getCreateTime)
                    .filter(Objects::nonNull).toList();
            if (!createTimes.isEmpty()) {
                vo.setStartDate(Collections.min(createTimes));
            }
            vo.setStatus(NodeStatusEnum.PROCESSING.getDescription());
        } else {
            // 已完成的节点
            vo = buildTaskNodeCoordinates(userTask, bpmnModel, extendHisprocinst);

            // 收集审批人
            List<String> assigneeCodes = new ArrayList<>();
            List<Date> createTimes = new ArrayList<>();
            List<Date> endTimes = new ArrayList<>();

            if (FlowConstant.FLOW_SUBMITTER.equals(userTask.getName())) {
                assigneeCodes.add(extendHisprocinst.getCurrentUserCode());
            } else {
                allTasks.stream()
                        .filter(t -> t.getTaskDefinitionKey().equals(userTask.getId()))
                        .forEach(t -> {
                            createTimes.add(t.getCreateTime());
                            endTimes.add(t.getEndTime());
                            if (StringUtils.isNotBlank(t.getAssignee())) {
                                assigneeCodes.add(t.getAssignee());
                            }
                        });
            }
            // 设置审批人名称（直接使用工号，后续可通过 Feign 查询真实姓名）
            if (CollectionUtils.isNotEmpty(assigneeCodes)) {
                vo.setApprover(String.join(";", assigneeCodes));
            }
            if (!createTimes.isEmpty()) {
                vo.setStartDate(Collections.min(createTimes));
            }
            if (!endTimes.isEmpty()) {
                vo.setEndDate(Collections.max(endTimes));
            }
            vo.setStatus(NodeStatusEnum.FINISH.getDescription());

            // 计算耗时
            long duration = 0;
            if (userTask.getBehavior() instanceof SequentialMultiInstanceBehavior) {
                // 多实例节点累加所有实例的耗时
                for (HistoricTaskInstance taskInstance : allTasks) {
                    if (taskInstance.getTaskDefinitionKey().equals(userTask.getId())
                            && taskInstance.getDurationInMillis() != null) {
                        duration += taskInstance.getDurationInMillis();
                    }
                }
            } else if (currentTask.getDurationInMillis() != null) {
                duration = currentTask.getDurationInMillis();
            }
            vo.setDuration(DurationUtils.getDuration(duration));
        }
        return vo;
    }

    /**
     * 构建未开始节点的信息（解析坐标和基本信息）
     */
    private ActivityVo buildUnstartedTaskNodeInfo(UserTask userTask, BpmnModel bpmnModel,
                                                  ExtendHisprocinst extendHisprocinst) {
        return buildUnstartedTaskNodeInfo(userTask, bpmnModel, extendHisprocinst, null);
    }

    /**
     * 构建未开始节点的信息，可指定已知的审批人工号
     */
    private ActivityVo buildUnstartedTaskNodeInfo(UserTask userTask, BpmnModel bpmnModel,
                                                  ExtendHisprocinst extendHisprocinst, String assignee) {
        ActivityVo vo = buildTaskNodeCoordinates(userTask, bpmnModel, extendHisprocinst);
        if (StringUtils.isNotBlank(assignee)) {
            vo.setApprover(assignee);
        } else {
            // 尝试从 BPMN 模型中解析审批人
            resolveAssigneeFromModel(userTask, extendHisprocinst, vo);
        }
        return vo;
    }

    /**
     * 从 BPMN 模型解析节点的审批人信息
     */
    private void resolveAssigneeFromModel(UserTask userTask, ExtendHisprocinst extendHisprocinst, ActivityVo vo) {
        try {
            if (StringUtils.isNotBlank(userTask.getAssignee())) {
                // 固定审批人或表达式
                if (FlowConstant.FLOW_SUBMITTER.equals(userTask.getName())) {
                    vo.setApprover(extendHisprocinst.getCurrentUserCode());
                } else {
                    // 对于表达式（如 ${assignee}），显示表达式原文
                    vo.setApprover(userTask.getAssignee());
                }
            } else {
                // 候选用户或候选组
                List<String> candidateUsers = userTask.getCandidateUsers();
                List<String> candidateGroups = userTask.getCandidateGroups();
                if (CollectionUtils.isNotEmpty(candidateUsers)) {
                    vo.setApprover(String.join(";", candidateUsers));
                } else if (CollectionUtils.isNotEmpty(candidateGroups)) {
                    vo.setApprover(String.join(";", candidateGroups));
                }
            }
        } catch (Exception e) {
            log.warn("解析节点审批人信息失败: {}", e.getMessage());
        }
    }

    /**
     * 构建节点坐标信息和基本属性
     */
    private ActivityVo buildTaskNodeCoordinates(UserTask userTask, BpmnModel bpmnModel,
                                                ExtendHisprocinst extendHisprocinst) {
        GraphicInfo graphicInfo = bpmnModelService.getGraphicInfo(bpmnModel, userTask.getId());
        ActivityVo vo = new ActivityVo();
        vo.setId(userTask.getId());
        if (graphicInfo != null) {
            vo.setX(graphicInfo.getX());
            vo.setY(graphicInfo.getY());
            vo.setWidth(graphicInfo.getWidth());
            vo.setHeight(graphicInfo.getHeight());
        }
        vo.setDocumentation(userTask.getDocumentation());
        vo.setName(userTask.getName());
        vo.setProceInsId(extendHisprocinst.getProcessInstanceId());
        vo.setProceDefId(extendHisprocinst.getProcessDefinitionId());
        vo.setTaskDefKey(userTask.getId());

        // 解析自定义节点类型属性
        try {
            String taskType = bpmnModelService.getSingleCustomProperty(
                    userTask.getId(), bpmnModel, FlowConstant.NODE_TYPE);
            if (StringUtils.isNotBlank(taskType)) {
                vo.setNodeType(resolveNodeType(taskType));
            } else {
                vo.setNodeType(NodeTypeEnum.APPLY.getDescription());
            }
        } catch (Exception e) {
            log.warn("解析节点类型属性失败: {}", e.getMessage());
            vo.setNodeType(NodeTypeEnum.APPLY.getDescription());
        }
        return vo;
    }

    /**
     * 将节点类型字符串映射为中文描述
     */
    private String resolveNodeType(String taskType) {
        for (NodeTypeEnum nodeType : NodeTypeEnum.values()) {
            if (nodeType.getType().equals(taskType)) {
                return nodeType.getDescription();
            }
        }
        return NodeTypeEnum.APPLY.getDescription();
    }
}
