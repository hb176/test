package com.gmp.workflow.service.flowable;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gmp.workflow.model.flowable.ExtendHisprocinst;

public interface IExtendHisprocinstService extends IService<ExtendHisprocinst> {
    ExtendHisprocinst findExtendHisprocinstByProcessInstanceId(String processInstanceId);
    void updateAllStatusByProcessInstanceId(ExtendHisprocinst extendHisprocinst);
}
