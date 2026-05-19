package com.gmp.common.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author hb176
 * @time 2019-12-17 15:05
 * @email xinhui.chen@abioplus.cn
 * @Description: 用于手动获取spring对象
 */
@Component
public class SpringUtil implements ApplicationContextAware {

    //用于手动获取bean的name
    public static final String JWT_PARSE_BEAN_NAME = "jwtTokenParse";
    public static final String MSG_SOURCE_BEAN_NAME = "messageSource";

    private static ApplicationContext applicationContext;

    @Value("${spring.application.name}")
    private  String applicationName;

    public static Object getBean(String name){
        return applicationContext.getBean(name);
    }

    public static <T> T getBean(Class<T> requiredType){
        return applicationContext.getBean(requiredType);
    }

    public static ApplicationContext getApplicationContext(){
        return applicationContext;
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        injectApplicationContext(applicationContext); // 因为spring会创建这个接口的实现类的一个对象，所以实例方法调用静态方法，只是目前这个类我们是看不到的
    }
    public static void injectApplicationContext(ApplicationContext applicationContext){
        SpringUtil.applicationContext = applicationContext;  // 这其实是实例方法调用静态方法
    }
    public String getApplicationName(){
       return applicationName ;
    }
}
