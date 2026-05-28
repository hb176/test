package com.gmp.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gmp.framework.base.CommonService;
import com.gmp.system.entity.TmsAssignment;
import com.gmp.system.mapper.TmsAssignmentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class TmsAssignmentService extends CommonService<TmsAssignmentMapper, TmsAssignment> {

    public void assignUsers(Long courseId, List<Long> userIds, List<String> userNames) {
        for (int i = 0; i < userIds.size(); i++) {
            // 避免重复分配
            long exists = lambdaQuery()
                    .eq(TmsAssignment::getCourseId, courseId)
                    .eq(TmsAssignment::getUserId, userIds.get(i))
                    .count();
            if (exists > 0) continue;

            TmsAssignment a = new TmsAssignment();
            a.setCourseId(courseId);
            a.setUserId(userIds.get(i));
            a.setUserName(i < userNames.size() ? userNames.get(i) : "");
            a.setStatus("ASSIGNED");
            save(a);
        }
        log.info("培训分配完成: courseId={}, 分配人数={}", courseId, userIds.size());
    }

    public void completeAssignment(Long id, Integer score) {
        TmsAssignment a = getById(id);
        if (a == null) return;
        a.setStatus("COMPLETED");
        a.setScore(score);
        a.setCompletedAt(LocalDateTime.now());
        updateById(a);
    }

    public List<TmsAssignment> getByCourseId(Long courseId) {
        return lambdaQuery().eq(TmsAssignment::getCourseId, courseId).orderByDesc(TmsAssignment::getCreateTime).list();
    }

    public List<TmsAssignment> getByUserId(Long userId) {
        return lambdaQuery().eq(TmsAssignment::getUserId, userId).orderByDesc(TmsAssignment::getCreateTime).list();
    }

    public List<TmsAssignment> getExpiring(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusDays(days);
        return lambdaQuery()
                .eq(TmsAssignment::getStatus, "COMPLETED")
                .isNotNull(TmsAssignment::getExpiryDate)
                .gt(TmsAssignment::getExpiryDate, now)
                .lt(TmsAssignment::getExpiryDate, threshold)
                .list();
    }

    public long countCompleted(Long courseId) {
        return lambdaQuery()
                .eq(TmsAssignment::getCourseId, courseId)
                .eq(TmsAssignment::getStatus, "COMPLETED")
                .count();
    }
}
