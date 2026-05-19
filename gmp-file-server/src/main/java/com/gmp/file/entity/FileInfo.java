package com.gmp.file.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.framework.base.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 文件信息实体 - 对标ct1.dms_file_info (DMS文件全生命周期管理)
 * 映射表: file_info
 *
 * 文件生命周期（GxP合规）：
 * 起草→审核→审批→生效→复审→作废
 *
 * @author hb176
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file_info")
public class FileInfo extends CommonEntity {

    /** 文件编码（业务编码，如 SOP-QA-001） */
    private String code;

    /** 文件名称 */
    private String name;

    /** 文件别名/标题 */
    private String alias;

    /** 英文名称 */
    private String enName;

    /** 原始文件名（上传时的文件名） */
    private String originalFileName;

    /** PDF路径（转PDF后的存储路径） */
    private String pdfPath;

    /** 真实文件存储路径 */
    private String realFilePath;

    /** 文件操作类型（NEW=新建, REVISE=修订, OBSOLETE=作废） */
    private String fileActionType;

    /** 文件分类（SOP=标准操作规程, RECORD=记录, REPORT=报告, DRAWING=图纸） */
    private String fileCategory;

    /** 文件归属类型（DEPARTMENT=部门, COMPANY=公司, PERSONAL=个人） */
    private String fileBelongType;

    /** 文件类型编码 */
    private String fileTypeCode;

    /** 文件类型 */
    private String fileType;

    /** 文件UUID（MinIO对象标识） */
    private String fileUuid;

    /** 文件版本号（从1递增） */
    private Long fileVersion;

    /** 文件版本号（主版本.次版本格式，如 1.0, 2.1） */
    private String fileVersionMi;

    /** 文件详情/描述 */
    private String detail;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 页数 */
    private Integer pages;

    /** 存储桶名称 */
    private String bucket;

    /** 存储路径 */
    private String storagePath;

    /** 临时路径 */
    private String tempPath;

    /** 系统模块（DMS/QMS/TMS） */
    private String systemModule;

    /** MD5哈希值 */
    private String md5;

    /** MD5十六进制值 */
    private String md5Hex;

    /** 所属文件夹ID */
    private Long folderId;

    /** 上传人ID */
    private String userId;

    /** 上传人名称 */
    private String userName;

    /** 上传人部门编码 */
    private String deptCode;

    /** 上传人部门ID */
    private Long deptId;

    /** 上传人部门名称 */
    private String deptName;

    /** 上传时间 */
    private LocalDateTime uploadTime;

    // ========== 文件状态（DMS生命周期状态） ==========
    /** 生效状态 */
    private String validStatus;

    /** 补发状态 */
    private String reissueStatus;

    /** 复审状态 */
    private String recheckStatus;

    /** 回收删除状态 */
    private String recDelStatus;

    /** 打印状态 */
    private String printStatus;

    /** 访问状态 */
    private String accessStatus;

    /** 培训状态（关联TMS） */
    private String trainStatus;

    /** FTR状态（File Training Record） */
    private String ftrStatus;

    /** 作废状态 */
    private String obsStatus;

    /** 通用状态 */
    private String status;

    /** 回收标记 */
    private Boolean recycle;

    /** 作废时间 */
    private LocalDate obsoleteTime;

    /** 作废人ID */
    private String obsoleteUserId;

    /** 作废人名称 */
    private String obsoleteUserName;

    /** 作废人部门ID */
    private Long obsoleteDeptId;

    /** 作废人部门名称 */
    private String obsoleteDeptName;

    /** 生效日期 */
    private LocalDate validTime;

    /** 截止日期 */
    private LocalDate endTime;

    /** 申请文件日期 */
    private LocalDate applyFileDate;

    /** 接收记录日期 */
    private LocalDate recRecorderDate;

    /** 延期日期 */
    private LocalDate delayDate;

    // ========== 工作流关联 ==========
    /** FAF审批记录编码（File Apply Form - 文件申请流程） */
    private String fafRecorderCode;

    /** FEA审批记录编码（File Effective Approval - 文件生效审批） */
    private String feaRecorderCode;

    /** FTE审批记录编码（File Training Evaluation - 文件培训评估） */
    private String fteRecorderCode;

    /** FPD审批记录编码（File Print Distribution - 文件打印分发） */
    private String fpdRecorderCode;

    /** FRD审批记录编码（File Recall/Destroy - 文件回收销毁） */
    private String frdRecorderCode;

    /** FSA审批记录编码（File Supplement Application - 文件补发申请） */
    private String fsaRecorderCode;

    /** FRX审批记录编码 */
    private String frxRecorderCode;

    /** 源文件编码 */
    private String sourceFileCode;

    /** 源文件ID */
    private Long sourceFileId;

    /** 关联文件 */
    private String relevantFile;

    /** 引用编码（JSON格式，存储引用的外部标准/法规） */
    private String refCode;

    // ========== 组织信息 ==========
    /** 公司ID */
    private Long comId;

    /** 子公司/分部ID */
    private Long subId;

    /** 记录ID（关联业务记录） */
    private String recordId;

    /** 记录类型 */
    private String recordType;

    // ========== 权限与内容 ==========
    /** 授权类型（JSON格式） */
    private String authTypes;

    /** 名称值（用于搜索匹配） */
    private String nameValue;

    /** 内容摘要 */
    private String content;

    /** 版本备注 */
    private String versionRemark;

    /** 草稿文件ID（关联draft表） */
    private Long draftFileId;

    /** 使用回收状态 */
    private String useRecycleStatus;

    /** 使用销毁状态 */
    private String useDestroyStatus;

    /** 合并状态 */
    private String mergeStatus;

    /** 并行下发状态 */
    private String bxdStatus;

    /** 临时文件编码 */
    private String tmpFileCode;
}
