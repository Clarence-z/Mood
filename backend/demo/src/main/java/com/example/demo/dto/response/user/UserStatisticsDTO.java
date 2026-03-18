package com.example.demo.dto.response.user;

import com.example.demo.entity.UserStatistics;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserStatisticsDTO {
    private Long userId;
    private Integer diaryCount = 0;
    private Integer todoCompleted = 0;
    private Integer aiMessageTotal = 0;
    private Integer totalLoginDays = 0;
    private Integer currentStreak = 0;
    private LocalDate lastLoginDate;
    private LocalDateTime updatedAt;

    public static UserStatisticsDTO fromEntity(UserStatistics entity) {
        if (entity == null) {
            return null;
        }

        UserStatisticsDTO dto = new UserStatisticsDTO();
        dto.setUserId(entity.getUserId());
        dto.setDiaryCount(entity.getDiaryCount());
        dto.setTodoCompleted(entity.getTodoCompleted());
        dto.setAiMessageTotal(entity.getAiMessageTotal());
        dto.setTotalLoginDays(entity.getTotalLoginDays());
        dto.setCurrentStreak(entity.getCurrentStreak());
        dto.setLastLoginDate(entity.getLastLoginDate());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
