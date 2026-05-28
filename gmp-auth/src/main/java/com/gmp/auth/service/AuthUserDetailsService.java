package com.gmp.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gmp.auth.entity.*;
import com.gmp.auth.mapper.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthUserDetailsService implements UserDetailsService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;

    /**
     * Spring Security 认证核心方法：根据 userId 查询用户
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUserId, username)
                        .last("LIMIT 1")
        );

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 查询用户权限（角色 + 菜单permission），设置为 Spring Security authority
        List<org.springframework.security.core.GrantedAuthority> authorities = new ArrayList<>();
        try {
            List<SysUserRole> urs = userRoleMapper.selectList(
                    new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId()));
            if (!urs.isEmpty()) {
                List<Long> roleIds = urs.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
                // 角色编码作为 authority
                List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
                roles.stream()
                        .filter(r -> r.getStatus() != null && r.getStatus() == 1)
                        .map(r -> new org.springframework.security.core.authority.SimpleGrantedAuthority(r.getRoleCode()))
                        .forEach(authorities::add);

                // 菜单权限标识作为 authority（如 system:user:add）
                List<SysRoleMenu> rms = roleMenuMapper.selectList(
                        new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds));
                if (!rms.isEmpty()) {
                    Set<Long> menuIds = rms.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toSet());
                    List<SysMenu> menus = menuMapper.selectBatchIds(menuIds);
                    if (menus != null) {
                        menus.stream()
                                .filter(m -> m.getPermission() != null && !m.getPermission().isEmpty()
                                        && (m.getVisible() == null || m.getVisible() == 1))
                                .map(m -> new org.springframework.security.core.authority.SimpleGrantedAuthority(m.getPermission()))
                                .forEach(authorities::add);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("查询用户权限失败: {}", e.getMessage());
        }

        return User.builder()
                .username(user.getUserId())
                .password(user.getPassword())
                .disabled("DISABLED".equals(user.getStatus()))
                .accountLocked(false)
                .accountExpired(false)
                .credentialsExpired(false)
                .authorities(authorities)
                .build();
    }

    /**
     * 根据 userId 获取完整用户信息
     */
    public SysUser getUserByUsername(String username) {
        return sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUserId, username)
                        .last("LIMIT 1")
        );
    }

    /**
     * 根据 userId 获取数据库主键 ID
     */
    public Long getIdByUsername(String username) {
        // 修复：只查ID + 限制1条 + 空值安全
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .select(SysUser::getId)
                        .eq(SysUser::getUserId, username)
                        .last("LIMIT 1")
        );
        return user == null ? null : user.getId();
    }

    /**
     * 根据 userId 获取用户部门ID
     */
    public Long getDeptIdByUsername(String username) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .select(SysUser::getDeptId)
                        .eq(SysUser::getUserId, username)
                        .last("LIMIT 1")
        );
        return user == null ? null : user.getDeptId();
    }

    /**
     * 根据 userId 获取用户角色列表（逗号分隔的角色编码）
     */
    public String getRolesByUsername(String username) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .select(SysUser::getId)
                        .eq(SysUser::getUserId, username)
                        .last("LIMIT 1")
        );
        if (user == null) return "";

        // 只查启用状态的角色
        List<SysUserRole> urs = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, user.getId())
                        .inSql(SysUserRole::getRoleId,
                                "SELECT id FROM sys_role WHERE status = 1 AND deleted = 0"));
        if (urs.isEmpty()) return "";

        List<Long> roleIds = urs.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
        return roles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.joining(","));
    }

    /**
     * 获取用户的权限标识列表（逗号分隔，如 system:user:add,system:role:edit）
     */
    public String getPermissionsByUsername(String username) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .select(SysUser::getId)
                        .eq(SysUser::getUserId, username)
                        .last("LIMIT 1")
        );
        if (user == null) return "";

        List<SysUserRole> urs = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, user.getId())
                        .inSql(SysUserRole::getRoleId,
                                "SELECT id FROM sys_role WHERE status = 1 AND deleted = 0"));
        if (urs.isEmpty()) return "";

        List<Long> roleIds = urs.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        List<SysRoleMenu> rms = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds));
        if (rms.isEmpty()) return "";

        Set<Long> menuIds = rms.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toSet());
        List<SysMenu> menus = menuMapper.selectBatchIds(menuIds);
        if (menus == null) return "";

        return menus.stream()
                .filter(m -> m.getPermission() != null && !m.getPermission().isEmpty()
                        && (m.getVisible() == null || m.getVisible() == 1))
                .map(SysMenu::getPermission)
                .distinct()
                .collect(Collectors.joining(","));
    }

    /**
     * 获取用户的菜单权限列表
     */
    public List<Map<String, Object>> getUserMenus(String username) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .select(SysUser::getId)
                        .eq(SysUser::getUserId, username)
                        .last("LIMIT 1")
        );
        if (user == null) return Collections.emptyList();

        // 查用户角色（只取启用状态的角色）
        List<SysUserRole> urs = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, user.getId())
                        .inSql(SysUserRole::getRoleId,
                                "SELECT id FROM sys_role WHERE status = 1 AND deleted = 0"));
        List<Long> roleIds = urs.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        if (roleIds.isEmpty()) return Collections.emptyList();

        // 查角色关联的菜单ID
        List<SysRoleMenu> rms = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds));
        Set<Long> menuIds = rms.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toSet());
        if (menuIds.isEmpty()) return Collections.emptyList();

        // 一次加载所有可能需要的菜单（直接关联 + 全表用于补全父级）
        List<SysMenu> allMenus = menuMapper.selectList(null);
        Map<Long, SysMenu> menuMap = allMenus.stream()
                .collect(Collectors.toMap(SysMenu::getId, m -> m, (a, b) -> a));

        // 补全父级菜单链
        Set<Long> allMenuIds = new HashSet<>(menuIds);
        for (Long menuId : menuIds) {
            SysMenu m = menuMap.get(menuId);
            while (m != null && m.getParentId() != null && m.getParentId() > 0) {
                if (!allMenuIds.add(m.getParentId())) break;
                m = menuMap.get(m.getParentId());
            }
        }

        // 构建结果
        return allMenuIds.stream()
                .map(menuMap::get)
                .filter(m -> m != null && (m.getVisible() == null || m.getVisible() == 1))
                .sorted(Comparator.comparingInt(m -> m.getSortOrder() != null ? m.getSortOrder() : 999))
                .map(m -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", m.getId());
                    map.put("parentId", m.getParentId());
                    map.put("menuName", m.getMenuName());
                    map.put("menuType", m.getMenuType());
                    map.put("path", m.getPath());
                    map.put("icon", m.getIcon());
                    map.put("permission", m.getPermission());
                    map.put("sortOrder", m.getSortOrder());
                    map.put("formKey", m.getFormKey());
                    return map;
                })
                .collect(Collectors.toList());
    }
}