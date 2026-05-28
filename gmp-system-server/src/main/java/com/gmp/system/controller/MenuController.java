package com.gmp.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gmp.framework.base.CommonController;
import com.gmp.framework.base.Result;
import com.gmp.system.entity.SysMenu;
import com.gmp.system.entity.SysRoleMenu;
import com.gmp.system.mapper.SysRoleMenuMapper;
import com.gmp.system.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/menu")
public class MenuController extends CommonController<SysMenuService, SysMenu> {

    private final SysMenuService sysMenuService;
    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    protected SysMenuService getService() {
        return sysMenuService;
    }

    @GetMapping("/tree")
    public Result<List<SysMenu>> tree() {
        return success(sysMenuService.lambdaQuery()
                .orderByAsc(SysMenu::getSortOrder)
                .list());
    }

    @GetMapping("/role/{roleId}")
    public Result<List<Long>> getRoleMenus(@PathVariable Long roleId) {
        List<Long> menuIds = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId))
                .stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
        return success(menuIds);
    }

    @PutMapping("/role/{roleId}")
    @PreAuthorize("hasAuthority('system:role:menu')")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> setRoleMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        // 删除旧关联
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        // 插入新关联
        for (Long menuId : menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            roleMenuMapper.insert(rm);
        }
        log.info("角色菜单已保存: roleId={}, menuIds={}", roleId, menuIds);
        return success();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:menu:add')")
    public Result<SysMenu> create(@RequestBody SysMenu menu) {
        return saveEntity(menu);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:edit')")
    public Result<SysMenu> update(@PathVariable Long id, @RequestBody SysMenu menu) {
        menu.setId(id);
        return updateEntity(menu);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        return deleteById(id);
    }
}
