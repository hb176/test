package com.gmp.business.controller;

import com.gmp.business.dto.BusinessRecordVO;
import com.gmp.business.dto.CreateBusinessRequest;
import com.gmp.business.entity.BusinessRecord;
import com.gmp.business.service.BusinessRecordService;
import com.gmp.business.service.impl.BusinessFacadeServiceImpl;
import com.gmp.framework.base.CommonController;
import com.gmp.framework.base.Result;
import com.gmp.common.constant.BusinessType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gmp.business.entity.BusinessRecord;
import com.gmp.framework.base.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

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

    @PreAuthorize("hasAuthority('tms:matrix:edit')")
    @PutMapping("/matrix/position/{positionId}")
    public Result<Void> setTrainingMatrix(@PathVariable Long positionId, @RequestBody List<Long> courseIds) {
        log.info("设置培训矩阵 - 岗位ID: {}, 课程数: {}", positionId, courseIds.size());
        return Result.okMsg("培训矩阵更新成功");
    }

    // ==================== 课程管理 ====================

    @GetMapping("/course/page")
    public Result<?> queryCourses(@RequestParam(defaultValue = "1") int pageNum,
                                  @RequestParam(defaultValue = "10") int pageSize,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String status) {
        return Result.ok(queryByType(BusinessType.TMS_COURSE.getCode(), pageNum, pageSize, keyword, status));
    }

    @PreAuthorize("hasAuthority('tms:course:add')")
    @PostMapping("/course")
    public Result<BusinessRecordVO> createCourse(@Valid @RequestBody CreateBusinessRequest request) {
        request.setBusinessType(BusinessType.TMS_COURSE.getCode());
        return Result.ok("课程创建成功", businessFacade.createBusinessRecord(request));
    }

    @GetMapping("/course/{id}")
    public Result<BusinessRecordVO> getCourse(@PathVariable Long id) {
        BusinessRecord r = businessRecordService.getById(id);
        return r != null ? Result.ok(BusinessRecordVO.fromEntity(r))
                : Result.fail(com.gmp.common.base.ResultCode.NOT_FOUND, "课程不存在");
    }

    @PreAuthorize("hasAuthority('tms:course:edit')")
    @PutMapping("/course/{id}")
    public Result<Void> updateCourse(@PathVariable Long id, @RequestBody CreateBusinessRequest request) {
        BusinessRecord r = businessRecordService.getById(id);
        if (r == null) return Result.fail(com.gmp.common.base.ResultCode.NOT_FOUND, "课程不存在");
        if (!"DRAFT".equals(r.getBusinessStatus())) return Result.fail(com.gmp.common.base.ResultCode.VALIDATION_FAILED, "仅草稿状态可编辑");
        if (request.getTitle() != null) r.setTitle(request.getTitle());
        if (request.getSummary() != null) r.setSummary(request.getSummary());
        businessRecordService.updateById(r);
        return Result.okMsg("更新成功");
    }

    @PreAuthorize("hasAuthority('tms:course:delete')")
    @DeleteMapping("/course/{id}")
    public Result<Void> deleteCourse(@PathVariable Long id) {
        BusinessRecord r = businessRecordService.getById(id);
        if (r == null) return Result.fail(com.gmp.common.base.ResultCode.NOT_FOUND, "课程不存在");
        if (!"DRAFT".equals(r.getBusinessStatus())) return Result.fail(com.gmp.common.base.ResultCode.VALIDATION_FAILED, "仅草稿状态可删除");
        businessRecordService.removeById(id);
        return Result.okMsg("删除成功");
    }

    // ==================== 培训计划 ====================

    @PreAuthorize("hasAuthority('tms:plan:add')")
    @PostMapping("/plan")
    public Result<BusinessRecordVO> createTrainingPlan(@Valid @RequestBody CreateBusinessRequest request) {
        request.setBusinessType(BusinessType.TMS_PLAN.getCode());
        return Result.ok("培训计划已创建", businessFacade.createBusinessRecord(request));
    }

    @GetMapping("/plan/page")
    public Result<?> queryPlans(@RequestParam(defaultValue = "1") int pageNum,
                                @RequestParam(defaultValue = "10") int pageSize,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String status) {
        return Result.ok(queryByType(BusinessType.TMS_PLAN.getCode(), pageNum, pageSize, keyword, status));
    }

    @GetMapping("/plan/{id}")
    public Result<BusinessRecordVO> getPlan(@PathVariable Long id) {
        BusinessRecord r = businessRecordService.getById(id);
        return r != null ? Result.ok(BusinessRecordVO.fromEntity(r))
                : Result.fail(com.gmp.common.base.ResultCode.NOT_FOUND, "计划不存在");
    }

    @PreAuthorize("hasAuthority('tms:plan:edit')")
    @PutMapping("/plan/{id}")
    public Result<Void> updatePlan(@PathVariable Long id, @RequestBody CreateBusinessRequest request) {
        BusinessRecord r = businessRecordService.getById(id);
        if (r == null) return Result.fail(com.gmp.common.base.ResultCode.NOT_FOUND, "计划不存在");
        if (!"DRAFT".equals(r.getBusinessStatus())) return Result.fail(com.gmp.common.base.ResultCode.VALIDATION_FAILED, "仅草稿状态可编辑");
        if (request.getTitle() != null) r.setTitle(request.getTitle());
        if (request.getSummary() != null) r.setSummary(request.getSummary());
        businessRecordService.updateById(r);
        return Result.okMsg("更新成功");
    }

    @PreAuthorize("hasAuthority('tms:plan:delete')")
    @DeleteMapping("/plan/{id}")
    public Result<Void> deletePlan(@PathVariable Long id) {
        BusinessRecord r = businessRecordService.getById(id);
        if (r == null) return Result.fail(com.gmp.common.base.ResultCode.NOT_FOUND, "计划不存在");
        if (!"DRAFT".equals(r.getBusinessStatus())) return Result.fail(com.gmp.common.base.ResultCode.VALIDATION_FAILED, "仅草稿状态可删除");
        businessRecordService.removeById(id);
        return Result.okMsg("删除成功");
    }

    // ==================== 培训记录 ====================

    @PostMapping("/record")
    public Result<BusinessRecordVO> recordTraining(@Valid @RequestBody CreateBusinessRequest request) {
        request.setBusinessType(BusinessType.TMS_RECORD.getCode());
        return Result.ok("培训记录已保存", businessFacade.createBusinessRecord(request));
    }

    @GetMapping("/record/page")
    public Result<?> queryRecords(@RequestParam(defaultValue = "1") int pageNum,
                                  @RequestParam(defaultValue = "10") int pageSize,
                                  @RequestParam(required = false) Long userId) {
        LambdaQueryWrapper<BusinessRecord> w = new LambdaQueryWrapper<>();
        w.eq(BusinessRecord::getBusinessType, BusinessType.TMS_RECORD.getCode());
        if (userId != null) w.eq(BusinessRecord::getInitiatorId, userId);
        w.orderByDesc(BusinessRecord::getCreateTime);
        return Result.ok(businessRecordService.pageQuery(pageNum, pageSize, w));
    }

    @GetMapping("/record/user/{userId}")
    public Result<List<BusinessRecordVO>> getUserTrainingRecords(@PathVariable Long userId) {
        LambdaQueryWrapper<BusinessRecord> w = new LambdaQueryWrapper<>();
        w.eq(BusinessRecord::getBusinessType, BusinessType.TMS_RECORD.getCode())
                .eq(BusinessRecord::getInitiatorId, userId);
        return Result.ok(businessRecordService.list(w).stream().map(BusinessRecordVO::fromEntity).toList());
    }

    @PostMapping("/record/{id}/evaluate")
    public Result<Void> evaluateTraining(@PathVariable Long id, @RequestBody CreateBusinessRequest data) {
        BusinessRecord r = businessRecordService.getById(id);
        if (r == null) return Result.fail(com.gmp.common.base.ResultCode.NOT_FOUND, "记录不存在");
        String evalResult = data.getTitle() != null ? data.getTitle() : "已完成";
        r.setSummary((r.getSummary() != null ? r.getSummary() : "") + " [评估: " + evalResult + "]");
        r.setBusinessStatus("COMPLETED");
        businessRecordService.updateById(r);
        return Result.okMsg("评估完成");
    }

    // ==================== 资质管理 ====================

    @GetMapping("/certification/user/{userId}")
    public Result<List<Map<String, Object>>> getUserCertifications(@PathVariable Long userId) {
        LambdaQueryWrapper<BusinessRecord> w = new LambdaQueryWrapper<>();
        w.eq(BusinessRecord::getBusinessType, BusinessType.TMS_CERTIFICATION.getCode())
                .eq(BusinessRecord::getInitiatorId, userId);
        List<Map<String, Object>> list = businessRecordService.list(w).stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("name", r.getTitle());
            m.put("status", r.getBusinessStatus());
            m.put("completedAt", r.getCompletedAt());
            return m;
        }).toList();
        return Result.ok(list);
    }

    @GetMapping("/certification/expiring")
    public Result<List<Map<String, Object>>> getExpiringCertifications(@RequestParam(defaultValue = "30") int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusDays(days);
        LambdaQueryWrapper<BusinessRecord> w = new LambdaQueryWrapper<>();
        w.eq(BusinessRecord::getBusinessType, BusinessType.TMS_CERTIFICATION.getCode())
                .eq(BusinessRecord::getBusinessStatus, "COMPLETED")
                .gt(BusinessRecord::getCompletedAt, now)
                .lt(BusinessRecord::getCompletedAt, threshold);
        List<Map<String, Object>> list = businessRecordService.list(w).stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("name", r.getTitle());
            m.put("completedAt", r.getCompletedAt());
            return m;
        }).toList();
        return Result.ok(list);
    }

    // ==================== 通用查询 ====================

    private PageResult<BusinessRecord> queryByType(String businessType, int pageNum, int pageSize, String keyword, String status) {
        LambdaQueryWrapper<BusinessRecord> w = new LambdaQueryWrapper<>();
        w.eq(BusinessRecord::getBusinessType, businessType);
        if (status != null && !status.isEmpty()) w.eq(BusinessRecord::getBusinessStatus, status);
        if (keyword != null && !keyword.isEmpty()) {
            w.and(q -> q.like(BusinessRecord::getTitle, keyword).or().like(BusinessRecord::getBusinessNo, keyword));
        }
        w.orderByDesc(BusinessRecord::getCreateTime);
        return businessRecordService.pageQuery(pageNum, pageSize, w);
    }
}
