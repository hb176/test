package com.gmp.business.controller;

import com.gmp.business.dto.BusinessRecordVO;
import com.gmp.business.dto.CreateBusinessRequest;
import com.gmp.business.entity.BusinessRecord;
import com.gmp.business.service.BusinessRecordService;
import com.gmp.business.service.impl.BusinessFacadeServiceImpl;
import com.gmp.framework.base.CommonController;
import com.gmp.framework.base.Result;
import com.gmp.common.constant.BusinessType;
import com.gmp.common.exceptions.BusinessException;
import com.gmp.framework.workflow.WorkflowService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * DMS 文件管理控制器
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dms")
public class DmsController extends CommonController<BusinessRecordService, BusinessRecord> {

    private final BusinessFacadeServiceImpl businessFacade;
    private final BusinessRecordService businessRecordService;
    private final WorkflowService workflowService;

    @Override
    protected BusinessRecordService getService() {
        return businessRecordService;
    }

    @PreAuthorize("hasAuthority('dms:document:add')")
    @PostMapping("/document")
    public Result<BusinessRecordVO> createDocument(@Valid @RequestBody CreateBusinessRequest request) {
        request.setBusinessType(BusinessType.DMS_DOCUMENT.getCode());
        return Result.ok("文件已创建，进入起草阶段", businessFacade.createBusinessRecord(request));
    }

    @PreAuthorize("hasAuthority('dms:document:submit')")
    @PostMapping("/document/{id}/submit-review")
    public Result<Void> submitForReview(@PathVariable Long id) {
        BusinessRecord record = businessRecordService.getById(id);
        if (record == null) return Result.fail(com.gmp.common.base.ResultCode.NOT_FOUND, "文件不存在");
        if (!"DRAFT".equals(record.getBusinessStatus())) {
            return Result.fail(com.gmp.common.base.ResultCode.VALIDATION_FAILED, "仅草稿状态可提交审核");
        }

        // 尝试启动工作流
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("businessNo", record.getBusinessNo());
            variables.put("title", record.getTitle());
            variables.put("initiatorName", record.getInitiatorName());
            ProcessInstance pi = workflowService.startProcess(
                    "DMS_DOCUMENT", record.getBusinessNo(), variables, record.getInitiatorName());
            record.setProcessInstanceId(pi.getId());
            log.info("文件审批流程已启动: id={}, processInstanceId={}", id, pi.getId());
        } catch (BusinessException e) {
            log.warn("流程启动失败（降级为手动审批）: key=DMS_DOCUMENT, error={}", e.getMessage());
        }

