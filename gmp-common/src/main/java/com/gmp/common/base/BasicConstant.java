package com.gmp.common.base;


public class BasicConstant {

    public final static String LOGIN_FAILED = "LOGIN_FAILED";
    public final static String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public final static String X_FORWARDED_FOR = "X-Forwarded-For";
    public final static String X_REAL_IP = "X-Real-IP";

    public final static String USER_ID = "userId";
    public final static String PASSWORD = "password";

    public final static String SY = "sy";
    public final static String AD = "ad";

    public final static String LOCK = "lock";
    public final static String NORM = "norm";
    public final static String DEL = "del";
    public final static String FAIL = "fail";

    public final static String SUCCESS = "SUCCESS";
    public final static String FAILURE = "FAILURE";

    //'成功','SUCCESS'
    //'失败','FAILURE'


    public final static String CONF01 = "CONF01"; //草稿
    public final static String CONF02 = "CONF02";  //启用/发布
    public final static String CONF03 = "CONF03";  //禁用

    public final static String TO_BE_RELEASED_01 = "TO_BE_RELEASED_01";  //待发布
    public final static String TO_BE_RELEASED_02 = "TO_BE_RELEASED_02";  //已发布

    public final static String DISPOSE01 = "DISPOSE01";  //待提交
    public final static String DISPOSE02 = "DISPOSE02";  //待审核
    public final static String DISPOSE03 = "DISPOSE03";  //已关闭

    public static final String HAN_ZI_REGEX = "[^\u4E00-\u9FA5]";
    public static final String SYMBOL_REGEX = "[\\pP\\p{Punct}]";

    //角色常量
    public static final String SA = "SA";   //超级管理员	SA	最高级管理员，拥有所有权限，SA：Super Manager
    public static final String GA = "GA";   //普通管理员	GA	普通管理员，管理客户信息；GA：General Admin
    public static final String DA = "DA";   //经销商管理	DA	经销商管理；DA：Dealer Admin
    public static final String CA = "CA";   //配置管理员	CA	二阶段客户所有权限，应用配置权限；CA：Customer Admin
    public static final String LA = "LA";   //客户管理员	LA	二阶段客户所有权限，应用配置权限；LA：Customer Admin
    public static final String SMA = "SMA";   //服务维护管理员	SMA	客户所有权限，应用配置权限；SMA：Service maintenance administrator

    //连接符号
    public final static String SPLIT_REGEX01 = "@";          //字符串分隔符 @
    public final static String SPLIT_REGEX02 = "-";          //中短横线连接线 -
    public final static String SPLIT_REGEX03 = "——";         //中长横线连接线 ——
    public final static String SPLIT_REGEX04 = "_";          //下划线连接线 _
    public final static String SPLIT_REGEX05 = "\\\\";       //路径分隔符 \\
    public final static String SPLIT_REGEX06 = "/";          //路径分隔符 /
    public final static String SPLIT_REGEX07 = ".";          //分隔符 .
    public final static String SPLIT_REGEX08 = ",";          //分隔符 英文,
    public final static String SPLIT_REGEX09 = "，";         //分隔符 中文,
    public final static String SPLIT_REGEX10 = "";           //分隔符 空,
    public final static String SPLIT_REGEX11 = "&";          //分隔符 &,
    public final static String SPLIT_REGEX12 = "+";        //分隔符 +,
    public final static String SPLIT_REGEX13 = "~";          //分隔符 +,
    public final static String SPLIT_REGEX14 = "*";          //分隔符 *,
    public final static String SPLIT_REGEX15 = " ----- ";    //分隔符 -----,
    public final static String SPLIT_REGEX16 = "=";         //分隔符 =,
    public final static String SPLIT_REGEX17 = "#";         //分隔符 =,
    public final static String SPLIT_REGEX18 = "、";         //分隔符 =,


    public final static String P_K1 = "process";
    public final static String P_K2 = "form";
    public final static String P_K3 = "data";
    public final static String MODULES_CONFIG = "modulesConfig";
    public final static String SALT = "abioplus";
    public final static String DFPASSWORD = "f";

    //是否常量
    public final static String CONSTANT_YES = "YES";          //是
    public final static String CONSTANT_NO = "NO";          //否
    //单位常量
    public final static String M = "M";          //单位分
    public final static String H = "H";          //单位时
    public final static String D = "D";          //单位日
    public final static String Y = "Y";          //单位年


