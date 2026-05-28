package com.gmp.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.framework.base.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends CommonEntity {

    private Long parentId;
    private String menuName;
    private Integer menuType;
    private String path;
    private String component;
    private String permission;
    private String icon;
    private Integer sortOrder;
    private Integer visible;
    private Boolean isExternal;
    private String formKey;
}
