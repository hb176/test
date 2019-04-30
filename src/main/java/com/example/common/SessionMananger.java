//package com.example.common;
//
//import org.apache.tomcat.util.descriptor.web.ErrorPage;
//import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
//import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.http.HttpStatus;
//
//
//import java.util.concurrent.TimeUnit;
//
//public class SessionMananger {
//    @Bean
//    public EmbeddedServletContainerFactory servletContainer() {
//        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
//        factory.setPort(8080);
//        factory.setSessionTimeout(10, TimeUnit.MINUTES);
//        factory.s
//        return factory;
//    }
//}
