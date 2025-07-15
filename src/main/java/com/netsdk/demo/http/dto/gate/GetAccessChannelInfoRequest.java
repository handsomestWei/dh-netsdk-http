package com.netsdk.demo.http.dto.gate;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class GetAccessChannelInfoRequest {
    @Schema(description = "通道号")
    private int channel;
} 