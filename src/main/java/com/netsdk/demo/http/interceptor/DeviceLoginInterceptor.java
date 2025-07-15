package com.netsdk.demo.http.interceptor;

import com.netsdk.demo.http.controller.BaseDeviceController;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.demo.module.ext.LoginExtModule;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/*
 * 在请求结束时自动释放登录句柄
 */
public class DeviceLoginInterceptor implements HandlerInterceptor {

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        NetSDKLib.LLong handle = BaseDeviceController.loginHandleHolder.get();
        if (handle != null && handle.longValue() != 0) {
            LoginExtModule.logout(handle);
        }
        BaseDeviceController.loginHandleHolder.remove();
    }
} 