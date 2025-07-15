package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;
/**
 * CLIENT_GetFaceRecognitionAppendToken 接口输出参数
*/
public class NET_OUT_GET_FACE_RECOGNITION_APPEND_TOKEN extends NetSDKLib.SdkStructure
{
    /**
     * 结构体大小
    */
    public int              dwSize;
    /**
     * 导入的令牌
    */
    public int              nToken;

    public NET_OUT_GET_FACE_RECOGNITION_APPEND_TOKEN() {
        this.dwSize = this.size();
    }
}

