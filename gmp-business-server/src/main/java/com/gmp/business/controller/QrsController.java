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

import java.util.Collections;

/**
 * QRS 质量回顾系统控制器
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/qrs")
public class QrsController extends CommonController<BusinessRecordService, BusinessRecord> {

    private final BusinessFacadeServiceImpl businessFacade;
    private final BusinessRecordService businessRecordService;

    @Override
    protected BusinessRecordService getService() {
        return businessRecordService;
    }

    // ==================== 产品质量回顾 ====================

    @PostMapping("/apqr")
    public Result<BusinessRecordVO> createApqr(@Valid @RequestBody CreateBusinessRequest request) {
        request.setBusinessType(BusinessType.QRS_APQR.getCode());
        return Result.ok("APQR已创建", businessFacade.createBusinessRecord(request));
    }

    @GetMapping("/apqr/page")
    public Result<?> queryApqrs(@RequestParam(defaultValue = "1") int pageNum,
                                @RequestParam(defaultValue = "10") int pageSize,
                                @RequestParam(required = false) String productName,
                                @RequestParam(required = false) Integer reviewYear) {
        return Result.ok(businessRecordService.pageByBusinessType(
                BusinessType.QRS_APQR.getCode(), pageNum, pageSize));
    }

    @GetMapping("/apqr/{id}")
    public Result<BusinessRecordVO> getApqrDetail(@PathVariable Long id) {
        return Result.ok(BusinessRecordVO.fromEntity(businessRecordService.getById(id)));
    }

    // ==================== 稳定性数据 ====================

    @PostMapping("/stability")
    public Result<Void> addStabilityData(@Valid @RequestBody CreateBusinessRequest data) {
        log.info("添加稳定性数据");
        return Result.okMsg("数据已添加");
    }

    @GetMapping("/stability/{productId}/{batchNo}")
    public Result<?> getStabilityData(@PathVariable Long productId, @PathVariable String batchNo) {
        return Result.ok(Collections.emptyList());
    }

    // ==================== 趋势分析 ====================

    @GetMapping("/trend/{productId}")
    public Result<?> getQualityTrend(@PathVariable Long productId,
                                     @RequestParam(defaultValue = "2026") int year,
                                     @RequestParam(required = false) String metricType) {
        return Result.ok(Collections.emptyMap());
    }

    @GetMapping("/dashboard")
    public Result<?> getDashboard() {
        return Result.ok(Collections.emptyMap());
    }
}
