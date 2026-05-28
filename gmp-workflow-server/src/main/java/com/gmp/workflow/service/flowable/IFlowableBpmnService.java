package com.gmp.workflow.service.flowable;

import com.gmp.framework.base.Result;
import com.gmp.workflow.model.flowable.ModelInfo;
import com.gmp.workflow.vo.flowable.model.ModelInfoVo;
import org.flowable.engine.repository.Deployment;

import java.util.List;
import java.util.Map;

/**
 * 流程模型生命周期管理服务接口
 * <p>
 * 提供 BPMN 模型的完整生命周期管理能力，包括：
 * 校验、保存、发布、停用、部署、加载等。
 * 对应 flow-core 的 IFlowableBpmnService。
 * </p>
 * <p>
 * 注意：本服务基于 Flowable 核心 RepositoryService API 实现，
 * 不依赖 flowable-ui-modeler 模块。
 * </p>
 */
public interface IFlowableBpmnService {

    /**
     * 校验 BPMN XML 的合法性
     * <p>
     * 通过 BpmnXMLConverter 解析 XML 并使用 ProcessValidator 校验，
     * 不产生部署副作用。
     * </p>
     *
     * @param modelXml BPMN XML 内容
     * @return 校验结果，成功返回 Result.ok()，失败返回错误信息
     */
    Result<String> validateBpmnXml(String modelXml);

    /**
     * 保存 BPMN 模型（含自动部署）
     * <p>
     * 流程：查找或创建 Flowable Model → 保存 XML → 自动部署 → 同步 ModelInfo
     * </p>
     *
     * @param params 模型参数，包含 modelId、modelName、modelXml、modelKey、category
     * @return 保存结果，成功返回模型 ID
     */
    Result<String> saveBpmnModel(Map<String, Object> params);

    /**
     * 发布模型
     * <p>
     * 校验模型状态 → 校验 BPMN 合法性 → 更新状态为已发布
     * </p>
     *
     * @param modelId Flowable 模型 ID
     * @return 发布结果
     */
    Result<String> publishBpmn(String modelId);

    /**
     * 停用模型
     * <p>
     * 将模型状态更新为"停用"，模型不再允许发起新流程。
     * </p>
     *
     * @param modelId Flowable 模型 ID
     * @return 停用结果
     */
    Result<String> stopBpmn(String modelId);

    /**
     * 部署模型到 Flowable 引擎
     * <p>
     * 将 BpmnModel 通过 RepositoryService 创建 Deployment。
     * </p>
     *
     * @param modelInfo 模型信息
     * @return 部署结果，包含 Deployment 对象
     */
    Result<Deployment> deployBpmn(ModelInfo modelInfo);

    /**
     * 根据模型 ID 加载 BPMN XML
     *
     * @param modelId Flowable 模型 ID
     * @return 包含模型 XML 的 ModelInfoVo，不存在返回 null
     */
    ModelInfoVo loadBpmnXmlByModelId(String modelId);

    /**
     * 根据模型 Key 加载 BPMN XML
     *
     * @param modelKey 模型唯一标识
     * @return 包含模型 XML 的 ModelInfoVo，不存在返回 null
     */
    ModelInfoVo loadBpmnXmlByModelKey(String modelKey);

    /**
     * 获取所有模型列表（按最后更新时间倒序）
     *
     * @return 模型列表，每项包含 id、modelId、key、name、category、version、createTime、lastUpdateTime
     */
    List<Map<String, Object>> getModelList();
}
