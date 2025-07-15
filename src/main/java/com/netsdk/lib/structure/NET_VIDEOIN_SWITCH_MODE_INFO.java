package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;
/**
 * 切换模式配置
*/
public class NET_VIDEOIN_SWITCH_MODE_INFO extends NetSDKLib.SdkStructure
{
    public int              dwSize;
    /**
     * 切换模式,参见枚举定义 {@link com.netsdk.lib.enumeration.NET_EM_SWITCH_MODE}
    */
    public int              emSwitchMode;
    /**
     * 大致日出时间,参见结构体定义 {@link com.netsdk.lib.structure.NET_SUN_TIME}
    */
    public NET_SUN_TIME     stuSunRiseTime = new NET_SUN_TIME();
    /**
     * 大致日落时间,参见结构体定义 {@link com.netsdk.lib.structure.NET_SUN_TIME}
    */
    public NET_SUN_TIME     stuSunSetTime = new NET_SUN_TIME();
    /**
     * 12个月的时间配置信息,参见结构体定义 {@link com.netsdk.lib.structure.NET_TIME_SECTION_V2}
    */
    public NET_TIME_SECTION_V2[] stuTimeSection = new NET_TIME_SECTION_V2[12];

    public NET_VIDEOIN_SWITCH_MODE_INFO() {
        this.dwSize = this.size();
        for(int i = 0; i < stuTimeSection.length; i++){
            stuTimeSection[i] = new NET_TIME_SECTION_V2();
        }
    }
}

