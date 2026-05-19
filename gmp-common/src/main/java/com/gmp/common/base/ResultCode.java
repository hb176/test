package com.gmp.common.base;

/**
 * 统一响应状态码枚举 - 定义系统中所有API响应状态码
 * 分类规则：
 * 2xx: 成功
 * 4xx: 客户端错误（参数校验、认证、授权、资源不存在等）
 * 5xx: 服务器内部错误
 *
 * @author hb176
 * @since 1.0.0
 */
public enum ResultCode {

    // ==================== 成功状态码 (2xx) ====================
    /** 操作成功 */
    SUCCESS(200, "操作成功"),

    // ==================== 客户端错误 (4xx) ====================
    /** 参数校验失败 */
    BAD_REQUEST(400, "请求参数错误"),
    /** 未认证（未登录或Token过期） */
    UNAUTHORIZED(401, "未登录或Token已过期，请重新登录"),
    /** 无权限访问 */
    FORBIDDEN(403, "无权限访问该资源"),
    /** 资源不存在 */
    NOT_FOUND(404, "请求的资源不存在"),
    /** 请求方法不支持 */
    METHOD_NOT_ALLOWED(405, "不支持的请求方法"),
    /** 请求过于频繁（限流） */
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),
    /** 数据重复（如用户名已存在） */
    DUPLICATE_KEY(440, "数据已存在，请勿重复提交"),
    /** 数据校验失败 */
    VALIDATION_FAILED(441, "数据校验失败"),

    // ==================== 服务器错误 (5xx) ====================
    /** 服务器内部错误 */
    INTERNAL_ERROR(500, "服务器内部错误，请联系管理员"),
    /** 服务不可用 */
    SERVICE_UNAVAILABLE(503, "服务暂不可用，请稍后再试"),
    /** 数据库操作失败 */
    DATABASE_ERROR(550, "数据库操作失败"),
    /** 流程引擎异常 */
    WORKFLOW_ERROR(560, "流程引擎执行异常"),
    /** 表单引擎异常 */
    FORM_ENGINE_ERROR(570, "表单引擎处理异常"),
    /** 文件操作异常 */
    FILE_ERROR(580, "文件操作异常");

    /** 状态码 */
    private final int code;
    /** 默认消息 */
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
