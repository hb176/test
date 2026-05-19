package com.gmp.common.enums;

import lombok.Getter;
@Getter
public enum GmpResponseCodeMsg  {

    // ==== SISQP NORMRESULT START 200400 ========================================
    SUCCESS(200,"OK"),
    CUR_USER_LOGIN_LOSE_EFFICACY(200400,"当前账号登录已失效，请重新登录"),
    INFORMATION_SAVE_FAILED(200401,"信息保存失败"),
    INFORMATION_NOT_FIND(200402,"信息获取失败"),
    INFORMATION_IS_NUT_REPEAT(200403,"键值已存在,不能重复; "),
    INVALID_LOGIN(200404,"登录已过期，请重新登录"),
    INVALID_LOGIN_AND_LOGIN_AGAIN(200405,"登录已过期，该账号已在ip地址为：{0}的电脑上登录，请重新登录"),
    INVALID_CUSTOMER_OVERDUE(200406,"当前所属公司已过期,无法登陆"),
    INFORMATION_IS_NUT_NULL(200407,"不能将值 NULL 插入列: "),
    INFORMATION_FETCHING_FAILED(200408,"信息抓取失败"),
    INFORMATION_DEL_FAILED(200409,"信息删除失败"),
    BIND_PERSONAL_DOMAIN_FILED_01(200410,"身份验证失败,工码信息无效（不存在），重新验证"),
    BIND_PERSONAL_DOMAIN_FILED_02(200411,"身份验证失败,工码或者用户名输入有误，重新验证"),
    BIND_PERSONAL_DOMAIN_ACCOUNT(200412,"身份验证成功,请绑定个人域账号信息"),
    DOMAIN_INFORMATION_FILED(200413,"域服务信息配置有误，请联系管理员"),
    ADD_DOMAIN_INFORMATION(200414,"请添加域服务信息"),
    JOB_NUM_IS_INCORRECT(200415,"工码输入有误，请重新录入"),
    SAME_USER_LOGIN_CONFLIT (200416,"相同账号登录冲突,该账号已在ip为{0}的电脑上登录，是否强制登录？"),
    DOMAINID_IS_NOT_NULL(200417,"域账号不能为空"),
    DOMAINID_BOUND_SUCCESSFULLY(200418,"域账号绑定成功，请重新登录"),
    FORCED_LOGIN_LOGIN(200419,"该账号已在ip地址为{0}的电脑上登录，是否强制登录？"),
    LOGIN_DOMAINID(200420,"登录信息有误"),
    DOMAIN_INFORMATION_ERROR(200421,"域信息录入有误，请重新录入"),
    DOMAIN_ACCOUNT_NOT_EXIST(200422,"域账号不存在，请确认域账号是否正确"),
    DOMAIN_INFORMATION_FILED_FOR_ADMIN(200423, "域服务信息配置有误，请确认连接信息是否正确"),
    DOMAIN_OR_AD_INFORMATION_FILED_FOR_ADMIN(200424,"域账号或域服务信息配置有误，请手动点击【域账号】信息确认或者【新用户】重新绑定域账号"),
    DOMAIN_OR_AD_INFORMATION_FILED(200425,"域账号或域服务信息配置有误，请手动点击【新用户】重新绑定域账号或联系管理员确认域服务信息配置是否正确"),
    RECORDER_CREATE_SUCCESS (200426,"流程创建成功，是否立即进入"),
    FORM_INFO_CREATE_SUCCESS (200427,"表单创建成功，是否立即进入"),
    FAILED_TO_OBTAIN_TOKEN (200428,"用户信息解析失败信息失败"),
    INVALID_PASSWORD(200429, "密码已过期,请立即修改"),
    DEFAULT_IS_PASSWORD(200430, "密码为初始密码,请立即修改"),
    INVALID_USERNAME_PASSWORD(200431, "无效的用户名或密码"),
    PASSWORD_IS_ERROR(200432,"密码输入有误，剩余次数:{0}"),
    LOCKED_USERID(200433,"账号被锁定,请联系管理员"),
    DEL_IS_USERID(200434,"账号已失效,请联系管理员"),
    USER_INFO_NOT_FIND(200435,"获取用户信息失败"),
    USER_NOT_FIND(200436,"用户不存在"),
    AUTHENTICATION_FAILED_UNAUTHORIZED(200437,"身份验证失败，没有访问权限"),
    USER_CHECK_ERROR(200438,"电子签名有误"),
    AUDIT_SAVE_FILED(200439,"审计录入失败"),
    SECRET_RSA_FILED(200440,"加密有误"),
    SECRET_RSA_ANALYSIS_FILED(200441,"加密解析失败"),
    SQL_VERIFY_FILED(200442,"SQL含有非法字符"),
    OLD_PWD_IS_ERROR(200443,"旧密码输入有误"),
    PWD_LENGTH_IS_ERROR(200444,"密码长度不符合要求"),
    PWD_STRENGTH_IS_ERROR(200445,"密码强度不符合要求"),
    PWD_RECORDS_IS_ERROR(200446,"新密码与最近过去的历史密码相同，请重新设定"),
    PWD_UPDATE_DAYS_ERROR(200447,"密码修改间隔时间太短，无法修改密码"),
    PWD_UPDATE_ERROR(200448,"密码修改失败"),
    USER_DEL_FAILED_ERROR(200449,"用户禁用失败，该用户正在TMS培训系统中使用，请先将该用户从TMS培训系统中失效，再禁用"),
    MSG_SEND_FAILED_00 (200450,"消息发送失败"),
    MSG_SEND_FAILED_01 (200451,"消息发送失败：{0}"),
    SERVER_ADDRESS_CANNOT_BE_EMPTY (200452,"发件服务器地址/端口不能为空"),
    EMAIL_ACCOUNT_CANNOT_BE_EMPTY (200453,"通知发送人账号/密码不能为空"),
    EMAIL_RECIPIENT_CANNOT_BE_EMPTY (200454,"通知接收人不能为空"),
    EMAIL_TITLE_CANNOT_BE_EMPTY (200455,"通知主题不能为空"),
    EMAIL_CONTENT_CANNOT_BE_EMPTY (200456,"通知内容不能为空"),
    AGENTID_CANNOT_BE_EMPTY (200457,"应用ID不能为空"),
    SECRET_CANNOT_BE_EMPTY (200458,"应用密钥不能为空"),
    CORPID_CANNOT_BE_EMPTY (200459,"企业号不能为空"),
    WXIN_ACCOUNT_CANNOT_BE_EMPTY (200460,"当前用户企业微信号为空"),
    WXIN_ACCOUNT_CANNOT_BE_ERROR (200461,"企业微信用户信息验证失败,请检查当前用户企业微信号是否正确"),
    SERVER_SMTP_PROTOCOL_CANNOT_BE_EMPTY (200462,"发件服务器加密协议不能为空"),
    EMAIL_RECIPIENT_ADDRESS_CANNOT_BE_EMPTY (200463,"通知接收人未配置通知接收地址"),
    PARAMETER_PASSING_ERROR (200464,"参数传递有误"),
    GET_LIST_FAILED(200465, "获取列表信息失败"),
    SYSTEM_RUNTIME_EXCEPTION(200466, "系统运行异常"),
    TABLE_IS_REPEAT(200467, "表已存在,不能重复; "),

