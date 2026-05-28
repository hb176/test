package com.gmp.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.framework.base.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_message")
public class SysMessage extends CommonEntity {
    private String title;
    private String content;
    private String msgType;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private Integer readFlag;
    private LocalDateTime readTime;
    private String businessType;
    private Long businessId;
}
