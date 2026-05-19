package com.gmp.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gmp.auth.entity.SysUser;
import com.gmp.auth.mapper.SysUserMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthUserDetailsService implements UserDetailsService {

    private final SysUserMapper sysUserMapper;

    /**
     * Spring Security 认证核心方法：根据 userId 查询用户
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 修复1：添加 last("LIMIT 1")，强制只查1条，杜绝多条数据报错
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUserId, username)
                        .last("LIMIT 1")
        );

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 修复2：显式声明所有状态，逻辑简化，无冗余代码
        return User.builder()
                .username(user.getUserId())
                .password(user.getPassword())
                .disabled("DISABLED".equals(user.getStatus()))
                .accountLocked(false)                  // 账号未锁定
                .accountExpired(false)                 // 账号未过期
                .credentialsExpired(false)             // 密码未过期
                .authorities(Collections.emptyList())  // 暂无权限
                .build();
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
}