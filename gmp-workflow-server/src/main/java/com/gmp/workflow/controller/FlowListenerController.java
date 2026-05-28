package com.gmp.workflow.controller;

import com.gmp.workflow.model.flowable.FlowListener;
import com.gmp.workflow.service.flowable.IFlowListenerService;
import com.gmp.workflow.tools.common.ReturnCode;
import com.gmp.workflow.tools.pager.PagerModel;
import com.gmp.workflow.tools.pager.Query;
import com.gmp.workflow.tools.vo.ReturnVo;
import com.gmp.workflow.vo.pager.ParamVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 流程监听器 CRUD 控制器
 *
 * 接口路径: /flow/flowable/flowListener/*
 * 参考: flow-admin-rest FlowListenerResource
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/flow/flowable/flowListener")
public class FlowListenerController {

    private final IFlowListenerService flowListenerService;

    /**
     * 获取监听器列表（不含参数）
     */
    @PostMapping(value = "/getList", produces = "application/json")
    public ReturnVo<List> getList(@RequestBody FlowListener param) {
        ReturnVo<List> returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "OK");
        List<FlowListener> pm = flowListenerService.getList(param);
        returnVo.setData(pm);
        return returnVo;
    }

    /**
     * 分页获取监听器列表
     */
    @PostMapping(value = "/getPagerModel", produces = "application/json")
    public ReturnVo<PagerModel> getPagerModel(@RequestBody ParamVo<FlowListener> params) {
        ReturnVo<PagerModel> returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "OK");
        PagerModel<FlowListener> pm = flowListenerService.getPagerModel(params.getEntity(), params.getQuery());
        returnVo.setData(pm);
        return returnVo;
    }

    /**
     * 保存或更新监听器
     */
    @PostMapping(value = "/saveOrUpdate", produces = "application/json")
    public ReturnVo<String> saveOrUpdate(@RequestBody FlowListener flowListener) {
        ReturnVo<String> returnVo = new ReturnVo<>(ReturnCode.FAIL, "保存失败");
        if (flowListener.getId() == null || flowListener.getId().isEmpty()) {
            flowListener.setCreateTime(new Date());
        }
        flowListener.setUpdateTime(new Date());
        flowListenerService.saveOrUpdate(flowListener);
        returnVo.setMsg("保存成功！");
        returnVo.setCode(ReturnCode.SUCCESS);
        return returnVo;
    }

    /**
     * 删除监听器
     */
    @PostMapping(value = "/deleteById/{id}", produces = "application/json")
    public ReturnVo<String> deleteById(@PathVariable String id) {
        ReturnVo<String> returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "OK");
        flowListenerService.deleteById(id);
        return returnVo;
    }

    /**
     * 根据 ID 获取监听器（含参数）
     */
    @GetMapping(value = "/getById/{id}", produces = "application/json")
    public ReturnVo<FlowListener> getById(@PathVariable String id) {
        ReturnVo<FlowListener> returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "OK");
        FlowListener listener = flowListenerService.getById(id);
        returnVo.setData(listener);
        return returnVo;
    }

    /**
     * 判断监听器是否存在
     */
    @PostMapping(value = "/checkEntityExist", produces = "application/json")
    public ReturnVo<Boolean> checkEntityExist(@RequestBody FlowListener flowListener) {
        ReturnVo<Boolean> returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "OK");
        boolean exists = flowListenerService.getFlowListenerByNameAndType(
                flowListener.getListenerType(), flowListener.getName()) != null;
        returnVo.setData(exists);
        return returnVo;
    }
}
