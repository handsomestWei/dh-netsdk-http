package com.netsdk.demo.http.controller.gate;

import com.netsdk.demo.http.controller.BaseDeviceController;
import com.netsdk.demo.module.ext.GateExtModule;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.demo.http.dto.CommonResponse;
import com.netsdk.demo.http.dto.gate.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.netsdk.demo.http.dto.gate.CardInfoDTO;
import com.netsdk.demo.http.adapt.GateDtoAdapt;
import com.netsdk.demo.http.util.TimeUtil;

@Tag(name = "门禁管理接口", description = "门禁相关操作API")
@RestController
@RequestMapping("/gate")
public class GateController extends BaseDeviceController {

    @Operation(summary = "添加卡", description = "添加门禁卡，支持多门通道")
    @PostMapping("/insertCard")
    public CommonResponse insertCard(@org.springframework.web.bind.annotation.RequestBody InsertCardRequest req) {
        boolean result = GateExtModule.insertCard(loginHandleHolder.get(), req.getCardNo(), req.getUserId(), req.getCardName(), req.getCardPwd(),
                req.getCardStatus(), req.getCardType(), req.getUseTimes(), req.getIsFirstEnter(), req.getIsValid(),
                req.getStartValidTime(), req.getEndValidTime(), req.getDoorIds());
        CommonResponse resp = new CommonResponse();
        resp.setSuccess(result);
        resp.setMessage(result ? "添加卡成功" : "添加卡失败");
        return resp;
    }

    @Operation(summary = "服务方式添加卡", description = "无需传门禁通道和有效期，人员侧已绑定")
    @PostMapping("/insertCardByService")
    public CommonResponse insertCardByService(@org.springframework.web.bind.annotation.RequestBody InsertCardByServiceRequest req) {
        boolean result = GateExtModule.insertCardByService(loginHandleHolder.get(), req.getCardNo(), req.getUserId(), req.getCardType());
        CommonResponse resp = new CommonResponse();
        resp.setSuccess(result);
        resp.setMessage(result ? "添加卡片成功" : "添加卡片失败");
        return resp;
    }

    @Operation(summary = "查询卡片列表", description = "支持按卡号和用户ID查询")
    @PostMapping("/findCardList")
    public CommonResponse<List<CardInfoDTO>> findCardList(@org.springframework.web.bind.annotation.RequestBody FindCardListRequest req) {
        List<NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD> cardList = GateExtModule.findCardList(loginHandleHolder.get(), req.getCardNo(), req.getUserId(), req.getMaxCount());
        List<CardInfoDTO> dtoList = GateDtoAdapt.toCardInfoDTOList(cardList);
        CommonResponse<List<CardInfoDTO>> resp = new CommonResponse<>();
        resp.setSuccess(true);
        resp.setMessage("查询成功");
        resp.setData(dtoList);
        return resp;
    }

    @Operation(summary = "获取门禁通道数量", description = "返回门禁通道数量")
    @GetMapping("/getAccessChannelCount")
    public CommonResponse<Integer> getAccessChannelCount() {
        int count = GateExtModule.getAccessChannelCount(loginHandleHolder.get());
        CommonResponse<Integer> resp = new CommonResponse<>();
        resp.setSuccess(true);
        resp.setMessage("查询成功");
        resp.setData(count);
        return resp;
    }

    @Operation(summary = "获取单个门禁通道信息", description = "根据通道号获取门禁通道信息")
    @PostMapping("/getAccessChannelInfo")
    public CommonResponse<AccessChannelInfoDTO> getAccessChannelInfo(@org.springframework.web.bind.annotation.RequestBody GetAccessChannelInfoRequest req) {
        NetSDKLib.CFG_ACCESS_EVENT_INFO info = GateExtModule.getAccessChannelInfo(loginHandleHolder.get(), req.getChannel());
        AccessChannelInfoDTO dto = info != null ? GateDtoAdapt.toAccessChannelInfoDTO(info, req.getChannel()) : null;
        CommonResponse<AccessChannelInfoDTO> resp = new CommonResponse<>();
        resp.setSuccess(dto != null);
        resp.setMessage(dto != null ? "查询成功" : "未查询到数据");
        resp.setData(dto);
        return resp;
    }

