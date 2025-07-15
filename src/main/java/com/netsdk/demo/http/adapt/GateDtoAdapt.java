package com.netsdk.demo.http.adapt;

import com.netsdk.demo.http.dto.gate.CardInfoDTO;
import com.netsdk.demo.http.dto.gate.AccessChannelInfoDTO;
import com.netsdk.demo.http.dto.gate.OpenDoorRecordDTO;
import com.netsdk.demo.http.dto.gate.AddOrUpdateUserRequest;
import com.netsdk.demo.http.dto.gate.UserInfoDTO;
import com.netsdk.lib.NetSDKLib;
import java.util.ArrayList;
import java.util.List;

/**
 * 门禁卡对象适配器
 */
public class GateDtoAdapt {

    /**
     * JNA门禁卡对象列表转DTO列表
     */
    public static List<CardInfoDTO> toCardInfoDTOList(List<NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD> cardList) {
        List<CardInfoDTO> dtoList = new ArrayList<>();
        int index = 0;
        for (NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD card : cardList) {
            CardInfoDTO dto = new CardInfoDTO();
            dto.setIndex(index + 1);
            dto.setCardNo(new String(card.szCardNo).trim());
            try { dto.setCardName(new String(card.szCardName, "GBK").trim()); } catch (Exception e) { dto.setCardName(""); }
            dto.setRecNo(card.nRecNo);
            dto.setUserId(new String(card.szUserID).trim());
            dto.setCardPwd(new String(card.szPsw).trim());
            dto.setCardStatus(card.emStatus);
            dto.setCardType(card.emType);
            dto.setUseTimes(card.nUserTime);
            dto.setStartValidTime(card.stuValidStartTime.toStringTimeEx());
            dto.setEndValidTime(card.stuValidEndTime.toStringTimeEx());
            dto.setDoorNum(card.nDoorNum);
            StringBuilder doors = new StringBuilder();
            for (int d = 0; d < card.nDoorNum; d++) {
                if (d > 0) doors.append(",");
                doors.append(card.sznDoors[d]);
            }
            dto.setDoors(doors.toString());
            dtoList.add(dto);
            index++;
        }
        return dtoList;
    }

    public static AccessChannelInfoDTO toAccessChannelInfoDTO(NetSDKLib.CFG_ACCESS_EVENT_INFO info, int index) {
        AccessChannelInfoDTO dto = new AccessChannelInfoDTO();
        dto.setIndex(index);
        dto.setName(new String(info.szChannelName).trim());
        dto.setState(info.emState);
        return dto;
    }

    public static List<AccessChannelInfoDTO> toAccessChannelInfoDTOList(List<NetSDKLib.CFG_ACCESS_EVENT_INFO> channelInfoList) {
        List<AccessChannelInfoDTO> dtoList = new ArrayList<>();
        for (int i = 0; i < channelInfoList.size(); i++) {
            dtoList.add(toAccessChannelInfoDTO(channelInfoList.get(i), i));
        }
        return dtoList;
    }

    public static List<OpenDoorRecordDTO> toOpenDoorRecordDTOList(List<NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC> records, int pageNum, int pageSize) {
        List<OpenDoorRecordDTO> dtoList = new ArrayList<>();
        if (records == null || records.isEmpty()) {
            return dtoList;
        }
        int startIndex = (pageNum - 1) * pageSize;
        int index = startIndex + 1;
        for (NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC rec : records) {
            OpenDoorRecordDTO dto = new OpenDoorRecordDTO();
            dto.setIndex(index++);
            dto.setCard(new String(rec.szCardNo).trim());
            dto.setUserId(new String(rec.szUserID).trim());
            dto.setEventTime(rec.stuTime != null ? rec.stuTime.toStringTimeEx() : "");
            dto.setChannel(rec.nDoor);
            dto.setOpenMethod(rec.emMethod);
            dto.setResult(rec.bStatus);
            dto.setErrCode(rec.nErrorCode);
            dtoList.add(dto);
        }
        return dtoList;
    }

    public static NetSDKLib.NET_ACCESS_USER_INFO toNetAccessUserInfo(AddOrUpdateUserRequest req) {
        NetSDKLib.NET_ACCESS_USER_INFO userInfo = new NetSDKLib.NET_ACCESS_USER_INFO();
        com.netsdk.lib.ToolKits.StringToByteArray(req.getUserId(), userInfo.szUserID);
        try { userInfo.emAuthority = Integer.parseInt(req.getAuthority()); } catch (Exception e) { userInfo.emAuthority = 0; }
        try { userInfo.emUserType = Integer.parseInt(req.getUserType()); } catch (Exception e) { userInfo.emUserType = 0; }
        userInfo.stuValidBeginTime = com.netsdk.lib.ToolKitEx.parseDateTime(req.getValidFrom());
        userInfo.stuValidEndTime = com.netsdk.lib.ToolKitEx.parseDateTime(req.getValidTo());
        userInfo.nUserStatus = 0;
        if (req.getChannelIds() != null && !req.getChannelIds().isEmpty()) {
            String[] parts = req.getChannelIds().split(",");
            userInfo.nDoorNum = parts.length;
            userInfo.nTimeSectionNum = parts.length;
            for (int i = 0; i < parts.length; i++) {
                try {
                    userInfo.nDoors[i] = Integer.parseInt(parts[i].trim());
                    userInfo.nTimeSectionNo[i] = 255;
                } catch (Exception ex) {
                    userInfo.nDoors[i] = 0;
                }
            }
        } else {
            userInfo.nDoorNum = 0;
        }
        return userInfo;
    }

    public static List<UserInfoDTO> toUserInfoDTOList(List<NetSDKLib.NET_ACCESS_USER_INFO> userList) {
        List<UserInfoDTO> dtoList = new ArrayList<>();
        int index = 1;
        for (NetSDKLib.NET_ACCESS_USER_INFO user : userList) {
            UserInfoDTO dto = new UserInfoDTO();
            dto.setIndex(index++);
            dto.setUserId(new String(user.szUserID).trim());
            dto.setUserName(new String(user.szName).trim());
            dto.setCard(""); // 若有卡号字段可补充
            dto.setAuthority(String.valueOf(user.emAuthority));
            dto.setType(String.valueOf(user.emUserType));
            dto.setValidFrom(user.stuValidBeginTime != null ? user.stuValidBeginTime.toStringTimeEx() : "");
            dto.setValidTo(user.stuValidEndTime != null ? user.stuValidEndTime.toStringTimeEx() : "");
            dto.setStatus(user.nUserStatus);
            dto.setDoorNum(String.valueOf(user.nDoorNum));
            StringBuilder doors = new StringBuilder();
            for (int d = 0; d < user.nDoorNum; d++) {
                if (d > 0) doors.append(",");
                doors.append(user.nDoors[d]);
            }
            dto.setDoorList(doors.toString());
            dtoList.add(dto);
        }
        return dtoList;
    }
}
