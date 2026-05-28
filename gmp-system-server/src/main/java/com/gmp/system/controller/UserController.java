package com.gmp.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gmp.framework.base.CommonController;
import com.gmp.framework.base.PageResult;
import com.gmp.framework.base.Result;
import com.gmp.common.base.ResultCode;
import com.gmp.system.entity.SysConfig;
import com.gmp.system.entity.SysRole;
import com.gmp.system.entity.SysUser;
import com.gmp.system.entity.SysUserRole;
import com.gmp.system.mapper.SysRoleMapper;
import com.gmp.system.mapper.SysUserRoleMapper;
import com.gmp.system.entity.SysDept;
import com.gmp.system.service.SysConfigService;
import com.gmp.system.service.SysDeptService;
import com.gmp.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/user")
public class UserController extends CommonController<SysUserService, SysUser> {

    /** 默认密码（从配置读取，支持运行时修改） */
    @Value("${gmp.user.default-password:Gmp@123456}")
    private String defaultPassword;
    private final SysUserService sysUserService;
    private final SysDeptService sysDeptService;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysConfigService sysConfigService;
    private final PasswordEncoder passwordEncoder;

    @Override
    protected SysUserService getService() {
        return sysUserService;
    }

    @GetMapping("/page")
    public Result<PageResult<SysUser>> page(@RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "10") int pageSize,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) Long deptId,
                                             @RequestParam(required = false) String status) {
        var wrapper = new LambdaQueryWrapper<SysUser>();
        if (keyword != null) wrapper.and(w -> w.like(SysUser::getUserName, keyword)
                .or().like(SysUser::getUserId, keyword));
        if (deptId != null) wrapper.and(w ->
                w.eq(SysUser::getDeptId, deptId).or().apply("FIND_IN_SET({0}, dept_ids)", deptId));
        if (status != null) wrapper.eq(SysUser::getStatus, status);
        wrapper.orderByAsc(SysUser::getDeptId);
        return success(sysUserService.pageQuery(pageNum, pageSize, wrapper));
    }

    @GetMapping("/{id}")
    public Result<SysUser> getUser(@PathVariable Long id) {
        return getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    public Result<SysUser> create(@RequestBody SysUser user) {
        // 密码处理：未提供密码时使用默认密码
        String rawPassword = user.getPassword();
        if (rawPassword == null || rawPassword.isEmpty()) {
            rawPassword = defaultPassword;
            log.info("用户 {} 使用默认密码创建", user.getUserId());
        }
        Integer PASSWORD_EXPIRE_DAYS = Integer.parseInt(getConfigValue("password.expire.days", "90"));
        // 密码策略校验
        String validateResult = validatePassword(rawPassword);
        if (validateResult != null) {
            return fail(ResultCode.VALIDATION_FAILED, validateResult);
        }

        user.setPassword(passwordEncoder.encode(rawPassword));

        // 设置密码过期时间
        if (PASSWORD_EXPIRE_DAYS > 0) {
            user.setPasswordExpireTime(LocalDate.now().plusDays(PASSWORD_EXPIRE_DAYS));
        }

        return saveEntity(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<SysUser> update(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            SysUser existing = sysUserService.getById(id);
            if (existing != null) user.setPassword(existing.getPassword());
        }
        return updateEntity(user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        return deleteById(id);
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('system:user:resetPwd')")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        SysUser user = sysUserService.getById(id);
        if (user == null) return fail(ResultCode.NOT_FOUND);

        String newPassword = body.get("password");
        if (newPassword == null || newPassword.isEmpty()) {
            newPassword = defaultPassword;
        }

        // 密码策略校验
        String validateResult = validatePassword(newPassword);
        if (validateResult != null) {
            return fail(ResultCode.VALIDATION_FAILED, validateResult);
        }
        Integer PASSWORD_EXPIRE_DAYS = Integer.parseInt(getConfigValue("password.expire.days", "90"));
        user.setPassword(passwordEncoder.encode(newPassword));
        // 重置密码后更新过期时间
        if (PASSWORD_EXPIRE_DAYS > 0) {
            user.setPasswordExpireTime(LocalDate.now().plusDays(PASSWORD_EXPIRE_DAYS));
        }
        sysUserService.updateById(user);
        log.info("密码重置: userId={}", user.getUserId());
        return success();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        SysUser user = sysUserService.getById(id);
        if (user == null) return fail(ResultCode.NOT_FOUND);
        user.setStatus(body.get("status"));
        sysUserService.updateById(user);
        return success();
    }

    @GetMapping("/{id}/roles")
    public Result<List<Long>> getUserRoles(@PathVariable Long id) {
        List<Long> roleIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id))
                .stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        return success(roleIds);
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> setUserRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        for (Long roleId : roleIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(id);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
        return success();
    }

    /**
     * 根据角色编码查询拥有该角色的用户列表
     * 用于流程引擎动态审批人解析
     */
    @GetMapping("/list-by-role")
    public Result<List<Map<String, Object>>> getUsersByRole(@RequestParam String roleCode) {
        // 1. 查询角色
        SysRole role = roleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, roleCode));
        if (role == null) {
            return success(List.of());
        }

        // 2. 查询拥有该角色的用户ID列表
        List<Long> userIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, role.getId()))
                .stream().map(SysUserRole::getUserId).collect(Collectors.toList());

        if (userIds.isEmpty()) {
            return success(List.of());
        }

        // 3. 查询用户信息
        List<SysUser> users = sysUserService.listByIds(userIds);
        List<Map<String, Object>> result = users.stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("userId", user.getUserId());
            map.put("userName", user.getUserName());
            map.put("deptId", user.getDeptId());
            return map;
        }).collect(Collectors.toList());

        return success(result);
    }

    /**
     * 查询用户所在部门的负责人
     * 用于流程引擎 BY_REPORT_LINE / BY_DEPT_LEADER 策略
     */
    @GetMapping("/{id}/dept-leader")
    public Result<Map<String, Object>> getUserDeptLeader(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null || user.getDeptId() == null) {
            return success(Map.of("message", "用户无部门"));
        }
        SysDept dept = sysDeptService.getById(user.getDeptId());
        if (dept == null) {
            return success(Map.of("message", "部门不存在"));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("userId", id);
        result.put("deptId", dept.getId());
        result.put("deptName", dept.getDeptName());
        result.put("leaderName", dept.getLeader());
        if (dept.getLeader() != null && !dept.getLeader().isEmpty()) {
            SysUser leader = sysUserService.lambdaQuery()
                    .eq(SysUser::getUserName, dept.getLeader()).one();
            if (leader == null) {
                leader = sysUserService.lambdaQuery()
                        .eq(SysUser::getUserId, dept.getLeader()).one();
            }
            if (leader != null) {
                result.put("leaderId", leader.getId());
                result.put("leaderUserId", leader.getUserId());
            }
        }
        result.put("parentDeptId", dept.getParentId());
        return success(result);
    }

    /**
     * 密码策略校验（动态从数据库读取配置）
     *
     * @param password 待校验密码
     * @return 校验失败原因，校验通过返回null
     */
    private String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "密码不能为空";
        }

        // 从数据库加载密码策略配置
        String level = getConfigValue("password.level", "medium");
        int minLength = Integer.parseInt(getConfigValue("password.min.length", "8"));
        int maxLength = Integer.parseInt(getConfigValue("password.max.length", "32"));

        // 长度校验
        if (password.length() < minLength) {
            return "密码长度不能少于" + minLength + "位";
        }
        if (password.length() > maxLength) {
            return "密码长度不能超过" + maxLength + "位";
        }

        // 根据等级校验
        switch (level) {
            case "low":
                // 低等级：只要求数字
                if (!password.matches(".*\\d.*")) {
                    return "密码必须包含至少一个数字";
                }
                break;

            case "medium":
                // 中等级：数字 + 英文字母
                if (!password.matches(".*\\d.*")) {
                    return "密码必须包含至少一个数字";
                }
                if (!password.matches(".*[a-zA-Z].*")) {
                    return "密码必须包含至少一个英文字母";
                }
                break;

            case "high":
                // 高等级：数字 + 英文字母 + 特殊字符
                if (!password.matches(".*\\d.*")) {
                    return "密码必须包含至少一个数字";
                }
                if (!password.matches(".*[a-zA-Z].*")) {
                    return "密码必须包含至少一个英文字母";
                }
                if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
                    return "密码必须包含至少一个特殊字符";
                }
                break;
        }

        return null;
    }

    /**
     * 获取配置值（带默认值）
     */
    private String getConfigValue(String key, String defaultValue) {
        SysConfig config = sysConfigService.getOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        return config != null && config.getConfigValue() != null
                ? config.getConfigValue()
                : defaultValue;
    }
}