    @Operation(summary = "获取所有门禁通道信息", description = "根据通道数量获取所有门禁通道信息")
    @PostMapping("/getAllAccessChannelInfo")
    public CommonResponse<List<AccessChannelInfoDTO>> getAllAccessChannelInfo(@org.springframework.web.bind.annotation.RequestBody GetAllAccessChannelInfoRequest req) {
        List<NetSDKLib.CFG_ACCESS_EVENT_INFO> list = GateExtModule.getAllAccessChannelInfo(loginHandleHolder.get(), req.getChannelCount());
        List<AccessChannelInfoDTO> dtoList = GateDtoAdapt.toAccessChannelInfoDTOList(list);
        CommonResponse<List<AccessChannelInfoDTO>> resp = new CommonResponse<>();
        resp.setSuccess(true);
        resp.setMessage("查询成功");
        resp.setData(dtoList);
        return resp;
    }

    @Operation(summary = "分页查询开门记录", description = "带分页参数，返回开门记录列表")
    @PostMapping("/getOpenDoorRecords")
    public CommonResponse<List<OpenDoorRecordDTO>> getOpenDoorRecords(@org.springframework.web.bind.annotation.RequestBody GetOpenDoorRecordsRequest req) {
        // 使用TimeUtil处理默认时间
        req.setStart(TimeUtil.getDefaultStartIfBlank(req.getStart()));
        req.setEnd(TimeUtil.getDefaultEndIfBlank(req.getEnd()));
        List<NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC> list = GateExtModule.getOpenDoorRecords(loginHandleHolder.get(), req.getStart(), req.getEnd(), req.getCardNo(), req.getPageNum(), req.getPageSize());
        List<OpenDoorRecordDTO> dtoList = GateDtoAdapt.toOpenDoorRecordDTOList(list, req.getPageNum(), req.getPageSize());
        CommonResponse<List<OpenDoorRecordDTO>> resp = new CommonResponse<>();
        resp.setSuccess(true);
        resp.setMessage("查询成功");
        resp.setData(dtoList);
        return resp;
    }

    @Operation(summary = "获取开门记录总数", description = "根据条件统计开门记录总数")
    @PostMapping("/getOpenDoorRecordCount")
    public CommonResponse<Integer> getOpenDoorRecordCount(@org.springframework.web.bind.annotation.RequestBody GetOpenDoorRecordCountRequest req) {
        // 使用TimeUtil处理默认时间
        req.setStart(TimeUtil.getDefaultStartIfBlank(req.getStart()));
        req.setEnd(TimeUtil.getDefaultEndIfBlank(req.getEnd()));
        int count = GateExtModule.getOpenDoorRecordCount(loginHandleHolder.get(), req.getStart(), req.getEnd(), req.getCardNo());
        CommonResponse<Integer> resp = new CommonResponse<>();
        resp.setSuccess(true);
        resp.setMessage("查询成功");
        resp.setData(count);
        return resp;
    }

    @Operation(summary = "开门", description = "门禁控制：开门")
    @PostMapping("/openDoor")
    public CommonResponse<Void> openDoor(@org.springframework.web.bind.annotation.RequestBody OpenDoorRequest req) {
        boolean result = GateExtModule.openDoor(loginHandleHolder.get(), req.getChannelNo());
        CommonResponse<Void> resp = new CommonResponse<>();
        resp.setSuccess(result);
        resp.setMessage(result ? "开门成功" : "开门失败");
        return resp;
    }

    @Operation(summary = "关门", description = "门禁控制：关门")
    @PostMapping("/closeDoor")
    public CommonResponse<Void> closeDoor(@org.springframework.web.bind.annotation.RequestBody OpenDoorRequest req) {
        boolean result = GateExtModule.closeDoor(loginHandleHolder.get(), req.getChannelNo());
        CommonResponse<Void> resp = new CommonResponse<>();
        resp.setSuccess(result);
        resp.setMessage(result ? "关门成功" : "关门失败");
        return resp;
    }