    // ==== CLIENT 组织架构信息 START 205001 ========================================
    CLIENT_GET_ALL_USER_01(201001,"获取当前厂区的所有用户 >> 用户解析失败"),
    CLIENT_GET_USER_BY_ROLE_02(201002,"获取当前厂区指定角色下的所有用户（不含部门去重） >> 用户解析失败"),
    CLIENT_GET_USER_BY_ROLE_03(201003,"获取当前厂区指定角色下的所有用户（含部门不去重） >> 用户解析失败"),
    CLIENT_GET_USER_BY_ROLE_AND_DEPT_04(201004,"获取指定单个角色/单个部门下的用户 >> 用户解析失败"),
    CLIENT_GET_USER_BY_ROLE_AND_DEPT_05(201005,"获取指定多个角色/单个部门下的所有用户（只返用户名称/账号） >> 用户解析失败"),
    CLIENT_GET_USER_05(201006,"获取当前厂区、当前环境下的所有用户及角色信息 >> 用户解析失败"),
    CLIENT_GET_ALL_DEPT_11(201011,"获取当前厂区下所有部门信息 >> 用户解析失败"),
    CLIENT_GET_ALL_DEPT_BY_USER_12(201012,"获取当前人所在厂区下所有相关部门信息 >> 用户解析失败"),
    CLIENT_GET_ALL_SUB_22(201022,"获取当前人所在公司下的所有厂区信息 >> 用户解析失败"),
    CLIENT_GET_CUR_COMP_23(201023,"获取当前人所在公司信息 >> 用户解析失败"),
    APP_REGENERATE_FILED(201101,"更新失败"),
    BUS_TYPE_INFORMATION_LOSE_FAILED(201011,"静态数据类型删除失败，该类型有子静态数据、请先删除该类型下的子静态数据"),
    MSG_INFORMATION_LOSE_FAILED(201012,"通知失效失败，该通知正在使用中、请先从定时器任务或者流程解除该通知"),
    MSG_INFORMATION_DEL_FAILED(201013,"通知删除失败，该通知正在使用中，请先从定时器任务或者流程解除该通知"),
    CUS_CODE_INFORMATION_DEL_FAILED(201014,"自定义编号规则删除失败，该规则正在使用中，请先将自定义表单或者文件夹中解除规则绑定"),
    INPP_AUTH_INFORMATION_DEL_FAILED(201015,"签批页标签删除失败，该标签正在使用中，请先将自定义流程中解除标签绑定"),
    CONCURRENT_LOCK_FAILED(201016,"操作失败，该对象已被其他用户锁定，无法进行操作"),
    CHECK_TABLE_FILED(201017,"校验失败,该数据已在目标表存在"),
    // ==== WORKFOOW START 203001 ========================================
    NUMBER_CALCULATE_FAILED(201034,"数值计算失败"),
    CUR_DATA_LOCK(201035,"当前数据已被占用锁定"),
    RECORDER_IS_NOT_FIND(203036,"当前流程已不存在，请重新发起流程"),
    ABNORMAL_CLOSE (203037,"非正常关闭"),
    ABNORMAL_CLOSE_CONTENT (203038,"流程{0}被非正常关闭"),
    BUTTON_UNLOCK (203039,"解锁"),
    UNLOCK_CONTENT (203040,"流程{0}被解锁"),
    BUTTON_REASSIGN (203041,"重分配"),
    REASSIGN_CONTENT (203042,"流程{0}被重分配"),
    PROPERITTY_STATUS (203043,"状态"),
    RECORDER_STATUS_CLOSE (203044,"关闭"),
    RECORDER_CREATE_FAILED (203045,"流程创建失败,请重试"),
    RECORDER_SOP_LIST_IS_NULL (203046,"当前流程未关联SOP文件，如有需要请联系管理员进行关联SOP文件绑定"),
    RECORDER_SOP_LIST_IS_EXPLAIN (203047,"当前展示的SOO文件列表，SOP文件列表中，文件名称为蓝色，代表当前节点关联的SOP文件；非蓝色文件名称则代表其它节点关联的SOP文件。"),


