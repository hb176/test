package com.gmp.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_oper_log")
public class SysOperLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String operType;
    private String module;
    private String description;
    private String requestUrl;
    private String requestMethod;
    private Long costTime;
    private String result;
    private String errorMsg;
    private String requestParams;
    private Long operUserId;
    private String operUserName;
    private String operIp;
    private LocalDateTime createTime;
}
