package com.gmp.system.service;

import com.gmp.framework.base.CommonService;
import com.gmp.system.entity.SysSignature;
import com.gmp.system.mapper.SysSignatureMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SysSignatureService extends CommonService<SysSignatureMapper, SysSignature> {

    public SysSignature getByTaskId(String taskId) {
        return lambdaQuery().eq(SysSignature::getTaskId, taskId).one();
    }
}
