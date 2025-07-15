package com.netsdk.demo.http.dto.gate;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class GetUserRecordsRequest {
    @Schema(description = "用户ID，可为空")
    private String userId;
    @Schema(description = "最大条数")
    private int maxCount;
} 