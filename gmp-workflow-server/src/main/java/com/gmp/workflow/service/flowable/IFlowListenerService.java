package com.gmp.workflow.service.flowable;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gmp.workflow.model.flowable.FlowListener;
import com.gmp.workflow.tools.pager.PagerModel;
import com.gmp.workflow.tools.pager.Query;

import java.util.List;

/**
 * 流程监听器服务接口
 * <p>
 * 管理 BPMN 流程中注册的执行监听器和任务监听器，
 * 支持 CRUD 操作和按类型/名称查询。
 * </p>
 */
public interface IFlowListenerService extends IService<FlowListener> {

    /**
     * 查询监听器列表并填充参数
     *
     * @param flowListener 查询条件
     * @return 包含参数的监听器列表
     */
    List<FlowListener> getListAndParams(FlowListener flowListener);

    /**
     * 查询监听器列表（不含参数）
     *
     * @param flowListener 查询条件
     * @return 监听器列表
     */
    List<FlowListener> getList(FlowListener flowListener);

    /**
     * 分页查询监听器
     *
     * @param flowListener 查询条件
     * @param query         分页参数
     * @return 分页结果
     */
    PagerModel<FlowListener> getPagerModel(FlowListener flowListener, Query query);

    /**
     * 删除监听器及其关联参数
     *
     * @param id 监听器 ID
     */
    void deleteById(String id);

    /**
     * 根据类型和名称查询监听器
     *
     * @param type 监听器类型
     * @param name 监听器名称
     * @return 监听器对象，不存在返回 null
     */
    FlowListener getFlowListenerByNameAndType(String type, String name);
}
