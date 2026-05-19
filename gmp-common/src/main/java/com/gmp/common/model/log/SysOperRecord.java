package com.gmp.common.model.log;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@TableName("tbl_sys_oper_record")
@NoArgsConstructor
@AllArgsConstructor
public class SysOperRecord implements Serializable {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String userCode;
    private String userName;
    private String operContent;
    private String operType;
    private String source;
    private Date dateTime;
    private String ip;
    private String date;
    private Integer year;
    private Integer month;
    private Integer day;

    @TableField(exist = false)
    private Date startTime;

    @TableField(exist = false)
    private Date endTime;
}
