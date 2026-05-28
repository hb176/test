package com.gmp.workflow.feign;

import com.gmp.framework.base.PageResult;
import com.gmp.framework.base.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统管理服务Feign客户端 - 用于跨服务查询用户、角色、部门信息
 */
@FeignClient(name = "gmp-system-server", path = "/system")
public interface SystemFeignClient {

    @GetMapping("/user/list-by-role")
    Result<List<Map<String, Object>>> getUsersByRole(@RequestParam("roleCode") String roleCode);

    @GetMapping("/user/page")
    Result<Map<String, Object>> getUserPage(
            @RequestParam("pageNum") int pageNum,
            @RequestParam("pageSize") int pageSize,
            @RequestParam(value = "keyword", required = false) String keyword);

    @GetMapping("/dept/{id}/leader")
    Result<Map<String, Object>> getDeptLeader(@PathVariable("id") Long deptId);

    @GetMapping("/user/{id}")
    Result<Map<String, Object>> getUserById(@PathVariable("id") Long userId);

    @GetMapping("/user/{id}/roles")
    Result<List<Long>> getUserRoles(@PathVariable("id") Long userId);

    @GetMapping("/role/{id}")
    Result<Map<String, Object>> getRoleById(@PathVariable("id") Long roleId);

    @GetMapping("/dept/{id}")
    Result<Map<String, Object>> getDeptById(@PathVariable("id") Long deptId);

    @GetMapping("/role/list")
    Result<List<Map<String, Object>>> getRoleList();

    @GetMapping("/dept/tree")
    Result<List<Map<String, Object>>> getDeptTree();

    @GetMapping("/dept/{id}/parent-leader")
    Result<Map<String, Object>> getParentDeptLeader(@PathVariable("id") Long deptId);

    @GetMapping("/user/{id}/dept-leader")
    Result<Map<String, Object>> getUserDeptLeader(@PathVariable("id") Long userId);

    @PostMapping("/message/send-by-template")
    Result<Void> sendMessageByTemplate(@RequestBody Map<String, Object> request);
}
