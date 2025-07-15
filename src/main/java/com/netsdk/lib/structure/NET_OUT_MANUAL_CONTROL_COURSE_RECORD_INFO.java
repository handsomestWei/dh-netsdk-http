package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;
/**
 * CLIENT_ManualControlCourseRecord 接口输出参数
*/
public class NET_OUT_MANUAL_CONTROL_COURSE_RECORD_INFO extends NetSDKLib.SdkStructure
{
    /**
     * 此结构体大小,必须赋值
    */
    public int              dwSize;
    /**
     * 错误码枚举,参见枚举定义 {@link com.netsdk.lib.enumeration.EM_MANUAL_CONTROL_ERRORCODE_TYPE}
    */
    public int              emErrorCode;

    public NET_OUT_MANUAL_CONTROL_COURSE_RECORD_INFO() {
        this.dwSize = this.size();
    }
}

