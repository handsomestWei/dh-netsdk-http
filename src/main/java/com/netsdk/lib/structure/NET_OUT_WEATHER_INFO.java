package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;
/**
 * 订阅气象信息输出参数
*/
public class NET_OUT_WEATHER_INFO extends NetSDKLib.SdkStructure
{
    public int              dwSize;

    public NET_OUT_WEATHER_INFO() {
        this.dwSize = this.size();
    }
}

