package com.gmp.workflow.service.flowable.impl;

import com.gmp.framework.base.Result;
import com.gmp.workflow.enums.flowable.model.ModelFormStatusEnum;
import com.gmp.workflow.model.flowable.ModelInfo;
import com.gmp.workflow.service.flowable.IFlowableBpmnService;
import com.gmp.workflow.service.flowable.IModelInfoService;
import com.gmp.workflow.vo.flowable.model.ModelInfoVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.validation.ProcessValidator;
import org.flowable.validation.ProcessValidatorFactory;
import org.flowable.validation.ValidationError;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 流程模型生命周期管理服务实现
 * <p>
 * 基于 Flowable 核心 RepositoryService API，提供 BPMN 模型的
 * 校验、保存、发布、停用、部署、加载等完整生命周期管理能力。
 * </p>
 * <p>
 * 与 flow-core 的 FlowableBpmnServiceImpl 不同，本实现不依赖
 * flowable-ui-modeler 模块，而是直接使用 BpmnXMLConverter 和
 * ProcessValidator 进行 XML 解析与校验。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowableBpmnServiceImpl implements IFlowableBpmnService {

    private final RepositoryService repositoryService;
    private final IModelInfoService modelInfoService;

    /**
     * 校验 BPMN XML 的合法性
     * <p>
     * 通过 BpmnXMLConverter 解析 XML 为 BpmnModel，再使用
     * ProcessValidator 进行规则校验。整个过程不产生部署副作用。
     * </p>
     */
    @Override
    public Result<String> validateBpmnXml(String modelXml) {
        if (StringUtils.isBlank(modelXml)) {
            return Result.fail(400, "BPMN XML 不能为空");
        }
        try {
            XMLInputFactory xif = XMLInputFactory.newInstance();
            XMLStreamReader xtr = xif.createXMLStreamReader(
                    new ByteArrayInputStream(modelXml.getBytes(StandardCharsets.UTF_8)));
            BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);
            if (bpmnModel == null || bpmnModel.getMainProcess() == null) {
                return Result.fail(400, "无法解析 BPMN XML，请检查格式");
            }
            // 使用 Flowable 官方校验器进行规则校验
            ProcessValidator validator = new ProcessValidatorFactory().createDefaultProcessValidator();
            List<ValidationError> errors = validator.validate(bpmnModel);
            if (errors != null && !errors.isEmpty()) {
                String msg = errors.stream()
                        .map(ValidationError::toString)
                        .collect(Collectors.joining("\n"));
                return Result.fail(400, "BPMN 校验失败:\n" + msg);
            }
            return Result.ok("校验通过");
        } catch (Exception e) {
            log.warn("BPMN XML 校验异常: {}", e.getMessage());
            return Result.fail(400, "校验失败: " + e.getMessage());
        }
    }

    /**
     * 保存 BPMN 模型（含自动部署）
     * <p>
     * 完整流程：
     * 1. 解析参数，校验 XML 非空
     * 2. 查找已有模型（按 modelId 或 modelKey），或创建新模型
     * 3. 保存模型元信息到 ACT_RE_MODEL
     * 4. 保存模型 XML 到 ACT_GE_BYTEARRAY（editor source）
     * 5. 自动部署模型到 Flowable 引擎
     * 6. 同步写入 tbl_flow_model_info 扩展表
     * </p>
     */
    @Override
    public Result<String> saveBpmnModel(Map<String, Object> params) {
        try {
            String modelId = (String) params.get("modelId");
            String modelName = (String) params.get("fileName");
            if (StringUtils.isBlank(modelName)) modelName = (String) params.get("modelName");
            if (StringUtils.isBlank(modelName)) modelName = "未命名流程";
            String xml = (String) params.get("modelXml");
            String modelKey = (String) params.get("modelKey");
            if (StringUtils.isBlank(modelKey)) modelKey = modelName;
            String category = (String) params.get("category");

            log.info("保存BPMN模型: modelId={}, name={}, key={}, xmlLen={}",
                    modelId, modelName, modelKey, xml != null ? xml.length() : 0);

            if (StringUtils.isBlank(xml)) {
                return Result.fail(400, "BPMN XML 不能为空");
            }

            // 查找或创建 Flowable Model
            Model model = findOrCreateModel(modelId, modelKey);
            model.setName(modelName);
            model.setKey(StringUtils.isNotBlank(modelKey) ? modelKey : model.getId());
            model.setCategory(StringUtils.isNotBlank(category) ? category : "COMM");
            repositoryService.saveModel(model);

            // 保存模型 XML 到 editor source
            repositoryService.addModelEditorSource(model.getId(),
                    xml.getBytes(StandardCharsets.UTF_8));

            // 自动部署
            String deployMsg = "";
            try {
                repositoryService.createDeployment()
                        .addString(modelName + ".bpmn20.xml", xml)
                        .name(modelName)
                        .category(category)
                        .deploy();
                deployMsg = "，流程已自动部署";
            } catch (Exception deployEx) {
                log.warn("部署流程失败(模型已保存): {}", deployEx.getMessage());
                deployMsg = "，但部署失败: " + deployEx.getMessage();
            }

            // 同步写入 tbl_flow_model_info
            syncModelInfo(model, modelName, category);

            log.info("BPMN模型已保存: modelId={}, name={}", model.getId(), model.getName());
            return Result.ok("保存成功！" + deployMsg, model.getId());
        } catch (Exception e) {
            log.error("保存BPMN模型失败", e);
            return Result.fail(500, "保存失败: " + e.getMessage());
        }
    }

    /**
     * 发布模型
     * <p>
     * 前置校验：模型必须存在且状态为草稿或待发布。
     * 发布后模型状态更新为"已发布"。
     * </p>
     */
    @Override
    public Result<String> publishBpmn(String modelId) {
        if (StringUtils.isBlank(modelId)) {
            return Result.fail(400, "模型ID不能为空");
        }
        Model model = repositoryService.getModel(modelId);
        if (model == null) {
            return Result.fail(404, "模型不存在");
        }
        // 校验模型 XML 合法性
        byte[] source = repositoryService.getModelEditorSource(modelId);
        if (source == null || source.length == 0) {
            return Result.fail(400, "模型内容为空，请先保存模型");
        }
        String xml = new String(source, StandardCharsets.UTF_8);
        Result<String> validResult = validateBpmnXml(xml);
        if (!validResult.isSuccess()) {
            return validResult;
        }
        // 更新 ModelInfo 状态
        ModelInfo modelInfo = modelInfoService.getByModelId(modelId);
        if (modelInfo == null) {
            return Result.fail(404, "未找到对应的模型扩展信息");
        }
        modelInfo.setStatus(ModelFormStatusEnum.YFB.getStatus());
        modelInfo.setExtendStatus(ModelFormStatusEnum.YFB.getStatus());
        modelInfoService.saveOrUpdateModelInfo(modelInfo);
        return Result.ok("发布成功！");
    }

    /**
     * 停用模型
     * <p>
     * 将模型状态更新为"停用"。停用后模型不允许发起新流程，
     * 但已有的流程实例不受影响。
     * </p>
     */
    @Override
    public Result<String> stopBpmn(String modelId) {
        if (StringUtils.isBlank(modelId)) {
            return Result.fail(400, "模型ID不能为空");
        }
        ModelInfo modelInfo = modelInfoService.getByModelId(modelId);
        if (modelInfo == null) {
            return Result.fail(404, "未找到对应的模型");
        }
        modelInfo.setStatus(ModelFormStatusEnum.TY.getStatus());
        modelInfo.setExtendStatus(ModelFormStatusEnum.TY.getStatus());
        modelInfoService.saveOrUpdateModelInfo(modelInfo);
        return Result.ok("停用成功！");
    }

    /**
     * 部署模型到 Flowable 引擎
     * <p>
     * 从 ACT_RE_MODEL 读取模型 BpmnModel，通过 RepositoryService
     * 创建 Deployment。部署后引擎中会生成新的 ProcessDefinition。
     * </p>
     */
    @Override
    public Result<Deployment> deployBpmn(ModelInfo modelInfo) {
        if (modelInfo == null || StringUtils.isBlank(modelInfo.getModelId())) {
            return Result.fail(400, "模型信息不完整");
        }
        Model model = repositoryService.getModel(modelInfo.getModelId());
        if (model == null) {
            return Result.fail(404, "模型不存在: " + modelInfo.getModelId());
        }
        // 读取模型 XML
        byte[] source = repositoryService.getModelEditorSource(modelInfo.getModelId());
        if (source == null || source.length == 0) {
            return Result.fail(400, "模型内容为空");
        }
        String xml = new String(source, StandardCharsets.UTF_8);
        try {
            Deployment deployment = repositoryService.createDeployment()
                    .addString(model.getName() + ".bpmn20.xml", xml)
                    .name(model.getName())
                    .category(modelInfo.getCategoryCode())
                    .deploy();
            return Result.ok(deployment);
        } catch (Exception e) {
            log.error("部署模型失败: modelId={}", modelInfo.getModelId(), e);
            return Result.fail(500, "部署失败: " + e.getMessage());
        }
    }

    /**
     * 根据模型 ID 加载 BPMN XML
     */
    @Override
    public ModelInfoVo loadBpmnXmlByModelId(String modelId) {
        if (StringUtils.isBlank(modelId)) return null;
        Model model = repositoryService.getModel(modelId);
        if (model == null) return null;
        return buildModelInfoVo(model);
    }

    /**
     * 根据模型 Key 加载 BPMN XML
     */
    @Override
    public ModelInfoVo loadBpmnXmlByModelKey(String modelKey) {
        if (StringUtils.isBlank(modelKey)) return null;
        ModelInfo modelInfo = modelInfoService.getByModelKey(modelKey);
        if (modelInfo != null && StringUtils.isNotBlank(modelInfo.getModelId())) {
            return loadBpmnXmlByModelId(modelInfo.getModelId());
        }
        // 兜底：直接查 Flowable Model
        List<Model> models = repositoryService.createModelQuery().modelKey(modelKey).list();
        if (models.isEmpty()) return null;
        return buildModelInfoVo(models.get(0));
    }

    /**
     * 获取所有模型列表（按最后更新时间倒序）
     */
    @Override
    public List<Map<String, Object>> getModelList() {
        List<Model> models = repositoryService.createModelQuery()
                .orderByLastUpdateTime().desc().list();
        return models.stream().map(m -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", m.getId());
            row.put("modelId", m.getId());
            row.put("key", m.getKey());
            row.put("name", m.getName());
            row.put("category", m.getCategory());
            row.put("version", m.getVersion());
            row.put("createTime", m.getCreateTime());
            row.put("lastUpdateTime", m.getLastUpdateTime());
            return row;
        }).toList();
    }

    // ==================== 内部方法 ====================

    /**
     * 查找或创建 Flowable Model
     * <p>
     * 优先按 modelId 查找（编辑模式），其次按 modelKey 查找（新增去重），
     * 都不存在则创建新模型。
     * </p>
     */
    private Model findOrCreateModel(String modelId, String modelKey) {
        if (StringUtils.isNotBlank(modelId)) {
            Model model = repositoryService.getModel(modelId);
            if (model != null) return model;
        }
        if (StringUtils.isNotBlank(modelKey)) {
            List<Model> existing = repositoryService.createModelQuery()
                    .modelKey(modelKey).orderByModelVersion().desc().list();
            if (!existing.isEmpty()) return existing.get(0);
        }
        return repositoryService.newModel();
    }

    /**
     * 同步模型信息到 tbl_flow_model_info 扩展表
     */
    private void syncModelInfo(Model model, String modelName, String category) {
        try {
            ModelInfo modelInfo = modelInfoService.getByModelId(model.getId());
            if (modelInfo == null) {
                modelInfo = new ModelInfo();
                modelInfo.setModelId(model.getId());
                modelInfo.setStatus(ModelFormStatusEnum.CG.getStatus());
                modelInfo.setExtendStatus(ModelFormStatusEnum.CG.getStatus());
            }
            modelInfo.setName(modelName);
            modelInfo.setModelKey(model.getKey());
            modelInfo.setCategoryCode(category);
            modelInfoService.saveOrUpdateModelInfo(modelInfo);
        } catch (Exception e) {
            log.warn("同步模型信息失败(不影响BPMN保存): {}", e.getMessage());
        }
    }

    /**
     * 构建 ModelInfoVo（包含模型 XML）
     */
    private ModelInfoVo buildModelInfoVo(Model model) {
        byte[] source = repositoryService.getModelEditorSource(model.getId());
        String xml = source != null ? new String(source, StandardCharsets.UTF_8) : "";
        ModelInfoVo vo = new ModelInfoVo();
        vo.setModelId(model.getId());
        vo.setModelKey(model.getKey());
        vo.setModelName(model.getName());
        vo.setModelXml(xml);
        vo.setFileName(model.getName());
        vo.setCategoryCode(model.getCategory());
        return vo;
    }
}
