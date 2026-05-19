package com.gmp.business.controller;

import com.gmp.business.dto.BusinessRecordVO;
import com.gmp.business.dto.CreateBusinessRequest;
import com.gmp.business.entity.BusinessRecord;
import com.gmp.business.service.BusinessRecordService;
import com.gmp.business.service.impl.BusinessFacadeServiceImpl;
import com.gmp.framework.base.CommonController;
import com.gmp.framework.base.Result;
import com.gmp.common.constant.BusinessType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * QMS 质量过程管理控制器
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms")
public class QmsController extends CommonController<BusinessRecordService, BusinessRecord> {

    private final BusinessFacadeServiceImpl businessFacade;
    private final BusinessRecordService businessRecordService;

    @Override
    protected BusinessRecordService getService() {
        return businessRecordService;
    }

    // ==================== 偏差管理 ====================

    @PostMapping("/deviation")
    public Result<BusinessRecordVO> createDeviation(@Valid @RequestBody CreateBusinessRequest request) {
        request.setBusinessType(BusinessType.QMS_DEVIATION.getCode());
        return Result.ok("偏差报告已提交，流程已启动", businessFacade.createBusinessRecord(request));
    }

    @GetMapping("/deviation/page")
    public Result<?> queryDeviations(@RequestParam(defaultValue = "1") int pageNum,
                                     @RequestParam(defaultValue = "10") int pageSize,
                                     @RequestParam(required = false) String status,
                                     @RequestParam(required = false) String keyword) {
        return Result.ok(businessRecordService.pageByBusinessType(
                BusinessType.QMS_DEVIATION.getCode(), pageNum, pageSize));
    }

    @GetMapping("/deviation/{id}")
    public Result<BusinessRecordVO> getDeviation(@PathVariable Long id) {
        return Result.ok(BusinessRecordVO.fromEntity(businessRecordService.getById(id)));
    }

    // ==================== CAPA管理 ====================

    @PostMapping("/capa")
    public Result<BusinessRecordVO> createCapa(@Valid @RequestBody CreateBusinessRequest request) {
        request.setBusinessType(BusinessType.QMS_CAPA.getCode());
        return Result.ok("CAPA已创建，流程已启动", businessFacade.createBusinessRecord(request));
    }

    @GetMapping("/capa/page")
    public Result<?> queryCapas(@RequestParam(defaultValue = "1") int pageNum,
                                @RequestParam(defaultValue = "10") int pageSize,
                                @RequestParam(required = false) String status) {
        return Result.ok(businessRecordService.pageByBusinessType(
                BusinessType.QMS_CAPA.getCode(), pageNum, pageSize));
    }

    @GetMapping("/capa/{id}")
    public Result<BusinessRecordVO> getCapa(@PathVariable Long id) {
        return Result.ok(BusinessRecordVO.fromEntity(businessRecordService.getById(id)));
    }

    @PostMapping("/capa/{id}/effectiveness")
    public Result<Void> evaluateEffectiveness(@PathVariable Long id, @RequestBody CreateBusinessRequest data) {
        log.info("CAPA有效性评估 - ID: {}", id);
        return Result.okMsg("评估完成");
    }

    // ==================== 变更控制 ====================

    @PostMapping("/change")
    public Result<BusinessRecordVO> createChange(@Valid @RequestBody CreateBusinessRequest request) {
        request.setBusinessType(BusinessType.QMS_CHANGE.getCode());
        return Result.ok("变更申请已提交", businessFacade.createBusinessRecord(request));
    }

    @GetMapping("/change/page")
    public Result<?> queryChanges(@RequestParam(defaultValue = "1") int pageNum,
                                  @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(businessRecordService.pageByBusinessType(
                BusinessType.QMS_CHANGE.getCode(), pageNum, pageSize));
    }

    // ==================== 供应商管理 ====================

    @PostMapping("/supplier")
    public Result<BusinessRecordVO> createSupplier(@Valid @RequestBody CreateBusinessRequest request) {
        request.setBusinessType(BusinessType.QMS_SUPPLIER.getCode());
        return Result.ok("供应商已添加", businessFacade.createBusinessRecord(request));
    }

    @GetMapping("/supplier/page")
    public Result<?> querySuppliers(@RequestParam(defaultValue = "1") int pageNum,
                                    @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(businessRecordService.pageByBusinessType(
                BusinessType.QMS_SUPPLIER.getCode(), pageNum, pageSize));
    }

    @PostMapping("/supplier/{id}/audit")
    public Result<Void> auditSupplier(@PathVariable Long id, @Valid @RequestBody CreateBusinessRequest data) {
        log.info("供应商审计 - ID: {}", id);
        return Result.okMsg("审计已发起");
    }

    // ==================== 审计管理 ====================

    @PostMapping("/audit")
    public Result<BusinessRecordVO> createAudit(@Valid @RequestBody CreateBusinessRequest request) {
        request.setBusinessType(BusinessType.QMS_AUDIT.getCode());
        return Result.ok("审计计划已创建", businessFacade.createBusinessRecord(request));
    }

    @GetMapping("/audit/page")
    public Result<?> queryAudits(@RequestParam(defaultValue = "1") int pageNum,
                                 @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(businessRecordService.pageByBusinessType(
                BusinessType.QMS_AUDIT.getCode(), pageNum, pageSize));
    }

    @PostMapping("/audit/{id}/finding")
    public Result<Void> addAuditFinding(@PathVariable Long id, @RequestBody CreateBusinessRequest data) {
        log.info("添加审计发现 - 审计ID: {}", id);
        return Result.okMsg("审计发现已记录");
    }

    // ==================== 风险管理 ====================

    @PostMapping("/risk")
    public Result<BusinessRecordVO> createRiskAssessment(@Valid @RequestBody CreateBusinessRequest request) {
        request.setBusinessType(BusinessType.QMS_RISK.getCode());
        return Result.ok("风险评估已创建", businessFacade.createBusinessRecord(request));
    }

    @GetMapping("/risk/page")
    public Result<?> queryRisks(@RequestParam(defaultValue = "1") int pageNum,
                                @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(businessRecordService.pageByBusinessType(
                BusinessType.QMS_RISK.getCode(), pageNum, pageSize));
    }
}
