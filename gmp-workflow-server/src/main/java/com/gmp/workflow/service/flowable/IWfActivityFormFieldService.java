package com.gmp.workflow.service.flowable;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gmp.workflow.model.flowable.WfActivityFormField;

import java.util.List;

public interface IWfActivityFormFieldService extends IService<WfActivityFormField> {

    /**
     * 查询指定流程节点的表单字段权限
     */
    List<WfActivityFormField> getByProcessKeyAndActivityId(String processKey, String activityId);

    /**
     * 保存节点的表单字段权限（先删后插）
     */
    void saveFieldConfigs(String processKey, String activityId, List<WfActivityFormField> fields);
}