    // ==== SISQP EXCEPTION START 500501 ========================================
    // == 电子签名 ==
    CUSTOM_FORM_MGR_FORM_UNFIND(500501,"编号[{0}]在自定义表单中不存在"),
    CHECK_CONTROL_FAILED_01(500502,"校验失败，参数有误"),
    CHECK_CONTROL_FAILED_02(500503,"校验失败，参数有误(0)"),
    CHECK_CONTROL_FAILED_03(500504,"校验失败，当前登录人不能参与当前复核"),
    CHECK_CONTROL_FAILED_04(500505,"该对象{0}没有操作权限"),

    // == 流程异常 ==
    REQUEST_PARAMETER_MISSING(500505,"请求参数缺失"),
    CUSTOM_FORM_ELEMENT_MATCHING_ERROR(500506,"自定义表单元素匹配自定义表错误:未获取到元素[{0}]的值"),
    CUSTOM_PROCESS_INEXISTENCE(500507,"自定义流程[{0}]不存在"),
    CUSTOM_PROCESS_NOT_HAVE_START_NODE(500508,"流程定义[id={0}]没有开始节点"),
    THE_SPECIFIED_TASK_DOES_NOT_EXIST(500509,"指定的任务[id={0}]不存在"),
    NOT_ALLOWED_TO_EXECUTE_THE_TASK(500510,"当前参与者[{0}]不允许执行任务[taskId={1}]"),
    THE_FOUNDER_DEPARTMENT_CANNOT_BE_EMPTY(500511,"创建人部门不能为空"),
    PROCESS_EXECUTION_FAILURE (500512,"执行流程失败:{0}"),
    THE_ARRAY_LENGTH_IS_INCONSISTENT (500513,"数组{0}与{1}长度不一致"),
    PROCESS_PARTICIPANTS_CANNOT_BE_EMPTY (500514,"流程参与人不能为空"),
    PROCESS_LOCKED (500515,"流程被锁定"),
    NEXT_STEP_IN_THE_PROCESS_IS_UNATTENDED (500517,"流程下一步无人操作"),
    CURRENT_OPERATOR_NOT_HAVE_CUR_DEPARTMENT (500518,"流程当前操作人无该职能部门"),
    CURRENT_PROCESS_HAVE_EXPIRED_DRAFTS (500519,"当前流程，存在过期草稿、请先处理草稿，才可重新发起流程"),
    EMAIL_SENDING_FAILURE (500520,"通知发送失败"),
    ASSOCIATION_FORM_EXCEPTION (500522,"关联表单异常，请联系管理员"),
    PROCESS_UNSUBMITTED_CANNOT_BE_VIEWED (500523,"当前流程未提交，不能查看"),
    ASSIGN_THE_OPERATOR_CONFIGURATION_IS_INCORRECT_01 (501001,"操作人配置有误：未定义{0}该对象处理流程"),
    ASSIGN_THE_OPERATOR_CONFIGURATION_IS_INCORRECT_02 (501002,"操作人配置有误：未定义{0}该对象处理表单"),
    FUN_FORM_INFORMATION_SAVE_FAILED(501003, "信息保存失败{}"),

