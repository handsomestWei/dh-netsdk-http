package com.netsdk.demo.http.dto.gate;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class OpenDoorRecordDTO {
    @Schema(description = "序号")
    private int index;
    @Schema(description = "卡号")
    private String card;
    @Schema(description = "用户ID")
    private String userId;
    @Schema(description = "事件时间")
    private String eventTime;
    @Schema(description = "通道号")
    private int channel;
    @Schema(description = "开门方式")
    private int openMethod;
    @Schema(description = "结果")
    private int result;
    @Schema(description = "错误码")
    private int errCode;
} 