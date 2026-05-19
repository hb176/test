package com.gmp.form.feign;

import com.gmp.framework.base.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 流程服务Feign客户端 - 表单服务调用流程服务的声明式接口
 *
 * 使用场景：
 * 1. 表单提交时启动流程
 * 2. 查询表单关联的流程状态
 * 3. 获取流程节点对表单字段的读写权限配置
 *
 * @author hb176
 * @since 1.0.0
 */
@FeignClient(name = "gmp-workflow-server", path = "/workflow")
public interface WorkflowFeignClient {

    /**
     * 根据流程定义Key获取最新发布的流程定义
     */
    @GetMapping("/definition/key/{processKey}")
    Result<Map<String, Object>> getDefinitionByKey(@PathVariable("processKey") String processKey);

    /**
     * 查询某条业务数据关联的流程实例状态
     */
    @GetMapping("/instance/by-business-key")
    Result<Map<String, Object>> getInstanceByBusinessKey(@RequestParam("businessKey") String businessKey);

    /**
     * 获取节点级别的表单字段权限配置
     * （哪些字段在某个审批节点只读/隐藏/可编辑）
     */
    @GetMapping("/definition/{definitionId}/field-permissions")
    Result<Map<String, Object>> getFieldPermissions(
            @PathVariable("definitionId") String definitionId,
            @RequestParam("nodeKey") String nodeKey);
}
