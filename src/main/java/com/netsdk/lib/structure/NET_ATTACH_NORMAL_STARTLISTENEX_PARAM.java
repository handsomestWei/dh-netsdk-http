package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;
import com.sun.jna.Pointer;
/**
 * EM_STARTLISTENEX对应的订阅参数
*/
public class NET_ATTACH_NORMAL_STARTLISTENEX_PARAM extends NetSDKLib.SdkStructure
{
    /**
     * 回调函数,参见回调函数定义 {@link com.netsdk.lib.NetSDKLib.fAttachNormalCallBack}
    */
    public NetSDKLib.fAttachNormalCallBack cbAttachNormal;
    /**
     * 用户信息
    */
    public Pointer          dwUser;
    /**
     * 预留字节
    */
    public byte[]           szReserved = new byte[1024];

    public NET_ATTACH_NORMAL_STARTLISTENEX_PARAM() {
    }
}

