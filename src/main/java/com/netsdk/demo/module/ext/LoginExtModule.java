package com.netsdk.demo.module.ext;

import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.NetSDKLib.NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY;
import com.netsdk.lib.NetSDKLib.NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY;
import com.netsdk.lib.ToolKits;
import lombok.extern.slf4j.Slf4j;

/*
 * 登陆接口实现扩展
 */
@Slf4j
public class LoginExtModule {

    public static NetSDKLib netsdk 		= NetSDKLib.NETSDK_INSTANCE;
	public static NetSDKLib configsdk 	= NetSDKLib.CONFIG_INSTANCE;

    private static boolean bInit    = false;
	private static boolean bLogopen = false;

    /**
     * 线程安全登录，返回独立句柄
     */
    public static NetSDKLib.LLong login(String ip, int port, String user, String pwd) {
        NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY inParam = new NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY();
        inParam.nPort = port;
        inParam.szIP = ip.getBytes();
        inParam.szPassword = pwd.getBytes();
        inParam.szUserName = user.getBytes();
        NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY outParam = new NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY();
        NetSDKLib.LLong handle = netsdk.CLIENT_LoginWithHighLevelSecurity(inParam, outParam);
        if (handle.longValue() == 0) {
            log.warn("Login Device[{}] Port[{}]Failed. {}", ip, port, ToolKits.getErrorCodePrint());
            return null;
        } else {
            log.debug("Login Success [ {} ]", ip);
            return handle;
        }
    }

    /**
     * 线程安全登出
     */
    public static boolean logout(NetSDKLib.LLong handle) {
        if (handle == null || handle.longValue() == 0) {
            return false;
        }
        boolean bRet = netsdk.CLIENT_Logout(handle);
        if (bRet) {
            handle.setValue(0);
            log.debug("Logout Success, handle={}", handle);
        } else {
            log.warn("Logout Failed, handle={}", handle);
        }
        return bRet;
    }

    /**
     * SDK初始化，支持断线重连回调、网络参数设置等
     */
    public static boolean init(NetSDKLib.fDisConnect disConnect, NetSDKLib.fHaveReConnect haveReConnect) {
        boolean bInit = netsdk.CLIENT_Init(disConnect, null);
        if (!bInit) {
            log.error("Initialize SDK failed");
            throw new RuntimeException("Initialize SDK failed");
        }
        // 设置断线重连回调接口，建议设置
        netsdk.CLIENT_SetAutoReconnect(haveReConnect, null);
        // 设置登录超时时间和尝试次数
        int waitTime = 5000; // 登录请求响应超时时间设置为5S
        int tryTimes = 1;    // 登录时尝试建立链接1次
        netsdk.CLIENT_SetConnectTime(waitTime, tryTimes);
        // 设置更多网络参数
        NetSDKLib.NET_PARAM netParam = new NetSDKLib.NET_PARAM();
        netParam.nConnectTime = 10000;      // 登录时尝试建立链接的超时时间
        netParam.nGetConnInfoTime = 3000;   // 设置子连接的超时时间
        netParam.nGetDevInfoTime = 3000;    // 获取设备信息超时时间，为0默认1000ms
        netsdk.CLIENT_SetNetworkParam(netParam);
        log.info("SDK初始化成功");
        return true;
    }

    /**
     * SDK软重启：先cleanup再init
     */
    @Deprecated
    public static synchronized void resetSdk() {
        log.warn("检测到native异常，准备软重启SDK...");
        cleanup();
        if (init(null, null)) {
            log.warn("SDK软重启完成");
        } else {
            log.error("SDK软重启失败");
        }
    }

    public static void cleanup() {
		if(bLogopen) {
			netsdk.CLIENT_LogClose();
		}
		
		if(bInit) {
			netsdk.CLIENT_Cleanup();
		}
	}
}
