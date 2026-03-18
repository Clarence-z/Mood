package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;


@Data
@TableName("conversations")
public class Conversation {
    @TableId(type= IdType.AUTO)
    private Long id;

    private String title;
    private Long userId;
    private Integer messageCount = 0;
    private Date createdAt;
    private Date updatedAt;

    @TableLogic
    private Integer isDeleted = 0;
}