    @Operation(summary = "常开", description = "门禁控制：常开")
    @PostMapping("/alwaysOpenDoor")
    public CommonResponse<Void> alwaysOpenDoor(@org.springframework.web.bind.annotation.RequestBody OpenDoorRequest req) {
        boolean result = GateExtModule.alwaysOpenDoor(loginHandleHolder.get(), req.getChannelNo());
        CommonResponse<Void> resp = new CommonResponse<>();
        resp.setSuccess(result);
        resp.setMessage(result ? "常开成功" : "常开失败");
        return resp;
    }

    @Operation(summary = "常闭", description = "门禁控制：常闭")
    @PostMapping("/alwaysCloseDoor")
    public CommonResponse<Void> alwaysCloseDoor(@org.springframework.web.bind.annotation.RequestBody OpenDoorRequest req) {
        boolean result = GateExtModule.alwaysCloseDoor(loginHandleHolder.get(), req.getChannelNo());
        CommonResponse<Void> resp = new CommonResponse<>();
        resp.setSuccess(result);
        resp.setMessage(result ? "常闭成功" : "常闭失败");
        return resp;
    }

    @Operation(summary = "新增或修改用户", description = "新增或修改门禁用户")
    @PostMapping("/addOrUpdateUser")
    public CommonResponse<Void> addOrUpdateUser(@org.springframework.web.bind.annotation.RequestBody AddOrUpdateUserRequest req) {
        NetSDKLib.NET_ACCESS_USER_INFO userInfo = GateDtoAdapt.toNetAccessUserInfo(req);
        boolean result = GateExtModule.addOrUpdateUser(loginHandleHolder.get(), userInfo);
        CommonResponse<Void> resp = new CommonResponse<>();
        resp.setSuccess(result);
        resp.setMessage(result ? "用户添加/更新成功" : "用户添加/更新失败");
        return resp;
    }

    @Operation(summary = "删除用户", description = "根据用户ID删除门禁用户")
    @PostMapping("/deleteUser")
    public CommonResponse<Void> deleteUser(@org.springframework.web.bind.annotation.RequestBody DeleteUserRequest req) {
        boolean result = GateExtModule.deleteUser(loginHandleHolder.get(), req.getUserId().getBytes());
        CommonResponse<Void> resp = new CommonResponse<>();
        resp.setSuccess(result);
        resp.setMessage(result ? "用户删除成功" : "用户删除失败");
        return resp;
    }

    @Operation(summary = "清空所有用户", description = "清空门禁设备所有用户")
    @PostMapping("/clearAllUsers")
    public CommonResponse<Void> clearAllUsers() {
        boolean result = GateExtModule.clearAllUsers(loginHandleHolder.get());
        CommonResponse<Void> resp = new CommonResponse<>();
        resp.setSuccess(result);
        resp.setMessage(result ? "清空用户成功" : "清空用户失败");
        return resp;
    }

    @Operation(summary = "查询用户信息列表", description = "分页查询门禁用户信息")
    @PostMapping("/getUserRecords")
    public CommonResponse<List<UserInfoDTO>> getUserRecords(@org.springframework.web.bind.annotation.RequestBody GetUserRecordsRequest req) {
        List<NetSDKLib.NET_ACCESS_USER_INFO> list = GateExtModule.getUserRecords(loginHandleHolder.get(), req.getUserId(), req.getMaxCount());
        List<UserInfoDTO> dtoList = GateDtoAdapt.toUserInfoDTOList(list);
        CommonResponse<List<UserInfoDTO>> resp = new CommonResponse<>();
        resp.setSuccess(true);
        resp.setMessage("查询成功");
        resp.setData(dtoList);
        return resp;
    }

    @Operation(summary = "查询用户总数", description = "查询门禁用户总数")
    @PostMapping("/getUserRecordsCount")
    public CommonResponse<Integer> getUserRecordsCount(@org.springframework.web.bind.annotation.RequestBody GetUserRecordsCountRequest req) {
        int count = GateExtModule.getUserRecordsCount(loginHandleHolder.get(), req.getUserId());
        CommonResponse<Integer> resp = new CommonResponse<>();
        resp.setSuccess(true);
        resp.setMessage("查询成功");
        resp.setData(count);
        return resp;
    }
}