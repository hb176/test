package com.gmp.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gmp.auth.entity.SysConfig;
import com.gmp.auth.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenConfigService {

    private static final String CONFIG_KEY = "token.expire-minutes";
    private static final long DEFAULT_EXPIRE_MINUTES = 30L;
    /** 内存缓存有效期（毫秒）：10分钟 */
    private static final long CACHE_TTL_MS = 10 * 60 * 1000;

    private final SysConfigMapper sysConfigMapper;

    private volatile long cachedMinutes = DEFAULT_EXPIRE_MINUTES;
    private volatile long cacheExpireAt = 0;

    public long getExpireMinutes() {
        long now = System.currentTimeMillis();
        if (now < cacheExpireAt) {
            return cachedMinutes;
        }
        long minutes = readFromDb();
        cachedMinutes = minutes;
        cacheExpireAt = now + CACHE_TTL_MS;
        return minutes;
    }

    public long getExpireSeconds() {
        return getExpireMinutes() * 60;
    }

    public void setExpireMinutes(long minutes) {
        SysConfig config = sysConfigMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, CONFIG_KEY));
        if (config == null) {
            config = new SysConfig();
            config.setConfigKey(CONFIG_KEY);
            config.setConfigValue(String.valueOf(minutes));
            config.setCategory("security");
            config.setDescription("登录Token过期时间（分钟）");
            sysConfigMapper.insert(config);
        } else {
            config.setConfigValue(String.valueOf(minutes));
            sysConfigMapper.updateById(config);
        }
        // 清除内存缓存，下次读取时从DB加载
        cacheExpireAt = 0;
        log.info("Token过期时间已更新为 {} 分钟", minutes);
    }

    private long readFromDb() {
        try {
            SysConfig config = sysConfigMapper.selectOne(
                    new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, CONFIG_KEY));
            if (config != null && config.getConfigValue() != null) {
                return Long.parseLong(config.getConfigValue());
            }
        } catch (Exception e) {
            log.warn("读取Token过期配置失败: {}", e.getMessage());
        }
        return DEFAULT_EXPIRE_MINUTES;
    }
}
