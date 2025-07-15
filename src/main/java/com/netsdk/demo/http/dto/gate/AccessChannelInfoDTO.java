package com.netsdk.demo.http.dto.gate;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class AccessChannelInfoDTO {
    @Schema(description = "通道序号")
    private int index;
    @Schema(description = "通道名称")
    private String name;
    @Schema(description = "通道状态")
    private int state;
} 