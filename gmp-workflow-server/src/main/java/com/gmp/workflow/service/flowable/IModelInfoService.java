package com.gmp.workflow.service.flowable;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gmp.workflow.model.flowable.ModelInfo;
import com.gmp.workflow.tools.pager.PagerModel;
import com.gmp.workflow.tools.pager.Query;
import com.gmp.workflow.tools.vo.ReturnVo;

import java.util.List;

/**
 * 模型扩展信息服务接口
 *
 * @author hb176
 * @since 1.0.0
 */
public interface IModelInfoService extends IService<ModelInfo> {

    /**
     * 保存或更新模型信息
     */
    ModelInfo saveOrUpdateModelInfo(ModelInfo modelInfo);

    /**
     * 根据 Flowable ModelId 查询模型信息
     */
    ModelInfo getByModelId(String modelId);

    /**
     * 根据模型 Key 查询模型信息
     */
    ModelInfo getByModelKey(String modelKey);

    /**
     * 批量删除
     */
    ReturnVo<String> deleteByIds(List<String> ids);

    /**
     * 判断字段是否存在
     */
    boolean checkEntityExist(ModelInfo modelInfo);

    /**
     * 分页查询
     */
    PagerModel<ModelInfo> getPagerModel(ModelInfo modelInfo, Query query);
}
