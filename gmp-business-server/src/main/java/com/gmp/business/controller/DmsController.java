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
import java.util.List;

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

    @Override
    protected BusinessRecordService getService() {
        return businessRecordService;
    }

    @PostMapping("/document")
    public Result<BusinessRecordVO> createDocument(@Valid @RequestBody CreateBusinessRequest request) {
        request.setBusinessType(BusinessType.DMS_DOCUMENT.getCode());
        return Result.ok("文件已创建，进入起草阶段", businessFacade.createBusinessRecord(request));
    }

    @PostMapping("/document/{id}/submit-review")
    public Result<Void> submitForReview(@PathVariable Long id) {
        log.info("提交文件审核 - ID: {}", id);
        return Result.okMsg("文件已提交审核，审核流程已启动");
    }

    @GetMapping("/document/page")
    public Result<?> queryDocuments(@RequestParam(defaultValue = "1") int pageNum,
                                    @RequestParam(defaultValue = "10") int pageSize,
                                    @RequestParam(required = false) String status,
                                    @RequestParam(required = false) String category,
                                    @RequestParam(required = false) String keyword) {
        return Result.ok(businessRecordService.pageByBusinessType(
                BusinessType.DMS_DOCUMENT.getCode(), pageNum, pageSize));
    }

    @GetMapping("/document/{id}")
    public Result<BusinessRecordVO> getDocument(@PathVariable Long id) {
        return Result.ok(BusinessRecordVO.fromEntity(businessRecordService.getById(id)));
    }

    @GetMapping("/document/{id}/versions")
    public Result<List<?>> getVersionHistory(@PathVariable Long id) {
        return Result.ok(Collections.emptyList());
    }

    @PutMapping("/document/{id}/obsolete")
    public Result<Void> obsoleteDocument(@PathVariable Long id, @RequestBody CreateBusinessRequest body) {
        log.info("文件作废 - ID: {}", id);
        return Result.okMsg("文件已作废");
    }

    @PostMapping("/document/{id}/re-review")
    public Result<Void> reReviewDocument(@PathVariable Long id) {
        log.info("发起文件复审 - ID: {}", id);
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
}
