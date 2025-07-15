package com.netsdk.demo.http.dto.gate;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class InsertCardByServiceRequest {
    @Schema(description = "卡号")
    private String cardNo;
    @Schema(description = "用户ID")
    private String userId;
    @Schema(description = "卡类型")
    private int cardType;
} 