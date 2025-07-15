package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;
/**
 * 联动报警输出通道信息
*/
public class NET_ALARM_OUT_CHANNEL_INFO extends NetSDKLib.SdkStructure
{
    /**
     * 比对报警规则,参见枚举定义 {@link com.netsdk.lib.enumeration.EM_ALARM_RULE_TYPE}
    */
    public int              emAlarmRuleType;
    /**
     * 报警输出延时, 单位秒, 范围1-300
    */
    public int              nAlarmOutLatch;
    /**
     * 保留字节
    */
    public byte[]           byReserved = new byte[128];

    public NET_ALARM_OUT_CHANNEL_INFO() {
    }
}

