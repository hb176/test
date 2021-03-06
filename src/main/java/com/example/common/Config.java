package com.example.common;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
    public class Config {
        @Bean
        public DruidDataSource druidDataSource() {
            //Druid 数据源配置
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/demo?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
            dataSource.setUsername("root");
            dataSource.setPassword("123456");
            //初始连接数(默认值0)
            dataSource.setInitialSize(8);
            //最小连接数(默认值0)
            dataSource.setMinIdle(8);
            //最大连接数(默认值8,注意"maxIdle"这个属性已经弃用)
            dataSource.setMaxActive(32);
            return dataSource;
        }
    }
