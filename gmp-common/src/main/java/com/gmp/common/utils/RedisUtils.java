package com.gmp.common.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类 - 封装常用Redis操作
 * 基于Spring Data Redis的RedisTemplate，提供类型安全的缓存操作
 *
 * 使用场景：
 * - Token黑名单管理
 * - 验证码存储与校验
 * - 分布式锁辅助
 * - 热点数据缓存
 * - 用户权限缓存
 *
 * @author hb176
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== 基础操作 ====================

    /**
     * 设置缓存（带过期时间）
     * @param key   缓存键
     * @param value 缓存值
     * @param timeout 过期时间
     * @param unit  时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 设置缓存（永不过期，慎用）
     * @param key   缓存键
     * @param value 缓存值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 获取缓存值
     * @param key 缓存键
     * @return 缓存值，不存在返回null
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取缓存值并转换为指定类型
     * @param key   缓存键
     * @param clazz 目标类型
     * @return 缓存值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return (T) value;
    }

    /**
     * 判断键是否存在
     * @param key 缓存键
     * @return true=存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 删除缓存
     * @param key 缓存键
     * @return true=删除成功
     */
    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /**
     * 批量删除缓存
     * @param keys 缓存键集合
     * @return 删除的键数量
     */
    public long delete(Collection<String> keys) {
        Long count = redisTemplate.delete(keys);
        return count != null ? count : 0;
    }

    /**
     * 设置过期时间
     * @param key     缓存键
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return true=设置成功
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }

    /**
     * 获取剩余过期时间（秒）
     * @param key 缓存键
     * @return 剩余秒数，-1=永不过期，-2=键不存在
     */
    public long getExpire(String key) {
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire != null ? expire : -2;
    }

    // ==================== 递增/递减操作 ====================

    /**
     * 自增（常用于限流计数器）
     * @param key   缓存键
     * @param delta 增量
     * @return 自增后的值
     */
    public long incr(String key, long delta) {
        Long value = redisTemplate.opsForValue().increment(key, delta);
        return value != null ? value : 0;
    }

    // ==================== Hash操作 ====================

    /**
     * Hash设置
     * @param key     缓存键
     * @param hashKey Hash键
     * @param value   值
     */
    public void hSet(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * Hash获取
     * @param key     缓存键
     * @param hashKey Hash键
     * @return 值
     */
    public Object hGet(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * Hash获取所有键值对
     * @param key 缓存键
     * @return Map
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * Hash删除
     * @param key     缓存键
     * @param hashKeys Hash键
     */
    public void hDelete(String key, Object... hashKeys) {
        redisTemplate.opsForHash().delete(key, hashKeys);
    }

    // ==================== Set操作 ====================

    /**
     * Set添加成员
     * @param key    缓存键
     * @param values 成员值
     */
    public void sAdd(String key, Object... values) {
        redisTemplate.opsForSet().add(key, values);
    }

    /**
     * Set获取所有成员
     * @param key 缓存键
     * @return 成员集合
     */
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * Set判断是否为成员
     * @param key   缓存键
     * @param value 值
     * @return true=是成员
     */
    public boolean sIsMember(String key, Object value) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
    }

    // ==================== List操作 ====================

    /**
     * List右侧推入（队列尾部添加）
     * @param key   缓存键
     * @param value 值
     */
    public void lPush(String key, Object value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * List左侧弹出（队列头部取出）
     * @param key 缓存键
     * @return 值
     */
    public Object rPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    /**
     * 获取List指定范围的元素
     * @param key   缓存键
     * @param start 起始索引
     * @param end   结束索引
     * @return 元素列表
     */
    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    // ==================== 模糊匹配查询 ====================

    /**
     * 根据模式匹配查找所有匹配的键（慎用：生产环境大数据量会影响性能）
     * @param pattern 匹配模式 (例: "gmp:user:*")
     * @return 匹配的键集合
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 根据模式匹配批量删除
     * @param pattern 匹配模式
     */
    public void deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