    //定时器
    public final static String CRON_TRIGGER = "CRON_TRIGGER";
    public final static String SIMPLE_TRIGGER = "SIMPLE_TRIGGER";
    public final static String systemFormat = "yyyy-MM-dd";
    public final static String MONTH_TYPE = "月";
    public final static String DAY_TYPE = "日";
    public static String FLAG = "FLAG_STATUS";
    public static String SEDATE = "date";
    public static String ADVANCE_DAY = "advanceDay";
    public static String TABLE_CODE = "tableCode";
    public static String SQL_CONTENT = "sqlContent";
    public static String PROCESS_VO = "processVo";
    public static String SE_PRAMS_VO = "selectPramsVo";
    public static String UP_PRAMS_VO = "updatePramsVo";
    public static String BEAN_METHOD = "BEAN_METHOD";
    public static String MSGS = "MSGS";

    //是否常量
    public final static Long CONSTANT_TRUE = 0L;          //不能为空
    public final static Long CONSTANT_FALSE = 1L;          //否


    //表后缀
    public final static String CONSTANT_TABLE_SUF = "_INFO";

    //文件类型
    public final static String DRAFT_DOCUMENT = "draft";           //草稿文件
    public final static String FORMAL_DOCUMENT = "formal";          //正式文件
    public final static String TEMPORARY_DOCUMENT = "temporary";    //临时文件
    public final static String ACCESSORY_DOCUMENT = "accessory";    //附件文件
    public final static String QUESTION_DOCUMENT = "question";    //附件文件

    //系统编号
    public final static String CONSTANT_QMS = "QMS";    //QMS质量管理系统
    public final static String CONSTANT_DMS = "DMS";    //DMS文件管理系统
    public final static String CONSTANT_TMS = "TMS";    //TMS培训管理系统
    public final static String CONSTANT_CMS = "CMS";    //CMS计量管理系统
    public final static String CONSTANT_LIMS = "LIMS";   //LIMS实验室管理系统
    public final static String CONSTANT_FMS = "FMS";    //FMS档案管理系统
    public final static String CONSTANT_EVENT = "EVENT";  //EVENT事件管理系统
    public final static String CONSTANT_ERMS = "ERMS";  //EVENT事件管理系统
    public final static String CONSTANT_VMS = "VMS";  //EVENT事件管理系统


    public final static String COMM_FORM_DOC_FORMAT = ".pdf,.docx";
    public final static String TMS_FORM_DOC_FORMAT = ".pdf,.docx,.xlsx,.jpg,.png,.jpge,.pptx,.ppt,.mp4,.avi";

    //状态
    public final static String STATUS1 = "NORM";
    public final static String STATUS2 = "del";
    public final static String COM_ROLE = "COM_ROLE";
    public final static String DEPT_ROLE = "DEPT_ROLE";

    public final static String SIGN_AD = "ad";
    public final static String SIGN_SYS = "sy";

    //功能状态
    public final static String FEATURE_TYPE1 = "STANDARD";
    public final static String FEATURE_TYPE2 = "SPECIAL";

    //所有、已选、未选
    public final static String ALL = "ALL";
    public final static String CHECK = "CHECK";
    public final static String UNCHECK = "UNCHECK";

    //后缀名
    public final static String ALIAS_PDF = "pdf";
    public final static String[] ALIAS_VIDEO = {"mp4", "avi"};
    public final static String[] ALIAS_IMAGES = {"png", "jpg", "gif", "jpeg"};
    public final static String[] ALIAS_OFFICES = {"doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf"};

    //sql关键字
    public final static String[] SQL_KEYWORD = {"DELETE", "DROP", "INSERT", "UPDATE", "TRUNCATE", "CREATE", "ALTER"};

    //检验提示信息前缀
    public final static String CMN = "CMN";
    public final static String WAR = "WAR";

    public final static String SISQP = "SISQP";
    public final static String GMP = "GMP";
    public final static String COM_STATIC = "comstatic";
    public final static String SYS_STATIC = "sysstatic";
    public final static String TOAST_STATIC = "toaststatic";
    public final static String BUS_KBN_STATIC = "buskbnstatic";
    public final static String MENU_STATIC = "menustatic";

    //用于存储子流程回传父流程元素参数的名称，逗号分割
    public final static String FORM_ELE_CHILD_TO_PARENT_KEY = "name_FORM_ELE_CHILD_TO_PARENT_KEY";
    //用于存储回传数据，父流程当前索引
    public final static String FORM_ELE_PARENT_IDX_KEY = "name_FORM_ELE_PARENT_IDX_KEY";

