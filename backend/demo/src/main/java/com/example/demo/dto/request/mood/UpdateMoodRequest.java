package com.example.demo.dto.request.mood;

import com.example.demo.entity.Mood;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMoodRequest {
    @Min(value = 1, message = "心情值应为1~5之间的整数")
    @Max(value = 5, message = "心情值应为1~5之间的整数")
    private Integer value;
    private String description;

    public void updateEntity(Mood entity) {
        if(this.value != null) {
            entity.setValue(this.value);
        }
        if(this.description != null) {
            entity.setDescription(this.description);
        }
    }

}
