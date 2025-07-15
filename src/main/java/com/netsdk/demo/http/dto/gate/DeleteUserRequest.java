package com.netsdk.demo.http.dto.gate;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class DeleteUserRequest {
    @Schema(description = "用户ID")
    private String userId;
} 