package com.gmp.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.framework.base.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tms_assignment")
public class TmsAssignment extends CommonEntity {
    private Long courseId;
    private Long userId;
    private String userName;
    /** 状态: ASSIGNED / IN_PROGRESS / COMPLETED */
    private String status;
    /** 培训分数（0-100） */
    private Integer score;
    private LocalDateTime completedAt;
    private LocalDateTime expiryDate;
    private String remark;
}
