package com.gmp.system.service;

import com.gmp.framework.base.CommonService;
import com.gmp.system.entity.SysMessageTemplate;
import com.gmp.system.mapper.SysMessageTemplateMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SysMessageTemplateService extends CommonService<SysMessageTemplateMapper, SysMessageTemplate> {

    /**
     * 根据模板编码查找启用的模板
     */
    public SysMessageTemplate getByCode(String templateCode) {
        return lambdaQuery()
                .eq(SysMessageTemplate::getTemplateCode, templateCode)
                .eq(SysMessageTemplate::getEnabled, 1)
                .one();
    }
}
