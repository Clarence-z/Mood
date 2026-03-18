package com.example.demo.dto.request.mood;

import com.example.demo.entity.Mood;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddMoodRequest {
    @NotNull(message = "心情值不能为空")
    private Integer value;
    private String description;

    public Mood toEntity() {
        Mood mood = new Mood();
        mood.setValue(value);
        mood.setDescription(description);
        return mood;
    }
}
