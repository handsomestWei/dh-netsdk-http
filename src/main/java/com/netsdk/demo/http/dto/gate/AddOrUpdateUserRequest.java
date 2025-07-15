package com.netsdk.demo.http.dto.gate;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class AddOrUpdateUserRequest {
    @Schema(description = "用户ID")
    private String userId;
    @Schema(description = "权限类型，数字字符串")
    private String authority;
    @Schema(description = "用户类型，数字字符串")
    private String userType;
    @Schema(description = "门通道ID列表，逗号分隔")
    private String channelIds;
    @Schema(description = "有效期起 yyyy-MM-dd HH:mm:ss")
    private String validFrom;
    @Schema(description = "有效期止 yyyy-MM-dd HH:mm:ss")
    private String validTo;
} 