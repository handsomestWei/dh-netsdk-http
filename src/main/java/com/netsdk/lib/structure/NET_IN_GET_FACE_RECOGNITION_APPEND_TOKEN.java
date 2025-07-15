package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;
/**
 * CLIENT_GetFaceRecognitionAppendToken 接口输入参数
*/
public class NET_IN_GET_FACE_RECOGNITION_APPEND_TOKEN extends NetSDKLib.SdkStructure
{
    /**
     * 结构体大小
    */
    public int              dwSize;

    public NET_IN_GET_FACE_RECOGNITION_APPEND_TOKEN() {
        this.dwSize = this.size();
    }
}

