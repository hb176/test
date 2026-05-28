package com.gmp.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gmp.workflow.model.flowable.FlowListener;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程监听器 Mapper
 */
@Mapper
public interface FlowListenerMapper extends BaseMapper<FlowListener> {
}
