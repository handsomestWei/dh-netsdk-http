package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;
/**
 * CLIENT_SetUnpackingResultWithPacket 接口输出参数
*/
public class NET_OUT_SET_UNPACKING_RESULT_WITH_PACKET extends NetSDKLib.SdkStructure
{
    /**
     * 结构体大小
    */
    public int              dwSize;

    public NET_OUT_SET_UNPACKING_RESULT_WITH_PACKET() {
        this.dwSize = this.size();
    }
}

