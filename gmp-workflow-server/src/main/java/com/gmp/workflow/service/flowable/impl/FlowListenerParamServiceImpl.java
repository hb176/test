package com.gmp.workflow.service.flowable.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gmp.workflow.mapper.FlowListenerParamMapper;
import com.gmp.workflow.model.flowable.FlowListenerParam;
import com.gmp.workflow.service.flowable.IFlowListenerParamService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 流程监听器参数服务实现
 */
@Service
public class FlowListenerParamServiceImpl extends ServiceImpl<FlowListenerParamMapper, FlowListenerParam>
        implements IFlowListenerParamService {

    /**
     * 根据监听器 ID 查询参数列表
     *
     * @param listenerId 监听器 ID
     * @return 参数列表
     */
    @Override
    public List<FlowListenerParam> getListByListenerId(String listenerId) {
        LambdaQueryWrapper<FlowListenerParam> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlowListenerParam::getListenerId, listenerId);
        return this.list(wrapper);
    }
}
