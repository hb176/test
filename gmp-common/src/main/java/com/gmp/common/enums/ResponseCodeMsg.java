package com.gmp.common.enums;

import lombok.Getter;


@Getter
public enum ResponseCodeMsg {

    SUCCESS(200, "OK"),
    INFORMATION_SAVE_FAILED(200401, "信息保存失败"),
    INFORMATION_NOT_FIND(200402, "信息获取失败"),
    INFORMATION_IS_NUT_REPEAT(200403, "键值已存在,不能重复; "),
    USER_INFO_NOT_FIND(200405, "获取用户信息失败"),
    INFORMATION_SAVE_FAILED_PRAMS(200405, "信息保存失败:{}"),
    FILE_NOT_FIND(200407, "文件查看失败"),
    FILE_UPLOAD_FILED(200408, "文件上传失败"),
    CUR_CUSTOMER_SAVE_FAILED(200409, "客户信息保存失败 >> 当前客户的『DB|MQ』连接失败"),
    //action server
    TABLE_IS_REPEAT(200410, "表已存在,不能重复; "),
    PLEASE_SEARCH_MODULE(200411, "请先选择系统模块; "),
    LOGIN_LOG_SAVE_FAILED(200412, "登录日志保存失败; "),
    GET_LIST_FAILED(200413, "获取列表信息失败"),
    //auth server
    GET_CUSTOMER_INFO_FAILED(200414,"获取当前所属公司信息失败,请联系管理员"),
    PARAMETER_PASSING_ERROR(200415, "参数传递有误"),
    SAME_USER_LOGIN_CONFLIT(200416, "相同账号登录冲突,该账号已在ip为{0}的电脑上登录，是否强制登录？"),
    DEFFERENT_USER_LOGIN_CONFLIT(200417, "账号登录冲突,本公司的其他账号已登录，请联系账号{0}的使用者"),
    SAME_IP_LOGIN_CONFLIT(200418, "账号登录冲突，该电脑（IP）上有其它账号{0}正在登录，是否强制登出其它账号，继续登录？"),
    FORCED_LOGIN_LOGIN(200419, "该账号已在ip地址为{0}的电脑上登录，是否强制登录？"),
    GMP_DATA_NOT_EXIT(200420, "该账号的配置信息不存在"),
    VALID_CODE_EXPIRED(200421, "登录校验码失效，请重新校验"),
    VALID_CODE_NULL(200422, "登录校验码为空，请传入校验码"),
    VALID_CODE_ERROR(200422, "登录校验码错误"),
    VALID_CODE_SEND_ERROR(200423, "登录校验码发送失败"),
    PHONE_DECRYPT_ERROR(200424, "手机号解密失败"),
    VALID_STATUS_EXPIRED(200425, "身份校验已过期，请重新校验"),
    LOGIN_PRAMS_ERROR(200426, "登录参数传递有误：loginMethod = {0} | curIpAddress = {1} "),
    VALID_PHONE_NUM_ERROR(200427, "该手机号码在系统数据存在多个，无法识别"),
    VALID_STATUS_OF_FIRST(200428, "该账号首次登录系统，请使用手机短信验证码进行登录"),
    LOGIN_COUNT_OF_OUT_OF_RANGE(200429, "登录失败，当前在线用户数已满，无法继续登录"),
    //cloud server
    DATABASE_INFO_FAILED(200430, "客户信息保存失败,数据库配置信息错误"),
    INIT_CUSTOMER_FAILED(200431, "客户信息初始化失败 >> 客户数据库连接异常，请联系管理员"),
    CONF_INFORMATION_NOT_FIND(200432, "当前客户配置信息获取失败，请联系管理员"),
    INVALID_CUSTOMER_OVERDUE(200433,"当前所属公司已过期,无法登陆"),
    INVALID_CUSTOMER_IS_FAILED(200434,"客户信息初始化失败"),
    //client server 200450
    INVALID_PASSWORD(200450, "密码已过期,请立即修改"),
    DEFAULT_IS_PASSWORD(200451, "密码为初始密码,请立即修改"),
    INVALID_USERNAME_PASSWORD(200452, "无效的用户名或密码"),
    PASSWORD_IS_ERROR(200453, "密码输入有误，剩余次数:{0}"),
    LOCKED_USERID(200454, "账号被锁定,请联系管理员"),
    DEL_IS_USERID(200455, "账号已失效,请联系管理员"),
    USER_NOT_FIND(200456, "用户不存在"),
    AUTHENTICATION_FAILED_UNAUTHORIZED(200457, "身份验证失败，没有访问权限"),
    USER_CHECK_ERROR(200458, "电子签名有误"),
    AUDIT_SAVE_FILED(200459, "审计录入失败"),
    SECRET_RSA_FILED(200460, "加密有误"),
    SECRET_RSA_ANALYSIS_FILED(200461, "服务异常 >> 秘钥解析失败,请联系管理员"),
    OLD_PWD_IS_ERROR(200462, "旧密码输入有误"),
    PWD_LENGTH_IS_ERROR(200463, "密码长度不符合要求"),
    PWD_STRENGTH_IS_ERROR(200464, "密码强度不符合要求"),
    PWD_RECORDS_IS_ERROR(200465, "新密码与最近过去的历史密码相同，请重新设定"),
    PWD_UPDATE_DAYS_ERROR(200466, "密码修改间隔时间太短，无法修改密码"),
    PWD_UPDATE_ERROR(200467, "密码修改失败"),
    USER_DEL_FAILED_ERROR(200468, "用户禁用失败，该用户正在TMS培训系统中使用，请先将该用户从TMS培训系统中失效，再禁用"),
    PROPERITTY_STATUS(200469, "状态"),
    FILE_DOWNLOAD_FILED(200470, "文件下载失败"),
    FILE_READ_FILED(200471, "模板解析失败 >> {0}"),
    FILE_SYN_FILED(200472, "数据同步失败 >> "),
    FILE_IMPORT_FILED(200473, "数据导入失败 >> 数据编号为：{0}的文件后缀格式不对"),
    NOTICE_USER_FLAG_FILED(200474, "公告读取状态标记失败"),
    COM_INFORMATION_LOSE_FAILED(200475, "公司失效失败，该公司下存在部门数据，请先移除该公司下的部门数据"),
    COM_INFORMATION_DEL_FAILED(200476, "公司删除失败，该公司下存在部门数据，请先删除该公司下部门数据"),
    SUB_INFORMATION_LOSE_FAILED(200477, "厂区失效失败，该厂区下存在部门数据，请先移除该厂区下的部门数据"),
    SUB_INFORMATION_DEL_FAILED(200478, "厂区删除失败，该厂区下存在部门数据，请先删除该厂区下部门数据"),
    DEPT_INFORMATION_LOSE_FAILED_01(200479, "部门失效失败，该部门下尚有子部门"),
    DEPT_INFORMATION_LOSE_FAILED_02(200480, "部门失效失败，该部门下尚有用户信息"),
    DEPT_INFORMATION_DEL_FAILED_01(200481, "部门删除失败，该部门下尚有子部门"),
    DEPT_INFORMATION_DEL_FAILED_02(200482, "部门删除失败，该部门下尚有用户信息"),
    ROLE_INFORMATION_LOSE_FAILED(200483, "角色失效失败，该角色下存在人员信息，请先移除该角色下的人员"),
    ROLE_INFORMATION_DEL_FAILED(200484, "角色删除失败，该角色下存在人员信息，请先删除该角色下的人员"),
    CUSTOM_USER_COUNT_OVER(200485, "最大用户数不能超过：{0}"),
    CUSTOM_FORM_COUNT_OVER(200486, "最大表单数不能超过：{0}"),
    CUSTOM_PROCESS_COUNT_OVER(200487, "最大流程数不能超过：{0}"),
    CUSTOM_PROCESS_THE_SAME_FORM_VER_IS_NOT_SAME(200488, "当前流程不同节点中绑定的相同表单的表单版本不一致"),
    CUS_CODE_INFORMATION_DEL_FAILED(200489, "自定义编号规则删除失败，该规则正在使用中，请先将自定义表单或者文件夹中解除规则绑定"),



