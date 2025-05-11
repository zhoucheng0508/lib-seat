package com.example.hello.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class QuickReserveRequest {
    @NotBlank(message = "预约日期不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "日期格式不正确，应为yyyy-MM-dd")
    private String date;

    @NotBlank(message = "开始时间不能为空")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "时间格式不正确，应为HH:mm")
    private String startTime;

    @NotBlank(message = "结束时间不能为空")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "时间格式不正确，应为HH:mm")
    private String endTime;

    @NotBlank(message = "用户ID不能为空")
    private String userId;
} 