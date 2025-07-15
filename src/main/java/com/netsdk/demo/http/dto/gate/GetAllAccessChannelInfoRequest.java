package com.netsdk.demo.http.dto.gate;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class GetAllAccessChannelInfoRequest {
    @Schema(description = "通道数量")
    private int channelCount;
} 