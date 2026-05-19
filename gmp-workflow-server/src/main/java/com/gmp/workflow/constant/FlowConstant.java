package com.gmp.workflow.constant;

public class FlowConstant {
    public static final String APP_SN = "flow";
    public static final int DEL_FLAG_0 = 0;
    public static final int DEL_FLAG_1 = 1;

    /** 自定义属性名 */
    public static final String IS_EDITDATA = "iseditdata";
    public static final String NODE_TYPE = "nodetype";
    public static final String ASSIGNEE_TYPE = "assigneeType";

    /** 提交人节点名称 */
    public static final String FLOW_SUBMITTER = "提交人";
    /** 提交人的变量名称 用于驳回 */
    public static final String FLOW_SUBMITTER_VAR = "initiator";
    /** 发起人的工号 */
    public static final String FLOW_STARTER_CODE_VAR = "starterCode";
    /** juel表达式解析类 */
    public static final String FLOW_JUEL = "fuel";
    /** 自动跳过节点设置属性 */
    public static final String FLOWABLE_SKIP_EXPRESSION_ENABLED = "_FLOWABLE_SKIP_EXPRESSION_ENABLED";

    /** mybatis的扫描mapper包路径 */
    public static final String MAPPER_SCAN_MYBATIS_PLUS = "com.gmp.*.mapper.*";
    /** MD5加盐 */
    public static final String MD5_PREFIX = "dragon-flow-lwj-xie-2021";
    /** sessionid的名称 */
    public static final String DRAGON_SESSION_ID = "DRAGON_SESSION_ID";
    /** 标记流程请求是后台 */
    public static final String FLOW_ADMIN = "flow_admin";
    /** 标记流程请求是前台 */
    public static final String FLOW_FRONT = "flow_front";

    /** 登录用户的session中的key值 */
    public static final String LOGIN_USER = "login_user";
    public static final String LOGIN_ROLES = "login_roles";
    public static final String LOGIN_USER_ACLS = "login_user_acls";
    public static final String LOGIN_MODULES = "login_modules";

    /** 缓存模块名称 */
    public static final String CACHE_PROCESS_ACTIVITYS = "cache-process-activitys";
    public static final String CACHE_PROCESS_HIGHLIGHTEDNODES = "cache-process-highlightednodes";
    public static final String CACHE_PROCESS_INSTANCE = "cache-process-instance";
    public static final String CACHE_PROCESS_DEFINITION = "cache-process-definition";
    public static final String CACHE_COMMON_DICTIONARY = "cache-common-dictionary";
    public static final String CACHE_START_PROCESSINSTANCE = "cache-start-processinstance";
    public static final String CACHE_ACL_PERMISSIONVALS = "cache-acl-permissionvals";
    public static final String CACHE_PROCESS_STATUS = "cache-process-status";
}
