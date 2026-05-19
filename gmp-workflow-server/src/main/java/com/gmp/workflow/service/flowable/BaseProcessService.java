package com.gmp.workflow.service.flowable;

import com.gmp.workflow.constant.FlowConstant;
import com.gmp.workflow.enums.flowable.runtime.CommentTypeEnum;
import com.gmp.workflow.enums.flowable.runtime.ProcessStatusEnum;
import com.gmp.workflow.exception.FlowException;
import com.gmp.workflow.model.flowable.CommentInfo;
import com.gmp.workflow.model.flowable.ExtendHisprocinst;
import com.gmp.workflow.vo.flowable.BaseProcessVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Date;

/**
 * 抽象的流程服务基类
 */
public abstract class BaseProcessService {

    protected final ICommentInfoService commentInfoService;
    protected final IExtendHisprocinstService extendHisprocinstService;
    protected final CacheManager cacheManager;

    protected BaseProcessService(ICommentInfoService commentInfoService,
                                  IExtendHisprocinstService extendHisprocinstService,
                                  CacheManager cacheManager) {
        this.commentInfoService = commentInfoService;
        this.extendHisprocinstService = extendHisprocinstService;
        this.cacheManager = cacheManager;
    }

    protected void addFlowCommentInfo(CommentInfo commentInfo) {
        commentInfoService.saveComment(commentInfo);
    }

    protected void addFlowCommentInfoAndProcessStatus(BaseProcessVo baseProcessVo) {
        ProcessStatusEnum processStatusEnum = baseProcessVo.getProcessStatusEnum();
        if (processStatusEnum != null) {
            Cache cache = cacheManager.getCache(FlowConstant.CACHE_PROCESS_STATUS);
            if (cache != null) {
                cache.put(baseProcessVo.getProcessInstanceId(), processStatusEnum.toString());
            }
        }
        CommentInfo commentInfo = new CommentInfo(
                baseProcessVo.getCommentTypeEnum().name(),
                baseProcessVo.getUserCode(),
                baseProcessVo.getProcessInstanceId(),
                baseProcessVo.getMessage());
        commentInfo.setTaskId(baseProcessVo.getTaskId());
        commentInfo.setActivityId(baseProcessVo.getActivityId());
        commentInfo.setActivityName(baseProcessVo.getActivityName());
        commentInfo.setCreator(baseProcessVo.getUserCode());
        commentInfo.setUpdator(baseProcessVo.getUserCode());
        commentInfo.setCreateTime(new Date());
        commentInfo.setUpdateTime(new Date());
        commentInfoService.saveComment(commentInfo);

        if (baseProcessVo.getCommentTypeEnum() != null
                && !baseProcessVo.getCommentTypeEnum().equals(CommentTypeEnum.YY)) {
            if (StringUtils.isBlank(baseProcessVo.getProcessInstanceId())) {
                throw new FlowException("请传入流程实例id");
            }
            ExtendHisprocinst extendHisprocinst = new ExtendHisprocinst(
                    baseProcessVo.getProcessInstanceId(),
                    baseProcessVo.getProcessStatusEnum().toString());
            extendHisprocinstService.updateAllStatusByProcessInstanceId(extendHisprocinst);
        }
    }

    protected void evictHighLightedNodeCache(String processInstanceId) {
        Cache cache = cacheManager.getCache(FlowConstant.CACHE_PROCESS_HIGHLIGHTEDNODES);
        if (cache != null) cache.evict(processInstanceId);
    }

    protected void evictOneActivityVoCache(String processInstanceId, String activityId) {
        Cache cache = cacheManager.getCache(FlowConstant.CACHE_PROCESS_ACTIVITYS);
        if (cache != null) cache.evict(processInstanceId + "-" + activityId);
    }
}
