//package com.example.common;
//
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
//import org.springframework.web.servlet.config.annotation.EnableWebMvc;
//import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//@ComponentScan
//@EnableWebMvc
//public class MyWebMvcConfigurer implements WebMvcConfigurer {
//    @Override
//    public void configureContentNegotiationePathMatch(PathMatchConfigurer pathMatchConfigurer) {
//        pathMatchConfigurer.setUseSuffixPatternMatch(false);
//        pathMatchConfigurer.setUseRegisteredSuffixPatternMatch(true);
//    }
//
//    @Override
//    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
//        configurer.favorPathExtension(false);
//    }
//}
