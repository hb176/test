package com.gmp.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring上下文持有者 - 在非Spring管理的类中获取Bean
 * 通过ApplicationContextAware接口在启动时获取ApplicationContext引用
 *
 * 使用场景：工具类、非Spring Bean中需要获取Spring管理的Bean时使用
 * 例如：SpringContextHolder.getBean(RedisTemplate.class)
 *
 * @author hb176
 * @since 1.0.0
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {

    /** Spring应用上下文（静态保存，全局可访问） */
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    /**
     * 获取ApplicationContext
     * @return Spring应用上下文
     */
    public static ApplicationContext getApplicationContext() {
        checkApplicationContext();
        return applicationContext;
    }

    /**
     * 根据Bean名称获取Bean实例
     * @param name Bean名称
     * @return Bean实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        checkApplicationContext();
        return (T) applicationContext.getBean(name);
    }

    /**
     * 根据Bean类型获取Bean实例
     * @param clazz Bean类型
     * @return Bean实例
     */
    public static <T> T getBean(Class<T> clazz) {
        checkApplicationContext();
        return applicationContext.getBean(clazz);
    }

    /**
     * 根据Bean名称和类型获取Bean实例
     * @param name Bean名称
     * @param clazz Bean类型
     * @return Bean实例
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        checkApplicationContext();
        return applicationContext.getBean(name, clazz);
    }

    /**
     * 获取当前激活的Spring Profile
     * @return 激活的Profile名称
     */
    public static String getActiveProfile() {
        checkApplicationContext();
        String[] profiles = applicationContext.getEnvironment().getActiveProfiles();
        return profiles.length > 0 ? profiles[0] : "default";
    }

    /**
     * 获取配置项的值
     * @param key 配置键
     * @return 配置值，不存在则返回null
     */
    public static String getProperty(String key) {
        checkApplicationContext();
        return applicationContext.getEnvironment().getProperty(key);
    }

    /**
     * 检查ApplicationContext是否已初始化
     */
    private static void checkApplicationContext() {
        if (applicationContext == null) {
            throw new IllegalStateException(
                    "Spring ApplicationContext未初始化！请确保SpringContextHolder已被Spring容器管理");
        }
    }
}
