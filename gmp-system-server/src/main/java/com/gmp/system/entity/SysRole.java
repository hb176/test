package com.gmp.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.framework.base.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends CommonEntity {

    private String roleCode;
    private String roleName;
    private String description;
    private Integer roleLevel;
    private Integer status;
    private Boolean isSystem;
}
