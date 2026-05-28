package com.gmp.workflow.controller;

import com.gmp.workflow.enums.flowable.model.ModelFormStatusEnum;
import com.gmp.workflow.model.flowable.ModelInfo;
import com.gmp.workflow.service.flowable.IModelInfoService;
import com.gmp.workflow.tools.common.ReturnCode;
import com.gmp.workflow.tools.pager.PagerModel;
import com.gmp.workflow.tools.pager.Query;
import com.gmp.workflow.tools.vo.ReturnVo;
import com.gmp.workflow.vo.pager.ParamVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 模型扩展信息控制器 — 适配 flow-admin-ui 设计器的 modelInfo 接口协议
 *
 * 接口路径: /flow/flowable/modelInfo/*
 * 返回格式: ReturnVo {code, msg, data}
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/flow/flowable/modelInfo")
public class ModelInfoController {

    private final IModelInfoService modelInfoService;

    /**
     * 分页获取模型列表
     */
    @PostMapping("/getPagerModel")
    public ReturnVo<PagerModel<ModelInfo>> getPagerModel(@RequestBody ParamVo<ModelInfo> params) {
        ReturnVo<PagerModel<ModelInfo>> returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "OK");
        PagerModel<ModelInfo> pm = modelInfoService.getPagerModel(params.getEntity(), params.getQuery());
        returnVo.setData(pm);
        return returnVo;
    }

    /**
     * 保存或更新模型信息
     */
    @PostMapping(value = "/saveOrUpdateModelInfo", produces = "application/json")
    public ReturnVo<ModelInfo> saveOrUpdateModelInfo(@RequestBody ModelInfo modelInfo) {
        ReturnVo<ModelInfo> returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "保存成功！");

        // 新增时检查 modelKey 是否重复
        if (StringUtils.isBlank(modelInfo.getId())) {
            if (modelInfoService.checkEntityExist(modelInfo)) {
                return new ReturnVo<>(ReturnCode.FAIL, "model key 不能重复！");
            }
        }

        modelInfo = modelInfoService.saveOrUpdateModelInfo(modelInfo);
        returnVo.setData(modelInfo);
        return returnVo;
    }

    /**
     * 批量删除
     */
    @PostMapping(value = "/deleteByIds", produces = "application/json")
    public ReturnVo<String> deleteByIds(@RequestBody List<String> ids) {
        return modelInfoService.deleteByIds(ids);
    }

    /**
     * 查询单个 — 通过主键ID
     */
    @GetMapping("/get/{id}")
    public ReturnVo<ModelInfo> get(@PathVariable String id) {
        ReturnVo<ModelInfo> returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "OK");
        ModelInfo modelInfo = modelInfoService.getById(id);
        returnVo.setData(modelInfo);
        return returnVo;
    }

    /**
     * 查询单个 — 通过 Flowable modelId
     */
    @GetMapping("/getByModelId/{modelId}")
    public ReturnVo<ModelInfo> getByModelId(@PathVariable String modelId) {
        ReturnVo<ModelInfo> returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "OK");
        ModelInfo modelInfo = modelInfoService.getByModelId(modelId);
        if (modelInfo != null) {
            ModelFormStatusEnum minStatus = ModelFormStatusEnum.getMinStatus(modelInfo.getStatus(), modelInfo.getExtendStatus());
            if (minStatus != null) {
                modelInfo.setStatusName(minStatus.getMsg());
                modelInfo.setStatus(minStatus.getStatus());
            }
        }
        returnVo.setData(modelInfo);
        return returnVo;
    }

    /**
     * 判断字段是否存在
     */
    @PostMapping(value = "/checkEntityExist", produces = "application/json")
    public ReturnVo<Boolean> checkEntityExist(@RequestBody ModelInfo modelInfo) {
        ReturnVo<Boolean> returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "OK");
        returnVo.setData(modelInfoService.checkEntityExist(modelInfo));
        return returnVo;
    }
}
