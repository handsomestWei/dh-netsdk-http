package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;
/**
 * CLIENT_StopApp 接口输出参数
*/
public class NET_OUT_STOP_APP extends NetSDKLib.SdkStructure
{
    /**
     * 结构体大小
    */
    public int              dwSize;

    public NET_OUT_STOP_APP() {
        this.dwSize = this.size();
    }
}

