package com.netsdk.demo.http.dto.gate;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class UserInfoDTO {
    @Schema(description = "序号")
    private int index;
    @Schema(description = "用户ID")
    private String userId;
    @Schema(description = "用户名")
    private String userName;
    @Schema(description = "卡号（如有）")
    private String card;
    @Schema(description = "权限")
    private String authority;
    @Schema(description = "用户类型")
    private String type;
    @Schema(description = "有效期起")
    private String validFrom;
    @Schema(description = "有效期止")
    private String validTo;
    @Schema(description = "用户状态")
    private int status;
    @Schema(description = "门数")
    private String doorNum;
    @Schema(description = "门列表，逗号分隔")
    private String doorList;
} 