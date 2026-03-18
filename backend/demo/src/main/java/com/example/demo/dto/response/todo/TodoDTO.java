package com.example.demo.dto.response.todo;

import com.example.demo.entity.Todo;
import com.example.demo.entity.enums.TodoPriority;
import com.example.demo.entity.enums.TodoStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class TodoDTO {
    private Long id;
    private String title;
    private String description;
    // 文本方便直接给前端呈现
    private Integer status; // 状态码
    private String statusText; // 状态文本
    private Integer priority; // 优先级码
    private String priorityText; // 优先级文本
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    private Boolean isPinned;
    private Integer estimatedMinutes;
    private Integer actualMinutes;
    private List<String> tags; // 标签列表
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isOverdue; // 是否过期
    private Boolean isDone; // 是否完成

    public static TodoDTO fromEntity(Todo todo) {
        if (todo == null) {
            return null;
        }

        TodoDTO dto = new TodoDTO();
        dto.setId(todo.getId());
        dto.setTitle(todo.getTitle());
        dto.setDescription(todo.getDescription());

        // 状态相关转换
        dto.setStatus(todo.getStatus());
        dto.setStatusText(todo.getStatusDesc());

        // 优先级相关转换
        dto.setPriority(todo.getPriority());
        dto.setPriorityText(todo.getPriorityDesc());

        dto.setDueDate(todo.getDueDate());
        dto.setCompletedAt(todo.getCompletedAt());
        dto.setIsPinned(todo.getIsPinned());
        dto.setEstimatedMinutes(todo.getEstimatedMinutes());
        dto.setActualMinutes(todo.getActualMinutes());

        // 标签转换
        dto.setTags(todo.getTagList());

        dto.setCreatedAt(todo.getCreatedAt());
        dto.setUpdatedAt(todo.getUpdatedAt());

        // 计算属性
        dto.setIsOverdue(todo.isOverdue());
        dto.setIsDone(todo.isDone());

        return dto;
    }

    public static List<TodoDTO> fromEntities(List<Todo> todos) {
        return todos.stream()
                .map(TodoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 总之备着？谁知道会在哪里用到。。
    // 便捷方法：获取状态枚举
    public TodoStatus getStatusEnum() {
        return this.status != null ? TodoStatus.fromCode(this.status) : null;
    }

    // 便捷方法：获取优先级枚举
    public TodoPriority getPriorityEnum() {
        return this.priority != null ? TodoPriority.fromCode(this.priority) : null;
    }
}
