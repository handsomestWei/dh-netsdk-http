package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;
/**
 * CLIENT_StopAudioRecordManagerChannel 接口输出参数
*/
public class NET_OUT_STOP_AUDIO_RECORD_MANAGER_CHANNEL extends NetSDKLib.SdkStructure
{
    /**
     * 此结构体大小,必须赋值
    */
    public int              dwSize;

    public NET_OUT_STOP_AUDIO_RECORD_MANAGER_CHANNEL() {
        this.dwSize = this.size();
    }
}

