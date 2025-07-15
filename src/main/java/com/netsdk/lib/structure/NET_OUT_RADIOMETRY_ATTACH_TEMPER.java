package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;
/**
 * CLIENT_RadiometryAttachTemper 出参
*/
public class NET_OUT_RADIOMETRY_ATTACH_TEMPER extends NetSDKLib.SdkStructure
{
    public int              dwSize;

    public NET_OUT_RADIOMETRY_ATTACH_TEMPER() {
        this.dwSize = this.size();
    }
}

