package com.netsdk.demo.http;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.netsdk.demo.http.util.ProcessGuardUtil;

@SpringBootApplication
public class NetSdkApplication {
    public static void main(String[] args) throws Exception {
        // SpringApplication.run(NetSdkApplication.class, args);
        ProcessGuardUtil.registerGlobalExceptionHandler();
        ProcessGuardUtil.launchWithGuard(NetSdkApplication.class, args);
    }
} 