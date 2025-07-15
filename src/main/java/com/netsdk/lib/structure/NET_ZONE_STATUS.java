package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;
/**
 * 防区异常信息
*/
public class NET_ZONE_STATUS extends NetSDKLib.SdkStructure
{
    /**
     * 防区号
    */
    public int              nIndex;
    /**
     * 防区异常状态,参见枚举定义 {@link com.netsdk.lib.enumeration.EM_ZONE_STATUS}
    */
    public int              emStatus;
    /**
     * 保留字节
    */
    public byte[]           byReserved = new byte[1024];

    public NET_ZONE_STATUS() {
    }
}

