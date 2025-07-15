package com.netsdk.lib;

public class ToolKitEx {

    // 工具方法：将yyyy-MM-dd HH:mm:ss字符串转为NET_TIME
    public static NetSDKLib.NET_TIME parseDateTime(String dateTime) {
        String[] parts = dateTime.split(" ");
        String[] date = parts[0].split("-");
        String[] time = parts[1].split(":");
        NetSDKLib.NET_TIME t = new NetSDKLib.NET_TIME();
        t.dwYear = Integer.parseInt(date[0]);
        t.dwMonth = Integer.parseInt(date[1]);
        t.dwDay = Integer.parseInt(date[2]);
        t.dwHour = Integer.parseInt(time[0]);
        t.dwMinute = Integer.parseInt(time[1]);
        t.dwSecond = Integer.parseInt(time[2]);
        return t;
    }
}
