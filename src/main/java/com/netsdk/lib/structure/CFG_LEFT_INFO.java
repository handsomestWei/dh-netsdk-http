package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;
/**
 * 事件类型EVENT_IVS_LEFTDETECTION(物品遗留事件)对应的规则配置
*/
public class CFG_LEFT_INFO extends NetSDKLib.SdkStructure
{
    /**
     * 规则名称,不同规则不能重名
    */
    public byte[]           szRuleName = new byte[128];
    /**
     * 规则使能
    */
    public byte             bRuleEnable;
    /**
     * 触发跟踪使能,仅对绊线，入侵规则有效
    */
    public byte             bTrackEnable;
    /**
     * 保留字段
    */
    public byte[]           bReserved = new byte[2];
    /**
     * 相应物体类型个数
    */
    public int              nObjectTypeNum;
    /**
     * 相应物体类型列表
    */
    public BYTE_ARRAY_128[] szObjectTypes = new BYTE_ARRAY_128[16];
    /**
     * 最短持续时间	单位：秒，0~65535
    */
    public int              nMinDuration;
    /**
     * 检测区域顶点数
    */
    public int              nDetectRegionPoint;
    /**
     * 检测区域,参见结构体定义 {@link com.netsdk.lib.NetSDKLib.CFG_POLYGON}
    */
    public NetSDKLib.CFG_POLYGON[] stuDetectRegion = new NetSDKLib.CFG_POLYGON[20];
    /**
     * 报警联动,参见结构体定义 {@link com.netsdk.lib.NetSDKLib.CFG_ALARM_MSG_HANDLE}
    */
    public NetSDKLib.CFG_ALARM_MSG_HANDLE stuEventHandler = new NetSDKLib.CFG_ALARM_MSG_HANDLE();
    /**
     * 事件响应时间段,参见结构体定义 {@link com.netsdk.lib.structure.CFG_TIME_SECTION}
    */
    public CFG_TIME_SECTION_ARRAY_10[] stuTimeSection = new CFG_TIME_SECTION_ARRAY_10[7];
    /**
     * 云台预置点编号	0~65535
    */
    public int              nPtzPresetId;
    /**
     * 触发报警位置数
    */
    public int              nTriggerPosition;
    /**
     * 触发报警位置,0-目标外接框中心, 1-目标外接框左端中心, 2-目标外接框顶端中心, 3-目标外接框右端中心, 4-目标外接框底端中心
    */
    public byte[]           bTriggerPosition = new byte[8];
    /**
     * 规则特定的尺寸过滤器是否有效
    */
    public int              bSizeFileter;
    /**
     * 规则特定的尺寸过滤器,参见结构体定义 {@link com.netsdk.lib.NetSDKLib.CFG_SIZEFILTER_INFO}
    */
    public NetSDKLib.CFG_SIZEFILTER_INFO stuSizeFileter = new NetSDKLib.CFG_SIZEFILTER_INFO();
    /**
     * 跟踪持续时间,0秒:一直跟踪,1~300秒:跟踪持续时间
    */
    public int              nTrackDuration;
    /**
     * 报告时间间隔，单位：秒 0~600;等于0表示不重复报警, 默认值30
    */
    public int              nReportInterval;

    public CFG_LEFT_INFO() {
        for(int i = 0; i < szObjectTypes.length; i++){
            szObjectTypes[i] = new BYTE_ARRAY_128();
        }
        for(int i = 0; i < stuDetectRegion.length; i++){
            stuDetectRegion[i] = new NetSDKLib.CFG_POLYGON();
        }
        for(int i = 0; i < stuTimeSection.length; i++){
            stuTimeSection[i] = new CFG_TIME_SECTION_ARRAY_10();
        }
    }
}

