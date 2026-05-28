package com.gmp.system.controller;

import com.gmp.common.base.ResultCode;
import com.gmp.framework.base.CommonController;
import com.gmp.framework.base.Result;
import com.gmp.system.entity.SysDept;
import com.gmp.system.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/dept")
public class DeptController extends CommonController<SysDeptService, SysDept> {

    private final SysDeptService deptService;

    @Override
    protected SysDeptService getService() {
        return deptService;
    }

    @GetMapping("/tree")
    public Result<List<SysDept>> tree() {
        List<SysDept> list = deptService.lambdaQuery()
                .orderByAsc(SysDept::getSortOrder)
                .orderByAsc(SysDept::getId)
                .list();
        return success(list);
    }

    @GetMapping("/children/{parentId}")
    public Result<List<SysDept>> children(@PathVariable Long parentId) {
        List<SysDept> list = deptService.lambdaQuery()
                .eq(SysDept::getParentId, parentId)
                .eq(SysDept::getStatus, 1)
                .orderByAsc(SysDept::getSortOrder)
                .list();
        return success(list);
    }

    @GetMapping("/{id}")
    public Result<SysDept> getDept(@PathVariable Long id) {
        return getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:dept:add')")
    public Result<SysDept> create(@RequestBody SysDept dept) {
        if (dept.getParentId() != null && dept.getParentId() > 0) {
            SysDept parent = deptService.getById(dept.getParentId());
            if (parent != null) {
                dept.setAncestors(parent.getAncestors() + "," + dept.getParentId());
            }
        } else {
            dept.setParentId(0L);
            dept.setAncestors("0");
        }
        return saveEntity(dept);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dept:edit')")
    public Result<SysDept> update(@PathVariable Long id, @RequestBody SysDept dept) {
        dept.setId(id);
        SysDept existing = deptService.getById(id);
        if (existing != null && dept.getParentId() != null
                && !dept.getParentId().equals(existing.getParentId())) {
            if (dept.getParentId() > 0) {
                SysDept parent = deptService.getById(dept.getParentId());
                if (parent != null) {
                    dept.setAncestors(parent.getAncestors() + "," + dept.getParentId());
                }
            } else {
                dept.setAncestors("0");
            }
        }
        return updateEntity(dept);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dept:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        long childCount = deptService.lambdaQuery()
                .eq(SysDept::getParentId, id)
                .count();
        if (childCount > 0) {
            return fail(ResultCode.VALIDATION_FAILED, "该部门下存在子部门，无法删除");
        }
        return deleteById(id);
    }

    /**
     * 查询部门负责人
     * 用于流程引擎动态审批人解析
     */
    @GetMapping("/{id}/leader")
    public Result<Map<String, Object>> getDeptLeader(@PathVariable Long id) {
        SysDept dept = deptService.getById(id);
        if (dept == null) {
            return fail(ResultCode.NOT_FOUND, "部门不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("deptId", id);
        result.put("deptName", dept.getDeptName());
        result.put("leader", dept.getLeader());
        // TODO: 如果leader字段存储的是用户名，需要查询对应的用户ID
        // 目前假设leader字段存储的是用户ID
        result.put("leaderId", dept.getLeader());
        return success(result);
    }

    @GetMapping("/{id}/parent-leader")
    public Result<Map<String, Object>> getParentDeptLeader(@PathVariable Long id) {
        SysDept dept = deptService.getById(id);
        if (dept == null || dept.getParentId() == null || dept.getParentId() == 0) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("message", "无上级部门");
            return success(empty);
        }
        return getDeptLeader(dept.getParentId());
    }
}
