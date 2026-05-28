package com.gmp.system.aspect;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gmp.common.security.SecurityContextHolder;
import com.gmp.framework.base.CommonEntity;
import com.gmp.system.entity.*;
import com.gmp.system.mapper.*;
import com.gmp.system.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private final SysOperLogMapper sysOperLogMapper;
    private final HttpServletRequest request;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysDeptService sysDeptService;
    private final SysMenuService sysMenuService;
    private final SysRoleService sysRoleService;
    private final SysUserService sysUserService;
    private final SysDictService sysDictService;
    private final SysConfigService sysConfigService;

    @Around("execution(* com.gmp..controller..*(..)) && !execution(* com.gmp.system.controller.LogController.*(..))")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long start = System.currentTimeMillis();
        String result = "SUCCESS";
        String errorMsg = null;
        String changeDetail = null;
        Object ret = null;

        Object[] args = point.getArgs();
        if ("PUT".equalsIgnoreCase(request.getMethod()) && args != null) {
            String url = request.getRequestURI();

            // 角色菜单权限变更
            if (url.matches(".*/menu/role/\\d+") && args.length >= 2 && args[1] instanceof List) {
                Long roleId = toLong(args[0]);
                @SuppressWarnings("unchecked")
                List<Long> newIds = (List<Long>) args[1];
                Set<Long> oldIds = sysRoleMenuMapper.selectByMap(Map.of("role_id", roleId))
                        .stream().map(SysRoleMenu::getMenuId).collect(Collectors.toSet());
                Set<Long> added = new HashSet<>(newIds); added.removeAll(oldIds);
                Set<Long> removed = new HashSet<>(oldIds); removed.removeAll(newIds);
                StringBuilder diff = new StringBuilder();
                if (!added.isEmpty()) diff.append("+").append(resolveMenuNames(added));
                if (!removed.isEmpty()) {
                    if (diff.length() > 0) diff.append(" ");
                    diff.append("-").append(resolveMenuNames(removed));
                }
                changeDetail = diff.length() > 0 ? diff.toString() : "菜单权限未变更";
            }
            // 用户角色变更
            else if (url.matches(".*/user/\\d+/roles") && args.length >= 2 && args[1] instanceof List) {
                @SuppressWarnings("unchecked")
                List<Long> newIds = (List<Long>) args[1];
                Long userId = toLong(args[0]);
                Set<Long> oldIds = sysUserRoleMapper.selectByMap(Map.of("user_id", userId))
                        .stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());
                Set<Long> added = new HashSet<>(newIds); added.removeAll(oldIds);
                Set<Long> removed = new HashSet<>(oldIds); removed.removeAll(newIds);
                StringBuilder diff = new StringBuilder();
                if (!added.isEmpty()) diff.append("+").append(resolveRoleNames(added));
                if (!removed.isEmpty()) {
                    if (diff.length() > 0) diff.append(" ");
                    diff.append("-").append(resolveRoleNames(removed));
                }
                changeDetail = diff.length() > 0 ? diff.toString() : "用户角色未变更";
            }
            // 普通实体更新 — 对比新旧值
            else {
                for (Object arg : args) {
                    if (arg instanceof CommonEntity entity && entity.getId() != null) {
                        CommonEntity old = getOldEntity(url, entity.getId());
                        if (old != null) {
                            JSONObject oldJson = JSON.parseObject(JSON.toJSONString(old));
                            JSONObject newJson = JSON.parseObject(JSON.toJSONString(entity));
                            StringBuilder diff = new StringBuilder();
                            for (Map.Entry<String, Object> e : newJson.entrySet()) {
                                String k = e.getKey();
                                Object nv = e.getValue();
                                Object ov = oldJson.get(k);
                                if (nv != null && !nv.equals(ov)
                                        && !"updateTime".equals(k) && !"updateBy".equals(k)
                                        && !"version".equals(k) && !"password".equals(k)) {
                                    String label = fieldLabel(k);
                                    String ovStr = resolveNames(k, ov);
                                    String nvStr = resolveNames(k, nv);
                                    diff.append(label).append(": ").append(ovStr).append(" → ").append(nvStr).append("; ");
                                }
                            }
                            if (diff.length() > 0) changeDetail = diff.toString().trim();
                        }
                        break;
                    }
                }
            }
        }

        try {
            ret = point.proceed();
            return ret;
        } catch (Throwable e) {
            result = "FAIL";
            errorMsg = e.getMessage();
            throw e;
        } finally {
            try {
                long cost = System.currentTimeMillis() - start;
                MethodSignature signature = (MethodSignature) point.getSignature();
                Method method = signature.getMethod();
                String methodName = point.getTarget().getClass().getSimpleName() + "." + method.getName();
                String httpMethod = request.getMethod();
                String url = request.getRequestURI();

                SysOperLog operLog = new SysOperLog();
                String operType = mapOperType(httpMethod, methodName);
                String module = extractModule(url);
                operLog.setOperType(operType);
                operLog.setModule(module);
                operLog.setDescription(buildDescription(operType, module, changeDetail, args));
                operLog.setRequestUrl(url);
                operLog.setRequestMethod(httpMethod);
                operLog.setCostTime(cost);
                operLog.setResult(result);
                operLog.setErrorMsg(errorMsg);
                operLog.setOperIp(request.getRemoteAddr());

                if (changeDetail != null) {
                    if (changeDetail.length() > 2000) changeDetail = changeDetail.substring(0, 2000);
                    operLog.setRequestParams(changeDetail);
                } else {
                    Object[] logArgs = point.getArgs();
                    if (logArgs != null && logArgs.length > 0) {
                        try {
                            String params = JSON.toJSONString(logArgs[0]);
                            if (params.length() > 2000) params = params.substring(0, 2000);
                            operLog.setRequestParams(params);
                        } catch (Exception ignored) {}
                    }
                }

                try {
                    Long currentUserId = SecurityContextHolder.getCurrentUserId();
                    operLog.setOperUserId(currentUserId);
                    if (currentUserId != null) {
                        SysUser currentUser = sysUserService.getById(currentUserId);
                        if (currentUser != null) {
                            operLog.setOperUserName(currentUser.getUserName());
                        }
                    }
                } catch (Exception ignored) {}

                sysOperLogMapper.insert(operLog);
            } catch (Exception e) {
                log.warn("操作日志记录失败: {}", e.getMessage());
            }
        }
    }

    private CommonEntity getOldEntity(String url, Long id) {
        if (url.contains("/system/user/role")) return null;
        if (url.contains("/system/user")) return sysUserService.getById(id);
        if (url.contains("/system/role")) return sysRoleService.getById(id);
        if (url.contains("/system/menu")) return sysMenuService.getById(id);
        if (url.contains("/system/dept")) return sysDeptService.getById(id);
        if (url.contains("/system/dict")) return sysDictService.getById(id);
        if (url.contains("/system/config")) return sysConfigService.getById(id);
        return null;
    }

    private Long toLong(Object v) {
        return v instanceof Long ? (Long) v : Long.valueOf(String.valueOf(v));
    }

    private String mapOperType(String httpMethod, String methodName) {
        if (methodName.contains("login") || methodName.contains("Login")) return "LOGIN";
        if (methodName.contains("logout")) return "LOGOUT";
        return switch (httpMethod) {
            case "GET" -> "QUERY";
            case "POST" -> "ADD";
            case "PUT" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> "OTHER";
        };
    }

    private String extractModule(String url) {
        if (url.contains("/system/user")) return "用户管理";
        if (url.contains("/system/role")) return "角色管理";
        if (url.contains("/system/menu")) return "菜单管理";
        if (url.contains("/system/dept")) return "部门管理";
        if (url.contains("/system/dict")) return "字典管理";
        if (url.contains("/system/config")) return "系统配置";
        if (url.contains("/system/log")) return "操作日志";
        if (url.contains("/auth/login")) return "登录认证";
        if (url.contains("/qms")) return "质量管理";
        if (url.contains("/dms")) return "文件管理";
        if (url.contains("/tms")) return "培训管理";
        if (url.contains("/qrs")) return "质量回顾";
        if (url.contains("/workflow")) return "流程管理";
        if (url.contains("/form")) return "表单管理";
        if (url.contains("/file")) return "文件服务";
        return "其他";
    }

    private String buildDescription(String operType, String module, String changeDetail, Object[] args) {
        StringBuilder sb = new StringBuilder();
        if ("ADD".equals(operType)) sb.append("新增");
        else if ("UPDATE".equals(operType)) sb.append("修改");
        else if ("DELETE".equals(operType)) sb.append("删除");
        else { sb.append("查询"); sb.append(module); return sb.toString(); }

        String url = request.getRequestURI();

        if ("ADD".equals(operType) && args != null && args.length > 0 && args[0] != null) {
            try {
                JSONObject obj = JSON.parseObject(JSON.toJSONString(args[0]));
                sb.append(buildAddDetail(module, obj));
            } catch (Exception ignored) {}
        }

        if ("UPDATE".equals(operType)) {
            if (url.matches(".*/menu/role/\\d+") && args.length >= 2 && args[1] instanceof List) {
                Long roleId = toLong(args[0]);
                SysRole role = sysRoleService.getById(roleId);
                String roleName = role != null ? role.getRoleName() : String.valueOf(roleId);
                sb.append("角色 ").append(roleName).append(" 菜单权限");
                if (changeDetail != null && !changeDetail.isEmpty()) sb.append(": ").append(changeDetail);
            } else if (url.matches(".*/user/\\d+/roles") && args.length >= 2 && args[1] instanceof List) {
                sb.append("用户角色");
                if (changeDetail != null && !changeDetail.isEmpty()) sb.append(": ").append(changeDetail);
            } else if (args != null && args.length > 0 && args[0] != null) {
                try {
                    JSONObject obj = JSON.parseObject(JSON.toJSONString(args[0]));
                    if (obj.containsKey("userName") || obj.containsKey("userId")) {
                        sb.append(obj.getString("userName"));
                        if (obj.containsKey("userId")) sb.append("(").append(obj.getString("userId")).append(")");
                    } else if (obj.containsKey("roleName")) {
                        sb.append(obj.getString("roleName"));
                    } else if (obj.containsKey("deptName")) {
                        sb.append(obj.getString("deptName"));
                    } else if (obj.containsKey("menuName")) {
                        sb.append(obj.getString("menuName"));
                    }
                } catch (Exception ignored) {}
                if (changeDetail != null && !changeDetail.isEmpty()) {
                    sb.append(": ").append(changeDetail);
                }
            }
            if (changeDetail != null && !changeDetail.isEmpty() && sb.indexOf(":") < 0) {
                sb.append(": ").append(changeDetail);
            }
        }

        if ("DELETE".equals(operType)) sb.append(module);

        String desc = sb.toString();
        return desc.length() > 500 ? desc.substring(0, 500) : desc;
    }

    private String buildAddDetail(String module, JSONObject obj) {
        StringBuilder sb = new StringBuilder();
        if (obj.containsKey("userName") || obj.containsKey("userId")) {
            sb.append("用户 ").append(obj.getString("userName")).append("(").append(obj.getString("userId")).append(")");
            if (obj.containsKey("deptId")) {
                String deptName = resolveDeptNames(String.valueOf(obj.get("deptId")));
                if (!deptName.equals(String.valueOf(obj.get("deptId")))) sb.append(" 部门:").append(deptName);
            }
        } else if (obj.containsKey("roleName")) {
            sb.append("角色 ").append(obj.getString("roleName")).append("(").append(obj.getString("roleCode")).append(")");
        } else if (obj.containsKey("deptName")) {
            sb.append("部门 ").append(obj.getString("deptName"));
        } else if (obj.containsKey("menuName")) {
            String type = obj.getIntValue("menuType") == 0 ? "目录" : obj.getIntValue("menuType") == 2 ? "按钮" : "菜单";
            sb.append(type).append(" ").append(obj.getString("menuName"));
        }
        return sb.toString();
    }

    private String fieldLabel(String field) {
        return switch (field) {
            case "deptId" -> "主部门";
            case "deptIds" -> "所属部门";
            case "deptName" -> "主部门名";
            case "roleName" -> "角色名";
            case "roleCode" -> "角色编码";
            case "userId" -> "登录账号";
            case "userName" -> "用户名";
            case "phone" -> "手机号";
            case "mail" -> "邮箱";
            case "status" -> "状态";
            case "description" -> "备注";
            case "roleLevel" -> "级别";
            case "deptCode" -> "部门编码";
            case "leader" -> "负责人";
            default -> field;
        };
    }

    private String resolveNames(String field, Object value) {
        if (value == null) return "空";
        String str = String.valueOf(value);
        if (str.isEmpty()) return "空";
        if ("deptIds".equals(field) || "deptId".equals(field)) {
            return resolveDeptNames(str);
        }
        if ("status".equals(field)) {
            return switch (str) {
                case "NORM", "1" -> "启用";
                case "DISABLED", "0" -> "禁用";
                case "LOCKED" -> "锁定";
                default -> str;
            };
        }
        return str;
    }

    private String resolveMenuNames(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return "";
        try {
            List<SysMenu> menus = sysMenuService.listByIds(new ArrayList<>(ids));
            Map<Long, String> idToName = menus.stream()
                    .collect(Collectors.toMap(SysMenu::getId, SysMenu::getMenuName, (a, b) -> a));
            return ids.stream()
                    .map(id -> idToName.getOrDefault(id, String.valueOf(id)))
                    .collect(Collectors.joining(","));
        } catch (Exception e) {
            log.warn("解析菜单名称失败: ids={}, error={}", ids, e.getMessage());
            return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        }
    }

    private String resolveRoleNames(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return "";
        try {
            List<SysRole> roles = sysRoleService.listByIds(new ArrayList<>(ids));
            Map<Long, String> idToName = roles.stream()
                    .collect(Collectors.toMap(SysRole::getId, SysRole::getRoleName, (a, b) -> a));
            return ids.stream()
                    .map(id -> idToName.getOrDefault(id, String.valueOf(id)))
                    .collect(Collectors.joining(","));
        } catch (Exception e) {
            log.warn("解析角色名称失败: ids={}, error={}", ids, e.getMessage());
            return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        }
    }

    private String resolveDeptNames(String ids) {
        if (ids == null || ids.trim().isEmpty()) return ids;
        try {
            List<Long> idList = Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(s -> {
                        try { return Long.parseLong(s); }
                        catch (NumberFormatException e) { return null; }
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
            if (idList.isEmpty()) return ids;

            List<SysDept> depts = sysDeptService.listByIds(idList);
            Map<Long, String> idToName = depts.stream()
                    .collect(Collectors.toMap(SysDept::getId, SysDept::getDeptName, (a, b) -> a));
            return idList.stream()
                    .map(id -> {
                        String name = idToName.get(id);
                        return name != null ? name : id + "(已删除)";
                    })
                    .collect(Collectors.joining(","));
        } catch (Exception e) {
            log.warn("解析部门名称失败: ids={}, error={}", ids, e.getMessage());
            return ids;
        }
    }
}
