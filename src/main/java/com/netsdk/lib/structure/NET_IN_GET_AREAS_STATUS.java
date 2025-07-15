package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;
/**
 * 获取区域状态 输入参数。此时CLIENT_GetAlarmRegionInfo的emType参数为NET_EM_GET_ALARMREGION_INFO_AREASTATUS
*/
public class NET_IN_GET_AREAS_STATUS extends NetSDKLib.SdkStructure
{
    /**
     * 结构体大小
    */
    public int              dwSize;
    /**
     * 获取异常防区类型,参见枚举定义 {@link com.netsdk.lib.enumeration.EM_GET_AREASSTATUS_TYPE}
    */
    public int              emType;

    public NET_IN_GET_AREAS_STATUS() {
        this.dwSize = this.size();
    }
}

