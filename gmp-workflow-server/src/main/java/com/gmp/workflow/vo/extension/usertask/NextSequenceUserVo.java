package com.gmp.workflow.vo.extension.usertask;

import lombok.Data;

import java.io.Serializable;

@Data
public class NextSequenceUserVo implements Serializable {
    public static final String SEQUENCE_KEY = "sequence";
    public static final String USER_KEY = "user";
    private String code;
    private String name;
    private boolean multiple;
    private String values;
}
