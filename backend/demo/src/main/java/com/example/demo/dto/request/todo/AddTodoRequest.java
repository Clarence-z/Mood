package com.example.demo.dto.request.todo;

import com.example.demo.entity.Todo;
import com.example.demo.entity.enums.TodoPriority;
import com.example.demo.entity.enums.TodoStatus;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AddTodoRequest {
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100字符")
    private String title;
    private String description;
    // 要考虑这个地方怎么被封装到返回的响应里
    @Min(value = 0, message = "优先级范围错误")
    @Max(value = 3, message = "优先级范围错误")
    private Integer priority;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dueDate;
    @Min(value = 1, message = "预计时间必须大于0")
    private Integer estimatedMinutes;
    private List<String> tags;

    public Todo toEntity() {
        Todo todo = new Todo();
        todo.setTitle(this.title);
        todo.setDescription(this.description);
        todo.setPriority(this.priority);
        todo.setDueDate(this.dueDate);
        todo.setEstimatedMinutes(this.estimatedMinutes);

        todo.setStatus(TodoStatus.TODO.getCode()); // 新建任务默认状态为待办
        todo.setIsPinned(false); // 默认不置顶

        // 设置标签
        if (this.tags != null && !this.tags.isEmpty()) {
            todo.setTagList(this.tags);
        }

        return todo;
    }
}
