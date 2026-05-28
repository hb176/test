package com.gmp.system.controller;

import com.gmp.framework.base.CommonController;
import com.gmp.framework.base.Result;
import com.gmp.system.entity.TmsAssignment;
import com.gmp.system.service.TmsAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tms/assignment")
public class TmsAssignmentController extends CommonController<TmsAssignmentService, TmsAssignment> {

    private final TmsAssignmentService assignmentService;

    @Override
    protected TmsAssignmentService getService() {
        return assignmentService;
    }

    @PostMapping
    public Result<Void> assignUsers(@RequestBody AssignRequest request) {
        assignmentService.assignUsers(request.getCourseId(), request.getUserIds(), request.getUserNames());
        return Result.okMsg("分配成功");
    }

    @GetMapping("/course/{courseId}")
    public Result<List<TmsAssignment>> getByCourseId(@PathVariable Long courseId) {
        return Result.ok(assignmentService.getByCourseId(courseId));
    }

    @GetMapping("/user/{userId}")
    public Result<List<TmsAssignment>> getByUserId(@PathVariable Long userId) {
        return Result.ok(assignmentService.getByUserId(userId));
    }

    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id, @RequestBody CompleteRequest request) {
        assignmentService.completeAssignment(id, request.getScore());
        return Result.okMsg("已完成");
    }

    @GetMapping("/expiring")
    public Result<List<TmsAssignment>> getExpiring(@RequestParam(defaultValue = "30") int days) {
        return Result.ok(assignmentService.getExpiring(days));
    }

    @GetMapping("/course/{courseId}/stats")
    public Result<Map<String, Object>> getCourseStats(@PathVariable Long courseId) {
        long total = assignmentService.lambdaQuery().eq(TmsAssignment::getCourseId, courseId).count();
        long completed = assignmentService.countCompleted(courseId);
        return Result.ok(Map.of("total", total, "completed", completed));
    }

    @lombok.Data
    public static class AssignRequest {
        private Long courseId;
        private List<Long> userIds;
        private List<String> userNames;
    }

    @lombok.Data
    public static class CompleteRequest {
        private Integer score;
    }
}
