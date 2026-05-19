package com.gmp.framework.security;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁注解 - 标记在方法上，自动获取和释放分布式锁
 *
 * 使用示例：
 * @DistributedLock(key = "user:#{#userId}", waitTime = 5, leaseTime = 30)
 * public void updateUser(Long userId) { ... }
 *
 * 参数说明：
 * - key: 锁的标识，支持SpEL表达式动态传参
 * - waitTime: 获取锁的最大等待时间
 * - leaseTime: 锁的持有时间（自动释放，防止死锁）
 * - timeUnit: 时间单位
 *
 * @author hb176
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    /**
     * 锁的键名 - 支持SpEL表达式
     * 例: "user:#{#userId}" 表示锁的用户ID维度
     * 最终生成的Redis Key为: gmp:lock:{methodName}:{key}
     */
    String key() default "default";

    /**
     * 获取锁的最大等待时间（秒）
     * 超过此时间未获取到锁，抛出异常返回繁忙提示
     */
    long waitTime() default 3;

    /**
     * 锁的持有时间（秒）
     * 超时自动释放，防止死锁。应大于业务方法的最大执行时间
     */
    long leaseTime() default 30;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
