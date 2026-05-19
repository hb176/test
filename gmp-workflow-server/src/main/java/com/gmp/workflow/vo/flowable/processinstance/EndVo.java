package com.gmp.workflow.vo.flowable.processinstance;

import com.gmp.workflow.vo.flowable.BaseProcessVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EndVo extends BaseProcessVo {
    private String processInstanceId;
}
