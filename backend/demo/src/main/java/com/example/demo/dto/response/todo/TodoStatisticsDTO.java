package com.example.demo.dto.response.todo;

import lombok.Data;

@Data
public class TodoStatisticsDTO {
    private Long todayCount;      // 今日任务数
    private Long totalCount;      // 总任务数
    private Long doneCount;       // 已完成数
    private Double completionRate;     // 完成率（百分比）
}
