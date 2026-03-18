package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;


@Data
@TableName("messages")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long conversationId; // 外键字段

    private String role; // 'user', 'assistant'

    private String content;

    private Integer messageOrder; // 消息顺序

    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;
}
