package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;
/**
 * CLIENT_DockStatusReply 接口输出参数
*/
public class NET_OUT_DOCK_STATUS_REPLY extends NetSDKLib.SdkStructure
{
    /**
     * 结构体大小
    */
    public int              dwSize;

    public NET_OUT_DOCK_STATUS_REPLY() {
        this.dwSize = this.size();
    }
}

