package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_statistics")
public class UserStatistics {
    @TableId(type = IdType.INPUT) // 非自增主键
    private Long userId;

    private Integer diaryCount = 0;
    private Integer todoCompleted = 0;
    private Integer aiMessageTotal = 0;
    private Integer totalLoginDays = 0;
    private Integer currentStreak = 0;
    private LocalDate lastLoginDate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
