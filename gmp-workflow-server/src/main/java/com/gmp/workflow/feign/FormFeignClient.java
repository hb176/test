package com.gmp.workflow.feign;

import com.gmp.framework.base.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 表单服务Feign客户端 - 流程服务调用表单服务的声明式接口
 *
 * 使用场景：
 * 1. 流程启动时获取表单定义（渲染审批页面）
 * 2. 流程结束时更新表单数据状态
 * 3. 审批过程中校验只读字段未被修改
 *
 * @author hb176
 * @since 1.0.0
 */
@FeignClient(name = "gmp-form-server", path = "/form")
public interface FormFeignClient {

    /**
     * 根据formKey获取最新已发布的表单定义（含完整JSON Schema）
     * 流程启动和审批页面渲染时调用
     */
    @GetMapping("/definition/key/{formKey}")
    Result<Map<String, Object>> getDefinitionByKey(@PathVariable("formKey") String formKey);

    /**
     * 获取单条表单数据（含完整JSON）
     * 审批人查看表单详情时调用
     */
    @GetMapping("/data/{id}")
    Result<Map<String, Object>> getFormDataById(@PathVariable("id") Long id);

    /**
     * 更新表单数据状态
     * 流程审批通过/驳回后，同步更新表单数据状态
     */
    @PutMapping("/data/{id}/status")
    Result<Void> updateFormDataStatus(@PathVariable("id") Long id,
                                       @RequestParam("status") String status);

    /**
     * 校验表单数据在指定节点是否被修改了只读字段
     */
    @GetMapping("/data/{id}/validate-readonly")
    Result<Map<String, Object>> validateReadonlyFields(
            @PathVariable("id") Long id,
            @RequestParam("nodeKey") String nodeKey);
}
