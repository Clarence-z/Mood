package com.example.demo.dto.response.user;

import com.example.demo.entity.Achievement;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AchievementDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String icon;
    private String conditionType;
    private Integer threshold;
    private Integer currentProgress; // 当前进度（从 user_statistics 计算）
    private Boolean isAchieved;      // 是否已获得
    private LocalDateTime achievedAt; // 获得时间（如果已获得）
    private Boolean isNew;           // 是否为新获得（用于前端小红点）

    public static AchievementDTO fromEntity(Achievement entity, Integer currentProgress, boolean isAchieved, LocalDateTime achievedAt, Boolean isNew) {
        if (entity == null) {
            return null;
        }

        AchievementDTO dto = new AchievementDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setIcon(entity.getIcon());
        dto.setConditionType(entity.getConditionType());
        dto.setThreshold(entity.getThreshold());
        // 传入业务数据
        dto.setCurrentProgress(currentProgress != null ? currentProgress : 0);
        dto.setIsAchieved(isAchieved);
        dto.setAchievedAt(achievedAt);
        dto.setIsNew(isNew);
        return dto;
    }

    // 计算完成百分比（用于进度条显示）
    public Integer getCompletionPercentage() {
        if (threshold == null || threshold <= 0) {
            return 100;
        }
        if (currentProgress == null || currentProgress <= 0) {
            return 0;
        }
        int percentage = (int) ((double) currentProgress / threshold * 100);
        return Math.min(100, percentage);
    }

    // 获取进度文本
    public String getProgressText() {
        if (isAchieved != null && isAchieved) {
            return "已达成";
        }
        if (currentProgress != null && threshold != null) {
            return String.format("%d/%d", currentProgress, threshold);
        }
        return "未开始";
    }
}
