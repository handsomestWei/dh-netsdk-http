package com.netsdk.demo.http.dto.gate;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class InsertCardRequest {
    @Schema(description = "卡号")
    private String cardNo;
    @Schema(description = "用户ID")
    private String userId;
    @Schema(description = "卡名")
    private String cardName;
    @Schema(description = "卡密码")
    private String cardPwd;
    @Schema(description = "卡状态")
    private int cardStatus;
    @Schema(description = "卡类型")
    private int cardType;
    @Schema(description = "使用次数")
    private int useTimes;
    @Schema(description = "是否首卡 1-true, 0-false")
    private int isFirstEnter;
    @Schema(description = "是否有效 1-true, 0-false")
    private int isValid;
    @Schema(description = "有效开始时间 yyyy-MM-dd HH:mm:ss")
    private String startValidTime;
    @Schema(description = "有效结束时间 yyyy-MM-dd HH:mm:ss")
    private String endValidTime;
    @Schema(description = "门通道ID数组")
    private int[] doorIds;
} 