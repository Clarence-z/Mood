package com.example.demo.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件（一个拦截器）
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor();

        paginationInterceptor.setDbType(DbType.MYSQL);

        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }
}
