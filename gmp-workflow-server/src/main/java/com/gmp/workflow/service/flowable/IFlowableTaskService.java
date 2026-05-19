package com.gmp.workflow.service.flowable;

import com.gmp.workflow.exception.FlowException;
import com.gmp.workflow.vo.flowable.task.CompleteTaskVo;
import com.gmp.workflow.vo.flowable.task.TaskQueryParamsVo;
import com.gmp.workflow.vo.flowable.task.TaskVo;
import com.gmp.workflow.tools.pager.PagerModel;
import com.gmp.workflow.tools.pager.Query;
import com.gmp.workflow.tools.vo.ReturnVo;
import org.apache.ibatis.annotations.Param;

public interface IFlowableTaskService {
    PagerModel<TaskVo> getAppingTasksPagerModel(TaskQueryParamsVo params, Query query);
    PagerModel<TaskVo> getApplyedTasksPagerModel(TaskQueryParamsVo params, Query query);
    ReturnVo<String> complete(CompleteTaskVo completeTaskVo) throws FlowException;
    Long getAppingTaskCont(@Param("params") TaskQueryParamsVo params);
}
