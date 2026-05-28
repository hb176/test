package com.gmp.workflow.service.flowable.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gmp.workflow.mapper.WfActivityFormFieldMapper;
import com.gmp.workflow.model.flowable.WfActivityFormField;
import com.gmp.workflow.service.flowable.IWfActivityFormFieldService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WfActivityFormFieldServiceImpl extends ServiceImpl<WfActivityFormFieldMapper, WfActivityFormField>
        implements IWfActivityFormFieldService {

    @Override
    public List<WfActivityFormField> getByProcessKeyAndActivityId(String processKey, String activityId) {
        return list(new LambdaQueryWrapper<WfActivityFormField>()
                .eq(WfActivityFormField::getProcessKey, processKey)
                .eq(WfActivityFormField::getActivityId, activityId)
                .eq(WfActivityFormField::getDeleted, 0)
                .orderByAsc(WfActivityFormField::getSortOrder));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveFieldConfigs(String processKey, String activityId, List<WfActivityFormField> fields) {
        // 先删除旧配置
        lambdaUpdate()
                .eq(WfActivityFormField::getProcessKey, processKey)
                .eq(WfActivityFormField::getActivityId, activityId)
                .set(WfActivityFormField::getDeleted, 1)
                .update();

        // 插入新配置
        if (fields != null && !fields.isEmpty()) {
            for (int i = 0; i < fields.size(); i++) {
                WfActivityFormField field = fields.get(i);
                field.setProcessKey(processKey);
                field.setActivityId(activityId);
                field.setSortOrder(i);
                field.setDeleted(0);
            }
            saveBatch(fields);
        }
    }
}
