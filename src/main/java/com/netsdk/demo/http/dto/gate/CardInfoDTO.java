package com.netsdk.demo.http.dto.gate;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class CardInfoDTO {
    @Schema(description = "序号")
    private int index;
    @Schema(description = "卡号")
    private String cardNo;
    @Schema(description = "卡名称")
    private String cardName;
    @Schema(description = "记录号")
    private int recNo;
    @Schema(description = "用户ID")
    private String userId;
    @Schema(description = "卡密码")
    private String cardPwd;
    @Schema(description = "卡状态")
    private int cardStatus;
    @Schema(description = "卡类型")
    private int cardType;
    @Schema(description = "可用次数")
    private int useTimes;
    @Schema(description = "有效期起")
    private String startValidTime;
    @Schema(description = "有效期止")
    private String endValidTime;
    @Schema(description = "门数")
    private int doorNum;
    @Schema(description = "门列表，逗号分隔")
    private String doors;
}