package com.gmp.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.framework.base.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_message_template")
public class SysMessageTemplate extends CommonEntity {
    private String templateCode;
    private String templateName;
    private String titleTemplate;
    private String contentTemplate;
    private String msgType;
    private String sysModule;
    private Integer enabled;
    private String description;
}
