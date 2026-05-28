package com.gmp.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gmp.framework.base.CommonController;
import com.gmp.framework.base.Result;
import com.gmp.system.entity.SysRole;
import com.gmp.system.entity.SysRoleMenu;
import com.gmp.system.mapper.SysRoleMenuMapper;
import com.gmp.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/role")
public class RoleController extends CommonController<SysRoleService, SysRole> {

    private final SysRoleService sysRoleService;
    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    protected SysRoleService getService() {
        return sysRoleService;
    }

    @GetMapping("/list")
    public Result<List<SysRole>> listAll() {
        return success(sysRoleService.list());
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getRole(@PathVariable Long id) {
        SysRole role = sysRoleService.getById(id);
        if (role == null) return fail(com.gmp.common.base.ResultCode.NOT_FOUND);
        List<Long> menuIds = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id))
                .stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", role.getId());
        result.put("roleCode", role.getRoleCode());
        result.put("roleName", role.getRoleName());
        result.put("description", role.getDescription());
        result.put("roleLevel", role.getRoleLevel());
        result.put("dataScope", role.getDataScope());
        result.put("status", role.getStatus());
        result.put("isSystem", role.getIsSystem());
        result.put("menuIds", menuIds);
        return success(result);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:role:add')")
    public Result<SysRole> create(@RequestBody SysRole role) {
        if (sysRoleService.lambdaQuery().eq(SysRole::getRoleCode, role.getRoleCode()).count() > 0) {
            return fail(com.gmp.common.base.ResultCode.VALIDATION_FAILED, "角色编码已存在");
        }
        if (sysRoleService.lambdaQuery().eq(SysRole::getRoleName, role.getRoleName()).count() > 0) {
            return fail(com.gmp.common.base.ResultCode.VALIDATION_FAILED, "角色名称已存在");
        }
        return saveEntity(role);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<SysRole> update(@PathVariable Long id, @RequestBody SysRole role) {
        if (sysRoleService.lambdaQuery().eq(SysRole::getRoleCode, role.getRoleCode()).ne(SysRole::getId, id).count() > 0) {
            return fail(com.gmp.common.base.ResultCode.VALIDATION_FAILED, "角色编码已存在");
        }
        if (sysRoleService.lambdaQuery().eq(SysRole::getRoleName, role.getRoleName()).ne(SysRole::getId, id).count() > 0) {
            return fail(com.gmp.common.base.ResultCode.VALIDATION_FAILED, "角色名称已存在");
        }
        role.setId(id);
        return updateEntity(role);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        // 删除角色时同时清除角色-菜单关联
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        return deleteById(id);
    }
}