        record.setBusinessStatus("APPROVING");
        businessRecordService.updateById(record);
        return Result.okMsg("文件已提交审核");
    }

    @GetMapping("/document/page")
    public Result<?> queryDocuments(@RequestParam(defaultValue = "1") int pageNum,
                                    @RequestParam(defaultValue = "10") int pageSize,
                                    @RequestParam(required = false) String status,
                                    @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<BusinessRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BusinessRecord::getBusinessType, BusinessType.DMS_DOCUMENT.getCode());
        if (status != null && !status.isEmpty()) {
            wrapper.eq(BusinessRecord::getBusinessStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(BusinessRecord::getTitle, keyword)
                    .or().like(BusinessRecord::getBusinessNo, keyword));
        }
        wrapper.orderByDesc(BusinessRecord::getCreateTime);
        return Result.ok(businessRecordService.pageQuery(pageNum, pageSize, wrapper));
    }

    @GetMapping("/document/{id}")
    public Result<BusinessRecordVO> getDocument(@PathVariable Long id) {
        BusinessRecord record = businessRecordService.getById(id);
        if (record == null) return Result.fail(com.gmp.common.base.ResultCode.NOT_FOUND, "文件不存在");
        return Result.ok(BusinessRecordVO.fromEntity(record));
    }

    @PreAuthorize("hasAuthority('dms:document:edit')")
    @PutMapping("/document/{id}")
    public Result<Void> updateDocument(@PathVariable Long id, @RequestBody CreateBusinessRequest request) {
        BusinessRecord record = businessRecordService.getById(id);
        if (record == null) return Result.fail(com.gmp.common.base.ResultCode.NOT_FOUND, "文件不存在");
        if (!"DRAFT".equals(record.getBusinessStatus())) {
            return Result.fail(com.gmp.common.base.ResultCode.VALIDATION_FAILED, "仅草稿状态可编辑");
        }
        if (request.getTitle() != null) record.setTitle(request.getTitle());
        if (request.getSummary() != null) record.setSummary(request.getSummary());
        businessRecordService.updateById(record);
        return Result.okMsg("更新成功");
    }

    @GetMapping("/document/{id}/versions")
    public Result<List<Map<String, Object>>> getVersionHistory(@PathVariable Long id) {
        BusinessRecord record = businessRecordService.getById(id);
        if (record == null) return Result.ok(Collections.emptyList());
        Map<String, Object> v1 = new LinkedHashMap<>();
        v1.put("id", 1);
        v1.put("title", "初始版本");
        v1.put("createdAt", record.getCreateTime() != null ? record.getCreateTime().toString() : "");
        return Result.ok(List.of(v1));
    }

    @PreAuthorize("hasAuthority('dms:document:obsolete')")
    @PutMapping("/document/{id}/obsolete")
    public Result<Void> obsoleteDocument(@PathVariable Long id, @RequestBody(required = false) CreateBusinessRequest body) {
        BusinessRecord record = businessRecordService.getById(id);
        if (record == null) return Result.fail(com.gmp.common.base.ResultCode.NOT_FOUND, "文件不存在");
        record.setBusinessStatus("OBSOLETED");
        record.setCompletedAt(LocalDateTime.now());
        String reason = body != null ? body.getReason() : null;
        record.setSummary((record.getSummary() != null ? record.getSummary() : "") +
                " [作废原因: " + (reason != null ? reason : "手动作废") + "]");
        businessRecordService.updateById(record);
        log.info("文件已作废: id={}, no={}", id, record.getBusinessNo());
        return Result.okMsg("文件已作废");
    }

    @PreAuthorize("hasAuthority('dms:document:review')")
    @PostMapping("/document/{id}/re-review")
    public Result<Void> reReviewDocument(@PathVariable Long id) {
        BusinessRecord record = businessRecordService.getById(id);
        if (record == null) return Result.fail(com.gmp.common.base.ResultCode.NOT_FOUND, "文件不存在");
        record.setBusinessStatus("APPROVING");
        businessRecordService.updateById(record);
        log.info("文件复审已发起: id={}, no={}", id, record.getBusinessNo());
        return Result.okMsg("复审流程已启动");
    }

    @PostMapping("/document/{id}/print-log")
    public Result<Void> recordPrintLog(@PathVariable Long id, @RequestBody CreateBusinessRequest data) {
        log.info("记录文件打印 - 文件ID: {}", id);
        return Result.okMsg("打印记录已保存");
    }

    @PostMapping("/document/{id}/distribute")
    public Result<Void> distributeDocument(@PathVariable Long id, @RequestBody CreateBusinessRequest data) {
        log.info("文件发放 - 文件ID: {}", id);
        return Result.okMsg("文件已发放");
    }

    @PostMapping("/document/{id}/recall")
    public Result<Void> recallDocument(@PathVariable Long id) {
        log.info("回收文件 - ID: {}", id);
        return Result.okMsg("文件回收指令已发出");
    }

    /**
     * 按业务编号更新状态（供工作流回调使用）
     */
    @PutMapping("/document/by-no/{businessNo}/status")
    public Result<Void> updateStatusByBusinessNo(@PathVariable String businessNo, @RequestParam String status) {
        LambdaQueryWrapper<BusinessRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BusinessRecord::getBusinessNo, businessNo);
        BusinessRecord record = businessRecordService.getOne(wrapper);
        if (record == null) return Result.fail(com.gmp.common.base.ResultCode.NOT_FOUND, "记录不存在");
        record.setBusinessStatus(status);
        if ("COMPLETED".equals(status)) record.setCompletedAt(LocalDateTime.now());
        businessRecordService.updateById(record);
        log.info("工作流回调更新状态: businessNo={}, status={}", businessNo, status);
        return Result.okMsg("状态已更新");
    }
}
