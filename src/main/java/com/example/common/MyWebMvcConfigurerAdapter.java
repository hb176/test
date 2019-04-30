package com.example.common;

import com.example.common.MyInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;


@Configuration
@EnableWebMvc
public class MyWebMvcConfigurerAdapter extends WebMvcConfigurerAdapter {

    @Bean
    MyInterceptor myInterceptor(){
        return new MyInterceptor();
    }
    @Override
      public void addInterceptors(InterceptorRegistry registry) {
        /*  多个拦截器组成一个拦截器链
                *  excludePathPatterns 用户排除拦截
                *  addPathPatterns 用于添加拦截规则
                *  拦截规则：除了"showgetLogin" 其他都拦截
                * */
        registry.addInterceptor(new MyInterceptor()).addPathPatterns("/**").excludePathPatterns("/loginUser","/loginOut","/login");//多个排除拦截的话，只需要用逗号隔开即可
        //这边还可以加好几个拦截器组成拦截器链
        super.addInterceptors(registry);
    }
    //自定义资源拦截路径可以和springBoot默认的资源拦截一起使用，但是我们如果自己定义的路径与默认的拦截重复，那么我们该方法定义的就会覆盖默认配置
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //这里也可以采用ResourceUtils.CLASSPATH_URL_PREFIX 它是：classpath:
        registry.addResourceHandler("webapp/img/**").addResourceLocations("classpath:webapp/img/");
        registry.addResourceHandler("webapp/js/**").addResourceLocations("classpath:webapp/js/");
        registry.addResourceHandler("webapp/css/**").addResourceLocations("classpath:webapp/css/");
        super.addResourceHandlers(registry);
    }

}