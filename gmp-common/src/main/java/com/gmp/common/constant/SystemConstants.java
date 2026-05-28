package com.gmp.common.constant;

/**
 * 系统常量定义 - 集中管理系统级别的常量值
 * 避免硬编码，提高代码可维护性
 *
 * @author hb176
 * @since 1.0.0
 */
public final class SystemConstants {

    private SystemConstants() {
        // 私有构造方法，防止实例化
    }

    // ==================== Token与认证相关 ====================
    /** Token在请求头中的键名 */
    public static final String TOKEN_HEADER = "Authorization";
    /** Token前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";
    /** Token在Cookie中的键名 */
    public static final String TOKEN_COOKIE = "GMP_TOKEN";
    /** Token有效期（秒）：30分钟 */
    public static final long TOKEN_EXPIRE_SECONDS = 1800L;
    /** 刷新Token有效期（秒）：7天 */
    public static final long REFRESH_TOKEN_EXPIRE_SECONDS = 604800L;

    // ==================== Redis相关 ====================
    /** Redis中Token黑名单的前缀 */
    public static final String REDIS_TOKEN_BLACKLIST_PREFIX = "gmp:token:blacklist:";
    /** Redis中用户活跃会话前缀（单设备登录） */
    public static final String REDIS_SESSION_PREFIX = "gmp:session:";
    /** Redis中验证码的前缀 */
    public static final String REDIS_CAPTCHA_PREFIX = "gmp:captcha:";
    /** Redis中分布式锁的前缀 */
    public static final String REDIS_LOCK_PREFIX = "gmp:lock:";
    /** Redis中用户权限缓存前缀 */
    public static final String REDIS_PERMISSION_PREFIX = "gmp:permission:";
    /** Redis中表单定义缓存前缀 */
    public static final String REDIS_FORM_DEF_PREFIX = "gmp:form:def:";
    /** Redis中流程定义缓存前缀 */
    public static final String REDIS_PROCESS_DEF_PREFIX = "gmp:process:def:";

    // ==================== 系统默认值 ====================
    /** 默认管理员角色编码 */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    /** 默认普通用户角色编码 */
    public static final String ROLE_USER = "ROLE_USER";
    /** 默认管理员账号 */
    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    /**
     * @deprecated 硬编码密码不安全，请使用 gmp.user.default-password 配置项
     */
    @Deprecated
    public static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    /** 默认分页大小 */
    public static final int DEFAULT_PAGE_SIZE = 10;
    /** 最大分页大小（防止恶意查询） */
    public static final int MAX_PAGE_SIZE = 500;

    // ==================== 数据状态 ====================
    /** 数据状态：正常/启用 */
    public static final int STATUS_ENABLED = 1;
    /** 数据状态：禁用 */
    public static final int STATUS_DISABLED = 0;
    /** 树形数据根节点的父节点ID */
    public static final long TREE_ROOT_PARENT_ID = 0L;

    // ==================== 流程相关常量 ====================
    /** 流程状态：草稿 */
    public static final String PROCESS_STATUS_DRAFT = "DRAFT";
    /** 流程状态：审批中 */
    public static final String PROCESS_STATUS_APPROVING = "APPROVING";
    /** 流程状态：已完成 */
    public static final String PROCESS_STATUS_COMPLETED = "COMPLETED";
    /** 流程状态：已驳回 */
    public static final String PROCESS_STATUS_REJECTED = "REJECTED";
    /** 流程状态：已撤回 */
    public static final String PROCESS_STATUS_WITHDRAWN = "WITHDRAWN";
    /** 流程状态：异常关闭 */
    public static final String PROCESS_STATUS_ABNORMAL = "ABNORMAL";

    // ==================== 表单相关常量 ====================
    /** 表单状态：草稿 */
    public static final String FORM_STATUS_DRAFT = "DRAFT";
    /** 表单状态：已发布 */
    public static final String FORM_STATUS_PUBLISHED = "PUBLISHED";
    /** 表单状态：已归档 */
    public static final String FORM_STATUS_ARCHIVED = "ARCHIVED";

    // ==================== 文件相关 ====================
    /** 文件上传最大大小（字节）：500MB */
    public static final long MAX_UPLOAD_SIZE = 524288000L;
}
