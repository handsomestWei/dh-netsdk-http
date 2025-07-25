package com.netsdk.demo.http.controller;

import com.netsdk.demo.http.util.DeviceAccessHttpUtil;
import com.netsdk.demo.module.ext.LoginExtModule;
import com.netsdk.lib.NetSDKLib;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseDeviceController {

    @javax.annotation.Resource
    protected DeviceAccessHttpUtil deviceAccessHttpUtil;

    // 每个请求线程独立持有loginHandle
    public static final ThreadLocal<NetSDKLib.LLong> loginHandleHolder = new ThreadLocal<>();

    @ModelAttribute
    public void deviceLogin() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest();
            // 统一用工具类静态方法获取并校验请求头参数
            DeviceAccessHttpUtil.DeviceAccessInfo info = DeviceAccessHttpUtil.parseAndValidateDeviceHeaders(request);
            NetSDKLib.LLong loginHandle = doDeviceLogin(info);
            if (loginHandle == null) {
                throw new RuntimeException("设备登录失败");
            }
        } catch (Exception e) {
            String eMsg = e.getMessage();
            log.error("设备登录异常", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, eMsg);
        }
    }

    /**
     * 设备登录，返回登录句柄
     */
    protected NetSDKLib.LLong doDeviceLogin(DeviceAccessHttpUtil.DeviceAccessInfo info) {
        int port = info.port;
        NetSDKLib.LLong handle = LoginExtModule.login(info.ip, port, info.user, info.password);
        if (handle == null || handle.longValue() == 0) {
            log.warn("设备登录失败: ip={}, port={}, user={}", info.ip, port, info.user);
            return null;
        }
        loginHandleHolder.set(handle);
        return handle;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ex.getReason());
    }
}
