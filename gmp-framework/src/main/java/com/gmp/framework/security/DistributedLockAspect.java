package com.gmp.framework.security;

import com.gmp.common.constant.SystemConstants;
import com.gmp.common.exceptions.BusinessException;
import com.gmp.common.base.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁切面 - 基于Redisson实现方法级的分布式锁
 *
 * 使用方式：在需要分布式锁的方法上添加 @DistributedLock 注解
 * @DistributedLock(key = "order:${orderId}", waitTime = 3, leaseTime = 10, timeUnit = TimeUnit.SECONDS)
 *
 * 锁机制说明：
 * - waitTime: 获取锁的最大等待时间，超时抛出异常（避免长时间阻塞）
 * - leaseTime: 锁的自动释放时间（防止死锁）
 * - key: 支持SpEL表达式，可以动态构造锁的键名
 *
 * Redisson底层使用Redis的SET NX PX命令 + Lua脚本实现高性能分布式锁
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final RedissonClient redissonClient;

    /**
     * 环绕通知 - 在方法执行前后自动获取和释放分布式锁
     */
    @Around("@annotation(com.gmp.framework.security.DistributedLock)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock lockAnnotation = method.getAnnotation(DistributedLock.class);

        // 构建锁的键名：前缀 + 方法名 + SpEL解析后的键
        String lockKey = SystemConstants.REDIS_LOCK_PREFIX + method.getName() + ":" + lockAnnotation.key();
        RLock lock = redissonClient.getLock(lockKey);

        long waitTime = lockAnnotation.waitTime();
        long leaseTime = lockAnnotation.leaseTime();
        TimeUnit timeUnit = lockAnnotation.timeUnit();

        log.debug("尝试获取分布式锁 - 键: {}, 等待时间: {}秒, 持有时间: {}秒",
                lockKey, timeUnit.toSeconds(waitTime), timeUnit.toSeconds(leaseTime));

        boolean acquired;
        try {
            // 尝试获取锁（带等待超时）
            acquired = lock.tryLock(waitTime, leaseTime, timeUnit);
            if (!acquired) {
                log.warn("获取分布式锁超时 - 键: {} (可能存在并发冲突)", lockKey);
                throw new BusinessException(ResultCode.TOO_MANY_REQUESTS,
                        "系统繁忙，请稍后重试（获取锁超时）");
            }
            log.debug("分布式锁获取成功 - 键: {}", lockKey);

            // 执行目标方法
            return joinPoint.proceed();

        } finally {
            // 释放锁（必须放在finally中确保一定释放）
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("分布式锁已释放 - 键: {}", lockKey);
            }
        }
    }
}
