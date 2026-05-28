package com.gmp.workflow.service.flowable;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gmp.workflow.enums.flowable.runtime.ProcessStatusEnum;
import com.gmp.workflow.model.flowable.ExtendProcinst;

/**
 * 流程实例运行时扩展信息服务接口
 * <p>
 * 管理 tbl_flow_extend_procinst 表，记录流程实例的业务扩展信息。
 * 与 IExtendHisprocinstService 配合，运行时数据在流程结束后迁移到历史表。
 * </p>
 */
public interface IExtendProcinstService extends IService<ExtendProcinst> {

    /**
     * 根据流程实例 ID 删除运行时扩展信息
     *
     * @param processInstanceId 流程实例 ID
     */
    void deleteExtendProcinstByProcessInstanceId(String processInstanceId);

    /**
     * 原子保存运行时扩展信息和历史扩展信息
     * <p>
     * 同时写入 tbl_flow_extend_procinst 和 tbl_flow_extend_hisprocinst，
     * 确保两条记录的状态一致。
     * </p>
     *
     * @param extendProcinst 运行时扩展信息
     */
    void saveExtendProcinstAndHis(ExtendProcinst extendProcinst);

    /**
     * 更新运行时和历史扩展信息的状态
     *
     * @param processStatus    目标状态
     * @param processInstanceId 流程实例 ID
     */
    void updateStatus(ProcessStatusEnum processStatus, String processInstanceId);

    /**
     * 根据流程实例 ID 查询运行时扩展信息
     *
     * @param processInstanceId 流程实例 ID
     * @return 运行时扩展信息，不存在时返回 null
     */
    ExtendProcinst findExtendProcinstByProcessInstanceId(String processInstanceId);
}
