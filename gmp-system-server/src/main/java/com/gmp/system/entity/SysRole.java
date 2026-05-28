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

    /** 数据权限范围: ALL=全部, DEPT=本部门, DEPT_AND_CHILDREN=本部门及子部门, SELF=仅本人, CUSTOM=自定义 */
    private String dataScope;

    private Integer status;
    private Boolean isSystem;
}
