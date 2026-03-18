package com.example.demo.dto.response.mood;

import com.example.demo.entity.Mood;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class MoodDTO {
    private Long id;
    private Integer value;
    private String description;
    private LocalDateTime createTime;

    public static MoodDTO fromEntity(Mood mood){
        MoodDTO dto = new MoodDTO();
        dto.setId(mood.getId());
        dto.setValue(mood.getValue());
        dto.setDescription(mood.getDescription());
        dto.setCreateTime(mood.getCreateTime());
        return dto;
    }

    // 列表转换
    public static List<MoodDTO> fromEntities(List<Mood> moods) {
        return moods.stream()
                .map(MoodDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
