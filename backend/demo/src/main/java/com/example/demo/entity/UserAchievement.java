package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_achievement")
public class UserAchievement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long achievementId;
    private LocalDateTime achievedAt;
    private Boolean isNew = true; // 用于前端判断是否为新获得

    // 关联的成就详情（非表字段）
    @TableField(exist = false)
    private Achievement achievement;
}
