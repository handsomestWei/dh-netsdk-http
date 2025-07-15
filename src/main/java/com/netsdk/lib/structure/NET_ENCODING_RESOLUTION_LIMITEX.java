package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;
/**
 * 主辅码流分辨率组合限制
*/
public class NET_ENCODING_RESOLUTION_LIMITEX extends NetSDKLib.SdkStructure
{
    /**
     * 分辨率1,参见枚举定义 {@link com.netsdk.lib.enumeration.EM_CAPTURE_SIZE_EX}
    */
    public int              emResolution1;
    /**
     * 分辨率2,参见枚举定义 {@link com.netsdk.lib.enumeration.EM_CAPTURE_SIZE_EX}
    */
    public int              emResolution2;
    /**
     * 保留
    */
    public byte[]           szReserved = new byte[1024];

    public NET_ENCODING_RESOLUTION_LIMITEX() {
    }
}

