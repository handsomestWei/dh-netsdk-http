package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;
/**
 * CLIENT_SetWaybillStatus 接口输出参数
*/
public class NET_OUT_SET_WAYBILL_STATUS extends NetSDKLib.SdkStructure
{
    /**
     * 结构体大小
    */
    public int              dwSize;

    public NET_OUT_SET_WAYBILL_STATUS() {
        this.dwSize = this.size();
    }
}

