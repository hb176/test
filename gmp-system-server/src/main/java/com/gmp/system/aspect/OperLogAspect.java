package com.gmp.system.aspect;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gmp.common.security.SecurityContextHolder;
import com.gmp.framework.base.CommonEntity;
import com.gmp.system.entity.SysOperLog;
import com.gmp.system.mapper.SysOperLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private final SysOperLogMapper sysOperLogMapper;
    private final HttpServletRequest request;
    @Autowired
    private ApplicationContext ctx;

    @Around("execution(* com.gmp..controller..*(..)) && !execution(* com.gmp.system.controller.LogController.*(..))")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long start = System.currentTimeMillis();
        String result = "SUCCESS";
        String errorMsg = null;
        String changeDetail = null;
        Object ret = null;

        // PUT 请求：记录新旧变更
        Object[] args = point.getArgs();
        if ("PUT".equalsIgnoreCase(request.getMethod()) && args != null) {
            String url = request.getRequestURI();
            // 角色菜单权限变更 — 对比新旧
            if (url.matches(".*/menu/role/\\d+") && args.length >= 2 && args[1] instanceof List) {
                Long roleId = args[0] instanceof Long ? (Long) args[0] : Long.valueOf(String.valueOf(args[0]));
                @SuppressWarnings("unchecked")
                List<Long> newIds = (List<Long>) args[1];
                try {
                    Object rmMapper = ctx.getBean("sysRoleMenuMapper");
                    Method selectByMap = rmMapper.getClass().getMethod("selectByMap", java.util.Map.class);
                    List<?> oldRecords = (List<?>) selectByMap.invoke(rmMapper, java.util.Map.of("role_id", roleId));
                    java.util.Set<Long> oldIds = new java.util.HashSet<>();
                    for (Object r : oldRecords) oldIds.add((Long) r.getClass().getMethod("getMenuId").invoke(r));
                    java.util.Set<Long> added = new java.util.HashSet<>(newIds); added.removeAll(oldIds);
                    java.util.Set<Long> removed = new java.util.HashSet<>(oldIds); removed.removeAll(newIds);
                    StringBuilder diff = new StringBuilder();
                    if (!added.isEmpty()) diff.append("+").append(resolveMenuNames(added));
                    if (!removed.isEmpty()) {
                        if (diff.length() > 0) diff.append(" ");
                        diff.append("-").append(resolveMenuNames(removed));
                    }
                    changeDetail = diff.length() > 0 ? diff.toString() : "菜单权限未变更";
                } catch (Exception e) { changeDetail = "菜单权限已更新(共" + newIds.size() + "项)"; }
            }
            // 用户角色变更 — 对比新旧
            else if (url.matches(".*/user/\\d+/roles") && args.length >= 2 && args[1] instanceof List) {
                @SuppressWarnings("unchecked")
                List<Long> newIds = (List<Long>) args[1];
                try {
                    Object urMapper = ctx.getBean("sysUserRoleMapper");
                    Method selectByMap = urMapper.getClass().getMethod("selectByMap", java.util.Map.class);
                    Long userId = args[0] instanceof Long ? (Long) args[0] : Long.valueOf(String.valueOf(args[0]));
                    List<?> oldRecords = (List<?>) selectByMap.invoke(urMapper, java.util.Map.of("user_id", userId));
                    java.util.Set<Long> oldIds = new java.util.HashSet<>();
                    for (Object r : oldRecords) oldIds.add((Long) r.getClass().getMethod("getRoleId").invoke(r));
                    java.util.Set<Long> added = new java.util.HashSet<>(newIds); added.removeAll(oldIds);
                    java.util.Set<Long> removed = new java.util.HashSet<>(oldIds); removed.removeAll(newIds);
                    StringBuilder diff = new StringBuilder();
                    if (!added.isEmpty()) diff.append("+").append(resolveRoleNames(added));
                    if (!removed.isEmpty()) {
                        if (diff.length() > 0) diff.append(" ");
                        diff.append("-").append(resolveRoleNames(removed));
                    }
                    changeDetail = diff.length() > 0 ? diff.toString() : "用户角色未变更";
                } catch (Exception e) { changeDetail = "用户角色已更新(共" + newIds.size() + "项)"; }
            }
            // 普通实体更新
            else {
                for (Object arg : args) {
                    if (arg instanceof CommonEntity entity && entity.getId() != null) {
                        try {
                            String serviceBean = findServiceBean(url);
                            if (serviceBean != null && ctx.containsBean(serviceBean)) {
                                Object svc = ctx.getBean(serviceBean);
                                Method getById = svc.getClass().getMethod("getById", java.io.Serializable.class);
                                Object old = getById.invoke(svc, entity.getId());
                                if (old != null) {
                                    JSONObject oldJson = JSON.parseObject(JSON.toJSONString(old));
                                    JSONObject newJson = JSON.parseObject(JSON.toJSONString(entity));
                                    StringBuilder diff = new StringBuilder();
                                    for (Map.Entry<String, Object> e : newJson.entrySet()) {
                                        String k = e.getKey();
                                        Object nv = e.getValue();
                                        Object ov = oldJson.get(k);
                                        if (nv != null && !nv.equals(ov) && !"updateTime".equals(k) && !"updateBy".equals(k) && !"version".equals(k) && !"password".equals(k)) {
                                            String label = fieldLabel(k);
                                            String ovStr = resolveNames(k, ov);
                                            String nvStr = resolveNames(k, nv);
                                            diff.append(label).append(": ").append(ovStr).append(" → ").append(nvStr).append("; ");
                                        }
                                    }
                                    if (diff.length() > 0) changeDetail = diff.toString().trim();
                                }
                            }
                        } catch (Exception ignored) {}
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
                operLog.setDescription(buildDescription(operType, module, changeDetail, point.getArgs()));
                operLog.setRequestUrl(url);
                operLog.setRequestMethod(httpMethod);
                operLog.setCostTime(cost);
                operLog.setResult(result);
                operLog.setErrorMsg(errorMsg);
                operLog.setOperIp(request.getRemoteAddr());

                // PUT 请求记录变更详情，其他记录请求参数
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
                    operLog.setOperUserId(SecurityContextHolder.getCurrentUserId());
                    operLog.setOperUserName("admin"); // TODO: get from token
                } catch (Exception ignored) {}

                sysOperLogMapper.insert(operLog);
            } catch (Exception e) {
                log.warn("操作日志记录失败: {}", e.getMessage());
            }
        }
    }

    private String mapOperType(String httpMethod, String methodName) {
        if (methodName.contains("login") || methodName.contains("Login")) return "LOGIN";
        if (methodName.contains("logout")) return "LOGOUT";
        return switch (httpMethod) {
            case "GET" -> methodName.contains("page") || methodName.contains("list") || methodName.contains("tree") ? "QUERY" : "QUERY";
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

    private String findServiceBean(String url) {
        if (url.contains("/system/user/role")) return null;
        if (url.contains("/system/user")) return "sysUserService";
        if (url.contains("/system/role")) return "sysRoleService";
        if (url.contains("/system/menu")) return "sysMenuService";
        if (url.contains("/system/dept")) return "sysDeptService";
        if (url.contains("/system/dict")) return "sysDictService";
        if (url.contains("/system/config")) return "sysConfigService";
        return null;
    }

    private String buildDescription(String operType, String module, String changeDetail, Object[] args) {
        StringBuilder sb = new StringBuilder();
        if ("ADD".equals(operType)) sb.append("新增");
        else if ("UPDATE".equals(operType)) sb.append("修改");
        else if ("DELETE".equals(operType)) sb.append("删除");
        else { sb.append("查询"); sb.append(module); return sb.toString(); }

        String url = request.getRequestURI();

        // ADD
        if ("ADD".equals(operType) && args != null && args.length > 0 && args[0] != null) {
            try {
                JSONObject obj = JSON.parseObject(JSON.toJSONString(args[0]));
                sb.append(buildAddDetail(module, obj));
            } catch (Exception ignored) {}
        }

        // UPDATE
        if ("UPDATE".equals(operType)) {
            // Batch operations
            if (url.matches(".*/menu/role/\\d+") && args.length >= 2 && args[1] instanceof List) {
                Long roleId = args[0] instanceof Long ? (Long) args[0] : Long.valueOf(String.valueOf(args[0]));
                try {
                    Object roleSvc = ctx.getBean("sysRoleService");
                    Object role = roleSvc.getClass().getMethod("getById", java.io.Serializable.class).invoke(roleSvc, roleId);
                    String roleName = role != null ? (String) role.getClass().getMethod("getRoleName").invoke(role) : String.valueOf(roleId);
                    sb.append("角色 ").append(roleName).append(" 菜单权限");
                } catch (Exception e) {
                    sb.append("角色菜单权限");
                }
                if (changeDetail != null && !changeDetail.isEmpty()) sb.append(": ").append(changeDetail);
            } else if (url.matches(".*/user/\\d+/roles") && args.length >= 2 && args[1] instanceof List) {
                sb.append("用户角色");
                if (changeDetail != null && !changeDetail.isEmpty()) sb.append(": ").append(changeDetail);
            } else if (args != null && args.length > 0 && args[0] != null) {
                // Regular entity update
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
            // Show dept if present
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
            case "deptId" -> "主部门ID";
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
        // deptIds / deptId: resolve to department names
        if ("deptIds".equals(field) || "deptId".equals(field)) {
            return resolveDeptNames(str);
        }
        // status field
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

    private String resolveMenuNames(java.util.Set<Long> ids) {
        if (ids.isEmpty()) return "";
        try {
            Object svc = ctx.getBean("sysMenuService");
            Method listMethod = svc.getClass().getMethod("list");
            java.util.List<?> menus = (java.util.List<?>) listMethod.invoke(svc);
            StringBuilder sb = new StringBuilder();
            for (Long id : ids) {
                for (Object m : menus) {
                    Method getId = m.getClass().getMethod("getId");
                    Method getName = m.getClass().getMethod("getMenuName");
                    if (id.equals(getId.invoke(m))) {
                        if (sb.length() > 0) sb.append(",");
                        sb.append(getName.invoke(m));
                        break;
                    }
                }
            }
            return sb.length() > 0 ? sb.toString() : String.valueOf(ids.size()) + "个";
        } catch (Exception e) {
            return String.valueOf(ids.size()) + "个";
        }
    }

    private String resolveRoleNames(java.util.Set<Long> ids) {
        if (ids.isEmpty()) return "";
        try {
            Object svc = ctx.getBean("sysRoleService");
            Method listMethod = svc.getClass().getMethod("list");
            java.util.List<?> roles = (java.util.List<?>) listMethod.invoke(svc);
            StringBuilder sb = new StringBuilder();
            for (Long id : ids) {
                for (Object r : roles) {
                    Method getId = r.getClass().getMethod("getId");
                    Method getName = r.getClass().getMethod("getRoleName");
                    if (id.equals(getId.invoke(r))) {
                        if (sb.length() > 0) sb.append(",");
                        sb.append(getName.invoke(r));
                        break;
                    }
                }
            }
            return sb.length() > 0 ? sb.toString() : String.valueOf(ids.size()) + "个";
        } catch (Exception e) {
            return String.valueOf(ids.size()) + "个";
        }
    }

    private String resolveDeptNames(String ids) {
        try {
            Object svc = ctx.getBean("sysDeptService");
            Method listMethod = svc.getClass().getMethod("list");
            @SuppressWarnings("unchecked")
            java.util.List<?> depts = (java.util.List<?>) listMethod.invoke(svc);
            StringBuilder sb = new StringBuilder();
            for (String idStr : ids.split(",")) {
                try {
                    long id = Long.parseLong(idStr.trim());
                    for (Object d : depts) {
                        Method getId = d.getClass().getMethod("getId");
                        Method getName = d.getClass().getMethod("getDeptName");
                        if (id == (Long) getId.invoke(d)) {
                            if (sb.length() > 0) sb.append(",");
                            sb.append(getName.invoke(d));
                            break;
                        }
                    }
                } catch (Exception ignored) {}
            }
            return sb.length() > 0 ? sb.toString() : ids;
        } catch (Exception e) {
            return ids;
        }
    }

    private String getParams(Object[] args) {
        if (args != null && args.length > 0) {
            try {
                String s = JSON.toJSONString(args[0]);
                return s.length() > 2000 ? s.substring(0, 2000) : s;
            } catch (Exception ignored) {}
        }
        return null;
    }
}
