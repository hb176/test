package com.gmp.workflow.service.flowable.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gmp.workflow.mapper.ExtendProcinstMapper;
import com.gmp.workflow.model.flowable.ExtendProcinst;
import com.gmp.workflow.service.flowable.IExtendProcinstService;
import org.springframework.stereotype.Service;

@Service
public class ExtendProcinstServiceImpl extends ServiceImpl<ExtendProcinstMapper, ExtendProcinst>
        implements IExtendProcinstService {
}
