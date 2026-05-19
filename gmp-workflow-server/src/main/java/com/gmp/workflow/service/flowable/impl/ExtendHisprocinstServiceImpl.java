package com.gmp.workflow.service.flowable.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gmp.workflow.enums.flowable.runtime.ProcessStatusEnum;
import com.gmp.workflow.mapper.ExtendHisprocinstMapper;
import com.gmp.workflow.model.flowable.ExtendHisprocinst;
import com.gmp.workflow.model.flowable.ExtendProcinst;
import com.gmp.workflow.service.flowable.IExtendHisprocinstService;
import com.gmp.workflow.service.flowable.IExtendProcinstService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExtendHisprocinstServiceImpl
        extends ServiceImpl<ExtendHisprocinstMapper, ExtendHisprocinst>
        implements IExtendHisprocinstService {

    @Lazy
    private final IExtendProcinstService extendProcinstService;
    private final HistoryService historyService;
    private final ThreadPoolTaskExecutor executor;

    @Override
    public ExtendHisprocinst findExtendHisprocinstByProcessInstanceId(String processInstanceId) {
        LambdaQueryWrapper<ExtendHisprocinst> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExtendHisprocinst::getProcessInstanceId, processInstanceId);
        return this.getOne(wrapper);
    }

    @Override
    public void updateAllStatusByProcessInstanceId(ExtendHisprocinst extendHisprocinst) {
        ExtendHisprocinst hisprocinst = findExtendHisprocinstByProcessInstanceId(
                extendHisprocinst.getProcessInstanceId());
        poolExecuteUpdateStatusByProcessInstanceId(
                hisprocinst.getProcessStatus(),
                extendHisprocinst.getProcessStatus(),
                extendHisprocinst.getProcessInstanceId());

        HistoricProcessInstance historicProcessInstance = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(extendHisprocinst.getProcessInstanceId()).singleResult();
        if (historicProcessInstance != null) {
            String rootProcessInstanceId = historicProcessInstance.getSuperProcessInstanceId();
            if (StringUtils.isNotBlank(rootProcessInstanceId)
                    && !rootProcessInstanceId.equals(extendHisprocinst.getProcessInstanceId())) {
                hisprocinst = this.findExtendHisprocinstByProcessInstanceId(rootProcessInstanceId);
                poolExecuteUpdateStatusByProcessInstanceId(
                        hisprocinst.getProcessStatus(),
                        extendHisprocinst.getProcessStatus(),
                        rootProcessInstanceId);
            }
        }
    }

    private void updateStatusByProcessInstanceId(String oldStatus, String status,
                                                  String processInstanceId) {
        if (!oldStatus.equals(ProcessStatusEnum.BJ.toString())
                && !oldStatus.equals(ProcessStatusEnum.ZZ.toString())) {
            LambdaUpdateWrapper<ExtendHisprocinst> wrapper = new LambdaUpdateWrapper<>();
            wrapper.set(ExtendHisprocinst::getProcessStatus, status)
                    .eq(ExtendHisprocinst::getProcessInstanceId, processInstanceId);
            this.update(wrapper);

            LambdaUpdateWrapper<ExtendProcinst> procWrapper = new LambdaUpdateWrapper<>();
            procWrapper.set(ExtendProcinst::getProcessStatus, status)
                    .eq(ExtendProcinst::getProcessInstanceId, processInstanceId);
            extendProcinstService.update(procWrapper);
        }
    }

    private void poolExecuteUpdateStatusByProcessInstanceId(String oldStatus, String status,
                                                             String processInstanceId) {
        executor.execute(() -> updateStatusByProcessInstanceId(oldStatus, status, processInstanceId));
    }
}
