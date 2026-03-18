package com.example.demo.config;

import com.example.demo.interceptor.JwtInterceptor;
import com.example.demo.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new LoginInterceptor()).addPathPatterns("/interceptor/**");
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/mood/**")
                .excludePathPatterns("/mood/user/login", "/mood/user/register");
    }

}
