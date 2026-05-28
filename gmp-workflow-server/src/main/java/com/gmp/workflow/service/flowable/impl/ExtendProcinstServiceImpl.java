package com.gmp.workflow.service.flowable.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gmp.workflow.enums.flowable.runtime.ProcessStatusEnum;
import com.gmp.workflow.mapper.ExtendProcinstMapper;
import com.gmp.workflow.model.flowable.ExtendHisprocinst;
import com.gmp.workflow.model.flowable.ExtendProcinst;
import com.gmp.workflow.service.flowable.IExtendHisprocinstService;
import com.gmp.workflow.service.flowable.IExtendProcinstService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 流程实例运行时扩展信息服务实现
 */
@Service
public class ExtendProcinstServiceImpl extends ServiceImpl<ExtendProcinstMapper, ExtendProcinst>
        implements IExtendProcinstService {

    private final IExtendHisprocinstService extendHisprocinstService;

    public ExtendProcinstServiceImpl(@Lazy IExtendHisprocinstService extendHisprocinstService) {
        this.extendHisprocinstService = extendHisprocinstService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteExtendProcinstByProcessInstanceId(String processInstanceId) {
        LambdaQueryWrapper<ExtendProcinst> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExtendProcinst::getProcessInstanceId, processInstanceId);
        this.remove(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveExtendProcinstAndHis(ExtendProcinst extendProcinst) {
        if (StringUtils.isBlank(extendProcinst.getId())) {
            extendProcinst.setId(IdWorker.get32UUID());
        }
        extendProcinst.setCreateTime(new Date());
        extendProcinst.setUpdateTime(new Date());
        this.saveOrUpdate(extendProcinst);

        // 同步写入历史扩展表
        ExtendHisprocinst extendHisprocinst = new ExtendHisprocinst();
        BeanUtils.copyProperties(extendProcinst, extendHisprocinst);
        extendHisprocinstService.saveOrUpdate(extendHisprocinst);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(ProcessStatusEnum processStatus, String processInstanceId) {
        // 更新运行时扩展表
        LambdaUpdateWrapper<ExtendProcinst> procinstWrapper = new LambdaUpdateWrapper<>();
        procinstWrapper.set(ExtendProcinst::getProcessStatus, processStatus.toString())
                .eq(ExtendProcinst::getProcessInstanceId, processInstanceId);
        this.update(procinstWrapper);

        // 更新历史扩展表
        LambdaUpdateWrapper<ExtendHisprocinst> hisprocinstWrapper = new LambdaUpdateWrapper<>();
        hisprocinstWrapper.set(ExtendHisprocinst::getProcessStatus, processStatus.toString())
                .eq(ExtendHisprocinst::getProcessInstanceId, processInstanceId);
        extendHisprocinstService.update(hisprocinstWrapper);
    }

    @Override
    public ExtendProcinst findExtendProcinstByProcessInstanceId(String processInstanceId) {
        LambdaQueryWrapper<ExtendProcinst> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExtendProcinst::getProcessInstanceId, processInstanceId);
        return this.getOne(wrapper);
    }
}
