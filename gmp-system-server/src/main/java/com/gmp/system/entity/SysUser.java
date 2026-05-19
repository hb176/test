package com.gmp.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.framework.base.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends CommonEntity {

    /** 登录账号（全局唯一） */
    private String userId;

    /** 密码（BCrypt + SALT加密） */
    private String password;

    /** 密码盐值 */
    private String salt;

    /** 工号 */
    private String jobNum;

    /** 用户名/显示名称 */
    private String userName;

    /** 域账号 */
    private String domainId;

    /** 用户类型（INTERNAL=内部, EXTERNAL=外部, SYSTEM=系统） */
    private String userType;

    /** 部门编码 */
    private String departmentCode;

    /** 部门ID */
    private Long deptId;

    /** 部门名称 */
    private String deptName;

    /** 多部门ID（逗号分隔，用于跨部门权限） */
    private String deptIds;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String mail;

    /** 微信号 */
    private String wxAccount;

    /** 钉钉账号 */
    private String dingDing;

    /** API账号 */
    private String apiAccount;

    /** 密码错误次数 */
    private Long passwordErrorNum;

    /** 密码过期时间 */
    private LocalDate passwordExpireTime;

    /** 子公司ID */
    private Long subsidiariesId;

    /** 子公司编码 */
    private String subsidiariesCode;

    /** 子公司名称 */
    private String subsidiariesName;

    /** 公司ID */
    private Long companyId;

    /** 公司编码 */
    private String companyCode;

    /** 公司名称 */
    private String companyName;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 头像 */
    private String headPortrait;

    /** 是否被封锁（YES/NO） */
    private String hasBloc;

    /** 状态（NORM=正常, DISABLED=禁用, LOCKED=锁定） */
    private String status;

    /** 锁定时间 */
    private LocalDateTime lockTime;
}
