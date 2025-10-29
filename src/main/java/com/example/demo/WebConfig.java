package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private AdminInterceptor adminInterceptor;
    
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/error/**", "/login", "/register");
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/users/**")
                .excludePathPatterns("/error/**", "/login", "/register", "/users", "/users/login", "/users/logout");
    }
}
