package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;
/**
 * 烟火检测查询条件 ( CLIENT_FindFileEx + DH_FILE_QUERY_SMOKE_FIRE )
*/
public class MEDIAFILE_SMOKE_FIRE_PARAM extends NetSDKLib.SdkStructure
{
    /**
     * 结构体大小
    */
    public int              dwSize;
    /**
     * 文件类型,0:查询任意类型,1:查询jpg图片,2:查询dav
    */
    public int              nFileType;
    /**
     * 通道号从0开始,-1表示查询所有通道
    */
    public int              nChannelID;
    /**
     * 开始时间,参见结构体定义 {@link com.netsdk.lib.NetSDKLib.NET_TIME}
    */
    public NetSDKLib.NET_TIME stuBeginTime = new NetSDKLib.NET_TIME();
    /**
     * 结束时间,参见结构体定义 {@link com.netsdk.lib.NetSDKLib.NET_TIME}
    */
    public NetSDKLib.NET_TIME stuEndTime = new NetSDKLib.NET_TIME();
    /**
     * 事件类型列表,参见智能分析事件类型，如EVENT_IVS_ALL、EVENT_IVS_SMOKEDETECTION
    */
    public int[]            nEventList = new int[256];
    /**
     * 事件总数
    */
    public int              nEventCount;
    /**
     * 过滤规则,参见结构体定义 {@link com.netsdk.lib.structure.NET_SMOKE_FIRE_FILTER}
    */
    public NET_SMOKE_FIRE_FILTER stuFilter = new NET_SMOKE_FIRE_FILTER();
    /**
     * 为TRUE表示仅下发stuStartTimeRealUTC和stuEndTimeRealUTC(不下发stuBeginTime, stuEndTime), 为FALSE表示仅下发stuBeginTime, stuEndTime(不下发stuStartTimeRealUTC和stuEndTimeRealUTC)
    */
    public int              bOnlySupportRealUTC;
    /**
     * UTC开始时间(标准UTC时间), 与stuEndTimeRealUTC配对使用，与(stuBeginTime, stuEndTime)互斥,参见结构体定义 {@link com.netsdk.lib.NetSDKLib.NET_TIME}
    */
    public NetSDKLib.NET_TIME stuStartTimeRealUTC = new NetSDKLib.NET_TIME();
    /**
     * UTC结束时间(标准UTC时间), 与stuStartTimeRealUTC配对使用，与(stuBeginTime, stuEndTime)互斥,参见结构体定义 {@link com.netsdk.lib.NetSDKLib.NET_TIME}
    */
    public NetSDKLib.NET_TIME stuEndTimeRealUTC = new NetSDKLib.NET_TIME();

    public MEDIAFILE_SMOKE_FIRE_PARAM() {
        this.dwSize = this.size();
    }
}