    //创建人信息content中键值
    public final static String COMMON_CREATERNAME = "COMMON_name_creatername";
    public final static String COMMON_RECORDEREXPIREDATE = "COMMON_name_expire_date";
    public final static String PARENT_RECORDER_ID = "PARENT_RECORDER_ID";
    public final static String ROOT_RECORDER_ID = "ROOT_RECORDER_ID";
    public final static String CREATER_ID = "CREATER_ID";
    public final static String CREATER_NAME = "CREATER_NAME";
    public final static String CREATER_TIME = "CREATER_TIME";
    public final static String CREATER_DATE = "CREATER_DATE";
    public final static String NAME_CREATE_DATE = "COMMON_name_createrdate";
    public final static String NAME_EXPIRE_DATE = "COMMON_name_expiredate";
    public final static String MASTER_ID = "COMMON_name_masterid";
    public final static String CUR_RECORDER_ID = "COMMON_name_recorderid";
    public final static String CUR_RECORDER_NAME = "COMMON_name_recordername";
    public final static String EXPIRE_DATE = "EXPIRE_DATE";
    public final static String CRE_DEPT_ID = "CREATER_DEPT_ID";
    public final static String CRE_DEPT_NAME = "CREATER_DEPT_NAME";
    public final static String CRE_DEPT_CODE = "CREATER_DEPT_CODE";
    public final static String CRE_SUB_NAME = "CREATER_SUB_NAME";
    public final static String CRE_SUB_CODE = "CREATER_SUB_CODE";
    public final static String CRE_SUB_ID = "CREATER_SUB_ID";
    public final static String CRE_COM_NAME = "CREATER_COM_NAME";
    public final static String CRE_COM_CODE = "CREATER_COM_CODE";
    public final static String CRE_COM_ID = "CREATER_COM_ID";
    public final static String CUR_DATE = "CUR_DATE";
    public final static String CUR_TIME = "CUR_TIME";
    public final static String CUR_DATE_TIME = "CUR_DATE_TIME";
    public final static String CUR_USER_ID = "CUR_USER_ID";
    public final static String CUR_USER_NAME = "CUR_USER_NAME";
    public final static String CUR_DEPT_NAME = "CUR_DEPT_NAME";
    public final static String CUR_DEPT_CODE = "CUR_DEPT_CODE";
    public final static String CUR_DEPT_ID = "CUR_DEPT_ID";
    public final static String CUR_ZNDEPT_ID = "CUR_ZNDEPT_ID";
    public final static String CUR_SUB_ID = "CUR_SUB_ID";
    public final static String CUR_SUB_NAME = "CUR_SUB_NAME";
    public final static String CUR_SUB_CODE = "CUR_SUB_CODE";
    public final static String CUR_COM_ID = "CUR_COM_ID";
    public final static String CUR_COM_NAME = "CUR_COM_NAME";
    public final static String CUR_COM_CODE = "CUR_COM_CODE";
    public final static String CUR_ALLDEPT_ID = "CUR_ALLDEPT_ID";
    public final static String CUR_SPECIAL_STR = "CUR_SPECIAL_STR";


    public final static String REN_CUSTOM_FORM = "com.sisqp.customform.entity.CustomForm";
    public final static String REN_DATA_PERSISTENCE = "com.sisqp.customnode.entity.DataPersistence";
    public final static String REN_PROCESS_INFO = "com.sisqp.customnode.entity.ProcessInfo";
    public final static String RANGE_FORM = "RANGE_FORM";
    public final static String RANGE_PROCESS = "RANGE_PROCESS";

    public static final String CLIENT_BUSS_KBN_TYPE_NAME = "静态数据类型";
    public static final String CLIENT_BUSS_KBN_INFO_NAME = "静态数据信息";
    public static final String ACTION_SCHEDULER_INFO_NAME = "定时器任务信息";
    public static final String CUSTOM_TOAST_INFO_NAME = "提示信息";
    public static final String CUSTOM_RULE_CODE_NAME = "自定义编号规则配置信息";
    public static final String CUSTOM_DATA_PERSISTENCE_NAME1 = "流程数据更新配置信息";
    public static final String CUSTOM_DATA_PERSISTENCE_NAME2 = "功能数据更新配置信息";
    public static final String CUSTOM_EMAIL_MODEL_NAME = "通知内容信息";
    public static final String CUSTOM_FORM_INFO_NAME = "自定义表单信息";
    public static final String CUSTOM_PROCESS_INFO_NAME = "自定义流程信息";


    public static final String MES_FC_CODE = "fc";
    public static final String MES_SUR_CODE = "sur";

    public static final String MES_PAGE_TYPE_HOME = "home";
    public static final String MES_PAGE_TYPE_FCHOME = "fcHome";
    public static final String MES_PAGE_TYPE_FCDETAIL = "fcDetail";
    public static final String MES_PAGE_TYPE_FCALARMSET = "fcAlarmset";
    public static final String MES_PAGE_TYPE_FCREALTIMEALARM = "fcRealTimeAlarm";

    //报警设定值
    public final static String[] ALARM_SET_ARRAY = {"alarmSetH", "alarmSetL", "alarmSetLL", "alarmSetLL", "alarmSetHH", "delaySecondsHH", "delaySecondsLL", "delaySecondsH", "delaySecondsL"};

    //蜂巢各个培养箱name名
    public final static String[] FC_INCUBATOR_NAME_ARRAY = {"A1", "A2", "B1", "B2", "C1", "C2"};
}
