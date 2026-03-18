package com.example.demo.dto.request.todo;

import com.example.demo.entity.Todo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdateTodoRequest {
    @Size(max = 100, message = "标题长度不能超过100字符")
    private String title;
    private String description;
    private Integer priority;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dueDate;

    @Min(value = 0, message = "状态值必须为0或1")
    @Max(value = 1, message = "状态值必须为0或1")
    private Integer status;
    private Boolean isPinned;
    private Integer estimatedMinutes;
    private Integer actualMinutes;
    private List<String> tags;

    public void updateEntity(Todo entity) {
        if(this.title != null){
            entity.setTitle(this.title);
        }
        if(this.description != null){
            entity.setDescription(this.description);
        }
        if(this.priority != null){
            entity.setPriority(this.priority);
        }
        if(this.dueDate != null){
            entity.setDueDate(this.dueDate);
        }

        if(this.status != null){
            entity.setStatus(this.status);
            if(this.status == 2){
                entity.setCompletedAt(LocalDateTime.now());
            }else if(entity.getCompletedAt() != null){
                entity.setCompletedAt(null);    // 从已完成改成别的状态，清空完成时间
            }
        }
        if(this.isPinned != null){
            entity.setIsPinned(this.isPinned);
        }
        if(this.estimatedMinutes != null){
            entity.setEstimatedMinutes(this.estimatedMinutes);
        }
        if(this.actualMinutes != null){
            entity.setActualMinutes(this.actualMinutes);
        }
        if(this.tags != null){
            entity.setTagList(this.tags);
        }
    }
}
