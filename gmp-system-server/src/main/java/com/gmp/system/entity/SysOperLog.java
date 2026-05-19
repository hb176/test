package com.gmp.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.framework.base.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日志实体 - 安全审计与问题追溯
 * 映射表: sys_oper_log
 *
 * @author hb176
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_oper_log")
public class SysOperLog extends CommonEntity {

    /** 操作类型: LOGIN/QUERY/ADD/UPDATE/DELETE/EXPORT/IMPORT */
    private String operType;

    /** 操作模块，如 用户管理 */
    private String module;

    /** 操作描述，如 新增用户admin */
    private String description;

    /** 请求URL */
    private String requestUrl;

    /** 请求方法: GET/POST/PUT/DELETE */
    private String requestMethod;

    /** 请求参数（敏感信息已脱敏） */
    private String requestParams;

    /** 请求耗时(ms) */
    private Long costTime;

    /** 操作结果: SUCCESS/FAIL */
    private String result;

    /** 失败时的错误信息 */
    private String errorMsg;

    /** 操作人ID */
    private Long operUserId;

    /** 操作人用户名 */
    private String operUserName;

    /** 操作人IP */
    private String operIp;
}
