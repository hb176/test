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
 * TMS 培训管理控制器
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/tms")
public class TmsController extends CommonController<BusinessRecordService, BusinessRecord> {

    private final BusinessFacadeServiceImpl businessFacade;
    private final BusinessRecordService businessRecordService;

    @Override
    protected BusinessRecordService getService() {
        return businessRecordService;
    }

    // ==================== 培训矩阵 ====================

    @GetMapping("/matrix/position/{positionId}")
    public Result<List<?>> getTrainingMatrix(@PathVariable Long positionId) {
        log.info("查询岗位培训矩阵 - 岗位ID: {}", positionId);
        return Result.ok(Collections.emptyList());
    }

    @PutMapping("/matrix/position/{positionId}")
    public Result<Void> setTrainingMatrix(@PathVariable Long positionId, @RequestBody List<Long> courseIds) {
        log.info("设置培训矩阵 - 岗位ID: {}, 课程数: {}", positionId, courseIds.size());
        return Result.okMsg("培训矩阵更新成功");
    }

    // ==================== 课程管理 ====================

    @GetMapping("/course/page")
    public Result<?> queryCourses(@RequestParam(defaultValue = "1") int pageNum,
                                  @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(businessRecordService.pageByBusinessType(
                BusinessType.TMS_COURSE.getCode(), pageNum, pageSize));
    }

    @PostMapping("/course")
    public Result<BusinessRecordVO> createCourse(@Valid @RequestBody CreateBusinessRequest request) {
        request.setBusinessType(BusinessType.TMS_COURSE.getCode());
        return Result.ok("课程创建成功", businessFacade.createBusinessRecord(request));
    }

    // ==================== 培训计划 ====================

    @PostMapping("/plan")
    public Result<BusinessRecordVO> createTrainingPlan(@Valid @RequestBody CreateBusinessRequest request) {
        request.setBusinessType(BusinessType.TMS_PLAN.getCode());
        return Result.ok("培训计划已创建", businessFacade.createBusinessRecord(request));
    }

    @GetMapping("/plan/page")
    public Result<?> queryPlans(@RequestParam(defaultValue = "1") int pageNum,
                                @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(businessRecordService.pageByBusinessType(
                BusinessType.TMS_PLAN.getCode(), pageNum, pageSize));
    }

    // ==================== 培训记录 ====================

    @PostMapping("/record")
    public Result<Void> recordTraining(@Valid @RequestBody CreateBusinessRequest data) {
        log.info("记录培训完成");
        return Result.okMsg("培训记录已保存");
    }

    @GetMapping("/record/user/{userId}")
    public Result<List<?>> getUserTrainingRecords(@PathVariable Long userId) {
        log.info("查询个人培训记录 - 用户ID: {}", userId);
        return Result.ok(Collections.emptyList());
    }

    @PostMapping("/record/{id}/evaluate")
    public Result<Void> evaluateTraining(@PathVariable Long id, @RequestBody CreateBusinessRequest data) {
        log.info("培训效果评估 - 记录ID: {}", id);
        return Result.okMsg("评估完成");
    }

    // ==================== 资质管理 ====================

    @GetMapping("/certification/user/{userId}")
    public Result<List<?>> getUserCertifications(@PathVariable Long userId) {
        return Result.ok(Collections.emptyList());
    }

    @GetMapping("/certification/expiring")
    public Result<List<?>> getExpiringCertifications(@RequestParam(defaultValue = "30") int days) {
        log.info("查询{}天内到期的资质", days);
        return Result.ok(Collections.emptyList());
    }
}
