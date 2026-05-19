package com.gmp.business.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gmp.business.entity.BusinessRecord;
import com.gmp.business.mapper.BusinessRecordMapper;
import com.gmp.framework.base.CommonService;
import com.gmp.framework.base.PageResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 业务记录服务 — 基础 CRUD，继承自 CommonService
 *
 * @author hb176
 * @since 1.0.0
 */
@Service
public class BusinessRecordService extends CommonService<BusinessRecordMapper, BusinessRecord> {

    public PageResult<BusinessRecord> pageByBusinessType(String businessType, long pageNum, long pageSize) {
        LambdaQueryWrapper<BusinessRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BusinessRecord::getBusinessType, businessType)
                .orderByDesc(BusinessRecord::getCreateTime);
        return pageQuery(pageNum, pageSize, wrapper);
    }

    public PageResult<BusinessRecord> pageByInitiator(Long initiatorId, long pageNum, long pageSize) {
        LambdaQueryWrapper<BusinessRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BusinessRecord::getInitiatorId, initiatorId)
                .orderByDesc(BusinessRecord::getCreateTime);
        return pageQuery(pageNum, pageSize, wrapper);
    }

    public BusinessRecord getByBusinessNo(String businessNo) {
        LambdaQueryWrapper<BusinessRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BusinessRecord::getBusinessNo, businessNo);
        return getOneByWrapper(wrapper);
    }

    public List<BusinessRecord> listByStatus(String status) {
        LambdaQueryWrapper<BusinessRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BusinessRecord::getBusinessStatus, status);
        return listByWrapper(wrapper);
    }
}
