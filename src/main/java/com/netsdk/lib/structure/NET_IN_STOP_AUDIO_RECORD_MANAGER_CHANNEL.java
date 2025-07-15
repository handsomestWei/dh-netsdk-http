package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;
/**
 * CLIENT_StopAudioRecordManagerChannel 接口输入参数
*/
public class NET_IN_STOP_AUDIO_RECORD_MANAGER_CHANNEL extends NetSDKLib.SdkStructure
{
    /**
     * 此结构体大小,必须赋值
    */
    public int              dwSize;
    /**
     * 音频通道号
    */
    public int              nChannel;
    /**
     * 码流类型,视频伴音的音频类型:"Main","Extra1",Extra2" "Extra3";Mic的音频类型："TalkBack"
    */
    public byte[]           szStream = new byte[16];

    public NET_IN_STOP_AUDIO_RECORD_MANAGER_CHANNEL() {
        this.dwSize = this.size();
    }
}

