package com.gmp.auth.entity;

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
    private Integer status;
}
