package com.netsdk.demo.http.dto.gate;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class FindCardListRequest {
    @Schema(description = "卡号，为空查询所有")
    private String cardNo;
    @Schema(description = "用户ID，为空不作为条件")
    private String userId;
    @Schema(description = "最大查询条数")
    private int maxCount;
} 