package com.netsdk.lib.structure;

import com.netsdk.lib.NetSDKLib;

/**
 * 门禁能力集的结构体
 */
public class NET_CFG_CAP_ACCESSCONTROL extends NetSDKLib.SdkStructure {
    /**
     * 门禁组数
     */
    public int              nAccessGroups;
    /**
     * 是否支持门禁报警日志记录在记录集中
     */
    public int              bSupAccessControlAlarmRecord;
    /**
     * AccessControlCustomPassword记录集中密码的保存方式
     */
    public int              emCustomPasswordEncryption;
    /**
     *是否支持信息功能
     */
    public int              emSupportFingerPrint;
    /**
     * 是否只支持单门授权(发卡)
     */
    public int              bOnlySingleDoorAuth;
    /**
     * 是否支持授权异步返回
     */
    public int              bAsynAuth;
    /**
     * 假日计划
     */
    public NET_SPECIAL_DAYS_SCHEDULE stSpecialDaysSchedule;
    /**
     * 是否支持多人多类型组合开门
     */
    public int              bSupportMultiUserMultiTypeAuth;
    /**
     * 是否支持门禁快速导入功能
     */
    public int              bSupportFastImport;
    /**
     * 是否支持门禁快速复核功能
     */
    public int              bSupportFastCheck;
    /**
     * 是否支持梯控功能
     */
    public int              bSupportCallLift;
}

