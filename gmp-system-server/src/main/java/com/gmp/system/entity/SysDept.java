package com.gmp.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.framework.base.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class SysDept extends CommonEntity {

    private Long parentId;
    private String ancestors;
    private String deptCode;
    private String deptName;
    private Integer sortOrder;
    private String leader;
    private String phone;
    private String email;
    private Integer status;
}
