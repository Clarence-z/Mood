package com.example.demo.dto.response.chat;

import com.example.demo.entity.Conversation;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ConversationDTO {
    private Long id;
    private String title;
    private Integer messageCount;
    private Date updatedAt;

    public static ConversationDTO fromEntity(Conversation conversation) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setTitle(conversation.getTitle());
        dto.setMessageCount(conversation.getMessageCount());
        dto.setUpdatedAt(conversation.getUpdatedAt());
        return dto;
    }

    public static List<ConversationDTO> fromEntities(List<Conversation> conversations) {
        return conversations.stream()
                .map(ConversationDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
