package com.gmp.workflow.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gmp.workflow.vo.flowable.task.TaskQueryParamsVo;
import com.gmp.workflow.vo.flowable.task.TaskVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FlowableTaskMapper {
    IPage<TaskVo> getApplyedTasksPagerModel(IPage<TaskVo> page, @Param("params") TaskQueryParamsVo params);
    IPage<TaskVo> getAppingTasksPagerModel(IPage<TaskVo> page, @Param("params") TaskQueryParamsVo params);
    Long getAppingTaskCont(@Param("params") TaskQueryParamsVo params);
    void updateHisAssignee(@Param("taskId") String taskId, @Param("assignee") String assignee);
}
