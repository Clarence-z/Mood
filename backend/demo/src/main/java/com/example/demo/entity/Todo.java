package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.demo.entity.enums.TodoPriority;
import com.example.demo.entity.enums.TodoStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@TableName("todo")
public class Todo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private Integer status;
    private Integer priority;
    private LocalDateTime dueDate;
    // 总是更新，即使为NULL
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime completedAt;
    private Boolean isPinned;
    private Integer estimatedMinutes;
    private Integer actualMinutes;
    private String tags;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    // 有这个注解就不用在方法里单独排除这个属性了
    @TableLogic
    private Integer isDeleted = 0;

    // status(code)->enum
    public TodoStatus getStatusEnum() {
        return TodoStatus.fromCode(this.status);
    }
    // setter for enum?
    public void setStatusEnum(TodoStatus statusEnum) {
        this.status = statusEnum.getCode();
    }
    // code->description
    public String getStatusDesc(){
        return TodoStatus.getDesc(this.status);
    }
    // priority
    public TodoPriority getPriorityEnum() {
        return TodoPriority.fromCode(this.priority);
    }
    public void setPriorityEnum(TodoPriority priorityEnum) {
        this.priority = priorityEnum.getCode();
    }
    public String getPriorityDesc(){
        return TodoPriority.getDesc(this.priority);
    }

    @TableField(exist = false)
    private List<String> tagList;

    // 增强的get方法，从String到List，用于从数据库中取出时，方便业务处理
    public List<String> getTagList() {
        if (this.tags == null || this.tags.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(this.tags.split(","))
                .map(String::trim)        // 去除前后空格
                .filter(tag -> !tag.isEmpty()) // 过滤空字符串
                .distinct()               // 去重
                .sorted()                 // 排序（可选）
                .collect(Collectors.toList());
    }
    // 增强的set方法，从List转为String，用于存到数据库里
    public void setTagList(List<String> tagList) {
        if (tagList == null || tagList.isEmpty()) {
            this.tags = null;
            return;
        }
        // 构建字符串时进行处理
        String joined = tagList.stream()
                .filter(tag -> tag != null)
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.joining(","));

        this.tags = joined.isEmpty() ? null : joined;
    }

    // 是否过期
    public boolean isOverdue() {
        if (dueDate == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(dueDate) && this.status == TodoStatus.TODO.getCode();
    }

    public boolean isDone() {
        return this.status == TodoStatus.DONE.getCode();
    }
}
