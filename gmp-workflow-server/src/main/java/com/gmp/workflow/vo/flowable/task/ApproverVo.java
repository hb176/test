package com.gmp.workflow.vo.flowable.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApproverVo implements Serializable {
    public static final String ROLE = "role";
    public static final String USER = "user";
    private String type;
    private String code;
    private String name;
    private String mobile;
}
