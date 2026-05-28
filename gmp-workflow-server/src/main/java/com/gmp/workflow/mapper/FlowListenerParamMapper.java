package com.gmp.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gmp.workflow.model.flowable.FlowListenerParam;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程监听器参数 Mapper
 */
@Mapper
public interface FlowListenerParamMapper extends BaseMapper<FlowListenerParam> {
}
