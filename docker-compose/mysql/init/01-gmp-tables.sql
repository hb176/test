-- ============================================================
-- GMP System - 数据库初始化脚本 (Database Initialization)
-- MySQL 8.0+, 字符集 utf8mb4
-- ============================================================
-- 表分类 (Table Categories):
--   [Platform]   sys_user / sys_role / sys_menu / sys_role_menu / sys_user_role
--                sys_dept / sys_config / sys_dict / sys_oper_log
--   [Business]   business_record / form_definition / form_data / form_field_template
--                file_info
--   [Workflow]   wf_process_definition_ext / wf_process_instance_ext / wf_task_ext
--                tbl_flow_listener / tbl_flow_listener_param / wf_activity_form_field
--   [Audit]      tbl_sys_oper_record
-- ============================================================

CREATE DATABASE IF NOT EXISTS gmp_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS nacos_config DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE gmp_system;

-- ================================================================
-- Platform Tables (平台基础表)
-- ================================================================

-- ==================== 1. sys_user (用户) ====================
CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键(自增)',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常,1=已删)',
    create_time     DATETIME        NULL     COMMENT '创建时间',
    create_by       BIGINT          NULL     COMMENT '创建人ID',
    update_time     DATETIME        NULL     COMMENT '更新时间',
    update_by       BIGINT          NULL     COMMENT '更新人ID',
    version         INT          NULL     DEFAULT 0 COMMENT '乐观锁版本号',
    user_id         VARCHAR(64)     NOT NULL COMMENT '登录账号(全局唯一)',
    password        VARCHAR(256)    NULL     COMMENT 'BCrypt密码',
    salt            VARCHAR(64)     NULL     COMMENT '密码盐值',
    job_num         VARCHAR(64)     NULL     COMMENT '工号',
    user_name       VARCHAR(128)    NULL     COMMENT '显示名称',
    domain_id       VARCHAR(64)     NULL     COMMENT '域账号',
    user_type       VARCHAR(32)     NULL     COMMENT '用户类型(INTERNAL/EXTERNAL/SYSTEM)',
    department_code VARCHAR(64)     NULL     COMMENT '部门编码',
    dept_id         BIGINT          NULL     COMMENT '部门ID',
    dept_name       VARCHAR(128)    NULL     COMMENT '部门名称',
    phone           VARCHAR(32)     NULL     COMMENT '手机号',
    mail            VARCHAR(128)    NULL     COMMENT '邮箱',
    wx_account      VARCHAR(128)    NULL     COMMENT '微信账号',
    ding_ding       VARCHAR(128)    NULL     COMMENT '钉钉账号',
    api_account     VARCHAR(128)    NULL     COMMENT 'API账号',
    password_error_num BIGINT       NULL     DEFAULT 0 COMMENT '密码错误次数',
    password_expire_time DATE      NULL     COMMENT '密码过期时间',
    subsidiaries_id       BIGINT     NULL     COMMENT '分子公司ID',
    subsidiaries_code     VARCHAR(64) NULL    COMMENT '分子公司编码',
    subsidiaries_name     VARCHAR(128) NULL   COMMENT '分子公司名称',
    company_id            BIGINT     NULL     COMMENT '公司ID',
    company_code          VARCHAR(64) NULL    COMMENT '公司编码',
    company_name          VARCHAR(128) NULL   COMMENT '公司名称',
    head_portrait   VARCHAR(256)    NULL     COMMENT '头像路径',
    has_bloc        VARCHAR(8)      NULL     COMMENT '集团封锁(YES/NO)',
    status          VARCHAR(16)     NULL     DEFAULT 'NORM' COMMENT '状态(NORM/DISABLED/LOCKED)',
    lock_time       DATETIME        NULL     COMMENT '锁定时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    KEY idx_dept_id (dept_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

