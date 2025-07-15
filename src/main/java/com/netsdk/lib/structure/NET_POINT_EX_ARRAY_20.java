package com.netsdk.lib.structure;
import com.netsdk.lib.NetSDKLib;

public class NET_POINT_EX_ARRAY_20 extends NetSDKLib.SdkStructure {
    /**
     * 参见结构体定义 {@link com.netsdk.lib.structure.NET_POINT_EX}
    */
    public NET_POINT_EX[] obj_20 = new NET_POINT_EX[20];

    public NET_POINT_EX_ARRAY_20() {
        for(int i = 0; i < obj_20.length; i++){
            obj_20[i] = new NET_POINT_EX();
        }
    }
}