    //流程 - 自定义编号
    CODE_BUILD_FILED_00 (500600,"编号控件配置有误,编号规则获取失败"),
    CODE_BUILD_FILED_01 (500601,"编号控件配置有误,关联参数三级目录表单值有误"),
    CODE_BUILD_FILED_02 (500602,"编号控件配置有误,文件编号关联参数值有误"),
    CODE_BUILD_FILED_03 (500603,"编号控件配置有误,关联参数文件编号表单值有误"),
    CODE_BUILD_FILED_04 (500604,"编号控件配置有误,编号规则获取失败"),
    CODE_BUILD_FILED_05 (500605,"编号控件配置有误,编号规则类型值有误"),
    CODE_BUILD_FILED_06 (500606,"编号控件配置有误,编号规则配置参数值获取失败"),
    CODE_BUILD_FILED_10 (500610,"编号生成失败,未正确配置编号前缀"),
    CODE_BUILD_FILED_11 (500611,"编号生成失败,系统时间异常"),
    CODE_BUILD_FILED_12 (500612,"编号生成失败,流水号长度已达设定最大长度，请重新定义流水号长度"),
    CODE_BUILD_FILED_13 (500613,"编号生成失败 {0}"),
    CODE_BUILD_FILED_14 (500614,"编号生成失败 >> 编号并发生成锁定中...请稍后刷新重试..."),
    CODE_BUILD_FILED_15 (500615,"编号生成异常失败"),

    //流程 - 功能节点 - 调整到期日期
    ADJUST_CUR_EXPIRE_HANDLER_FAILED (500630,"调整当前流程到期日期 >> 参数传递有误 >> {0}"),
    //数据导入 - DMS ==============
    IMPORT_FILE_FILED_00(500700, "数据导入失败 >> 模板文件解析失败，请联系管理员"),
    IMPORT_FILE_FILED_01(500701, "数据导入失败 >> 模板文件解析失败（模板或被非法篡改）"),
    IMPORT_FILE_FILED_02(500702, "数据导入失败 >> 模板文件解析失败（未解析出所需的SHEET或模板被非法篡改）"),
    IMPORT_FILE_FILED_03(500703, "数据导入失败 >> 请输入正确的数据类型"),
    IMPORT_FILE_FILED_04(500704, "数据导入失败 >> 请传入需要上传的文件"),
    IMPORT_FILE_FILED_05(500705, "数据导入失败 >> 数据编号为：{0}的文件后缀格式不对"),
    IMPORT_FILE_FILED_06(500706, "数据导入失败 >> 模板数据转换错误"),
    IMPORT_FILE_FILED_07(500707, "数据导入失败 >> 需要添加的数据列表为空"),
    IMPORT_FILE_FILED_08(500708, "数据导入失败 >> {0}该值在系统内不存在或者不属于筛选条件范围内的值"),
    IMPORT_FILE_FILED_09(500709, "数据导入失败 >> 数据编号为：{0}已经存在,不能重复添加"),
    IMPORT_FILE_FILED_10(500710, "数据导入失败 >> 未找到相应的数据表"),
    IMPORT_FILE_FILED_11(500711, "数据导入失败 >> 有数据编号为空"),
    IMPORT_DATA_SYB_FILED_20(500720, "数据同步失败，请联系管理员"),
    IMPORT_DATA_SYB_FILED_21(500721, "数据同步失败 >> 数据编号为：{0}已经存在,不能重复添加"),
    DELETE_FILE_FILED_30(500730, "数据删除失败 >> 未选中待删除的数据"),
    IMPORT_DATA_ERROR_00(500800,"数据导入失败{0}"),
    IMPORT_DATA_NULL(500801,"需要修改的数据为空{0}"),
    IMPORT_DATA_TABLE_NAME_ERROR(500802,"导出失败 >> 表名不能为空"),
    IMPORT_DATA_PARAM_ERROR(500803,"导出失败 >> 需要过滤的参数为空"),
    IMPORT_DATA_NOT_FORND_DATA(500804,"没有找的要插入的数据"),
    IMPORT_DATA_COUNT_ERROR(500805,"要插入的数据与查询出来的数据条数不一致"),
    IMPORT_DATA_ERROR(500806,"插入数据不是过滤出来的数据"),
    IMPORT_DATA_PARAMS_ISNULL(500807,"导出失败 >> 需要录入的字段不能为空"),
    //功能控件计算 ==Function control ============
    FUNCTION_CONTROL_EVAL_FAILED_00(500820,"日期计算失败{0}"),
    FUNCTION_CONTROL_EVAL_FAILED_01(500821,"日期计算失败，日期A值参数有误"),
    FUNCTION_CONTROL_EVAL_FAILED_02(500822,"日期计算失败，日期B值参数有误"),

