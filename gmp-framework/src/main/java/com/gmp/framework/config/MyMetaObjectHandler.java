package com.gmp.framework.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.gmp.common.security.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("执行插入自动填充");
        LocalDateTime now = LocalDateTime.now();
        Long currentUserId = getCurrentUserId();

        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "createBy", Long.class, currentUserId);
        this.strictInsertFill(metaObject, "updateBy", Long.class, currentUserId);
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
        this.strictInsertFill(metaObject, "version", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("执行更新自动填充");
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", Long.class, getCurrentUserId());
    }

    private Long getCurrentUserId() {
        try {
            Long userId = SecurityContextHolder.getCurrentUserId();
            return userId != null ? userId : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }
}
