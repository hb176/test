package com.gmp.system.controller;

import com.gmp.common.base.ResultCode;
import com.gmp.framework.base.CommonController;
import com.gmp.framework.base.Result;
import com.gmp.system.entity.SysDept;
import com.gmp.system.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public Result<Void> delete(@PathVariable Long id) {
        long childCount = deptService.lambdaQuery()
                .eq(SysDept::getParentId, id)
                .count();
        if (childCount > 0) {
            return fail(ResultCode.VALIDATION_FAILED, "该部门下存在子部门，无法删除");
        }
        return deleteById(id);
    }
}