    CREATE_TABLE_FIELD_NULL(502000,"该表的字段的信息不存在"),





//===COMM-FUNCTION START ===================================================
    /**
     * CODE: 202
     * @description COMM-FUNCTION 异常/错误 提示信息
     * @I18nResultEnumAnnotation(value = "2/500 40/1 08 000")
     * <p>
     *     explain：  2/500 40/1 08 000
     *     200:正常返回编码 (eg.200...)
     *     500:异常返回编码 (eg.500...)
     *     40:错误返回提示-无参数返回 (eg.20040...)
     *     41:错误返回提示-有参数返回 (eg.20041...)
     *     02: CLIENT (eg.2004108...)
     *     000:错误信息起始编号  (eg.2004108000)
     * </p>
     */
    CUS_LABEL_RANGE_DEL_FAILED_1(202001,"标签适用类型删除失败、该类型绑定了标签"),
    CUS_LABEL_RANGE_DEL_FAILED_2(202002,"标签适用类型删除失败、该类型下有子类型"),
    CUS_FILE_RANGE_DEL_FAILED_1(202003,"记录文件适用类型删除失败、该类型绑定了记录文件"),
    CUS_FILE_RANGE_DEL_FAILED_2(202004,"记录文件适用类型删除失败、该类型下有子类型"),
    START_CHILD_PROCESS_FAILED_1(202005,"数据库表生成子流程参数配置有误"),
    START_CHILD_PROCESS_FAILED_2(202006,"数据库表生成子流程查询过滤参数配置有误"),
    START_CHILD_PROCESS_FAILED_3(202007,"数据库表生成子流程表中没查询到相关数据"),
    START_CHILD_PROCESS_FAILED_4(202008,"数据库表生成子流程表中sql查询有误"),
    CUS_LABEL_REPRINT_FAILED(202013,"标签补打失败，该标签不存在补打列表"),
    CUS_LABEL_PRINT_FAILED(202014,"标签打印失败，该标签已经打印，补打请去补打列表打印"),
    CUS_LABEL_PRINT_FAILED_01(202015,"标签打印失败，未配置自定义编号规则"),
    CUS_LABEL_PRINT_FAILED_02(202016,"标签打印失败，配置的自定义编号规则信息获取失败"),
    CUS_CUS_TABLE_DEL_FAILED_1(202017,"自定义建表类型删除失败、该类型绑定了自定义表"),
    CUS_CUS_TABLE_DEL_FAILED_2(202018,"自定义建表类型删除失败、该类型下有子类型"),


//===QMS START ===================================================
    /**
     * CODE: 203
     * @description QMS 异常/错误 提示信息
     * @I18nResultEnumAnnotation(value = "2/500 40/1 01 000")
     * <p>
     *     explain：  2/500 40/1 01 000
     *     200:正常返回编码 (eg.200...)
     *     500:异常返回编码 (eg.500...)
     *     40:错误返回提示-无参数返回 (eg.20040...)
     *     41:错误返回提示-有参数返回 (eg.20041...)
     *     01:QMS (eg.2004102...)
     *     000:错误信息起始编号  (eg.2004101000)
     * </p>
     */



//===DMS START ===================================================
    /**
     * CODE : 204
     * @description DMS 异常/错误 提示信息
     * @I18nResultEnumAnnotation(value = "2/500 40/1 02 000")
     * <p>
     *     explain：  2/500 40/1 02 000
     *     200:正常返回编码 (eg.200...)
     *     500:异常返回编码 (eg.500...)
     *     40:错误返回提示-无参数返回 (eg.20040...)
     *     41:错误返回提示-有参数返回 (eg.20041...)
     *     04:DMS (eg.2004102...)
     *     000:错误信息起始编号  (eg.2004102000)
     * </p>
     */
    FILE_UPLOAD_FILED(204001,"文件上传失败"),
    FILE_DOWNLOAD_FILED(204002,"文件下载失败"),
    FILE_DELETE_FILED(204003,"文件删除失败"),
    FILE_SAVE_FILED(204004,"文件保存失败"),
    FILE_NOT_FIND(204005,"文件查看失败 {0}"),
    FILE_EXPORT_FAILED_01(204006,"文件查看失败"),
    FILE_EXPORT_LOG_FAILED_01(204007,"日志文件导出失败"),
    FILE_INDEX_CREATE_FILED(204008,"文件不存在"),
    FILE_INDEX_DELETE_FILED(204009,"文件索引删除失败"),
    FILE_INDEX_INIT_CREATE_FILED(204009,"文件索引初始化创建失败"),
    FILE_CODE_NUMEBR_NOT_FIND(204010,"文件编号规则不存在"),
    THE_FILE_PATH_CANNOT_BE_EMPTY (204010,"文件路径不能为空"),
    FILE_DISTRIBUTE_DEPT(204011,"文件分发部门不存在"),
    FILE_ADD_BOOK_PAGE(204012,"文件添加页码失败"),
    FILE_GETMD5_FAILED(204013,"文件MD5获取失败"),
    FILE_NOT_FIND_00(204014,"文件信息获取失败"),
    FILE_NOT_FIND_01(204015,"文件信息-文件类型获取失败"),
    FILE_NOT_FIND_02(204016,"文件已被非法修改"),
    FILE_UNLOCK_FAILED(204017,"文件解锁失败、该文件不存在"),
    FILE_PRINT_FAILED_00(204020,"文件打印失败"),
    FILE_PRINT_FAILED_01(204021,"文件打印异常结束,请联系管理员"),
    FILE_PRINT_FAILED_02(204022,"文件打印失败 >> 文件 fileId = {0}不存在"),
    FILE_PRINT_FAILED_03(204023,"文件打印失败 >> 不能重复打印"),
    FILE_PRINT_FAILED_04(204024,"文件打印失败 >> 文件转换异常{0}"),
    FILE_PRINT_FAILED_05(204025,"文件打印失败 >> 文件打印份数不能为空"),
    FILE_PRINT_FAILED_06(204026,"文件添加水印失败"),
    FILE_PRINT_FAILED_07(204027,"文件打印失败 >>文件存在异常"),
    FILE_PRINT_FAILED_08(204028,"文件打印失败 {0}"),
    FILE_PRINT_FAILED_09(204029,"文件打印失败 >>编号规则获取失败，请联系管理员检查相关配置"),
    FILE_PRINT_FAILED_10 (204030,"文件打印失败:当前有用户正在打印文件，为避免打印编号重复或者跳号，请等待1-2分钟之后再次尝试打印"),
    FOLDER_DEL_FAILED_01(204041,"文件夹删除失败、只可删除禁用状态的文件夹"),
    FOLDER_DEL_FAILED_02(204042,"文件夹删除失败、请先删除子文件夹"),
    FOLDER_DEL_FAILED_03(204043,"文件夹删除失败、请先删除文件夹下的文件"),
    FOLDER_FORBIDDEN_FAILED_01(204044,"文件夹禁用失败、清先禁用子文件夹"),
    FOLDER_FORBIDDEN_FAILED_02(204045,"文件夹禁用失败、请先删除文件夹下的文件"),
    // ==== DMS - FILE HANDLER START 204101 ========================================
    FILE_OPERATION_FAILURE_01 (204101,"待操作文件参数信息获取失败，请检查是否未配置待处理文件的表单参数"),
    FILE_OPERATION_FAILURE_02 (204102,"待处理文件参数:{0}获取失败，请检查配置"),
    FILE_OPERATION_FAILURE_03 (204103,"待操作文件:{0} >> 参数:{1}获取失败，请检查配置"),
    FILE_OPERATION_FAILURE_04 (204104,"待操作文件:{0}不存在，请检查文件是否存在"),
    FILE_OPERATION_FAILURE_05 (204105,"待操作文件:{0}被非法修改，文件操作失败"),
    FILE_OPERATION_FAILURE_06 (204106,"待操作文件信息获取失败，请检查文件是否存在"),
    FILE_OPERATION_FAILURE_07 (204107,"文件操作失败，请联系管理员"),
    FILE_OPERATION_FAILURE_08 (204108,"文件签批页处理异常，请联系管理员"),
    FILE_OPERATION_FAILURE_09 (204109,"文件分发部门标签处理异常，请联系管理员"),
    FILE_OPERATION_FAILURE_10 (204110,"文件自定义标签处理异常，请联系管理员"),
    FILE_OPERATION_FAILURE_11 (204111,"生效日期标签处理异常，请联系管理员"),
    FILE_OPERATION_FAILURE_12 (204112,"复审历史记录处理异常，请联系管理员"),
    FILE_OPERATION_FAILURE_13 (204113,"修订历史记录处理异常，请联系管理员"),
    DESCRIPTION_FAILED_REPLACE_CONTENT_01 (204114,"文件签批页处理 >> {0}操作失败"),
    DESCRIPTION_FAILED_REPLACE_CONTENT_02 (204115,"文件分发部门标签处理 >> {0}操作失败"),
    DESCRIPTION_FAILED_REPLACE_CONTENT_03 (204116,"文件自定义标签处理 >> {0}操作失败"),
    DESCRIPTION_FAILED_REPLACE_CONTENT_04 (204117,"生效日期标签处理 >> {0}操作失败"),
    DESCRIPTION_FAILED_REPLACE_CONTENT_05 (204118,"复审历史记录处理 >> {0}操作失败"),
    DESCRIPTION_FAILED_REPLACE_CONTENT_06 (204119,"修订历史记录处理 >> {0}操作失败"),
    DESCRIPTION_FAILED_REPLACE_CONTENT_07 (204120,"文件打印 >> 文件标签处理失败"),
    FILE_OPERATION_FAILURE_121 (204121,"复审历史记录处理异常,文件中复审历史表格不存在"),
    DRAFT_TO_FILE_FAILED_01 (204121,"转正式文件失败，实体文件操作失败"),
    DRAFT_TO_FILE_FAILED_02 (204122,"转正式文件失败，主文件不存在（获取文件参数 >> 记录编号：{0}）"),
    DRAFT_TO_FILE_FAILED_03 (204123,"转正式文件失败，主文件不存在（获取文件参数 >> 文件ID：{0}）"),
    DRAFT_TO_FILE_FAILED_04 (204124,"转正式文件失败，待转换文件信息获取失败（获取文件参数 >> 记录编号：{0} || 文件ID：{1}）"),
    DRAFT_TO_FILE_FAILED_05 (204125,"转正式文件失败，文件信息获取失败"),
    DRAFT_TO_FILE_FAILED_06 (204126,"转正式文件失败，请联系管理员"),



//===TMS START ===================================================
    /**
     * CODE : 205
     * @description TMS 异常/错误 提示信息
     * @I18nResultEnumAnnotation(value = "2/500 40/1 03 000")
     * <p>
     *     explain：  2/500 40/1 03 000
     *     200:正常返回编码 (eg.200...)
     *     500:异常返回编码 (eg.500...)
     *     40:错误返回提示-无参数返回 (eg.20040...)
     *     41:错误返回提示-有参数返回 (eg.20041...)
     *     05:TMS (eg.2004103...)
     *     000:错误信息起始编号  (eg.2004103001)
     * </p>
     */
    EXAM_USER_NOT_FIND(205001,"信息获取失败，该TMS用户不存在或状态异常"),
    PAPER_RANGE_DEL_FAILED_1(205002,"考题适用类型删除失败、该类型绑定了考题"),
    PAPER_RANGE_DEL_FAILED_2(205003,"考题适用类型删除失败、该类型下有子类型"),
    EXAM_PAPER_LOAD_FAILED_1(205004,"初始化考卷失败 >> 考卷关联的考题信息有误"),
    EXAM_PAPER_LOAD_FAILED_2(205005,"初始化考卷失败 >> 流程配置生成考卷节点>>【控件：生成考卷控件】信息有误"),
    EXAM_PAPER_LOAD_FAILED_3(205006,"初始化考卷失败 >> 流程配置生成考卷节点>>【参数：考卷规则】信息有误"),
    EXAM_PAPER_LOAD_FAILED_4(205007,"考卷预览失败 >> 题库中：{0}类型下的考题数量不够"),
    TRAIN_AND_EXAM_ERR_01(205008,"当前考核出现同步提交异常，请勿再次提交"),
    TRAIN_AND_EXAM_ERR_02(205009,"考卷信息获取失败 >> 当前考核正在进行中 ..."),
    EXAM_PAPER_RULE_DEL_ERR(205010,"考卷规则删除失败 >> 请先解除该考卷规则所绑定的所有课程..."),
    USER_INFO_OR_CODE_CREATE_ERR(205011,"二维码生成异常..."),
    TMS_USER_INFO_ERROR (205012,"当前用户数据有误，TMS系统当前用户USER_ID不唯一"),
    RETRAIN_ACTION_FAILED(205013,"补考操作失败、请先对该课程进行补训"),
    SYSTEM_ENV_ERROR(205014,"考题已在使用中，不可删除"),
    QUESTION_DEL_FAILED(205015,"考题已在使用中，不可删除"),
    PERCENT_OF_PASS_FAILED(205016,"合格率计算失败，参数传递有误"),
    TRAIN_DURATION_IS_OUT_OF_RANGE(205017,"当前您的培训时长已超出每天限定时长，无法继续培训"),
    TRAIN_ERROR_IN_PROGRESS_01(205018,"当前有培训正在进行中，请在当前培训结束之后再次进行培训"),
    TRAIN_IN_PROGRESS_RISK_WARNING(205019,"当前有培训正在进行中，如果继续培训会存在合规风险，是否要继续培训操作？"),
    TRAIN_ERROR_IN_PROGRESS_02(205020,"当前课程下的该教材正在培训中，相同课程下的相同教材无法同时进行培训"),


//===LIMS START ===================================================
    /**
     * @description LIMS 异常/错误 提示信息
     * @I18nResultEnumAnnotation(value = "2/500 40/1 04 000")
     * <p>
     *     explain：  2/500 40/1 04 000
     *     200:正常返回编码 (eg.200...)
     *     500:异常返回编码 (eg.500...)
     *     40:错误返回提示-无参数返回 (eg.20040...)
     *     41:错误返回提示-有参数返回 (eg.20041...)
     *     04:QMS (eg.2004104...)
     *     000:错误信息起始编号  (eg.2004104000)
     * </p>
     */



//===FMS START ===================================================
    /**
     * @description FMS 异常/错误 提示信息
     * @I18nResultEnumAnnotation(value = "2/500 40/1 06 000")
     * <p>
     *     explain：  2/500 40/1 06 000
     *     200:正常返回编码 (eg.200...)
     *     500:异常返回编码 (eg.500...)
     *     40:错误返回提示-无参数返回 (eg.20040...)
     *     41:错误返回提示-有参数返回 (eg.20041...)
     *     06:ERMS (eg.2004106...)
     *     000:错误信息起始编号  (eg.2004106000)
     * </p>
     */
    ARCHIVES_DEL_FAILED_01(206001,"档案目录删除失败、只可删除禁用状态的档案目录"),
    ARCHIVES_DEL_FAILED_02(206002,"档案目录删除失败、请先删除子档案目录"),
    ARCHIVES_DEL_FAILED_03(206003,"档案目录删除失败、请先删除档案目录下的档案"),
    ARCHIVES_FORBIDDEN_FAILED_01(206004,"档案目录禁用失败、清先禁用子档案目录"),
    ARCHIVES_FORBIDDEN_FAILED_02(206005,"档案目录禁用失败、请先删除档案目录下的档案"),



//===MES START ===================================================
    /**
     * @description MES 异常/错误 提示信息 207
     * @I18nResultEnumAnnotation(value = "2/500 40/1 07 000")
     * <p>
     *     explain：  2/500 40/1 07 000
     *     200:正常返回编码 (eg.200...)
     *     500:异常返回编码 (eg.500...)
     *     40:错误返回提示-无参数返回 (eg.20040...)
     *     41:错误返回提示-有参数返回 (eg.20041...)
     *     07:MES (eg.2004107...)
     *     000:错误信息起始编号  (eg.2004107000)
     * </p>
     */
    POINT_EXIST_ERROR(207001,"该点位已存在"),



//===VMS START ===================================================
    /**
     * @description VMS 异常/错误 提示信息
     * @I18nResultEnumAnnotation(value = "2/500 40/1 08 000")
     * <p>
     *     explain：  2/500 40/1 08 000
     *     200:正常返回编码 (eg.200...)
     *     500:异常返回编码 (eg.500...)
     *     40:错误返回提示-无参数返回 (eg.20040...)
     *     41:错误返回提示-有参数返回 (eg.20041...)
     *     08:VMS (eg.2004108...)
     *     000:错误信息起始编号  (eg.2004108000)
     * </p>
     */




//===ERMS START ===================================================
    /**
     * @description ERMS 异常/错误 提示信息
     * @I18nResultEnumAnnotation(value = "2/500 40/1 05 000")
     * <p>
     *     explain：  2/500 40/1 05 000
     *     200:正常返回编码 (eg.200...)
     *     500:异常返回编码 (eg.500...)
     *     40:错误返回提示-无参数返回 (eg.20040...)
     *     41:错误返回提示-有参数返回 (eg.20041...)
     *     05:ERMS (eg.2004105...)
     *     000:错误信息起始编号  (eg.2004105000)
     * </p>
     */
    // ==== ERMS ==============
    ERMS_TABLE_RANGE_DEL_FAILED_1(209001,"自定义表适用类型删除失败、该类型绑定了自定义表"),
    ERMS_TABLE_RANGE_DEL_FAILED_2(209002,"自定义表适用类型删除失败、该类型下有子类型"),
    ERMS_TABLE_CHECK_CONTROL_FAILED_1(209003,"校验失败，参数有误"),
    ERMS_TABLE_CHECK_CONTROL_FAILED_2(209004,"校验失败，当前电子表单该列复核人信息配置有误"),
    ERMS_CREATE_TABLE_TASK_FAILED(209005,"任务创建失败，当前已有该任务正在执行中..."),









    ;
    private int code;
    private String message;

    GmpResponseCodeMsg(int code, String message) {
        this.code = code;
        this.message = message;
    }
}