    //==== Exception ===============================
    SYSTEM_RUNTIME_EXCEPTION(500401, "系统运行异常"),
    SERVICE_CONNECTION_FAILED(500402, "服务连接失败：{0}"),
    FAILED_TO_OBTAIN_TOKEN(500403, "身份信息获取失败"),
    CUSTOM_PROCESS_NOT_EXIST_01(500404, "指定的流程定义[id/name={0},version={1}]不存在"),
    CUSTOM_PROCESS_NOT_EXIST_02(500405, "指定的流程定义[id/name={0}]不存在"),
    MQ_INIT_FAILED(500406, "客户同步MQ初始化失败"),
    CHECK_CONTROL_FAILED(500407,"校验失败，参数有误! {0}"),
    CHECK_SQL_SYNTAX_FAILED(500408,"校验失败，SQL语法错误!"),
    CHECK_SQL_SYNTAX_FAILED_01(500409,"校验失败，请录入查询SQL语句!"),
    CHECK_SQL_SYNTAX_FAILED_02(500410,"校验失败，查询SQL语句中不能带换行符号!"),
    CHECK_SQL_SYNTAX_FAILED_03(500411,"校验失败，请输入正确查询SQL语句!"),
    CHECK_SQL_SYNTAX_FAILED_04(500412,"校验失败，必须指定查询字段，不能用*查询所有!"),
    CHECK_SQL_SYNTAX_FAILED_05(500413,"校验失败，必须使用 AS 指定查询字段显示名称!"),
    CHECK_SQL_SYNTAX_FAILED_06(500414,"校验失败，必须指定查询字段显示名称!"),
    CHECK_SQL_SYNTAX_FAILED_07(500415,"校验失败，需要搜索的字段必须在显示字段里面!"),
    ASSOCIATION_FORM_EXCEPTION (500419,"关联表单异常!"),
    SYNC_DATA_WITH_CLIENT_IS_FAILED(500420, "数据同步失败 >> 数据编号为：{0}已经存在,不能重复添加"),
    SYNC_DATA_WITH_USER_IS_FAILED(500421, "数据同步失败 >> 用户账号{0}已存在,不能重复添加"),
    IMPORT_DATA_WITH_CLIENT_IS_FAILED_01(500422, "数据导入失败 >> 数据编号不能为空"),
    IMPORT_DATA_WITH_CLIENT_IS_FAILED_02(500423, "数据导入失败 >> 数据编号为：{0}已经存在,不能重复添加"),
    IMPORT_DATA_WITH_DEPT_IS_FAILED(500424, "数据导入失败 >> 部门编号{0}已存在,不能重复添加"),
    IMPORT_DATA_WITH_SUB_IS_FAILED(500425, "数据导入失败 >> 厂区编号{0}已存在,不能重复添加"),
    USER_DATA_NULL(500426,"需要导出的用户数据为空"),
    MENU_CUS_SQL_IS_NOT_NULL(500427,"自定义类型，列表来源SQL不能为空，异常栏目名称{}"),
    MENU_CUS_SQL_WITH_MAIN_TABLE_IS_NOT_NULL(500428,"自定义类型，主表不能为空，异常栏目名称{}"),
    MENU_CUS_SQL_CHECK_IS_FAILED(500429,"自定义SQL校验失败，{}"),
    I18N_LANG_LIST_IS_FAILED(500440,"中文获取失败，解析该栏目中绑定的流程信息失败"),

    APP_BACKUP_FILED(500474, "程序备份失败"),
    APP_RENEWAL_UPLOAD_FILED(500475, "更新包上传失败"),
    RENEWAL_ACCESS_CODE_GET_FILED(5200476, "更新编码获取失败"),
    CODE_ANALYSIS_AND_GET_DATA_FILED(500477, "解析编码获取更新列表失败"),
    APP_REGENERATE_FILED(500478, "更新失败"),
    GET_REGENERATE_LIST_FILED(500479, "获取可更新列表失败"),
    NO_UPDATE_PERMISSION(500480, "无更新权限");

    private int code;
    private String message;

    ResponseCodeMsg(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
