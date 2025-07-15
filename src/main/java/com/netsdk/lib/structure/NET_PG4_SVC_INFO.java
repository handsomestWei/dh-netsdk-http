package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;
/**
 * 对应CLIENT_StartSearchDevicesEx接口TTLV回调
*/
public class NET_PG4_SVC_INFO extends NetSDKLib.SdkStructure
{
    public int              nEz;
    public int              nEzs;
    /**
     * 扩展字段
    */
    public byte[]           szReserved = new byte[2048];

    public NET_PG4_SVC_INFO() {
    }
}

