package com.gmp.workflow.vo.flowable.task;

import com.gmp.workflow.vo.extension.usertask.NextSequenceUserVo;
import com.gmp.workflow.vo.flowable.BaseProcessVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "审批参数")
public class CompleteTaskVo extends BaseProcessVo implements Serializable {
    private static final long serialVersionUID = -5583096980348786337L;
    @Schema(description = "流程标题")
    private String formTitle;
    @Schema(description = "任务参数")
    private Map<String, Object> variables;
    private NextSequenceUserVo nextSequenceFlow;
    private List<NextSequenceUserVo> nextUsers;
}