-- ==================== 2. sys_role (角色) ====================
CREATE TABLE IF NOT EXISTS sys_role (
    id              BIGINT          NOT NULL,
    deleted         TINYINT         NOT NULL DEFAULT 0,
    create_time     DATETIME        NULL,
    create_by       BIGINT          NULL,
    update_time     DATETIME        NULL,
    update_by       BIGINT          NULL,
    version         VARCHAR(32)     NULL,
    role_code       VARCHAR(64)     NOT NULL COMMENT '角色编码(如ROLE_ADMIN)',
    role_name       VARCHAR(128)    NULL     COMMENT '角色名称',
    description     VARCHAR(256)    NULL     COMMENT '角色描述',
    role_level      INT             NULL     COMMENT '角色级别',
    data_scope      VARCHAR(32)     NULL     DEFAULT 'ALL' COMMENT '数据权限(ALL/DEPT/DEPT_AND_CHILDREN/SELF/CUSTOM)',
    status          INT             NULL     DEFAULT 1 COMMENT '状态(1=启用,0=禁用)',
    is_system       TINYINT(1)      NULL     DEFAULT 0 COMMENT '是否系统内置',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色';

-- ==================== 3. sys_menu (菜单/权限) ====================
CREATE TABLE IF NOT EXISTS sys_menu (
    id              BIGINT          NOT NULL,
    deleted         TINYINT         NOT NULL DEFAULT 0,
    create_time     DATETIME        NULL,
    create_by       BIGINT          NULL,
    update_time     DATETIME        NULL,
    update_by       BIGINT          NULL,
    version         VARCHAR(32)     NULL,
    parent_id       BIGINT          NULL     DEFAULT 0 COMMENT '父菜单ID(0=顶级)',
    menu_name       VARCHAR(128)    NULL     COMMENT '菜单名称',
    menu_type       INT             NULL     COMMENT '类型(0=目录,1=菜单,2=按钮)',
    path            VARCHAR(256)    NULL     COMMENT '路由路径',
    component       VARCHAR(256)    NULL     COMMENT '前端组件路径',
    permission      VARCHAR(256)    NULL     COMMENT '权限标识(如system:user:add)',
    icon            VARCHAR(128)    NULL     COMMENT '图标',
    sort_order      INT             NULL     COMMENT '排序',
    visible         INT             NULL     DEFAULT 1 COMMENT '是否可见(1=是,0=否)',
    is_external     TINYINT(1)      NULL     DEFAULT 0 COMMENT '是否外链',
    form_key        VARCHAR(128)    NULL     COMMENT '绑定表单Key(form_definition.code)',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单';

-- ==================== 4. sys_config (系统配置) ====================
CREATE TABLE IF NOT EXISTS sys_config (
    id              BIGINT          NOT NULL,
    deleted         TINYINT         NOT NULL DEFAULT 0,
    create_time     DATETIME        NULL,
    create_by       BIGINT          NULL,
    update_time     DATETIME        NULL,
    update_by       BIGINT          NULL,
    version         VARCHAR(32)     NULL,
    config_key      VARCHAR(128)    NOT NULL COMMENT '配置键',
    config_value    TEXT            NULL     COMMENT '配置值',
    category        VARCHAR(64)     NULL     COMMENT '分类(system/security/storage)',
    description     VARCHAR(256)    NULL     COMMENT '描述',
    is_system       TINYINT(1)      NULL     DEFAULT 0 COMMENT '是否系统内置',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置';

-- ==================== 5. sys_dict (字典) ====================
CREATE TABLE IF NOT EXISTS sys_dict (
    id              BIGINT          NOT NULL,
    deleted         TINYINT         NOT NULL DEFAULT 0,
    create_time     DATETIME        NULL,
    create_by       BIGINT          NULL,
    update_time     DATETIME        NULL,
    update_by       BIGINT          NULL,
    version         VARCHAR(32)     NULL,
    dict_code       VARCHAR(64)     NOT NULL COMMENT '字典编码(如GENDER)',
    dict_name       VARCHAR(128)    NULL     COMMENT '字典名称',
    item_label      VARCHAR(256)    NULL     COMMENT '显示标签',
    item_value      VARCHAR(256)    NULL     COMMENT '存储值',
    sort_order      INT             NULL     COMMENT '排序',
    status          INT             NULL     DEFAULT 1 COMMENT '状态(1=启用,0=禁用)',
    css_class       VARCHAR(64)     NULL     COMMENT 'CSS样式类',
    remark          VARCHAR(512)    NULL     COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_dict_code (dict_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典';

-- ==================== 6. sys_dept (部门) ====================
CREATE TABLE IF NOT EXISTS sys_dept (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键(自增)',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常,1=已删)',
    create_time     DATETIME        NULL     COMMENT '创建时间',
    create_by       BIGINT          NULL     COMMENT '创建人ID',
    update_time     DATETIME        NULL     COMMENT '更新时间',
    update_by       BIGINT          NULL     COMMENT '更新人ID',
    version         INT          NULL     DEFAULT 0 COMMENT '乐观锁版本号',
    parent_id       BIGINT          NOT NULL DEFAULT 0 COMMENT '父部门ID(0=顶级)',
    ancestors       VARCHAR(500)    NULL     COMMENT '祖级列表(如0,1,5)',
    dept_code       VARCHAR(64)     NOT NULL COMMENT '部门编码(全局唯一)',
    dept_name       VARCHAR(128)    NOT NULL COMMENT '部门名称',
    sort_order      INT             NULL     DEFAULT 0 COMMENT '排序号',
    leader          VARCHAR(64)     NULL     COMMENT '负责人',
    phone           VARCHAR(32)     NULL     COMMENT '联系电话',
    email           VARCHAR(128)    NULL     COMMENT '邮箱',
    status          INT             NULL     DEFAULT 1 COMMENT '状态(1=启用,0=禁用)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dept_code (dept_code),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门信息';

-- ==================== 7. sys_oper_log (操作日志) ====================
CREATE TABLE IF NOT EXISTS sys_oper_log (
    id              BIGINT          NOT NULL,
    deleted         TINYINT         NOT NULL DEFAULT 0,
    create_time     DATETIME        NULL,
    create_by       BIGINT          NULL,
    update_time     DATETIME        NULL,
    update_by       BIGINT          NULL,
    version         VARCHAR(32)     NULL,
    oper_type       VARCHAR(32)     NULL     COMMENT '操作类型(LOGIN/QUERY/ADD/UPDATE/DELETE/EXPORT)',
    module          VARCHAR(128)    NULL     COMMENT '模块名称',
    description     VARCHAR(512)    NULL     COMMENT '操作描述',
    request_url     VARCHAR(256)    NULL     COMMENT '请求URL',
    request_method  VARCHAR(16)     NULL     COMMENT '请求方法(GET/POST/PUT/DELETE)',
    request_params  TEXT            NULL     COMMENT '请求参数(脱敏)',
    cost_time       BIGINT          NULL     COMMENT '耗时(ms)',
    result          VARCHAR(16)     NULL     COMMENT '结果(SUCCESS/FAIL)',
    error_msg       TEXT            NULL     COMMENT '错误信息',
    oper_user_id    BIGINT          NULL     COMMENT '操作人ID',
    oper_user_name  VARCHAR(128)    NULL     COMMENT '操作人名称',
    oper_ip         VARCHAR(64)     NULL     COMMENT '操作IP',
    PRIMARY KEY (id),
    KEY idx_create_time (create_time),
    KEY idx_oper_type (oper_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';

-- ================================================================
-- Business Tables (业务表)
-- ================================================================

-- ==================== 7. business_record (业务记录) ====================
CREATE TABLE IF NOT EXISTS business_record (
    id                  BIGINT          NOT NULL,
    deleted             TINYINT         NOT NULL DEFAULT 0,
    create_time         DATETIME        NULL,
    create_by           BIGINT          NULL,
    update_time         DATETIME        NULL,
    update_by           BIGINT          NULL,
    version             VARCHAR(32)     NULL,
    business_type       VARCHAR(32)     NOT NULL COMMENT '业务类型(QMS_DEVIATION/DMS_DOCUMENT等)',
    business_no         VARCHAR(64)     NULL     COMMENT '业务编号(如DEV-2026-001)',
    title               VARCHAR(256)    NULL     COMMENT '业务标题',
    business_status     VARCHAR(32)     NULL     COMMENT '业务状态(与流程状态同步)',
    form_key            VARCHAR(128)    NULL     COMMENT '表单定义Key',
    form_data_id        BIGINT          NULL     COMMENT '表单数据ID',
    process_instance_ext_id BIGINT      NULL     COMMENT '流程实例扩展ID',
    process_instance_id VARCHAR(64)     NULL     COMMENT 'Flowable流程实例ID',
    initiator_id        BIGINT          NULL     COMMENT '发起人ID',
    initiator_name      VARCHAR(128)    NULL     COMMENT '发起人名称',
    initiator_dept_id   BIGINT          NULL     COMMENT '发起人部门ID',
    initiated_at        DATETIME        NULL     COMMENT '发起时间',
    completed_at        DATETIME        NULL     COMMENT '完成时间',
    urgency             VARCHAR(16)     NULL     DEFAULT 'NORMAL' COMMENT '紧急程度(NORMAL/URGENT/CRITICAL)',
    parent_business_id  BIGINT          NULL     COMMENT '上级业务记录ID',
    product_id          BIGINT          NULL     COMMENT '产品ID',
    product_name        VARCHAR(256)    NULL     COMMENT '产品名称',
    batch_no            VARCHAR(128)    NULL     COMMENT '批号',
    tags                VARCHAR(1024)   NULL     COMMENT '标签(JSON数组)',
    summary             TEXT            NULL     COMMENT '业务摘要',
    PRIMARY KEY (id),
    UNIQUE KEY uk_business_no (business_no),
    KEY idx_business_type (business_type),
    KEY idx_initiator_id (initiator_id),
    KEY idx_process_instance_id (process_instance_id),
    KEY idx_business_status (business_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务记录(单表继承)';

-- ==================== 8. form_definition (表单定义) ====================
CREATE TABLE IF NOT EXISTS form_definition (
    id              BIGINT          NOT NULL,
    deleted         TINYINT         NOT NULL DEFAULT 0,
    create_time     DATETIME        NULL,
    create_by       BIGINT          NULL,
    update_time     DATETIME        NULL,
    update_by       BIGINT          NULL,
    version         VARCHAR(32)     NULL,
    name            VARCHAR(256)    NULL     COMMENT '表单名称',
    code            VARCHAR(128)    NOT NULL COMMENT '表单编码(全局唯一)',
    sys_module      VARCHAR(32)     NULL     COMMENT '系统模块(QMS/DMS/TMS等)',
    module          VARCHAR(128)    NULL     COMMENT '子模块',
    module_name     VARCHAR(128)    NULL     COMMENT '子模块名称',
    des             VARCHAR(512)    NULL     COMMENT '描述',
    sort_code       VARCHAR(64)     NULL     COMMENT '排序码',
    status          VARCHAR(16)     NULL     COMMENT '状态(NORM/DISABLED)',
    lock_user       VARCHAR(64)     NULL     COMMENT '锁定编辑人ID',
    form_range_type VARCHAR(32)     NULL     COMMENT '范围(RANGE_PROCESS/RANGE_FUNCTION)',
    form_name_show  VARCHAR(8)      NULL     COMMENT '是否显示名称(YES/NO)',
    edit_content    MEDIUMTEXT      NULL     COMMENT '编辑态JSON Schema',
    view_content    MEDIUMTEXT      NULL     COMMENT '查看态JSON Schema',
    add_content     MEDIUMTEXT      NULL     COMMENT '新增态JSON Schema',
    js_content      TEXT            NULL     COMMENT 'JS联动脚本',
    fun_form_recorder_id_rule VARCHAR(256) NULL COMMENT 'SpEL编号生成规则',
    fun_form_auditor TEXT           NULL     COMMENT '功能表单审批人配置JSON',
    workflow_key    VARCHAR(128)    NULL     COMMENT '绑定流程定义Key',
    is_system       TINYINT(1)      NULL     DEFAULT 0 COMMENT '是否系统内置',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表单定义';

-- ==================== 9. form_data (表单数据) ====================
CREATE TABLE IF NOT EXISTS form_data (
    id                  BIGINT          NOT NULL,
    deleted             TINYINT         NOT NULL DEFAULT 0,
    create_time         DATETIME        NULL,
    create_by           BIGINT          NULL,
    update_time         DATETIME        NULL,
    update_by           BIGINT          NULL,
    version             VARCHAR(32)     NULL,
    form_def_id         BIGINT          NULL     COMMENT '表单定义ID',
    form_key            VARCHAR(128)    NULL     COMMENT '表单Key(冗余)',
    form_version        INT             NULL     COMMENT '表单版本号',
    form_data           MEDIUMTEXT      NULL     COMMENT '表单数据JSON',
    business_key        VARCHAR(128)    NULL     COMMENT '业务Key',
    process_instance_id VARCHAR(64)     NULL     COMMENT '流程实例ID',
    data_status         VARCHAR(16)     NULL     COMMENT '数据状态(DRAFT/SUBMITTED/APPROVED/REJECTED)',
    submit_user_id      BIGINT          NULL     COMMENT '提交人ID',
    submit_user_name    VARCHAR(128)    NULL     COMMENT '提交人名称',
    submit_time         DATETIME        NULL     COMMENT '提交时间',
    indexed_title       VARCHAR(256)    NULL     COMMENT '索引字段-标题',
    indexed_code        VARCHAR(128)    NULL     COMMENT '索引字段-编码',
    indexed_field1      VARCHAR(256)    NULL     COMMENT '索引字段1(扩展)',
    indexed_field2      VARCHAR(256)    NULL     COMMENT '索引字段2(扩展)',
    indexed_field3      VARCHAR(256)    NULL     COMMENT '索引字段3(扩展)',
    PRIMARY KEY (id),
    KEY idx_form_key (form_key),
    KEY idx_process_instance_id (process_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表单数据';

-- ==================== 10. form_field_template (字段模板) ====================
CREATE TABLE IF NOT EXISTS form_field_template (
    id              BIGINT          NOT NULL,
    deleted         TINYINT         NOT NULL DEFAULT 0,
    create_time     DATETIME        NULL,
    create_by       BIGINT          NULL,
    update_time     DATETIME        NULL,
    update_by       BIGINT          NULL,
    version         VARCHAR(32)     NULL,
    field_key       VARCHAR(128)    NOT NULL COMMENT '字段模板Key',
    field_name      VARCHAR(256)    NULL     COMMENT '字段名称',
    field_type      VARCHAR(32)     NULL     COMMENT '字段类型(TEXT/NUMBER/DATE/SELECT/FILE/TABLE)',
    category        VARCHAR(32)     NULL     COMMENT '分类(BASIC/BUSINESS/SYSTEM)',
    default_config  TEXT            NULL     COMMENT '默认配置JSON',
    is_system       TINYINT(1)      NULL     DEFAULT 0 COMMENT '是否系统内置',
    PRIMARY KEY (id),
    UNIQUE KEY uk_field_key (field_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表单字段模板';

-- ==================== 11. file_info (文件信息) ====================
CREATE TABLE IF NOT EXISTS file_info (
    id                  BIGINT          NOT NULL,
    deleted             TINYINT         NOT NULL DEFAULT 0,
    create_time         DATETIME        NULL,
    create_by           BIGINT          NULL,
    update_time         DATETIME        NULL,
    update_by           BIGINT          NULL,
    version             VARCHAR(32)     NULL,
    code                VARCHAR(128)    NULL     COMMENT '文件编号(如SOP-QA-001)',
    name                VARCHAR(256)    NULL     COMMENT '文件名称',
    alias               VARCHAR(256)    NULL     COMMENT '别名/标题',
    en_name             VARCHAR(256)    NULL     COMMENT '英文名称',
    original_file_name  VARCHAR(512)    NULL     COMMENT '原始上传文件名',
    pdf_path            VARCHAR(512)    NULL     COMMENT 'PDF存储路径',
    real_file_path      VARCHAR(512)    NULL     COMMENT '真实文件路径',
    file_action_type    VARCHAR(16)     NULL     COMMENT '操作类型(NEW/REVISE/OBSOLETE)',
    file_category       VARCHAR(16)     NULL     COMMENT '文件类别(SOP/RECORD/REPORT/DRAWING)',
    file_belong_type    VARCHAR(16)     NULL     COMMENT '归属类型(DEPARTMENT/COMPANY/PERSONAL)',
    file_type_code      VARCHAR(64)     NULL     COMMENT '文件类型编码',
    file_type           VARCHAR(64)     NULL     COMMENT '文件类型',
    file_uuid           VARCHAR(128)    NULL     COMMENT 'MinIO对象标识',
    file_version        BIGINT          NULL     COMMENT '版本号(整数)',
    file_version_mi     VARCHAR(32)     NULL     COMMENT '版本号(字符串,如1.0)',
    detail              TEXT            NULL     COMMENT '描述',
    file_size           BIGINT          NULL     COMMENT '文件大小(字节)',
    pages               INT             NULL     COMMENT '页数',
    bucket              VARCHAR(128)    NULL     COMMENT '存储桶',
    storage_path        VARCHAR(512)    NULL     COMMENT '存储路径',
    temp_path           VARCHAR(512)    NULL     COMMENT '临时路径',
    system_module       VARCHAR(32)     NULL     COMMENT '系统模块(DMS/QMS/TMS)',
    md5                 VARCHAR(64)     NULL     COMMENT 'MD5',
    md5_hex             VARCHAR(128)    NULL     COMMENT 'MD5 Hex',
    folder_id           BIGINT          NULL     COMMENT '文件夹ID',
    user_id             VARCHAR(64)     NULL     COMMENT '上传人ID',
    user_name           VARCHAR(128)    NULL     COMMENT '上传人名称',
    dept_code           VARCHAR(64)     NULL     COMMENT '部门编码',
    dept_id             BIGINT          NULL     COMMENT '部门ID',
    dept_name           VARCHAR(128)    NULL     COMMENT '部门名称',
    upload_time         DATETIME        NULL     COMMENT '上传时间',
    valid_status        VARCHAR(16)     NULL     COMMENT '生效状态',
    reissue_status      VARCHAR(16)     NULL     COMMENT '补发状态',
    recheck_status      VARCHAR(16)     NULL     COMMENT '复核状态',
    rec_del_status      VARCHAR(16)     NULL     COMMENT '回收/删除状态',
    print_status        VARCHAR(16)     NULL     COMMENT '打印状态',
    access_status       VARCHAR(16)     NULL     COMMENT '访问状态',
    train_status        VARCHAR(16)     NULL     COMMENT '培训状态',
    ftr_status          VARCHAR(16)     NULL     COMMENT '培训记录状态',
    obs_status          VARCHAR(16)     NULL     COMMENT '作废状态',
    status              VARCHAR(16)     NULL     COMMENT '综合状态',
    recycle             TINYINT(1)      NULL     COMMENT '回收标记',
    obsolete_time       DATE            NULL     COMMENT '作废日期',
    obsolete_user_id    VARCHAR(64)     NULL     COMMENT '作废人ID',
    obsolete_user_name  VARCHAR(128)    NULL     COMMENT '作废人名称',
    obsolete_dept_id    BIGINT          NULL     COMMENT '作废部门ID',
    obsolete_dept_name  VARCHAR(128)    NULL     COMMENT '作废部门名称',
    valid_time          DATE            NULL     COMMENT '生效日期',
    end_time            DATE            NULL     COMMENT '截止日期',
    apply_file_date     DATE            NULL     COMMENT '申请日期',
    rec_recorder_date   DATE            NULL     COMMENT '接收记录日期',
    delay_date          DATE            NULL     COMMENT '延期日期',
    faf_recorder_code   VARCHAR(128)    NULL     COMMENT 'FAF审批编码',
    fea_recorder_code   VARCHAR(128)    NULL     COMMENT 'FEA审批编码',
    fte_recorder_code   VARCHAR(128)    NULL     COMMENT 'FTE审批编码',
    fpd_recorder_code   VARCHAR(128)    NULL     COMMENT 'FPD审批编码',
    frd_recorder_code   VARCHAR(128)    NULL     COMMENT 'FRD审批编码',
    fsa_recorder_code   VARCHAR(128)    NULL     COMMENT 'FSA审批编码',
    frx_recorder_code   VARCHAR(128)    NULL     COMMENT 'FRX审批编码',
    source_file_code    VARCHAR(128)    NULL     COMMENT '源文件编码',
    source_file_id      BIGINT          NULL     COMMENT '源文件ID',
    relevant_file       TEXT            NULL     COMMENT '关联文件',
    ref_code            TEXT            NULL     COMMENT 'JSON引用',
    com_id              BIGINT          NULL     COMMENT '公司ID',
    sub_id              BIGINT          NULL     COMMENT '分子公司ID',
    record_id           VARCHAR(64)     NULL     COMMENT '关联业务记录ID',
    record_type         VARCHAR(32)     NULL     COMMENT '记录类型',
    auth_types          TEXT            NULL     COMMENT '授权类型JSON',
    name_value          VARCHAR(512)    NULL     COMMENT '名称值(检索)',
    content             TEXT            NULL     COMMENT '内容摘要',
    draft_file_id       BIGINT          NULL     COMMENT '草稿文件ID',
    use_recycle_status  VARCHAR(16)     NULL     COMMENT '使用回收状态',
    use_destroy_status  VARCHAR(16)     NULL     COMMENT '使用销毁状态',
    merge_status        VARCHAR(16)     NULL     COMMENT '合并状态',
    bxd_status          VARCHAR(16)     NULL     COMMENT '并行分发状态',
    tmp_file_code       VARCHAR(128)    NULL     COMMENT '临时文件编码',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code),
    KEY idx_folder_id (folder_id),
    KEY idx_file_type (file_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息';

-- ================================================================
-- Workflow Tables (流程引擎表)
-- ================================================================

-- ==================== 12. wf_process_definition_ext (流程定义扩展) ====================
CREATE TABLE IF NOT EXISTS wf_process_definition_ext (
    id                      BIGINT          NOT NULL,
    deleted                 TINYINT         NOT NULL DEFAULT 0,
    create_time             DATETIME        NULL,
    create_by               BIGINT          NULL,
    update_time             DATETIME        NULL,
    update_by               BIGINT          NULL,
    version                 VARCHAR(32)     NULL,
    process_definition_id   VARCHAR(128)    NULL     COMMENT 'Flowable流程定义ID',
    process_key             VARCHAR(128)    NOT NULL COMMENT '流程Key(BPMN process id)',
    process_name            VARCHAR(256)    NULL     COMMENT '流程中文名',
    category                VARCHAR(32)     NULL     COMMENT '分类(COMM/DMS/QMS/TMS等)',
    description             TEXT            NULL     COMMENT '描述',
    form_key                VARCHAR(128)    NULL     COMMENT '绑定表单Key',
    start_form_key          VARCHAR(128)    NULL     COMMENT '发起表单Key',
    diagram_url             VARCHAR(512)    NULL     COMMENT '流程图缩略图URL',
    expire_expression       VARCHAR(64)     NULL     COMMENT '超时表达式(如PT72H)',
    priority                INT             NULL     COMMENT '优先级',
    allow_withdraw          TINYINT(1)      NULL     DEFAULT 1 COMMENT '允许撤回',
    allow_urge              TINYINT(1)      NULL     DEFAULT 1 COMMENT '允许催办',
    mobile_enabled          TINYINT(1)      NULL     DEFAULT 0 COMMENT '移动端审批',
    status                  VARCHAR(16)     NULL     COMMENT '状态(DRAFT/PUBLISHED/SUSPENDED)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_process_key (process_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程定义扩展';

-- ==================== 13. wf_process_instance_ext (流程实例扩展) ====================
CREATE TABLE IF NOT EXISTS wf_process_instance_ext (
    id                      BIGINT          NOT NULL,
    deleted                 TINYINT         NOT NULL DEFAULT 0,
    create_time             DATETIME        NULL,
    create_by               BIGINT          NULL,
    update_time             DATETIME        NULL,
    update_by               BIGINT          NULL,
    version                 VARCHAR(32)     NULL,
    recorder_id             VARCHAR(64)     NULL     COMMENT '实例唯一编码',
    recorder_name           VARCHAR(256)    NULL     COMMENT '实例标题',
    workflow_name           VARCHAR(256)    NULL     COMMENT '流程名称',
    cur_status              VARCHAR(32)     NULL     COMMENT '当前状态(APPROVING/COMPLETED/REJECTED)',
    process_id              BIGINT          NULL     COMMENT '流程定义ID',
    parent_node_name        VARCHAR(128)    NULL     COMMENT '当前节点名称',
    parent_recorder_id      VARCHAR(64)     NULL     COMMENT '父实例编码',
    root_recorder_id        VARCHAR(64)     NULL     COMMENT '根实例编码',
    the_content             MEDIUMTEXT      NULL     COMMENT '表单编辑数据',
    tmp_the_content         MEDIUMTEXT      NULL     COMMENT '临时表单内容',
    process_content         MEDIUMTEXT      NULL     COMMENT '节点定义快照JSON',
    creater_id              VARCHAR(64)     NULL     COMMENT '创建人ID',
    creater_name            VARCHAR(128)    NULL     COMMENT '创建人名称',
    creater_dept_id         BIGINT          NULL     COMMENT '创建人部门ID',
    creater_dept_code       VARCHAR(64)     NULL     COMMENT '创建人部门编码',
    creater_dept_name       VARCHAR(128)    NULL     COMMENT '创建人部门名称',
    creater_subsi_id        BIGINT          NULL     COMMENT '创建人分子公司ID',
    creater_subsi_code      VARCHAR(64)     NULL     COMMENT '创建人分子公司编码',
    creater_subsi_name      VARCHAR(128)    NULL     COMMENT '创建人分子公司名称',
    creater_comp_id         BIGINT          NULL     COMMENT '创建人公司ID',
    creater_comp_code       VARCHAR(64)     NULL     COMMENT '创建人公司编码',
    creater_comp_name       VARCHAR(128)    NULL     COMMENT '创建人公司名称',
    cur_dept_id             BIGINT          NULL     COMMENT '当前处理部门ID',
    cur_dept_code           VARCHAR(64)     NULL     COMMENT '当前处理部门编码',
    cur_dept_name           VARCHAR(128)    NULL     COMMENT '当前处理部门名称',
    create_date             DATE            NULL     COMMENT '创建日期',
    expire_date             DATE            NULL     COMMENT '截止日期',
    abnormal_close_status   VARCHAR(16)     NULL     COMMENT '异常关闭状态',
    accessory_id            VARCHAR(512)    NULL     COMMENT '附件ID',
    sys_module_code         VARCHAR(32)     NULL     COMMENT '系统模块编码',
    flowable_process_instance_id VARCHAR(64) NULL     COMMENT 'Flowable实例ID',
    business_key            VARCHAR(128)    NULL     COMMENT '业务Key',
    form_data_id            BIGINT          NULL     COMMENT '表单数据ID',
    form_key                VARCHAR(128)    NULL     COMMENT '表单Key',
    PRIMARY KEY (id),
    KEY idx_recorder_id (recorder_id),
    KEY idx_cur_status (cur_status),
    KEY idx_flowable_instance (flowable_process_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程实例扩展';

-- ==================== 14. wf_task_ext (任务扩展) ====================
CREATE TABLE IF NOT EXISTS wf_task_ext (
    id                      BIGINT          NOT NULL,
    deleted                 TINYINT         NOT NULL DEFAULT 0,
    create_time             DATETIME        NULL,
    create_by               BIGINT          NULL,
    update_time             DATETIME        NULL,
    update_by               BIGINT          NULL,
    version                 VARCHAR(32)     NULL,
    task_id                 VARCHAR(128)    NULL     COMMENT 'Flowable任务ID',
    process_instance_ext_id BIGINT          NULL     COMMENT '流程实例扩展ID',
    task_type               VARCHAR(16)     NULL     COMMENT '任务类型(MAJOR/ASSIST/COUNTERSIGN)',
    task_name               VARCHAR(256)    NULL     COMMENT '任务名称(冗余)',
    assignee                VARCHAR(64)     NULL     COMMENT '审批人ID',
    assignee_name           VARCHAR(128)    NULL     COMMENT '审批人名称',
    comment                 TEXT            NULL     COMMENT '审批意见',
    approve_result          VARCHAR(16)     NULL     COMMENT '审批结果(APPROVED/REJECTED/DELEGATED/TRANSFERRED)',
    duration_minutes        BIGINT          NULL     COMMENT '完成耗时(分钟)',
    is_add_sign             TINYINT(1)      NULL     DEFAULT 0 COMMENT '是否加签',
    add_sign_from_task_id   VARCHAR(128)    NULL     COMMENT '加签来源任务ID',
    is_delegated            TINYINT(1)      NULL     DEFAULT 0 COMMENT '是否委派',
    delegator_id            VARCHAR(64)     NULL     COMMENT '原审批人ID',
    delegator_name          VARCHAR(128)    NULL     COMMENT '原审批人名称',
    is_overdue              TINYINT(1)      NULL     DEFAULT 0 COMMENT '是否超期完成',
    node_form_key           VARCHAR(128)    NULL     COMMENT '节点自定义表单Key',
    readonly_fields         VARCHAR(1024)   NULL     COMMENT '只读字段JSON数组',
    hidden_fields           VARCHAR(1024)   NULL     COMMENT '隐藏字段JSON数组',
    PRIMARY KEY (id),
    KEY idx_task_id (task_id),
    KEY idx_process_instance_ext_id (process_instance_ext_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程任务扩展';

-- ================================================================
-- Audit & Extended Tables (审计及扩展表)
-- ================================================================

-- ==================== 15. tbl_sys_oper_record (操作记录-独立主键) ====================
CREATE TABLE IF NOT EXISTS tbl_sys_oper_record (
    id              VARCHAR(64)     NOT NULL COMMENT 'UUID主键',
    user_code       VARCHAR(64)     NULL     COMMENT '用户编码',
    user_name       VARCHAR(128)    NULL     COMMENT '用户名称',
    oper_content    TEXT            NULL     COMMENT '操作内容',
    oper_type       VARCHAR(32)     NULL     COMMENT '操作类型',
    source          VARCHAR(64)     NULL     COMMENT '来源',
    date_time       DATETIME        NULL     COMMENT '操作时间',
    ip              VARCHAR(64)     NULL     COMMENT 'IP地址',
    date            VARCHAR(32)     NULL     COMMENT '日期字符串',
    year            INT             NULL     COMMENT '年',
    month           INT             NULL     COMMENT '月',
    day             INT             NULL     COMMENT '日',
    PRIMARY KEY (id),
    KEY idx_date_time (date_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作记录';

-- ==================== 16. tbl_flow_listener (流程监听器) ====================
CREATE TABLE IF NOT EXISTS tbl_flow_listener (
    id              VARCHAR(64)     NOT NULL COMMENT 'UUID主键',
    name            VARCHAR(128)    NULL     COMMENT '监听器名称',
    listener_type   VARCHAR(32)     NULL     COMMENT '监听器类型(execution/task)',
    event_type      VARCHAR(32)     NULL     COMMENT '事件类型(create/assignment/complete/delete/take)',
    value           VARCHAR(512)    NULL     COMMENT '实现类全限定名或Spring Bean名称',
    remark          VARCHAR(512)    NULL     COMMENT '备注说明',
    order_no        INT             NULL     DEFAULT 0 COMMENT '排序号',
    create_time     DATETIME        NULL     COMMENT '创建时间',
    creator         VARCHAR(64)     NULL     COMMENT '创建人',
    update_time     DATETIME        NULL     COMMENT '更新时间',
    updator         VARCHAR(64)     NULL     COMMENT '更新人',
    del_flag        TINYINT         NOT NULL DEFAULT 1 COMMENT '删除标识(1=正常,0=已删)',
    PRIMARY KEY (id),
    KEY idx_listener_type (listener_type),
    KEY idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程监听器';

-- ==================== 17. tbl_flow_listener_param (监听器参数) ====================
CREATE TABLE IF NOT EXISTS tbl_flow_listener_param (
    id              VARCHAR(64)     NOT NULL COMMENT 'UUID主键',
    listener_id     VARCHAR(64)     NOT NULL COMMENT '所属监听器ID',
    name            VARCHAR(128)    NULL     COMMENT '参数名称',
    value           VARCHAR(512)    NULL     COMMENT '参数值',
    remark          VARCHAR(512)    NULL     COMMENT '备注说明',
    create_time     DATETIME        NULL     COMMENT '创建时间',
    creator         VARCHAR(64)     NULL     COMMENT '创建人',
    update_time     DATETIME        NULL     COMMENT '更新时间',
    updator         VARCHAR(64)     NULL     COMMENT '更新人',
    del_flag        TINYINT         NOT NULL DEFAULT 1 COMMENT '删除标识(1=正常,0=已删)',
    PRIMARY KEY (id),
    KEY idx_listener_id (listener_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程监听器参数';

-- ==================== 18. wf_activity_form_field (节点表单字段权限) ====================
CREATE TABLE IF NOT EXISTS wf_activity_form_field (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键(自增)',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常,1=已删)',
    create_time     DATETIME        NULL     COMMENT '创建时间',
    create_by       BIGINT          NULL     COMMENT '创建人ID',
    update_time     DATETIME        NULL     COMMENT '更新时间',
    update_by       BIGINT          NULL     COMMENT '更新人ID',
    process_key     VARCHAR(128)    NOT NULL COMMENT '流程Key',
    activity_id     VARCHAR(128)    NOT NULL COMMENT '节点ID(BPMN activity id)',
    field_key       VARCHAR(128)    NOT NULL COMMENT '表单字段Key',
    field_name      VARCHAR(256)    NULL     COMMENT '字段名称(冗余)',
    readonly_flag   TINYINT(1)      NULL     DEFAULT 0 COMMENT '是否只读(1=是,0=否)',
    hidden_flag     TINYINT(1)      NULL     DEFAULT 0 COMMENT '是否隐藏(1=是,0=否)',
    required_flag   TINYINT(1)      NULL     DEFAULT 0 COMMENT '是否必填(1=是,0=否)',
    sort_order      INT             NULL     DEFAULT 0 COMMENT '排序号',
    PRIMARY KEY (id),
    KEY idx_process_activity (process_key, activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程节点表单字段权限';

-- ==================== 19. sys_message (站内信) ====================
CREATE TABLE IF NOT EXISTS sys_message (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键(自增)',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常,1=已删)',
    create_time     DATETIME        NULL     COMMENT '创建时间',
    create_by       BIGINT          NULL     COMMENT '创建人ID',
    update_time     DATETIME        NULL     COMMENT '更新时间',
    update_by       BIGINT          NULL     COMMENT '更新人ID',
    version         INT             NULL     DEFAULT 0 COMMENT '乐观锁版本号',
    title           VARCHAR(256)    NOT NULL COMMENT '消息标题',
    content         TEXT            NULL     COMMENT '消息内容',
    msg_type        VARCHAR(32)     NULL     DEFAULT 'NOTIFICATION' COMMENT '消息类型(SYSTEM/NOTIFICATION/WARNING/APPROVAL)',
    sender_id       BIGINT          NULL     COMMENT '发送人ID(系统消息为0)',
    sender_name     VARCHAR(128)    NULL     COMMENT '发送人名称',
    receiver_id     BIGINT          NOT NULL COMMENT '接收人ID',
    receiver_name   VARCHAR(128)    NULL     COMMENT '接收人名称',
    read_flag       TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否已读(0=未读,1=已读)',
    read_time       DATETIME        NULL     COMMENT '阅读时间',
    business_type   VARCHAR(32)     NULL     COMMENT '关联业务类型',
    business_id     BIGINT          NULL     COMMENT '关联业务ID',
    PRIMARY KEY (id),
    KEY idx_receiver_read (receiver_id, read_flag),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内信';

-- ==================== 20. sys_message_template (消息模板) ====================
CREATE TABLE IF NOT EXISTS sys_message_template (
    id               BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键(自增)',
    deleted          TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常,1=已删)',
    create_time      DATETIME        NULL     COMMENT '创建时间',
    create_by        BIGINT          NULL     COMMENT '创建人ID',
    update_time      DATETIME        NULL     COMMENT '更新时间',
    update_by        BIGINT          NULL     COMMENT '更新人ID',
    version          INT             NULL     DEFAULT 0 COMMENT '乐观锁版本号',
    template_code    VARCHAR(128)    NOT NULL COMMENT '模板编码',
    template_name    VARCHAR(256)    NOT NULL COMMENT '模板名称',
    title_template   VARCHAR(512)    NULL     COMMENT '标题模板',
    content_template TEXT            NULL     COMMENT '内容模板',
    msg_type         VARCHAR(32)     NULL     DEFAULT 'INTERNAL' COMMENT '消息类型(INTERNAL/EMAIL/SMS/FEISHU/WEIXIN)',
    sys_module       VARCHAR(32)     NULL     COMMENT '所属模块(HCP/DMS/TMS/QMS)',
    enabled          TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '启用状态(1=启用,0=禁用)',
    description      VARCHAR(512)    NULL     COMMENT '描述',
    PRIMARY KEY (id),
    UNIQUE KEY uk_template_code (template_code),
    KEY idx_module (sys_module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息模板';

-- ==================== 21. sys_scheduled_task (定时任务定义) ====================
CREATE TABLE IF NOT EXISTS sys_scheduled_task (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键(自增)',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常,1=已删)',
    create_time     DATETIME        NULL     COMMENT '创建时间',
    create_by       BIGINT          NULL     COMMENT '创建人ID',
    update_time     DATETIME        NULL     COMMENT '更新时间',
    update_by       BIGINT          NULL     COMMENT '更新人ID',
    version         INT             NULL     DEFAULT 0 COMMENT '乐观锁版本号',
    task_code       VARCHAR(128)    NOT NULL COMMENT '任务编码',
    task_name       VARCHAR(256)    NOT NULL COMMENT '任务名称',
    cron_expression VARCHAR(64)     NOT NULL COMMENT 'Cron表达式(如0 0 8 * * ?)',
    bean_name       VARCHAR(256)    NULL     COMMENT 'Spring Bean名称(动态调用)',
    method_name     VARCHAR(128)    NULL     COMMENT '方法名称',
    sys_module      VARCHAR(32)     NULL     COMMENT '所属模块(HCP/DMS/TMS)',
    description     VARCHAR(512)    NULL     COMMENT '任务描述',
    status          TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '状态(1=启用,0=禁用)',
    last_exec_time  DATETIME        NULL     COMMENT '最后执行时间',
    next_exec_time  DATETIME        NULL     COMMENT '下次执行时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_task_code (task_code),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务定义';

-- ==================== 22. sys_scheduled_task_log (定时任务执行日志) ====================
CREATE TABLE IF NOT EXISTS sys_scheduled_task_log (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键(自增)',
    task_id         BIGINT          NOT NULL COMMENT '任务ID',
    task_code       VARCHAR(128)    NULL     COMMENT '任务编码(冗余)',
    start_time      DATETIME        NOT NULL COMMENT '开始时间',
    end_time        DATETIME        NULL     COMMENT '结束时间',
    cost_ms         BIGINT          NULL     COMMENT '耗时(毫秒)',
    result          VARCHAR(16)     NOT NULL COMMENT '执行结果(SUCCESS/FAIL)',
    error_msg       TEXT            NULL     COMMENT '错误信息',
    create_time     DATETIME        NULL     COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_task_id (task_id),
    KEY idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务执行日志';

CREATE TABLE IF NOT EXISTS sys_signature (
    id                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键(自增)',
    deleted              TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常,1=已删)',
    create_time          DATETIME     NULL     COMMENT '创建时间',
    create_by            BIGINT       NULL     COMMENT '创建人ID',
    update_time          DATETIME     NULL     COMMENT '修改时间',
    update_by            BIGINT       NULL     COMMENT '修改人ID',
    version              INT          NOT NULL DEFAULT 0 COMMENT '乐观锁',
    process_instance_id  VARCHAR(128) NULL     COMMENT '流程实例ID',
    task_id              VARCHAR(128) NOT NULL COMMENT '任务ID',
    user_id              BIGINT       NOT NULL COMMENT '签名人ID',
    user_name            VARCHAR(64)  NULL     COMMENT '签名人姓名',
    signature_data       MEDIUMTEXT   NOT NULL COMMENT '签名数据(Base64 PNG)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_task_id (task_id),
    KEY idx_process_instance (process_instance_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子签名';

CREATE TABLE IF NOT EXISTS tms_assignment (
    id           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键(自增)',
    deleted      TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=正常,1=已删)',
    create_time  DATETIME    NULL     COMMENT '创建时间',
    create_by    BIGINT      NULL     COMMENT '创建人ID',
    update_time  DATETIME    NULL     COMMENT '修改时间',
    update_by    BIGINT      NULL     COMMENT '修改人ID',
    version      INT         NOT NULL DEFAULT 0 COMMENT '乐观锁',
    course_id    BIGINT      NOT NULL COMMENT '课程ID',
    user_id      BIGINT      NOT NULL COMMENT '学员ID',
    user_name    VARCHAR(64) NULL     COMMENT '学员姓名',
    status       VARCHAR(20) NOT NULL DEFAULT 'ASSIGNED' COMMENT '状态(ASSIGNED/IN_PROGRESS/COMPLETED)',
    score        INT         NULL     COMMENT '培训分数(0-100)',
    completed_at DATETIME    NULL     COMMENT '完成时间',
    expiry_date  DATETIME    NULL     COMMENT '有效期截止',
    remark       VARCHAR(500) NULL    COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_course_id (course_id),
    KEY idx_user_id (user_id),
    UNIQUE KEY uk_course_user (course_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='培训任务分配';

-- ================================================================
-- Seed Data (初始数据)
-- ================================================================

-- 默认管理员
INSERT INTO sys_user (id, deleted, create_time, create_by, update_time, update_by, version,
    user_id, password, user_name, user_type, status)
VALUES (1, 0, NOW(), 1, NOW(), 1, 0,
    'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'SYSTEM', 'NORM');

-- 默认角色
INSERT INTO sys_role (id, deleted, create_time, create_by, update_time, update_by, version,
    role_code, role_name, description, role_level, status, is_system)
VALUES (1, 0, NOW(), 1, NOW(), 1, 0,
    'ROLE_ADMIN', '系统管理员', '拥有所有权限', 1, 1, 1),
       (2, 0, NOW(), 1, NOW(), 1, 0,
    'ROLE_USER', '普通用户', '基础用户权限', 0, 1, 1);

-- 默认菜单(目录+页面+按钮)
INSERT INTO sys_menu (id, deleted, create_time, create_by, update_time, update_by, version,
    parent_id, menu_name, menu_type, path, permission, icon, sort_order, visible, is_external)
VALUES
    -- ===== 一级目录 =====
    (1,  0, NOW(), 1, NOW(), 1, 0, 0, '系统管理', 0, '/system', NULL, 'Setting',     1, 1, 0),
    (2,  0, NOW(), 1, NOW(), 1, 0, 0, '质量管理', 0, '/qms',   NULL, 'Monitor',     2, 1, 0),
    (3,  0, NOW(), 1, NOW(), 1, 0, 0, '文件管理', 0, '/dms',   NULL, 'Document',    3, 1, 0),
    (4,  0, NOW(), 1, NOW(), 1, 0, 0, '培训管理', 0, '/tms',   NULL, 'School',      4, 1, 0),
    (5,  0, NOW(), 1, NOW(), 1, 0, 0, '质量回顾', 0, '/qrs',   NULL, 'TrendCharts', 5, 1, 0),

    -- ===== 系统管理 > 用户管理 =====
    (11, 0, NOW(), 1, NOW(), 1, 0, 1, '用户管理', 1, '/system/user',  'system:user:list',   'User',          1, 1, 0),
    (111,0, NOW(), 1, NOW(), 1, 0, 11,'用户新增', 2, NULL,           'system:user:add',    NULL,            1, 1, 0),
    (112,0, NOW(), 1, NOW(), 1, 0, 11,'用户编辑', 2, NULL,           'system:user:edit',   NULL,            2, 1, 0),
    (113,0, NOW(), 1, NOW(), 1, 0, 11,'用户删除', 2, NULL,           'system:user:delete', NULL,            3, 1, 0),
    (114,0, NOW(), 1, NOW(), 1, 0, 11,'重置密码', 2, NULL,           'system:user:resetPwd',NULL,           4, 1, 0),

    -- ===== 系统管理 > 角色管理 =====
    (12, 0, NOW(), 1, NOW(), 1, 0, 1, '角色管理', 1, '/system/role', 'system:role:list',   'Avatar',        2, 1, 0),
    (121,0, NOW(), 1, NOW(), 1, 0, 12,'角色新增', 2, NULL,           'system:role:add',    NULL,            1, 1, 0),
    (122,0, NOW(), 1, NOW(), 1, 0, 12,'角色编辑', 2, NULL,           'system:role:edit',   NULL,            2, 1, 0),
    (123,0, NOW(), 1, NOW(), 1, 0, 12,'角色删除', 2, NULL,           'system:role:delete', NULL,            3, 1, 0),
    (124,0, NOW(), 1, NOW(), 1, 0, 12,'菜单权限', 2, NULL,           'system:role:menu',   NULL,            4, 1, 0),

    -- ===== 系统管理 > 菜单管理 =====
    (13, 0, NOW(), 1, NOW(), 1, 0, 1, '菜单管理', 1, '/system/menu', 'system:menu:list',   'Menu',          3, 1, 0),
    (131,0, NOW(), 1, NOW(), 1, 0, 13,'菜单新增', 2, NULL,           'system:menu:add',    NULL,            1, 1, 0),
    (132,0, NOW(), 1, NOW(), 1, 0, 13,'菜单编辑', 2, NULL,           'system:menu:edit',   NULL,            2, 1, 0),
    (133,0, NOW(), 1, NOW(), 1, 0, 13,'菜单删除', 2, NULL,           'system:menu:delete', NULL,            3, 1, 0),

    -- ===== 系统管理 > 部门管理 =====
    (14, 0, NOW(), 1, NOW(), 1, 0, 1, '部门管理', 1, '/system/dept', 'system:dept:list',   'OfficeBuilding',4, 1, 0),
    (141,0, NOW(), 1, NOW(), 1, 0, 14,'部门新增', 2, NULL,           'system:dept:add',    NULL,            1, 1, 0),
    (142,0, NOW(), 1, NOW(), 1, 0, 14,'部门编辑', 2, NULL,           'system:dept:edit',   NULL,            2, 1, 0),
    (143,0, NOW(), 1, NOW(), 1, 0, 14,'部门删除', 2, NULL,           'system:dept:delete', NULL,            3, 1, 0),

    -- ===== 系统管理 > 字典管理 =====
    (15, 0, NOW(), 1, NOW(), 1, 0, 1, '字典管理', 1, '/system/dict', 'system:dict:list',  'Collection',    5, 1, 0),
    (151,0, NOW(), 1, NOW(), 1, 0, 15,'字典新增', 2, NULL,           'system:dict:add',    NULL,            1, 1, 0),
    (152,0, NOW(), 1, NOW(), 1, 0, 15,'字典编辑', 2, NULL,           'system:dict:edit',   NULL,            2, 1, 0),
    (153,0, NOW(), 1, NOW(), 1, 0, 15,'字典删除', 2, NULL,           'system:dict:delete', NULL,            3, 1, 0),

    -- ===== 系统管理 > 操作日志 =====
    (16, 0, NOW(), 1, NOW(), 1, 0, 1, '操作日志', 1, '/system/log', 'system:log:list',   'DocumentChecked',6, 1, 0),

    -- ===== 系统管理 > 系统设置 =====
    (17, 0, NOW(), 1, NOW(), 1, 0, 1, '系统设置', 1, '/system/settings', 'system:config:list', 'Timer', 7, 1, 0),

    -- ===== 质量管理子菜单 =====
    (21, 0, NOW(), 1, NOW(), 1, 0, 2, '偏差管理', 1, '/qms/deviation', 'qms:deviation:list', 'Warning',    1, 1, 0),
    (22, 0, NOW(), 1, NOW(), 1, 0, 2, 'CAPA管理', 1, '/qms/capa',     'qms:capa:list',      'CircleCheck', 2, 1, 0),
    (23, 0, NOW(), 1, NOW(), 1, 0, 2, '变更控制', 1, '/qms/change',   'qms:change:list',    'Switch',      3, 1, 0),
    (24, 0, NOW(), 1, NOW(), 1, 0, 2, '审计管理', 1, '/qms/audit',   'qms:audit:list',     'Search',      4, 1, 0),

    -- ===== 文件管理子菜单 =====
    (31, 0, NOW(), 1, NOW(), 1, 0, 3, '文件列表', 1, '/dms/document', 'dms:document:list',  'Folder',      1, 1, 0),

    -- ===== 培训管理子菜单 =====
    (41, 0, NOW(), 1, NOW(), 1, 0, 4, '培训课程', 1, '/tms/course',   'tms:course:list',    'School',      1, 1, 0),

    -- ===== 质量回顾子菜单 =====
    (51, 0, NOW(), 1, NOW(), 1, 0, 5, 'APQR报告', 1, '/qrs/apqr',   'qrs:apqr:list',      'DataAnalysis', 1, 1, 0);

-- 默认角色-菜单(系统管理员拥有全部权限)
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT id + 900, 1, id FROM sys_menu ON DUPLICATE KEY UPDATE menu_id = menu_id;

-- 默认用户-角色(管理员=角色1)
INSERT INTO sys_user_role (id, user_id, role_id) VALUES (1, 1, 1);

-- 默认部门
INSERT INTO sys_dept (id, deleted, create_time, create_by, update_time, update_by, version,
    parent_id, ancestors, dept_code, dept_name, sort_order, leader, phone, email, status)
VALUES
    (1, 0, NOW(), 1, NOW(), 1, 0, 0, '0',    'ROOT',   '总公司',   0, 'Admin', '13800000000', 'admin@gmp.com', 1),
    (2, 0, NOW(), 1, NOW(), 1, 0, 1, '0,1',  'DEV',    '研发部',   1, NULL, NULL, NULL, 1),
    (3, 0, NOW(), 1, NOW(), 1, 0, 1, '0,1',  'QA',     '质量部',   2, NULL, NULL, NULL, 1),
    (4, 0, NOW(), 1, NOW(), 1, 0, 2, '0,1,2','DEV-FE', '前端组',   1, NULL, NULL, NULL, 1),
    (5, 0, NOW(), 1, NOW(), 1, 0, 2, '0,1,2','DEV-BE', '后端组',   2, NULL, NULL, NULL, 1);
