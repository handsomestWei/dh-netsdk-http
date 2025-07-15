package com.netsdk.demo.http.config;

import com.netsdk.demo.module.ext.LoginExtModule;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.netsdk.demo.http.interceptor.DeviceLoginInterceptor;

@Component
@Configuration
public class NetSDKInitConfig implements WebMvcConfigurer {

    @PostConstruct
    public void initSDK() {
        // 可自定义断线/重连回调，这里用null
        LoginExtModule.init(null, null);
    }

    @PreDestroy
    public void cleanupSDK() {
        LoginExtModule.cleanup();
    }

    /*
     * 添加拦截器，在请求结束时自动释放登录句柄
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new DeviceLoginInterceptor()).addPathPatterns("/**");
    }
} 