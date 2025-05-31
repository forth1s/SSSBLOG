package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@SpringBootApplication
@EnableScheduling//开启定时任务支持
@MapperScan("com.example.mapper")
public class SSSBlogApplication extends WebMvcConfigurationSupport {
    public static void main(String[] args) {
        SpringApplication.run(SSSBlogApplication.class, args);
    }
}
