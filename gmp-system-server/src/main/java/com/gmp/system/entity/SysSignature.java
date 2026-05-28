package com.gmp.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.framework.base.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_signature")
public class SysSignature extends CommonEntity {
    private String processInstanceId;
    private String taskId;
    private Long userId;
    private String userName;
    private String signatureData;
}
