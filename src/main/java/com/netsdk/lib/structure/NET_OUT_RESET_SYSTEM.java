package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;
/**
 * 恢复出厂设置出参
*/
public class NET_OUT_RESET_SYSTEM extends NetSDKLib.SdkStructure
{
    public int              dwSize;

    public NET_OUT_RESET_SYSTEM() {
        this.dwSize = this.size();
    }
}

