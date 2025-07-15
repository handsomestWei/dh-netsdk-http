package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;
/**
 * 恢复出厂设置入参
*/
public class NET_IN_RESET_SYSTEM extends NetSDKLib.SdkStructure
{
    public int              dwSize;

    public NET_IN_RESET_SYSTEM() {
        this.dwSize = this.size();
    }
}

