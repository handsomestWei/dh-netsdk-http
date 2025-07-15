package com.netsdk.lib.structure;

import com.netsdk.lib.NetSDKLib;

/**
 * @author 251823
 * @description CLIENT_StartQueryLog 输出参数
 * @date 2023/06/12 09:21:42
 */
public class NET_OUT_START_QUERYLOG extends NetSDKLib.SdkStructure {
    public int              dwSize;

	public NET_OUT_START_QUERYLOG() {
		this.dwSize = this.size();
	}
}

