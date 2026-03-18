package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("moodtable")
public class Mood {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer value;
    private String description;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    private Long userId;
    @TableLogic
    private Integer isDeleted;
}
