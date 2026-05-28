package com.gmp.workflow.service.flowable.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gmp.workflow.mapper.FlowListenerMapper;
import com.gmp.workflow.model.flowable.FlowListener;
import com.gmp.workflow.model.flowable.FlowListenerParam;
import com.gmp.workflow.service.flowable.IFlowListenerParamService;
import com.gmp.workflow.service.flowable.IFlowListenerService;
import com.gmp.workflow.tools.pager.PagerModel;
import com.gmp.workflow.tools.pager.Query;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 流程监听器服务实现
 */
@Service
@RequiredArgsConstructor
public class FlowListenerServiceImpl extends ServiceImpl<FlowListenerMapper, FlowListener>
        implements IFlowListenerService {

    private final IFlowListenerParamService flowListenerParamService;

    /**
     * 查询监听器列表并填充参数
     * <p>
     * 先查询符合条件的监听器列表，再为每个监听器关联查询其参数列表。
     * </p>
     *
     * @param flowListener 查询条件（listenerType、name 等）
     * @return 包含参数的监听器列表
     */
    @Override
    public List<FlowListener> getListAndParams(FlowListener flowListener) {
        List<FlowListener> list = getList(flowListener);
        // 为每个监听器填充参数
        for (FlowListener listener : list) {
            List<FlowListenerParam> params = flowListenerParamService.getListByListenerId(listener.getId());
            listener.setParams(params);
        }
        return list;
    }

    /**
     * 查询监听器列表（不含参数）
     *
     * @param flowListener 查询条件
     * @return 监听器列表，按 orderNo 升序、createTime 降序排列
     */
    @Override
    public List<FlowListener> getList(FlowListener flowListener) {
        LambdaQueryWrapper<FlowListener> wrapper = new LambdaQueryWrapper<>();
        if (flowListener != null) {
            if (StringUtils.isNotBlank(flowListener.getListenerType())) {
                wrapper.eq(FlowListener::getListenerType, flowListener.getListenerType());
            }
            if (StringUtils.isNotBlank(flowListener.getName())) {
                wrapper.like(FlowListener::getName, flowListener.getName());
            }
        }
        wrapper.eq(FlowListener::getDelFlag, 1);
        wrapper.orderByAsc(FlowListener::getOrderNo);
        wrapper.orderByDesc(FlowListener::getCreateTime);
        return this.list(wrapper);
    }

    /**
     * 分页查询监听器
     *
     * @param flowListener 查询条件（支持 keyword 模糊搜索 name/value/remark）
     * @param query         分页参数（pageNum、pageSize）
     * @return 分页结果
     */
    @Override
    public PagerModel<FlowListener> getPagerModel(FlowListener flowListener, Query query) {
        IPage<FlowListener> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<FlowListener> wrapper = new LambdaQueryWrapper<>();
        if (flowListener != null) {
            if (StringUtils.isNotBlank(flowListener.getListenerType())) {
                wrapper.eq(FlowListener::getListenerType, flowListener.getListenerType());
            }
            if (StringUtils.isNotBlank(flowListener.getKeyword())) {
                wrapper.and(w -> w.like(FlowListener::getName, flowListener.getKeyword())
                        .or().like(FlowListener::getValue, flowListener.getKeyword())
                        .or().like(FlowListener::getRemark, flowListener.getKeyword()));
            }
        }
        wrapper.eq(FlowListener::getDelFlag, 1);
        wrapper.orderByDesc(FlowListener::getCreateTime);
        wrapper.orderByAsc(FlowListener::getOrderNo);
        IPage<FlowListener> result = this.page(page, wrapper);
        return new PagerModel<>(result.getTotal(), result.getRecords());
    }

    /**
     * 删除监听器及其关联参数（事务）
     * <p>
     * 先删除关联的参数记录，再删除监听器本身。
     * </p>
     *
     * @param id 监听器 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        // 先删除关联的参数
        LambdaQueryWrapper<FlowListenerParam> paramWrapper = new LambdaQueryWrapper<>();
        paramWrapper.eq(FlowListenerParam::getListenerId, id);
        flowListenerParamService.remove(paramWrapper);
        // 再删除监听器本身
        this.removeById(id);
    }

    /**
     * 根据类型和名称精确查找监听器
     *
     * @param type 监听器类型（execution/task）
     * @param name 监听器名称
     * @return 监听器实体，不存在返回 null
     */
    @Override
    public FlowListener getFlowListenerByNameAndType(String type, String name) {
        LambdaQueryWrapper<FlowListener> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlowListener::getListenerType, type);
        wrapper.eq(FlowListener::getName, name);
        wrapper.eq(FlowListener::getDelFlag, 1);
        return this.getOne(wrapper);
    }
}
