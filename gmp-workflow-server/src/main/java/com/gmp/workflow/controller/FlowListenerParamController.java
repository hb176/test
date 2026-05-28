package com.gmp.workflow.controller;

import com.gmp.workflow.model.flowable.FlowListenerParam;
import com.gmp.workflow.service.flowable.IFlowListenerParamService;
import com.gmp.workflow.tools.common.ReturnCode;
import com.gmp.workflow.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程监听器参数 CRUD 控制器
 *
 * 接口路径: /flow/flowable/flowListenerParam/*
 * 参考: flow-admin-rest FlowListenerParamResource
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/flow/flowable/flowListenerParam")
public class FlowListenerParamController {

    private final IFlowListenerParamService flowListenerParamService;

    /**
     * 根据监听器 ID 获取参数列表
     */
    @GetMapping(value = "/getList/{listenerId}", produces = "application/json")
    public ReturnVo<List> getList(@PathVariable String listenerId) {
        ReturnVo<List> returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "OK");
        List<FlowListenerParam> pm = flowListenerParamService.getListByListenerId(listenerId);
        returnVo.setData(pm);
        return returnVo;
    }

    /**
     * 保存或更新监听器参数
     */
    @PostMapping(value = "/saveOrUpdate", produces = "application/json")
    public ReturnVo<String> saveOrUpdate(@RequestBody FlowListenerParam flowListenerParam) {
        ReturnVo<String> returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "OK");
        flowListenerParamService.saveOrUpdate(flowListenerParam);
        return returnVo;
    }

    /**
     * 删除监听器参数
     */
    @PostMapping(value = "/deleteById/{id}", produces = "application/json")
    public ReturnVo<String> deleteById(@PathVariable String id) {
        ReturnVo<String> returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "OK");
        flowListenerParamService.removeById(id);
        return returnVo;
    }

    /**
     * 判断参数是否存在
     */
    @PostMapping(value = "/checkEntityExist", produces = "application/json")
    public ReturnVo<Boolean> checkEntityExist(@RequestBody FlowListenerParam flowListenerParam) {
        ReturnVo<Boolean> returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "OK");
        // 简单校验：同名参数是否已存在
        List<FlowListenerParam> existing = flowListenerParamService.getListByListenerId(flowListenerParam.getListenerId());
        boolean exists = existing.stream().anyMatch(p ->
                p.getName().equals(flowListenerParam.getName())
                        && !p.getId().equals(flowListenerParam.getId()));
        returnVo.setData(exists);
        return returnVo;
    }
}
