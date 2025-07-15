package com.netsdk.demo.module.ext;

import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;
import com.sun.jna.Memory;
import com.netsdk.demo.module.LoginModule;
import com.netsdk.lib.NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD;
import com.netsdk.lib.NetSDKLib.NET_CTRL_RECORDSET_INSERT_PARAM;
import com.netsdk.lib.NetSDKLib.NET_IN_FIND_NEXT_RECORD_PARAM;
import com.netsdk.lib.NetSDKLib.NET_OUT_FIND_NEXT_RECORD_PARAM;
import com.netsdk.lib.NetSDKLib.EM_NET_RECORD_TYPE;
import com.netsdk.lib.NetSDKLib.CtrlType;
import com.netsdk.lib.NetSDKLib.LLong;
import java.io.UnsupportedEncodingException;
import java.util.List;

/*
 * 门禁模块扩展
 */
public class GateExtModule {

    /**
     * 添加卡（支持多门通道）
     * @param cardNo   卡号
     * @param userId   用户ID
     * @param cardName 卡名
     * @param cardPwd  卡密码
     * @param cardStatus 卡状态
     * @param cardType   卡类型
     * @param useTimes   使用次数
     * @param isFirstEnter 是否首卡, 1-true, 0-false
     * @param isValid      是否有效, 1-true, 0-false
     * @param startValidTime  有效开始时间
     * @param endValidTime    有效结束时间
     * @param doorIds   门通道ID数组（可多个）
     * @param loginHandle 登录句柄
     * @return true:成功   false:失败
     */
    public static boolean insertCard(NetSDKLib.LLong loginHandle, String cardNo, String userId, String cardName, String cardPwd,
                                     int cardStatus, int cardType, int useTimes, int isFirstEnter,
                                     int isValid, String startValidTime, String endValidTime, int[] doorIds) {
        NET_RECORDSET_ACCESS_CTL_CARD accessCardInfo = new NET_RECORDSET_ACCESS_CTL_CARD();
        // 卡号
        System.arraycopy(cardNo.getBytes(), 0, accessCardInfo.szCardNo, 0, cardNo.getBytes().length);
        // 用户ID
        System.arraycopy(userId.getBytes(), 0, accessCardInfo.szUserID, 0, userId.getBytes().length);
        // 卡名
        try {
            System.arraycopy(cardName.getBytes("GBK"), 0, accessCardInfo.szCardName, 0, cardName.getBytes("GBK").length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 卡密码
        System.arraycopy(cardPwd.getBytes(), 0, accessCardInfo.szPsw, 0, cardPwd.getBytes().length);
        // 设置开门权限
        if (doorIds != null && doorIds.length > 0) {
            for (int i = 0; i < doorIds.length; i++) {
                accessCardInfo.sznDoors[i] = doorIds[i];
                accessCardInfo.sznTimeSectionNo[i] = 255; // 全天有效
            }
            accessCardInfo.nDoorNum = doorIds.length;
            accessCardInfo.nTimeSectionNum = doorIds.length;
        } else {
            accessCardInfo.nDoorNum = 0;
            accessCardInfo.nTimeSectionNum = 0;
        }
        // 卡状态
        accessCardInfo.emStatus = cardStatus;
        // 卡类型
        accessCardInfo.emType = cardType;
        // 使用次数
        accessCardInfo.nUserTime = useTimes;
        // 是否首卡
        accessCardInfo.bFirstEnter = isFirstEnter;
        // 是否有效。注意该值可能不准确，需要根据设备实际配置情况判断
        accessCardInfo.bIsValid = isValid;
        // 有效开始时间
        String[] startTimes = startValidTime.split(" ");
        accessCardInfo.stuValidStartTime.dwYear = Integer.parseInt(startTimes[0].split("-")[0]);
        accessCardInfo.stuValidStartTime.dwMonth = Integer.parseInt(startTimes[0].split("-")[1]);
        accessCardInfo.stuValidStartTime.dwDay = Integer.parseInt(startTimes[0].split("-")[2]);
        accessCardInfo.stuValidStartTime.dwHour = Integer.parseInt(startTimes[1].split(":")[0]);
        accessCardInfo.stuValidStartTime.dwMinute = Integer.parseInt(startTimes[1].split(":")[1]);
        accessCardInfo.stuValidStartTime.dwSecond = Integer.parseInt(startTimes[1].split(":")[2]);
        // 有效结束时间
        String[] endTimes = endValidTime.split(" ");
        accessCardInfo.stuValidEndTime.dwYear = Integer.parseInt(endTimes[0].split("-")[0]);
        accessCardInfo.stuValidEndTime.dwMonth = Integer.parseInt(endTimes[0].split("-")[1]);
        accessCardInfo.stuValidEndTime.dwDay = Integer.parseInt(endTimes[0].split("-")[2]);
        accessCardInfo.stuValidEndTime.dwHour = Integer.parseInt(endTimes[1].split(":")[0]);
        accessCardInfo.stuValidEndTime.dwMinute = Integer.parseInt(endTimes[1].split(":")[1]);
        accessCardInfo.stuValidEndTime.dwSecond = Integer.parseInt(endTimes[1].split(":")[2]);
        // 记录集操作
        NET_CTRL_RECORDSET_INSERT_PARAM insert = new NET_CTRL_RECORDSET_INSERT_PARAM();
        insert.stuCtrlRecordSetInfo.emType = EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARD;
        insert.stuCtrlRecordSetInfo.pBuf = accessCardInfo.getPointer();
        accessCardInfo.write();
        insert.write();
        boolean bRet = LoginModule.netsdk.CLIENT_ControlDevice(loginHandle,
                CtrlType.CTRLTYPE_CTRL_RECORDSET_INSERT, insert.getPointer(), 5000);
        insert.read();
        accessCardInfo.read();
        if (!bRet) {
            System.err.println("添加卡信息失败." + ToolKits.getErrorCodePrint());
            return false;
        } else {
            System.out.println("添加卡信息成功,卡信息记录集编号 : " + insert.stuCtrlRecordSetResult.nRecNo);
        }
        return true;
    }

    /**
     * 新增卡片，无需再传门禁通道和有效期，在人员侧已经绑定，只需关联人员id即可。需要先添加人员，且人员要先关联门通道和人员有效期
     * @param cardNo   卡号
     * @param userId   用户ID
     * @param cardType 卡类型
     * @param loginHandle 登录句柄
     * @return true:成功   false:失败
     */
    public static boolean insertCardByService(NetSDKLib.LLong loginHandle, String cardNo, String userId, int cardType) {
        NetSDKLib.NET_ACCESS_CARD_INFO cardInfo = new NetSDKLib.NET_ACCESS_CARD_INFO();
        System.arraycopy(cardNo.getBytes(), 0, cardInfo.szCardNo, 0, Math.min(cardNo.getBytes().length, cardInfo.szCardNo.length));
        System.arraycopy(userId.getBytes(), 0, cardInfo.szUserID, 0, Math.min(userId.getBytes().length, cardInfo.szUserID.length));
        cardInfo.emType = cardType;

        // 构造入参
        NetSDKLib.NET_IN_ACCESS_CARD_SERVICE_INSERT inParam = new NetSDKLib.NET_IN_ACCESS_CARD_SERVICE_INSERT();
        inParam.nInfoNum = 1;
        com.sun.jna.Memory mem = new com.sun.jna.Memory(cardInfo.size());
        cardInfo.write();
        mem.write(0, cardInfo.getPointer().getByteArray(0, cardInfo.size()), 0, cardInfo.size());
        inParam.pCardInfo = mem;
        inParam.write();

        // 构造出参
        NetSDKLib.NET_OUT_ACCESS_CARD_SERVICE_INSERT outParam = new NetSDKLib.NET_OUT_ACCESS_CARD_SERVICE_INSERT();
        outParam.nMaxRetNum = inParam.nInfoNum;
        NetSDKLib.FAIL_CODE[] failCodes = (NetSDKLib.FAIL_CODE[]) new NetSDKLib.FAIL_CODE().toArray(inParam.nInfoNum);
        outParam.pFailCode = failCodes[0].getPointer();
        for (int i = 0; i < inParam.nInfoNum; i++) {
            failCodes[i].write();
        }
        outParam.write();

        boolean ret = LoginModule.netsdk.CLIENT_OperateAccessCardService(
            loginHandle,
            NetSDKLib.NET_EM_ACCESS_CTL_CARD_SERVICE.NET_EM_ACCESS_CTL_CARD_SERVICE_INSERT,
            inParam.getPointer(),
            outParam.getPointer(),
            5000
        );
        outParam.read();
        if (!ret) {
            System.err.println("添加卡片失败." + ToolKits.getErrorCodePrint());
            return false;
        } else {
            System.out.println("添加卡片成功");
        }
        return true;
    }

    /**
     * 查询卡信息，支持按卡号和用户ID查询，返回卡片列表，线程安全
     * @param cardNo 卡号，为空查询所有。注意某些设备卡号长度默认6位，需要前补0对齐，否则查不到
     * @param userId 用户ID，为空不作为条件
     * @param maxCount 最大查询条数
     * @param loginHandle 登录句柄
     * @return List<NET_RECORDSET_ACCESS_CTL_CARD> 查询到的卡片信息
     */
    public static java.util.List<NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD> findCardList(NetSDKLib.LLong loginHandle, String cardNo, String userId, int maxCount) {
        java.util.List<NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD> resultList = new java.util.ArrayList<>();
        NetSDKLib.FIND_RECORD_ACCESSCTLCARD_CONDITION findCondition = new NetSDKLib.FIND_RECORD_ACCESSCTLCARD_CONDITION();
        boolean precise = false;
        if (cardNo != null && !cardNo.isEmpty()) {
            findCondition.abCardNo = 1;
            System.arraycopy(cardNo.getBytes(), 0, findCondition.szCardNo, 0, cardNo.getBytes().length);
            precise = true;
        }
        if (userId != null && !userId.isEmpty()) {
            findCondition.abUserID = 1;
            System.arraycopy(userId.getBytes(), 0, findCondition.szUserID, 0, userId.getBytes().length);
            precise = true;
        }
        if (precise) {
            maxCount = 1;
        }
        NetSDKLib.NET_IN_FIND_RECORD_PARAM stIn = new NetSDKLib.NET_IN_FIND_RECORD_PARAM();
        stIn.emType = NetSDKLib.EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARD;
        if ((cardNo != null && !cardNo.isEmpty()) || (userId != null && !userId.isEmpty())) {
            stIn.pQueryCondition = findCondition.getPointer();
        }
        NetSDKLib.NET_OUT_FIND_RECORD_PARAM stOut = new NetSDKLib.NET_OUT_FIND_RECORD_PARAM();
        findCondition.write();
        boolean ret = LoginModule.netsdk.CLIENT_FindRecord(loginHandle, stIn, stOut, 5000);
        findCondition.read();
        if (!ret || stOut.lFindeHandle.longValue() == 0) {
            return resultList;
        }
        final int BATCH_SIZE = 100;
        int remain = maxCount;
        try {
            while (remain > 0) {
                int batch = Math.min(BATCH_SIZE, remain);
                NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD[] pstRecord = new NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD[batch];
                for (int i = 0; i < batch; i++) {
                    pstRecord[i] = new NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD();
                }
                NetSDKLib.NET_IN_FIND_NEXT_RECORD_PARAM stNextIn = new NetSDKLib.NET_IN_FIND_NEXT_RECORD_PARAM();
                stNextIn.lFindeHandle = stOut.lFindeHandle;
                stNextIn.nFileCount = batch;
                NetSDKLib.NET_OUT_FIND_NEXT_RECORD_PARAM stNextOut = new NetSDKLib.NET_OUT_FIND_NEXT_RECORD_PARAM();
                stNextOut.nMaxRecordNum = batch;
                stNextOut.pRecordList = new com.sun.jna.Memory(pstRecord[0].dwSize * batch);
                stNextOut.pRecordList.clear(pstRecord[0].dwSize * batch);
                ToolKits.SetStructArrToPointerData(pstRecord, stNextOut.pRecordList);
                if (LoginModule.netsdk.CLIENT_FindNextRecord(stNextIn, stNextOut, 5000)) {
                    if (stNextOut.nRetRecordNum == 0) break;
                    // 只分配实际返回条数的数组，防止越界
                    NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD[] realRecords = new NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD[stNextOut.nRetRecordNum];
                    for (int i = 0; i < stNextOut.nRetRecordNum; i++) {
                        realRecords[i] = new NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD();
                    }
                    ToolKits.GetPointerDataToStructArr(stNextOut.pRecordList, realRecords);
                    for (int i = 0; i < stNextOut.nRetRecordNum; i++) {
                        resultList.add(realRecords[i]);
                    }
                    remain -= stNextOut.nRetRecordNum;
                    if (stNextOut.nRetRecordNum < batch) break;
                } else {
                    break;
                }
            }
        } finally {
            if (stOut.lFindeHandle.longValue() != 0) {
                LoginModule.netsdk.CLIENT_FindRecordClose(stOut.lFindeHandle);
            }
        }
        return resultList;
    }

    /**
     * 获取门禁通道数量。
     * 注意：某些高端设备或特殊配置下，一个门禁组下可能会有多个门，此时组数和门数就不是一一对应。作为门禁数量来用的，这在绝大多数大华门禁设备上是成立的，即"组数=门数"。
     * @param loginHandle 登录句柄
     * @return 门禁通道数量，失败返回0
     */
    public static int getAccessChannelCount(NetSDKLib.LLong loginHandle) {
        if (loginHandle == null || loginHandle.longValue() == 0) {
            return 0;
        }
        int nBufferLen = 1024;
        byte[] szBuf = new byte[nBufferLen];
        com.sun.jna.ptr.IntByReference nError = new com.sun.jna.ptr.IntByReference(0);

        boolean bRet = NetSDKLib.NETSDK_INSTANCE.CLIENT_QueryNewSystemInfo(
                loginHandle,
                com.netsdk.lib.enumeration.EM_CFG_CAP_CMD.CFG_CAP_CMD_ACCESSCONTROLMANAGER.getCmd(),
                -1,
                szBuf,
                nBufferLen,
                nError,
                3000
        );
        if (!bRet) {
            return 0;
        }

        com.netsdk.lib.structure.CFG_CAP_ACCESSCONTROL stuCap = new com.netsdk.lib.structure.CFG_CAP_ACCESSCONTROL();
        boolean bParse = NetSDKLib.CONFIG_INSTANCE.CLIENT_ParseData(
                com.netsdk.lib.enumeration.EM_CFG_CAP_CMD.CFG_CAP_CMD_ACCESSCONTROLMANAGER.getCmd(),
                szBuf,
                stuCap.getPointer(),
                stuCap.size(),
                null
        );
        if (!bParse) {
            return 0;
        }
        stuCap.read();

        return stuCap.nAccessControlGroups;
    }

    /**
     * 获取单个门禁通道信息，直接返回SDK结构体
     */
    public static NetSDKLib.CFG_ACCESS_EVENT_INFO getAccessChannelInfo(NetSDKLib.LLong loginHandle, int channel) {
        NetSDKLib.CFG_ACCESS_EVENT_INFO info = new NetSDKLib.CFG_ACCESS_EVENT_INFO();
        boolean ret = com.netsdk.lib.ToolKits.GetDevConfig(loginHandle, channel, NetSDKLib.CFG_CMD_ACCESS_EVENT, info);
        if (!ret) {
            System.err.printf("[GateExtModule] 获取通道%d信息失败, 错误: %s\n", channel, com.netsdk.lib.ToolKits.getErrorCodeShow());
            return null;
        }
        return info;
    }

    /**
     * 获取所有门禁通道信息列表，直接返回SDK结构体列表
     */
    public static java.util.List<NetSDKLib.CFG_ACCESS_EVENT_INFO> getAllAccessChannelInfo(NetSDKLib.LLong loginHandle, int channelCount) {
        java.util.List<NetSDKLib.CFG_ACCESS_EVENT_INFO> list = new java.util.ArrayList<>();
        for (int i = 0; i < channelCount; i++) {
            NetSDKLib.CFG_ACCESS_EVENT_INFO info = getAccessChannelInfo(loginHandle, i);
            if (info != null) {
                list.add(info);
            }
        }
        return list;
    }

    /**
     * 查询开门记录，带分页参数，返回结果列表
     * @param loginHandle 登录句柄
     * @param start 开始时间
     * @param end 结束时间
     * @param cardNo 卡号
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 记录列表
     */
    public static java.util.List<NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC> getOpenDoorRecords(NetSDKLib.LLong loginHandle, String start, String end, String cardNo, int pageNum, int pageSize) {
        java.util.List<NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC> outList = new java.util.ArrayList<>();
        if (loginHandle == null || loginHandle.longValue() == 0) {
            return outList;
        }
        // 1. 构造查询条件
        NetSDKLib.FIND_RECORD_ACCESSCTLCARDREC_CONDITION_EX condition = new NetSDKLib.FIND_RECORD_ACCESSCTLCARDREC_CONDITION_EX();
        condition.bTimeEnable = 1;
        condition.stStartTime = com.netsdk.lib.ToolKitEx.parseDateTime(start);
        condition.stEndTime = com.netsdk.lib.ToolKitEx.parseDateTime(end);
        if (cardNo != null && !cardNo.isEmpty()) {
            condition.bCardNoEnable = 1;
            ToolKits.StringToByteArray(cardNo.trim(), condition.szCardNo);
        }
        // 2. 入参
        NetSDKLib.NET_IN_FIND_RECORD_PARAM inParam = new NetSDKLib.NET_IN_FIND_RECORD_PARAM();
        inParam.emType = NetSDKLib.EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARDREC_EX;
        inParam.pQueryCondition = new com.sun.jna.Memory(condition.size());
        ToolKits.SetStructDataToPointer(condition, inParam.pQueryCondition, 0);
        // 3. 出参
        NetSDKLib.NET_OUT_FIND_RECORD_PARAM outParam = new NetSDKLib.NET_OUT_FIND_RECORD_PARAM();
        // 4. 获取查询句柄
        if (!LoginModule.netsdk.CLIENT_FindRecord(loginHandle, inParam, outParam, 5000)) {
            return outList;
        }
        NetSDKLib.LLong findHandle = outParam.lFindeHandle;
        try {
            java.util.List<NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC> records = getOpenDoorRecords(loginHandle, findHandle, pageNum, pageSize);
            outList.addAll(records);
            // 统一转换为UTC+8
            convertRecordsToUTC8(outList);
        } finally {
            LoginModule.netsdk.CLIENT_FindRecordClose(findHandle);
        }
        return outList;
    }

    /**
     * 分页查询开门记录，返回结果列表
     * @param loginHandle 登录句柄
     * @param findHandle 查询句柄
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页条数
     * @return 记录列表
     */
    private static java.util.List<NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC> getOpenDoorRecords(NetSDKLib.LLong loginHandle, NetSDKLib.LLong findHandle, int pageNum, int pageSize) {
        int batchSize = pageSize;
        int startIndex = (pageNum - 1) * pageSize;
        java.util.List<NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC> resultList = new java.util.ArrayList<>();
        // 跳过前面页的数据
        int skipped = 0;
        while (skipped < startIndex) {
            int skipCount = Math.min(batchSize, startIndex - skipped);
            if (skipCount <= 0) break;
            NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC[] skipRecords = new NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC[skipCount];
            for (int i = 0; i < skipCount; i++) {
                skipRecords[i] = new NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC();
            }
            com.sun.jna.Memory skipMemory = new com.sun.jna.Memory(skipRecords[0].dwSize * skipCount);
            ToolKits.SetStructArrToPointerData(skipRecords, skipMemory);
            NetSDKLib.NET_IN_FIND_NEXT_RECORD_PARAM skipIn = new NetSDKLib.NET_IN_FIND_NEXT_RECORD_PARAM();
            skipIn.lFindeHandle = findHandle;
            skipIn.nFileCount = skipCount;
            NetSDKLib.NET_OUT_FIND_NEXT_RECORD_PARAM skipOut = new NetSDKLib.NET_OUT_FIND_NEXT_RECORD_PARAM();
            skipOut.nMaxRecordNum = skipCount;
            skipOut.pRecordList = skipMemory;
            if (!LoginModule.netsdk.CLIENT_FindNextRecord(skipIn, skipOut, 5000)) {
                return resultList;
            }
            if (skipOut.nRetRecordNum == 0) return resultList;
            skipped += skipOut.nRetRecordNum;
        }
        // 取当前页数据
        NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC[] records = new NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC[batchSize];
        for (int i = 0; i < batchSize; i++) {
            records[i] = new NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC();
        }
        com.sun.jna.Memory memory = new com.sun.jna.Memory(records[0].dwSize * batchSize);
        ToolKits.SetStructArrToPointerData(records, memory);
        NetSDKLib.NET_IN_FIND_NEXT_RECORD_PARAM nextIn = new NetSDKLib.NET_IN_FIND_NEXT_RECORD_PARAM();
        nextIn.lFindeHandle = findHandle;
        nextIn.nFileCount = batchSize;
        NetSDKLib.NET_OUT_FIND_NEXT_RECORD_PARAM nextOut = new NetSDKLib.NET_OUT_FIND_NEXT_RECORD_PARAM();
        nextOut.nMaxRecordNum = batchSize;
        nextOut.pRecordList = memory;
        if (!LoginModule.netsdk.CLIENT_FindNextRecord(nextIn, nextOut, 5000)) {
            return resultList;
        }
        nextOut.read();
        if (nextOut.nRetRecordNum == 0) return resultList;
        ToolKits.GetPointerDataToStructArr(memory, records);
        for (int i = 0; i < nextOut.nRetRecordNum; i++) {
            resultList.add(records[i]);
        }
        return resultList;
    }

    // 将stuTime转为Calendar，+8小时后回填，自动处理所有进位
    private static void convertRecordsToUTC8(List<NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC> outList) {
        for (NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC rec : outList) {
            if (rec.stuTime != null) {
                try {
                    java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
                    cal.set(java.util.Calendar.YEAR, rec.stuTime.dwYear);
                    cal.set(java.util.Calendar.MONTH, rec.stuTime.dwMonth - 1); // Calendar月份从0开始
                    cal.set(java.util.Calendar.DAY_OF_MONTH, rec.stuTime.dwDay);
                    cal.set(java.util.Calendar.HOUR_OF_DAY, rec.stuTime.dwHour);
                    cal.set(java.util.Calendar.MINUTE, rec.stuTime.dwMinute);
                    cal.set(java.util.Calendar.SECOND, rec.stuTime.dwSecond);
                    cal.set(java.util.Calendar.MILLISECOND, 0);
                    // +8小时
                    cal.add(java.util.Calendar.HOUR_OF_DAY, 8);
                    // 回填
                    rec.stuTime.dwYear = cal.get(java.util.Calendar.YEAR);
                    rec.stuTime.dwMonth = cal.get(java.util.Calendar.MONTH) + 1;
                    rec.stuTime.dwDay = cal.get(java.util.Calendar.DAY_OF_MONTH);
                    rec.stuTime.dwHour = cal.get(java.util.Calendar.HOUR_OF_DAY);
                    rec.stuTime.dwMinute = cal.get(java.util.Calendar.MINUTE);
                    rec.stuTime.dwSecond = cal.get(java.util.Calendar.SECOND);
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    /**
     * 获取开门记录总记录数（先查句柄再查总数，SDK接口不支持直接条件统计）
     * @param loginHandle 登录句柄
     * @param start 开始时间
     * @param end 结束时间
     * @param cardNo 卡号
     * @return 总记录数
     */
    public static int getOpenDoorRecordCount(NetSDKLib.LLong loginHandle, String start, String end, String cardNo) {
        if (loginHandle == null || loginHandle.longValue() == 0) {
            return 0;
        }
        // 1. 构造条件结构体
        NetSDKLib.FIND_RECORD_ACCESSCTLCARDREC_CONDITION_EX condition = new NetSDKLib.FIND_RECORD_ACCESSCTLCARDREC_CONDITION_EX();
        condition.bTimeEnable = 1;
        condition.stStartTime = com.netsdk.lib.ToolKitEx.parseDateTime(start);
        condition.stEndTime = com.netsdk.lib.ToolKitEx.parseDateTime(end);
        if (cardNo != null && !cardNo.isEmpty()) {
            condition.bCardNoEnable = 1;
            ToolKits.StringToByteArray(cardNo.trim(), condition.szCardNo);
        }
        // 2. 构造查找入参
        NetSDKLib.NET_IN_FIND_RECORD_PARAM inParam = new NetSDKLib.NET_IN_FIND_RECORD_PARAM();
        inParam.emType = NetSDKLib.EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARDREC_EX;
        inParam.pQueryCondition = new com.sun.jna.Memory(condition.size());
        ToolKits.SetStructDataToPointer(condition, inParam.pQueryCondition, 0);
        // 3. 构造查找出参
        NetSDKLib.NET_OUT_FIND_RECORD_PARAM outParam = new NetSDKLib.NET_OUT_FIND_RECORD_PARAM();
        boolean findRet = LoginModule.netsdk.CLIENT_FindRecord(loginHandle, inParam, outParam, 5000);
        if (!findRet || outParam.lFindeHandle == null || outParam.lFindeHandle.longValue() == 0) {
            return 0;
        }
        // 4. 用句柄查总数
        NetSDKLib.NET_IN_QUEYT_RECORD_COUNT_PARAM countIn = new NetSDKLib.NET_IN_QUEYT_RECORD_COUNT_PARAM();
        countIn.lFindeHandle = outParam.lFindeHandle;
        NetSDKLib.NET_OUT_QUEYT_RECORD_COUNT_PARAM countOut = new NetSDKLib.NET_OUT_QUEYT_RECORD_COUNT_PARAM();
        boolean ret = LoginModule.netsdk.CLIENT_QueryRecordCount(countIn, countOut, 5000);
        int totalCount = ret ? countOut.nRecordCount : 0;
        // 5. 关闭查询句柄
        LoginModule.netsdk.CLIENT_FindRecordClose(outParam.lFindeHandle);
        return totalCount;
    }

    /**
     * 门禁控制：开门
     */
    public static boolean openDoor(NetSDKLib.LLong loginHandle, int channelNo) {
        // 获取当前门状态
        NetSDKLib.CFG_ACCESS_EVENT_INFO info = new NetSDKLib.CFG_ACCESS_EVENT_INFO();
        boolean getRet = com.netsdk.lib.ToolKits.GetDevConfig(loginHandle, channelNo, NetSDKLib.CFG_CMD_ACCESS_EVENT, info);
        if (!getRet) {
            return false;
        }
        // 如果不是正常状态，先设置为正常
        if (info.emState != NetSDKLib.CFG_ACCESS_STATE.ACCESS_STATE_NORMAL) {
            info.emState = NetSDKLib.CFG_ACCESS_STATE.ACCESS_STATE_NORMAL;
            boolean setRet = com.netsdk.lib.ToolKits.SetDevConfig(loginHandle, channelNo, NetSDKLib.CFG_CMD_ACCESS_EVENT, info);
            if (!setRet) {
                return false;
            }
        }
        // 执行开门
        NetSDKLib.NET_CTRL_ACCESS_OPEN openParam = new NetSDKLib.NET_CTRL_ACCESS_OPEN();
        openParam.nChannelID = channelNo;
        openParam.write();
        boolean ret = LoginModule.netsdk.CLIENT_ControlDevice(
                loginHandle,
                NetSDKLib.CtrlType.CTRLTYPE_CTRL_ACCESS_OPEN,
                openParam.getPointer(),
                3000
        );
        return ret;
    }

    /**
     * 门禁控制：关门
     */
    public static boolean closeDoor(NetSDKLib.LLong loginHandle, int channelNo) {
        NetSDKLib.CFG_ACCESS_EVENT_INFO info = new NetSDKLib.CFG_ACCESS_EVENT_INFO();
        boolean getRet = com.netsdk.lib.ToolKits.GetDevConfig(loginHandle, channelNo, NetSDKLib.CFG_CMD_ACCESS_EVENT, info);
        if (!getRet) {
            return false;
        }
        if (info.emState != NetSDKLib.CFG_ACCESS_STATE.ACCESS_STATE_NORMAL) {
            info.emState = NetSDKLib.CFG_ACCESS_STATE.ACCESS_STATE_NORMAL;
            boolean setRet = com.netsdk.lib.ToolKits.SetDevConfig(loginHandle, channelNo, NetSDKLib.CFG_CMD_ACCESS_EVENT, info);
            if (!setRet) {
                return false;
            }
        }
        NetSDKLib.NET_CTRL_ACCESS_CLOSE closeParam = new NetSDKLib.NET_CTRL_ACCESS_CLOSE();
        closeParam.nChannelID = channelNo;
        closeParam.write();
        boolean ret = LoginModule.netsdk.CLIENT_ControlDevice(
                loginHandle,
                NetSDKLib.CtrlType.CTRLTYPE_CTRL_ACCESS_CLOSE,
                closeParam.getPointer(),
                3000
        );
        return ret;
    }

    /**
     * 门禁控制：常开
     */
    public static boolean alwaysOpenDoor(NetSDKLib.LLong loginHandle, int channelNo) {
        NetSDKLib.CFG_ACCESS_EVENT_INFO info = new NetSDKLib.CFG_ACCESS_EVENT_INFO();
        boolean getRet = com.netsdk.lib.ToolKits.GetDevConfig(loginHandle, channelNo, NetSDKLib.CFG_CMD_ACCESS_EVENT, info);
        if (!getRet) {
            return false;
        }
        info.emState = NetSDKLib.CFG_ACCESS_STATE.ACCESS_STATE_OPENALWAYS;
        boolean setRet = com.netsdk.lib.ToolKits.SetDevConfig(loginHandle, channelNo, NetSDKLib.CFG_CMD_ACCESS_EVENT, info);
        return setRet;
    }

    /**
     * 门禁控制：常闭
     */
    public static boolean alwaysCloseDoor(NetSDKLib.LLong loginHandle, int channelNo) {
        NetSDKLib.CFG_ACCESS_EVENT_INFO info = new NetSDKLib.CFG_ACCESS_EVENT_INFO();
        boolean getRet = com.netsdk.lib.ToolKits.GetDevConfig(loginHandle, channelNo, NetSDKLib.CFG_CMD_ACCESS_EVENT, info);
        if (!getRet) {
            return false;
        }
        info.emState = NetSDKLib.CFG_ACCESS_STATE.ACCESS_STATE_CLOSEALWAYS;
        boolean setRet = com.netsdk.lib.ToolKits.SetDevConfig(loginHandle, channelNo, NetSDKLib.CFG_CMD_ACCESS_EVENT, info);
        return setRet;
    }

    /**
     * 新增或修改用户
     * @param loginHandle 登录句柄
     * @param userInfo 用户信息结构体
     * @return true:成功 false:失败
     */
    public static boolean addOrUpdateUser(NetSDKLib.LLong loginHandle, NetSDKLib.NET_ACCESS_USER_INFO userInfo) {
        NetSDKLib.NET_IN_ACCESS_USER_SERVICE_INSERT inParam = new NetSDKLib.NET_IN_ACCESS_USER_SERVICE_INSERT();
        NetSDKLib.NET_OUT_ACCESS_USER_SERVICE_INSERT outParam = new NetSDKLib.NET_OUT_ACCESS_USER_SERVICE_INSERT();
        inParam.dwSize = inParam.size();
        outParam.dwSize = outParam.size();
        inParam.nInfoNum = 1;
        int userInfoSize = userInfo.size();
        com.sun.jna.Memory mem = new com.sun.jna.Memory(userInfoSize);
        ToolKits.SetStructDataToPointer(userInfo, mem, 0);
        inParam.pUserInfo = mem;
        inParam.write();
        // 注意：sdk有坑，这里必须设置outParam相关字段值，否则接口调用会提示(0x80000000|7) 用户参数不合法
        outParam.nMaxRetNum = inParam.nInfoNum;
        NetSDKLib.FAIL_CODE[] failCodes = (NetSDKLib.FAIL_CODE[]) new NetSDKLib.FAIL_CODE().toArray(inParam.nInfoNum);
        outParam.pFailCode = failCodes[0].getPointer();
        for (int i = 0; i < inParam.nInfoNum; i++) {
            failCodes[i].write();
        }
        outParam.write();
        boolean ret = LoginModule.netsdk.CLIENT_OperateAccessUserService(loginHandle, NetSDKLib.NET_EM_ACCESS_CTL_USER_SERVICE.NET_EM_ACCESS_CTL_USER_SERVICE_INSERT, inParam.getPointer(), outParam.getPointer(), 5000);
        return ret;
    }

    /**
     * 删除用户
     * @param loginHandle 登录句柄
     * @param szUserID 用户ID
     * @return true:成功 false:失败
     */
    public static boolean deleteUser(NetSDKLib.LLong loginHandle,  byte[] szUserID) {
        NetSDKLib.NET_IN_ACCESS_USER_SERVICE_REMOVE inParam = new NetSDKLib.NET_IN_ACCESS_USER_SERVICE_REMOVE();
        NetSDKLib.NET_OUT_ACCESS_USER_SERVICE_REMOVE outParam = new NetSDKLib.NET_OUT_ACCESS_USER_SERVICE_REMOVE();
        inParam.nUserNum = 1;
        inParam.szUserID[0].szUserID = szUserID;
        inParam.szUserID[0].write();
        inParam.write();
        outParam.nMaxRetNum = inParam.nUserNum;
        NetSDKLib.FAIL_CODE[] failCodes = (NetSDKLib.FAIL_CODE[]) new NetSDKLib.FAIL_CODE().toArray(inParam.nUserNum);
        outParam.pFailCode = failCodes[0].getPointer();
        for (int i = 0; i < inParam.nUserNum; i++) {
            failCodes[i].write();
        }
        outParam.write();
        boolean ret = LoginModule.netsdk.CLIENT_OperateAccessUserService(loginHandle, NetSDKLib.NET_EM_ACCESS_CTL_USER_SERVICE.NET_EM_ACCESS_CTL_USER_SERVICE_REMOVE, inParam.getPointer(), outParam.getPointer(), 5000);
        return ret;
    }

    /**
     * 清空所有用户
     * @param loginHandle 登录句柄
     * @return true:成功 false:失败
     */
    public static boolean clearAllUsers(NetSDKLib.LLong loginHandle) {
        NetSDKLib.NET_IN_ACCESS_USER_SERVICE_CLEAR inParam = new NetSDKLib.NET_IN_ACCESS_USER_SERVICE_CLEAR();
        NetSDKLib.NET_OUT_ACCESS_USER_SERVICE_CLEAR outParam = new NetSDKLib.NET_OUT_ACCESS_USER_SERVICE_CLEAR();
        inParam.dwSize = inParam.size();
        outParam.dwSize = outParam.size();
        inParam.write();
        outParam.write();
        boolean ret = LoginModule.netsdk.CLIENT_OperateAccessUserService(
            loginHandle,
            NetSDKLib.NET_EM_ACCESS_CTL_USER_SERVICE.NET_EM_ACCESS_CTL_USER_SERVICE_CLEAR,
            inParam.getPointer(),
            outParam.getPointer(),
            5000
        );
        return ret;
    }

    /**
     * 查询用户信息列表
     * @param loginHandle 登录句柄
     * @param userId 用户ID，可为空
     * @param maxCount 最大条数
     * @return 用户信息列表
     */
    public static java.util.List<NetSDKLib.NET_ACCESS_USER_INFO> getUserRecords(NetSDKLib.LLong loginHandle, String userId, int maxCount) {
        java.util.List<NetSDKLib.NET_ACCESS_USER_INFO> userList = new java.util.ArrayList<>();
        if (loginHandle == null || loginHandle.longValue() == 0) {
            throw new IllegalStateException("请先登录设备");
        }
        NetSDKLib.NET_IN_USERINFO_START_FIND inStart = new NetSDKLib.NET_IN_USERINFO_START_FIND();
        if (userId != null && !userId.isEmpty()) {
            com.netsdk.lib.ToolKits.StringToByteArray(userId, inStart.szUserID);
        }
        NetSDKLib.NET_OUT_USERINFO_START_FIND outStart = new NetSDKLib.NET_OUT_USERINFO_START_FIND();
        NetSDKLib.LLong findHandle = com.netsdk.demo.module.LoginModule.netsdk.CLIENT_StartFindUserInfo(loginHandle, inStart, outStart, 5000);
        if (findHandle == null || findHandle.longValue() == 0) {
            throw new RuntimeException("开始查询失败");
        }
        try {
            int totalCount = outStart.nTotalCount;
            final int BATCH_SIZE = 100;
            int fetched = 0;
            int remain = (maxCount > 0 && maxCount < totalCount) ? maxCount : totalCount;
            while (fetched < totalCount && remain > 0) {
                int thisBatch = Math.min(BATCH_SIZE, Math.min(totalCount - fetched, remain));
                NetSDKLib.NET_IN_USERINFO_DO_FIND inDo = new NetSDKLib.NET_IN_USERINFO_DO_FIND();
                inDo.nStartNo = fetched;
                inDo.nCount = thisBatch;
                NetSDKLib.NET_OUT_USERINFO_DO_FIND outDo = new NetSDKLib.NET_OUT_USERINFO_DO_FIND();
                outDo.nMaxNum = thisBatch;
                int userInfoSize = new NetSDKLib.NET_ACCESS_USER_INFO().size();
                // 注意有坑：
                // JNA 的 .size() 只根据 Java 结构体推算，但如果结构体有指针字段（如 Pointer），JNA 只分配指针本身的大小，不会递归分配指针指向的内容。
                // C 端如果期望 NET_ACCESS_USER_INFO 是“全内存展开”，而你只分配了指针本身，native 端访问指针内容时会发生“Invalid memory access”
                // 所有指针字段必须置为 NULL，不能让 native 端去解引用
                com.sun.jna.Memory mem = new com.sun.jna.Memory(userInfoSize * thisBatch);
                mem.clear(); // 清零，确保所有指针字段为0
                outDo.pstuInfo = mem;
                inDo.write();
                outDo.write();
                boolean ret = com.netsdk.demo.module.LoginModule.netsdk.CLIENT_DoFindUserInfo(findHandle, inDo, outDo, 5000);
                outDo.read();
                if (ret && outDo.nRetNum > 0) {
                    NetSDKLib.NET_ACCESS_USER_INFO[] users = new NetSDKLib.NET_ACCESS_USER_INFO[thisBatch];
                    for (int i = 0; i < thisBatch; i++) {
                        users[i] = new NetSDKLib.NET_ACCESS_USER_INFO();
                    }
                    com.netsdk.lib.ToolKits.GetPointerDataToStructArr(mem, users);
                    for (int i = 0; i < outDo.nRetNum; i++) {
                        userList.add(users[i]);
                    }
                    if (outDo.nRetNum < thisBatch) break;
                    fetched += outDo.nRetNum;
                    remain -= outDo.nRetNum;
                } else {
                    break;
                }
            }
            return userList;
        } finally {
            com.netsdk.demo.module.LoginModule.netsdk.CLIENT_StopFindUserInfo(findHandle);
        }
    }

    /**
     * 查询用户总数
     * @param loginHandle 登录句柄
     * @param userId 用户ID，可为空
     * @return 总数
     */
    public static int getUserRecordsCount(NetSDKLib.LLong loginHandle, String userId) {
        if (loginHandle == null || loginHandle.longValue() == 0) {
            return 0;
        }
        NetSDKLib.NET_IN_USERINFO_START_FIND inStart = new NetSDKLib.NET_IN_USERINFO_START_FIND();
        if (userId != null && !userId.isEmpty()) {
            com.netsdk.lib.ToolKits.StringToByteArray(userId, inStart.szUserID);
        }
        NetSDKLib.NET_OUT_USERINFO_START_FIND outStart = new NetSDKLib.NET_OUT_USERINFO_START_FIND();
        NetSDKLib.LLong findHandle = com.netsdk.demo.module.LoginModule.netsdk.CLIENT_StartFindUserInfo(loginHandle, inStart, outStart, 5000);
        if (findHandle == null || findHandle.longValue() == 0) {
            return 0;
        }
        try {
            return outStart.nTotalCount;
        } finally {
            com.netsdk.demo.module.LoginModule.netsdk.CLIENT_StopFindUserInfo(findHandle);
        }
    }

}
