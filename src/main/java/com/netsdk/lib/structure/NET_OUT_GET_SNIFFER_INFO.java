package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;
import com.sun.jna.Pointer;
/**
 * CLIENT_GetSnifferInfo 接口输入参数
*/
public class NET_OUT_GET_SNIFFER_INFO extends NetSDKLib.SdkStructure
{
    public int              dwSize;
    /**
     * 抓包信息数组, 用户分配内存,参见结构体定义 {@link com.netsdk.lib.structure.NET_SNIFFER_INFO}
    */
    public Pointer          pstuSniffers;
    /**
     * 抓包信息数组大小, 用户填写
    */
    public int              nMaxSnifferCount;
    /**
     * 返回的抓包信息数量
    */
    public int              nRetSnifferCount;

    public NET_OUT_GET_SNIFFER_INFO() {
        this.dwSize = this.size();
    }
}

