package com.netsdk.demo.http.dto.gate;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class GetOpenDoorRecordsRequest {
    @Schema(description = "开始时间 yyyy-MM-dd HH:mm:ss")
    private String start;
    @Schema(description = "结束时间 yyyy-MM-dd HH:mm:ss")
    private String end;
    @Schema(description = "卡号")
    private String cardNo;
    @Schema(description = "页码，从1开始")
    private int pageNum;
    @Schema(description = "每页条数")
    private int pageSize;
} 