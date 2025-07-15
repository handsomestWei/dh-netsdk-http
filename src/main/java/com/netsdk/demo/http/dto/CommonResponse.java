package com.netsdk.demo.http.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CommonResponse<T> {
    @Schema(description = "是否成功")
    private boolean success;
    @Schema(description = "提示信息")
    private String message;
    @Schema(description = "返回数据")
    private T data;
} 