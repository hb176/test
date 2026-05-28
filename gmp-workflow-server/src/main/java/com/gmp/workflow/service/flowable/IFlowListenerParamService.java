package com.gmp.workflow.service.flowable;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gmp.workflow.model.flowable.FlowListenerParam;

import java.util.List;

/**
 * 流程监听器参数服务接口
 */
public interface IFlowListenerParamService extends IService<FlowListenerParam> {

    /**
     * 根据监听器 ID 查询参数列表
     *
     * @param listenerId 监听器 ID
     * @return 参数列表
     */
    List<FlowListenerParam> getListByListenerId(String listenerId);
}
