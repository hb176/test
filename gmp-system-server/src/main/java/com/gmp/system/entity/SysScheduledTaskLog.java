package com.gmp.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_scheduled_task_log")
public class SysScheduledTaskLog implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String taskCode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long costMs;
    private String result;
    private String errorMsg;
    private LocalDateTime createTime;
}
