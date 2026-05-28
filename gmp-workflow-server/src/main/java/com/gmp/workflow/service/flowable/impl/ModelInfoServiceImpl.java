package com.gmp.workflow.service.flowable.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gmp.workflow.constant.FlowConstant;
import com.gmp.workflow.mapper.ModelInfoMapper;
import com.gmp.workflow.model.flowable.ModelInfo;
import com.gmp.workflow.service.flowable.IModelInfoService;
import com.gmp.workflow.tools.common.ReturnCode;
import com.gmp.workflow.tools.pager.PagerModel;
import com.gmp.workflow.tools.pager.Query;
import com.gmp.workflow.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 模型扩展信息服务实现
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelInfoServiceImpl extends ServiceImpl<ModelInfoMapper, ModelInfo> implements IModelInfoService {

    private final ModelInfoMapper modelInfoMapper;

    @Override
    public ModelInfo saveOrUpdateModelInfo(ModelInfo modelInfo) {
        Date now = new Date();
        if (StringUtils.isBlank(modelInfo.getId())) {
            // 新增
            modelInfo.setCreateTime(now);
            modelInfo.setUpdateTime(now);
            modelInfo.setDelFlag(FlowConstant.DEL_FLAG_1);
            if (modelInfo.getStatus() == null) {
                modelInfo.setStatus(0); // 草稿
            }
            if (modelInfo.getModelType() == null) {
                modelInfo.setModelType(ModelInfo.CUSTOM_MODEL_TYPE);
            }
        } else {
            // 更新
            ModelInfo exist = this.getById(modelInfo.getId());
            if (exist != null) {
                modelInfo.setModelId(exist.getModelId());
                modelInfo.setCreateTime(exist.getCreateTime());
                modelInfo.setCreator(exist.getCreator());
            }
            modelInfo.setUpdateTime(now);
        }
        this.saveOrUpdate(modelInfo);
        log.info("模型信息已保存 - id: {}, name: {}, modelId: {}", modelInfo.getId(), modelInfo.getName(), modelInfo.getModelId());
        return modelInfo;
    }

    @Override
    public ModelInfo getByModelId(String modelId) {
        if (StringUtils.isBlank(modelId)) return null;
        LambdaQueryWrapper<ModelInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ModelInfo::getModelId, modelId);
        return this.getOne(queryWrapper);
    }

    @Override
    public ModelInfo getByModelKey(String modelKey) {
        if (StringUtils.isBlank(modelKey)) return null;
        LambdaQueryWrapper<ModelInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ModelInfo::getModelKey, modelKey);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    @Override
    public ReturnVo<String> deleteByIds(List<String> ids) {
        ReturnVo<String> returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "OK");
        if (ids != null && !ids.isEmpty()) {
            this.removeByIds(ids);
            log.info("模型信息已删除 - ids: {}", ids);
        }
        return returnVo;
    }

    @Override
    public boolean checkEntityExist(ModelInfo modelInfo) {
        if (modelInfo == null) return false;
        LambdaQueryWrapper<ModelInfo> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(modelInfo.getModelKey())) {
            queryWrapper.eq(ModelInfo::getModelKey, modelInfo.getModelKey());
        }
        if (StringUtils.isNotBlank(modelInfo.getId())) {
            queryWrapper.ne(ModelInfo::getId, modelInfo.getId());
        }
        return this.count(queryWrapper) > 0;
    }

    @Override
    public PagerModel<ModelInfo> getPagerModel(ModelInfo modelInfo, Query query) {
        IPage<ModelInfo> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<ModelInfo> result = modelInfoMapper.getPagerModel(page, modelInfo);
        return new PagerModel<>(result.getTotal(), result.getRecords());
    }
}
