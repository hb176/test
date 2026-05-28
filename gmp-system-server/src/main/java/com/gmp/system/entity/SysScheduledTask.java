package com.gmp.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.framework.base.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_scheduled_task")
public class SysScheduledTask extends CommonEntity {
    private String taskCode;
    private String taskName;
    private String cronExpression;
    private String beanName;
    private String methodName;
    private String sysModule;
    private String description;
    private Integer status;
    private LocalDateTime lastExecTime;
    private LocalDateTime nextExecTime;
}
