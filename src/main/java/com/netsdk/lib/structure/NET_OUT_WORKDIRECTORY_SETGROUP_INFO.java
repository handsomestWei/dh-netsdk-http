package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;
/**
 * 设置工作目录组名 出参
*/
public class NET_OUT_WORKDIRECTORY_SETGROUP_INFO extends NetSDKLib.SdkStructure
{
    public int              dwSize;

    public NET_OUT_WORKDIRECTORY_SETGROUP_INFO() {
        this.dwSize = this.size();
    }
}

