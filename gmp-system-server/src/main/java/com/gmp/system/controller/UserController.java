package com.gmp.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gmp.framework.base.CommonController;
import com.gmp.framework.base.PageResult;
import com.gmp.framework.base.Result;
import com.gmp.common.base.ResultCode;
import com.gmp.system.entity.SysUser;
import com.gmp.system.entity.SysUserRole;
import com.gmp.system.mapper.SysUserRoleMapper;
import com.gmp.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/user")
public class UserController extends CommonController<SysUserService, SysUser> {

    private final SysUserService sysUserService;
    private final SysUserRoleMapper userRoleMapper;

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
    public Result<SysUser> create(@RequestBody SysUser user) {
        return saveEntity(user);
    }

    @PutMapping("/{id}")
    public Result<SysUser> update(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        return updateEntity(user);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return deleteById(id);
    }

    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        SysUser user = sysUserService.getById(id);
        if (user == null) return fail(ResultCode.NOT_FOUND);
        user.setPassword(body.get("password"));
        sysUserService.updateById(user);
        log.info("密码重置: userId={}", user.getUserId());
        return success();
    }

    @PutMapping("/{id}/status")
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
}
